package app.ebbplanner.domain.routines

import app.ebbplanner.data.local.dao.RoutineDao
import app.ebbplanner.data.local.dao.TaskDao
import app.ebbplanner.data.local.entity.RoutineEntity
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.reminders.TaskReminderSync
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineInstanceGenerator
    @Inject
    constructor(
        private val routineDao: RoutineDao,
        private val taskDao: TaskDao,
        private val reminderSync: TaskReminderSync,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) {
        suspend fun ensureForDate(date: LocalDate) {
            val remindersOn = userPreferencesRepository.remindersDefault.first()
            val routines = routineDao.listEnabled()
            val now = System.currentTimeMillis()
            val dateString = date.toString()
            routines.forEach { routine ->
                if (!routine.enabled || !appliesOn(routine, date)) return@forEach
                if (taskDao.getByRoutineAndDate(routine.id, dateString) != null) return@forEach
                val task =
                    TaskEntity(
                        id = UUID.randomUUID().toString(),
                        title = routine.title,
                        placement = TaskEntity.PLACEMENT_SCHEDULED,
                        date = dateString,
                        startLocalTime = routine.startLocalTime,
                        durationMin = routine.durationMin,
                        colorKey = routine.colorKey,
                        iconKey = routine.iconKey,
                        reminderEnabled = remindersOn,
                        sourceRoutineId = routine.id,
                        createdAt = now,
                        updatedAt = now,
                    )
                taskDao.insert(task)
                reminderSync.syncTask(task.id)
            }
        }

        private fun appliesOn(
            routine: RoutineEntity,
            date: LocalDate,
        ): Boolean =
            when (routine.repeatRule) {
                RoutineEntity.REPEAT_DAILY -> true
                RoutineEntity.REPEAT_WEEKDAYS ->
                    date.dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY
                RoutineEntity.REPEAT_WEEKLY -> {
                    if (routine.weeklyDays.isBlank()) {
                        true
                    } else {
                        val allowed =
                            routine.weeklyDays.split(',')
                                .mapNotNull { it.trim().toIntOrNull() }
                                .toSet()
                        allowed.contains(date.dayOfWeek.value)
                    }
                }
                else -> false
            }
    }