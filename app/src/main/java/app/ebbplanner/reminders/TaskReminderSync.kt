package app.ebbplanner.reminders

import app.ebbplanner.data.local.dao.ReminderDao
import app.ebbplanner.data.local.dao.TaskDao
import app.ebbplanner.data.local.entity.ReminderEntity
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.domain.reminders.ReminderKind
import app.ebbplanner.domain.reminders.ReminderRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskReminderSync
    @Inject
    constructor(
        private val taskDao: TaskDao,
        private val reminderDao: ReminderDao,
        private val scheduler: ReminderScheduler,
        private val alarmGrant: ExactAlarmGrant,
    ) {
        suspend fun syncTask(taskId: String) =
            withContext(Dispatchers.IO) {
                scheduler.cancelForTask(taskId)
                reminderDao.deleteForTask(taskId)
                val task = taskDao.getById(taskId) ?: return@withContext
                val request = task.toReminderRequest() ?: return@withContext
                val now = System.currentTimeMillis()
                reminderDao.upsertAll(
                    listOf(
                        ReminderEntity(
                            id = UUID.randomUUID().toString(),
                            taskId = task.id,
                            fireAtEpochMillis = request.fireAtEpochMillis,
                            kind = request.kind.name,
                            title = request.title,
                            exact = request.exact,
                            createdAt = now,
                            updatedAt = now,
                        ),
                    ),
                )
                scheduler.scheduleTask(request, alarmGrant.isGranted())
            }

        suspend fun cancelTask(taskId: String) =
            withContext(Dispatchers.IO) {
                scheduler.cancelForTask(taskId)
                reminderDao.deleteForTask(taskId)
            }

        suspend fun scheduleOneOff(request: ReminderRequest) =
            withContext(Dispatchers.IO) {
                scheduler.scheduleTask(request, alarmGrant.isGranted())
            }

        suspend fun rescheduleAllEnabledFuture() =
            withContext(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                val tasks = taskDao.getActiveScheduledWithReminders()
                val requests =
                    tasks.mapNotNull { task ->
                        task.toReminderRequest()?.takeIf { it.fireAtEpochMillis > now }
                    }
                val existing = reminderDao.getAll()
                existing.forEach { scheduler.cancelForTask(it.taskId) }
                reminderDao.deleteAll()
                reminderDao.upsertAll(
                    requests.map { req ->
                        ReminderEntity(
                            id = UUID.randomUUID().toString(),
                            taskId = req.taskId,
                            fireAtEpochMillis = req.fireAtEpochMillis,
                            kind = req.kind.name,
                            title = req.title,
                            exact = req.exact,
                            createdAt = now,
                            updatedAt = now,
                        )
                    },
                )
                scheduler.rescheduleAll(requests, alarmGrant.isGranted())
            }

        @Suppress("ReturnCount")
        private fun TaskEntity.toReminderRequest(): ReminderRequest? {
            if (!reminderEnabled) return null
            if (placement != TaskEntity.PLACEMENT_SCHEDULED) return null
            val start = startLocalTime ?: return null
            val parts = start.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val day = date?.let { LocalDate.parse(it) } ?: return null
            val fire =
                day.atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            return ReminderRequest(
                taskId = id,
                fireAtEpochMillis = fire,
                kind = ReminderKind.TaskStart,
                title = title,
                exact = true,
            )
        }
    }
