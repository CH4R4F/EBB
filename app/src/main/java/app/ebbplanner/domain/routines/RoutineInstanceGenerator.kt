package app.ebbplanner.domain.routines

import app.ebbplanner.data.local.dao.RoutineDao
import app.ebbplanner.data.local.dao.RoutineSkipDao
import app.ebbplanner.data.local.dao.TaskDao
import app.ebbplanner.data.repository.updateRoutineTask
import app.ebbplanner.data.local.entity.RoutineEntity
import app.ebbplanner.data.local.entity.RoutineSkipEntity
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.RoutineDeferredWorkPreferences
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
        private val generationGate: RoutineGenerationGate,
        private val repairState: RoutineRepairState,
        private val deferredWorkPreferences: RoutineDeferredWorkPreferences,
    ) {
        suspend fun ensureRollingWindow(daysAhead: Int = DEFAULT_WINDOW_DAYS) {
            if (repairState.isRepairPending()) return
            generationGate.withExclusive {
                if (repairState.isRepairPending()) return@withExclusive
                applyDeferredRoutineWorkLocked()
                ensureRollingWindowLocked(daysAhead)
            }
        }

        suspend fun ensureRollingWindowLocked(daysAhead: Int = DEFAULT_WINDOW_DAYS) {
            val start = LocalDate.now()
            val end = start.plusDays(daysAhead.toLong() - 1)
            var day = start
            while (!day.isAfter(end)) {
                ensureForDateLocked(day)
                day = day.plusDays(1)
            }
        }

        suspend fun ensureForDate(date: LocalDate) {
            if (repairState.isRepairPending()) return
            generationGate.withExclusive {
                if (repairState.isRepairPending()) return@withExclusive
                ensureForDateLocked(date)
            }
        }

        private suspend fun ensureForDateLocked(date: LocalDate) {
            val remindersOn = userPreferencesRepository.remindersDefault.first()
            val occurrences = date.toString()
            val now = System.currentTimeMillis()
            routineDao.listEnabled().forEach { routine ->
                if (!routine.enabled || !appliesOn(routine, date)) return@forEach
                if (routineSkipDao.isSkipped(routine.id, occurrences) != null) return@forEach
                if (taskDao.getRoutineOccurrence(routine.id, occurrences) != null) return@forEach
                if (taskDao.blocksRoutineOccurrenceOnDate(routine.id, occurrences, occurrences)) {
                    return@forEach
                }
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
                val rowId = taskDao.insertRoutineInstanceIfAbsent(task)
                if (rowId == -1L) {
                    return@forEach
                }
                reminderSync.syncTask(task.id)
            }
        }

        suspend fun recordSkip(
            routineId: String,
            occurrenceDate: String,
        ) {
            generationGate.withExclusive {
                routineSkipDao.insert(RoutineSkipEntity(routineId, occurrenceDate))
            }
        }

        suspend fun onRoutineDeleted(routineId: String) {
            generationGate.withExclusive {
                val today = LocalDate.now().toString()
                val open = taskDao.listOpenRoutineInstancesFrom(routineId, today)
                open.forEach { task ->
                    taskDao.softDelete(task.id, System.currentTimeMillis(), System.currentTimeMillis())
                    reminderSync.cancelTask(task.id)
                }
                routineSkipDao.deleteForRoutine(routineId)
            }
        }

        suspend fun persistRoutineAndSyncInstances(
            upsertRoutine: suspend () -> Unit,
            routine: RoutineEntity,
            linkTaskId: String?,
            linkOnSave: Boolean,
        ): Boolean {
            var deferred = false
            generationGate.withExclusive {
                upsertRoutine()
                if (repairState.isRepairPending()) {
                    deferred = true
                    if (linkTaskId != null && linkOnSave) {
                        deferredWorkPreferences.enqueuePendingLink(routine.id, linkTaskId)
                    } else {
                        deferredWorkPreferences.enqueuePendingSync(routine.id)
                    }
                    return@withExclusive
                }
                if (linkTaskId != null && linkOnSave) {
                    linkExistingTaskLocked(linkTaskId, routine)
                } else {
                    syncFutureInstancesLocked(routine)
                }
            }
            return deferred
        }

        suspend fun applyDeferredRoutineWorkLocked() {
            val links = deferredWorkPreferences.takePendingLinks()
            links.forEach { (routineId, taskId) ->
                val routine = routineDao.getById(routineId) ?: return@forEach
                linkExistingTaskLocked(taskId, routine)
            }
            val syncIds = deferredWorkPreferences.takePendingSyncRoutineIds()
            syncIds.forEach { routineId ->
                val routine = routineDao.getById(routineId) ?: return@forEach
                syncFutureInstancesLocked(routine)
            }
        }

        suspend fun linkExistingTask(
            taskId: String,
            routine: RoutineEntity,
        ) {
            if (repairState.isRepairPending()) return
            generationGate.withExclusive {
                if (repairState.isRepairPending()) return@withExclusive
                linkExistingTaskLocked(taskId, routine)
            }
        }

        private suspend fun linkExistingTaskLocked(
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
            val rows = taskDao.updateRoutineTask(updated)
            if (rows == 0) {
                val holder = taskDao.getRoutineOccurrence(routine.id, occurrence)
                taskDao.updateRoutineTask(
                    task.copy(
                        sourceRoutineId = routine.id,
                        routineInstanceDetached = true,
                        routineOccurrenceDate = null,
                        updatedAt = now,
                    ),
                )
                holder?.let { reminderSync.syncTask(it.id) }
                syncFutureInstancesLocked(routine, skipLinkDay = occurrence)
                return
            }
            reminderSync.syncTask(taskId)
            syncFutureInstancesLocked(routine, skipLinkDay = occurrence)
        }

        suspend fun syncFutureInstances(
            routine: RoutineEntity,
            skipLinkDay: String? = null,
        ) {
            if (repairState.isRepairPending()) return
            generationGate.withExclusive {
                if (repairState.isRepairPending()) return@withExclusive
                syncFutureInstancesLocked(routine, skipLinkDay)
            }
        }

        private suspend fun syncFutureInstancesLocked(
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
                        if (taskDao.updateRoutineTask(updated) > 0) {
                            reminderSync.syncTask(updated.id)
                        }
                    }
                }
                day = day.plusDays(1)
            }
            ensureRollingWindowLocked()
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