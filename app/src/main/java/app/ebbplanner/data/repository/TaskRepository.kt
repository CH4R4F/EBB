package app.ebbplanner.data.repository

import app.ebbplanner.data.local.dao.DayPlanDao
import app.ebbplanner.data.local.dao.SubtaskDao
import app.ebbplanner.data.local.dao.TaskDao
import app.ebbplanner.data.local.entity.DayPlanEntity
import app.ebbplanner.data.local.entity.SubtaskEntity
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.reminders.TaskReminderSync
import app.ebbplanner.domain.day.DayTimeShift
import app.ebbplanner.domain.routines.RoutineInstanceGenerator
import app.ebbplanner.ui.time.ClockFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class TaskRepository
    @Inject
    constructor(
        private val taskDao: TaskDao,
        private val dayPlanDao: DayPlanDao,
        private val subtaskDao: SubtaskDao,
        private val reminderSync: TaskReminderSync,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val routineInstanceGenerator: RoutineInstanceGenerator,
    ) {
        private val zoneId: ZoneId = ZoneId.systemDefault()

        fun observeTasksForDate(date: LocalDate): Flow<List<TaskEntity>> = taskDao.observeForDate(date.toString())

        fun observeInboxTasks(): Flow<List<TaskEntity>> = taskDao.observeInbox()

        fun observeTasksBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<TaskEntity>> = taskDao.observeBetween(start.toString(), end.toString())

        fun observeDayPlan(date: LocalDate): Flow<DayPlanEntity?> = dayPlanDao.observeForDate(date.toString())

        suspend fun seedSampleTasksForToday(date: LocalDate) {
            val now = System.currentTimeMillis()
            val dateString = date.toString()
            taskDao.insertAll(buildSampleTasks(dateString, now))
            dayPlanDao.insert(
                DayPlanEntity(
                    id = UUID.randomUUID().toString(),
                    date = dateString,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            taskDao.getActiveScheduledWithReminders().forEach { reminderSync.syncTask(it.id) }
        }

        private fun buildSampleTasks(
            dateString: String,
            now: Long,
        ): List<TaskEntity> =
            listOf(
                sampleTask(
                    title = "Morning check-in",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    dateString = dateString,
                    startLocalTime = "09:00",
                    durationMin = 30,
                    colorKey = "green",
                    now = now,
                ),
                sampleTask(
                    title = "Deep work",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    dateString = dateString,
                    startLocalTime = "10:30",
                    durationMin = 90,
                    colorKey = "sky",
                    now = now,
                ),
                sampleTask(
                    title = "Drink water",
                    placement = TaskEntity.PLACEMENT_ANYTIME,
                    dateString = dateString,
                    durationMin = 5,
                    colorKey = "lemon",
                    now = now,
                ),
                sampleTask(
                    title = "Buy stamps",
                    placement = TaskEntity.PLACEMENT_ANYTIME,
                    dateString = dateString,
                    durationMin = 15,
                    colorKey = "rose",
                    now = now,
                ),
            )

        private fun sampleTask(
            title: String,
            placement: String,
            dateString: String,
            now: Long,
            startLocalTime: String? = null,
            durationMin: Int = TaskEntity.DEFAULT_DURATION_MIN,
            colorKey: String = "slate",
        ): TaskEntity =
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                placement = placement,
                date = dateString,
                startLocalTime = startLocalTime,
                zoneId = zoneId.id,
                durationMin = durationMin,
                colorKey = colorKey,
                createdAt = now,
                updatedAt = now,
            )

        suspend fun insertTask(task: TaskEntity) {
            taskDao.insert(task)
            reminderSync.syncTask(task.id)
        }

        suspend fun updateTask(task: TaskEntity) {
            val rows =
                if (task.sourceRoutineId != null) {
                    taskDao.updateRoutineTask(task)
                } else {
                    taskDao.update(task)
                }
            if (rows > 0) {
                reminderSync.syncTask(task.id)
            }
        }

        suspend fun restoreDeletedTask(id: String): Boolean {
            val now = System.currentTimeMillis()
            val rows = taskDao.undeleteRoutineTask(id, now)
            if (rows <= 0) return false
            taskDao.getById(id)?.let { reminderSync.syncTask(id) }
            return true
        }

        suspend fun deleteTask(id: String) {
            val task = taskDao.getById(id)
            val now = System.currentTimeMillis()
            taskDao.softDelete(id, now, now)
            reminderSync.cancelTask(id)
            val routineId = task?.sourceRoutineId
            val occurrence = task?.routineOccurrenceDate ?: task?.date
            if (routineId != null && occurrence != null) {
                routineInstanceGenerator.recordSkip(routineId, occurrence)
            }
        }

        suspend fun markTaskDone(id: String) {
            val task = taskDao.getById(id) ?: return
            val now = System.currentTimeMillis()
            val updated = task.copy(status = TaskEntity.STATUS_DONE, updatedAt = now)
            taskDao.update(updated)
            reminderSync.cancelTask(id)
        }

        suspend fun shiftRestOfDay(
            date: LocalDate,
            offsetMinutes: Int,
            fromMinutes: Int,
            startFromNow: Boolean = false,
        ): Int {
            val tasks =
                observeTasksForDate(date).first().filter { task ->
                    task.placement == TaskEntity.PLACEMENT_SCHEDULED &&
                        task.startLocalTime != null &&
                        task.status != TaskEntity.STATUS_DONE &&
                        !task.pinned
                }
            val now = System.currentTimeMillis()
            if (startFromNow) {
                val parsed =
                    tasks.mapNotNull { task ->
                        val start = ClockFormat.parseMinutes(task.startLocalTime ?: return@mapNotNull null) ?: return@mapNotNull null
                        task to start
                    }
                val overdue = parsed.filter { it.second < fromMinutes }.sortedBy { it.second }
                val upcoming = parsed.filter { it.second >= fromMinutes }.sortedBy { it.second }
                var cursor = fromMinutes
                var shifted = 0
                overdue.forEach { (task, start) ->
                    val shiftedSchedule = DayTimeShift.shiftStart(date, start, cursor - start)
                    val updated =
                        task.copy(
                            date = shiftedSchedule.date.toString(),
                            startLocalTime = shiftedSchedule.startLocalTime,
                            routineInstanceDetached = task.routineInstanceDetached || task.sourceRoutineId != null,
                            updatedAt = now,
                        )
                    if (applyTaskUpdate(task, updated)) {
                        reminderSync.syncTask(task.id)
                    }
                    shifted++
                    cursor += task.durationMin
                }
                if (upcoming.isEmpty()) return shifted
                val push = (cursor - upcoming.first().second).coerceAtLeast(0)
                upcoming.forEach { (task, start) ->
                    val offset = if (push > 0) push else 0
                    val shiftedSchedule = DayTimeShift.shiftStart(date, start, offset)
                    val updated =
                        task.copy(
                            date = shiftedSchedule.date.toString(),
                            startLocalTime = shiftedSchedule.startLocalTime,
                            routineInstanceDetached = task.routineInstanceDetached || task.sourceRoutineId != null,
                            updatedAt = now,
                        )
                    if (applyTaskUpdate(task, updated)) {
                        reminderSync.syncTask(task.id)
                    }
                    shifted++
                }
                return shifted
            }
            val movable =
                tasks.mapNotNull { task ->
                    val start = ClockFormat.parseMinutes(task.startLocalTime ?: return@mapNotNull null) ?: return@mapNotNull null
                    if (start < fromMinutes) return@mapNotNull null
                    task to start
                }
            var shifted = 0
            movable.forEach { (task, start) ->
                val shiftedSchedule = DayTimeShift.shiftStart(date, start, offsetMinutes)
                val updated =
                    task.copy(
                        date = shiftedSchedule.date.toString(),
                        startLocalTime = shiftedSchedule.startLocalTime,
                        routineInstanceDetached = task.routineInstanceDetached || task.sourceRoutineId != null,
                        updatedAt = now,
                    )
                if (applyTaskUpdate(task, updated)) {
                    reminderSync.syncTask(task.id)
                }
                shifted++
            }
            return shifted
        }

        private suspend fun applyTaskUpdate(
            before: TaskEntity,
            updated: TaskEntity,
        ): Boolean {
            val rows =
                if (before.sourceRoutineId != null || updated.sourceRoutineId != null) {
                    taskDao.updateRoutineTask(updated)
                } else {
                    taskDao.update(updated)
                }
            return rows > 0
        }

        suspend fun rescheduleTaskStart(
            taskId: String,
            date: LocalDate,
            startMinutes: Int,
            offsetMinutes: Int,
        ) {
            val shifted = DayTimeShift.shiftStart(date, startMinutes, offsetMinutes)
            scheduleTask(taskId, TaskEntity.PLACEMENT_SCHEDULED, shifted.date, shifted.startLocalTime)
        }

        suspend fun shiftRestOfDayFromNotification(
            date: LocalDate,
            anchorTaskId: String,
            offsetMinutes: Int,
        ): Int {
            val anchor = taskDao.getById(anchorTaskId)
            val from =
                anchor?.startLocalTime?.let { ClockFormat.parseMinutes(it) }
                    ?: java.time.LocalTime.now().hour * 60 + java.time.LocalTime.now().minute
            return shiftRestOfDay(date, offsetMinutes, from)
        }

        suspend fun getTask(id: String): TaskEntity? = taskDao.getById(id)

        suspend fun nextIncompleteTaskAfter(
            date: LocalDate,
            afterTaskId: String,
        ): String? {
            val after = taskDao.getById(afterTaskId)
            val afterStart = after?.startLocalTime?.let { ClockFormat.parseMinutes(it) } ?: -1
            val scheduledNext =
                observeTasksForDate(date).first()
                    .filter {
                        it.id != afterTaskId &&
                            it.status != TaskEntity.STATUS_DONE &&
                            it.placement == TaskEntity.PLACEMENT_SCHEDULED &&
                            it.startLocalTime != null
                    }
                    .mapNotNull { task ->
                        val start = ClockFormat.parseMinutes(task.startLocalTime ?: return@mapNotNull null) ?: return@mapNotNull null
                        if (start <= afterStart) return@mapNotNull null
                        task.id to start
                    }
                    .minByOrNull { it.second }
                    ?.first
            if (scheduledNext != null) return scheduledNext
            return observeTasksForDate(date).first().firstOrNull {
                it.id != afterTaskId &&
                    it.status != TaskEntity.STATUS_DONE &&
                    it.placement == TaskEntity.PLACEMENT_ANYTIME
            }?.id
        }

        fun observeSubtasks(taskId: String) = subtaskDao.observeForTask(taskId)

        suspend fun listSubtasks(taskId: String): List<SubtaskEntity> = subtaskDao.listForTask(taskId)

        suspend fun toggleSubtask(subtaskId: String) {
            val sub = subtaskDao.getById(subtaskId) ?: return
            val now = System.currentTimeMillis()
            subtaskDao.setDone(subtaskId, !sub.done)
            touchTaskUpdated(sub.taskId, now)
        }

        suspend fun addSubtask(
            taskId: String,
            title: String,
        ) {
            val existing = subtaskDao.listForTask(taskId)
            val now = System.currentTimeMillis()
            subtaskDao.insert(
                SubtaskEntity(
                    id = UUID.randomUUID().toString(),
                    taskId = taskId,
                    title = title.trim(),
                    sortOrder = existing.size,
                    durationMin = null,
                    done = false,
                ),
            )
            touchTaskUpdated(taskId, now)
        }

        suspend fun updateTaskFields(
            id: String,
            transform: (TaskEntity) -> TaskEntity,
        ) {
            val task = taskDao.getById(id) ?: return
            var updated = transform(task).copy(updatedAt = System.currentTimeMillis())
            if (task.sourceRoutineId != null) {
                val breatherOnly =
                    task.breatherBufferMin != updated.breatherBufferMin &&
                        updated.title == task.title &&
                        updated.startLocalTime == task.startLocalTime &&
                        (updated.durationMin - task.durationMin) == (updated.breatherBufferMin - task.breatherBufferMin)
                val customized =
                    !breatherOnly &&
                        (
                            updated.title != task.title ||
                                updated.durationMin != task.durationMin ||
                                updated.startLocalTime != task.startLocalTime
                        )
                if (customized) {
                    updated = updated.copy(routineInstanceDetached = true)
                }
            }
            val rows =
                if (task.sourceRoutineId != null || updated.sourceRoutineId != null) {
                    taskDao.updateRoutineTask(updated)
                } else {
                    taskDao.update(updated)
                }
            if (rows > 0) {
                reminderSync.syncTask(id)
            }
        }

        private suspend fun touchTaskUpdated(
            taskId: String,
            now: Long,
        ) {
            val task = taskDao.getById(taskId) ?: return
            taskDao.update(task.copy(updatedAt = now))
        }

        suspend fun scheduleTask(
            id: String,
            placement: String,
            date: LocalDate?,
            startLocalTime: String?,
        ) {
            val defaultReminders = userPreferencesRepository.remindersDefault.first()
            val before = taskDao.getById(id)
            val newDate = date?.toString()
            if (before?.sourceRoutineId != null) {
                val oldOccurrence = before.routineOccurrenceDate ?: before.date
                if (oldOccurrence != null &&
                    newDate != null &&
                    oldOccurrence != newDate &&
                    before.routineOccurrenceDate == newDate
                ) {
                    routineInstanceGenerator.recordSkip(before.sourceRoutineId, oldOccurrence)
                }
            }
            updateTaskFields(id) { task ->
                val wasScheduled = task.placement == TaskEntity.PLACEMENT_SCHEDULED && task.startLocalTime != null
                val userDisabled = wasScheduled && !task.reminderEnabled
                val enableReminders =
                    when {
                        placement != TaskEntity.PLACEMENT_SCHEDULED || startLocalTime == null -> false
                        userDisabled -> false
                        else -> defaultReminders
                    }
                val priorOccurrence = task.routineOccurrenceDate ?: task.date
                val keptOccurrence =
                    if (task.sourceRoutineId != null && newDate != null) {
                        when {
                            task.routineOccurrenceDate != null -> task.routineOccurrenceDate
                            priorOccurrence == newDate -> newDate
                            priorOccurrence != null -> priorOccurrence
                            else -> newDate
                        }
                    } else {
                        task.routineOccurrenceDate
                    }
                val next =
                    task.copy(
                        placement = placement,
                        date = date?.toString(),
                        startLocalTime = startLocalTime,
                        reminderEnabled = enableReminders,
                        routineOccurrenceDate = keptOccurrence,
                    )
                if (task.sourceRoutineId != null &&
                    (next.startLocalTime != task.startLocalTime || next.date != task.date)
                ) {
                    next.copy(routineInstanceDetached = true)
                } else {
                    next
                }
            }
        }

        suspend fun addQuickCapture(
            title: String,
            date: LocalDate,
            placement: String = TaskEntity.PLACEMENT_INBOX,
            startLocalTime: String? = null,
            durationMin: Int = TaskEntity.DEFAULT_DURATION_MIN,
            reminderEnabled: Boolean? = null,
        ): TaskEntity {
            val defaultReminders = userPreferencesRepository.remindersDefault.first()
            val enableReminders =
                when {
                    reminderEnabled != null -> reminderEnabled
                    placement != TaskEntity.PLACEMENT_SCHEDULED || startLocalTime == null -> false
                    else -> defaultReminders
                }
            val now = System.currentTimeMillis()
            val task =
                TaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = title.trim(),
                    placement = placement,
                    date = if (placement == TaskEntity.PLACEMENT_INBOX) null else date.toString(),
                    startLocalTime = startLocalTime,
                    zoneId = zoneId.id,
                    durationMin = durationMin,
                    reminderEnabled = enableReminders,
                    createdAt = now,
                    updatedAt = now,
                )
            taskDao.insert(task)
            reminderSync.syncTask(task.id)
            return task
        }
    }
