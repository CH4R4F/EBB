package app.ebbplanner.domain.routines

import app.ebbplanner.data.local.dao.RoutineDao
import app.ebbplanner.data.local.dao.RoutineSkipDao
import app.ebbplanner.data.local.dao.TaskDao
import app.ebbplanner.data.local.entity.RoutineEntity
import app.ebbplanner.data.local.entity.RoutineSkipEntity
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.reminders.TaskReminderSync
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineInstanceGenerator
    @Inject
    constructor(
        private val routineDao: RoutineDao,
        private val routineSkipDao: RoutineSkipDao,
        private val taskDao: TaskDao,
        private val reminderSync: TaskReminderSync,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) {
        suspend fun ensureRollingWindow(daysAhead: Int = DEFAULT_WINDOW_DAYS) {
            val start = LocalDate.now()
            val end = start.plusDays(daysAhead.toLong() - 1)
            var day = start
            while (!day.isAfter(end)) {
                ensureForDate(day)
                day = day.plusDays(1)
            }
        }

        suspend fun ensureForDate(date: LocalDate) {
            val remindersOn = userPreferencesRepository.remindersDefault.first()
            val occurrences = date.toString()
            val now = System.currentTimeMillis()
            routineDao.listEnabled().forEach { routine ->
                if (!routine.enabled || !appliesOn(routine, date)) return@forEach
                if (routineSkipDao.isSkipped(routine.id, occurrences) != null) return@forEach
                if (taskDao.getRoutineOccurrence(routine.id, occurrences) != null) return@forEach
                val task =
                    TaskEntity(
                        id = UUID.randomUUID().toString(),
                        title = routine.title,
                        placement = TaskEntity.PLACEMENT_SCHEDULED,
                        date = occurrences,
                        startLocalTime = routine.startLocalTime,
                        durationMin = routine.durationMin,
                        colorKey = routine.colorKey,
                        iconKey = routine.iconKey,
                        reminderEnabled = remindersOn,
                        sourceRoutineId = routine.id,
                        routineOccurrenceDate = occurrences,
                        createdAt = now,
                        updatedAt = now,
                    )
                taskDao.insert(task)
                reminderSync.syncTask(task.id)
            }
        }

        suspend fun recordSkip(
            routineId: String,
            occurrenceDate: String,
        ) {
            routineSkipDao.insert(RoutineSkipEntity(routineId, occurrenceDate))
        }

        suspend fun onRoutineDeleted(routineId: String) {
            val today = LocalDate.now().toString()
            val open = taskDao.listOpenRoutineInstancesFrom(routineId, today)
            open.forEach { task ->
                taskDao.softDelete(task.id, System.currentTimeMillis(), System.currentTimeMillis())
                reminderSync.cancelTask(task.id)
            }
            routineSkipDao.deleteForRoutine(routineId)
        }

        suspend fun linkExistingTask(
            taskId: String,
            routine: RoutineEntity,
        ) {
            val task = taskDao.getById(taskId) ?: return
            val today = LocalDate.now()
            val occurrence =
                when {
                    task.placement == TaskEntity.PLACEMENT_INBOX ||
                        task.placement == TaskEntity.PLACEMENT_SOMEDAY ||
                        task.date.isNullOrBlank() ->
                        firstApplicableOnOrAfter(routine, today).toString()
                    else -> {
                        val parsed = runCatching { LocalDate.parse(task.date) }.getOrNull()
                        if (parsed != null && !parsed.isBefore(today) && appliesOn(routine, parsed)) {
                            parsed.toString()
                        } else {
                            firstApplicableOnOrAfter(routine, today).toString()
                        }
                    }
                }
            val now = System.currentTimeMillis()
            val updated =
                task.copy(
                    sourceRoutineId = routine.id,
                    routineOccurrenceDate = occurrence,
                    placement =
                        if (task.placement == TaskEntity.PLACEMENT_INBOX) {
                            TaskEntity.PLACEMENT_SCHEDULED
                        } else {
                            task.placement
                        },
                    date = occurrence,
                    startLocalTime = task.startLocalTime ?: routine.startLocalTime,
                    durationMin = task.durationMin,
                    colorKey = task.colorKey.ifBlank { routine.colorKey },
                    iconKey = task.iconKey ?: routine.iconKey,
                    routineInstanceDetached = task.routineInstanceDetached,
                    updatedAt = now,
                )
            taskDao.update(updated)
            reminderSync.syncTask(taskId)
            syncFutureInstances(routine, skipLinkDay = occurrence)
        }

        suspend fun syncFutureInstances(
            routine: RoutineEntity,
            skipLinkDay: String? = null,
        ) {
            pruneNonApplyingInstances(routine)
            val today = LocalDate.now()
            val end = today.plusDays(DEFAULT_WINDOW_DAYS.toLong() - 1)
            var day = today
            while (!day.isAfter(end)) {
                val occurrence = day.toString()
                if (appliesOn(routine, day)) {
                    val existing = taskDao.getRoutineOccurrence(routine.id, occurrence)
                    if (existing != null &&
                        existing.status != TaskEntity.STATUS_DONE &&
                        !existing.routineInstanceDetached &&
                        occurrence != skipLinkDay
                    ) {
                        val updated =
                            existing.copy(
                                title = routine.title,
                                startLocalTime = routine.startLocalTime,
                                durationMin = routine.durationMin,
                                colorKey = routine.colorKey,
                                iconKey = routine.iconKey,
                                date = existing.date ?: occurrence,
                                updatedAt = System.currentTimeMillis(),
                            )
                        taskDao.update(updated)
                        reminderSync.syncTask(updated.id)
                    }
                }
                day = day.plusDays(1)
            }
            ensureRollingWindow()
        }

        private suspend fun pruneNonApplyingInstances(routine: RoutineEntity) {
            val today = LocalDate.now().toString()
            val open = taskDao.listOpenRoutineInstancesFrom(routine.id, today)
            open.forEach { task ->
                val occ =
                    task.routineOccurrenceDate ?: task.date
                        ?: return@forEach
                val occDate = runCatching { LocalDate.parse(occ) }.getOrNull() ?: return@forEach
                if (!appliesOn(routine, occDate) && !task.routineInstanceDetached) {
                    taskDao.softDelete(task.id, System.currentTimeMillis(), System.currentTimeMillis())
                    reminderSync.cancelTask(task.id)
                }
            }
        }

        private fun firstApplicableOnOrAfter(
            routine: RoutineEntity,
            from: LocalDate,
        ): LocalDate {
            var day = from
            repeat(DEFAULT_WINDOW_DAYS * 2) {
                if (appliesOn(routine, day)) return day
                day = day.plusDays(1)
            }
            return from
        }

        private fun appliesOn(
            routine: RoutineEntity,
            date: LocalDate,
        ): Boolean =
            when (routine.repeatRule) {
                RoutineEntity.REPEAT_DAILY -> true
                RoutineEntity.REPEAT_WEEKDAYS -> {
                    val d = date.dayOfWeek.value
                    d in 1..5
                }
                RoutineEntity.REPEAT_WEEKLY -> {
                    val allowed =
                        routine.weeklyDays.split(',')
                            .mapNotNull { it.trim().toIntOrNull() }
                            .toSet()
                    if (allowed.isEmpty()) false else allowed.contains(date.dayOfWeek.value)
                }
                else -> false
            }

        companion object {
            const val DEFAULT_WINDOW_DAYS = 14
        }
    }