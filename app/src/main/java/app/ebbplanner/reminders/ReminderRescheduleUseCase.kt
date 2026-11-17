package app.ebbplanner.reminders

import android.app.NotificationManager
import android.content.Context
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.reminders.ReminderKind
import app.ebbplanner.domain.reminders.ReminderRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRescheduleUseCase
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val taskReminderSync: TaskReminderSync,
        private val taskRepository: TaskRepository,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun rescheduleNow() {
            scope.launch {
                taskReminderSync.rescheduleAllEnabledFuture()
            }
        }

        fun snoozeTask(
            taskId: String?,
            title: String?,
        ) {
            if (taskId == null) return
            scope.launch {
                val taskTitle = title ?: taskRepository.getTask(taskId)?.title ?: return@launch
                taskReminderSync.cancelTask(taskId)
                val request =
                    ReminderRequest(
                        taskId = taskId,
                        fireAtEpochMillis = System.currentTimeMillis() + SNOOZE_MILLIS,
                        kind = ReminderKind.TaskStart,
                        title = taskTitle,
                        exact = true,
                    )
                taskReminderSync.scheduleOneOff(request)
            }
        }

        fun markDoneFromNotification(taskId: String?) {
            if (taskId == null) return
            scope.launch {
                taskRepository.markTaskDone(taskId)
                dismissNotification(taskId)
            }
        }

        fun shiftDayFromNotification(taskId: String?) {
            if (taskId == null) return
            scope.launch {
                taskRepository.shiftScheduledTaskByMinutes(taskId, SHIFT_MINUTES)
                dismissNotification(taskId)
            }
        }

        fun dismissNotification(taskId: String?) {
            if (taskId == null) return
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId.hashCode())
        }

        companion object {
            private const val SNOOZE_MILLIS = 10 * 60 * 1000L
            private const val SHIFT_MINUTES = 15
        }
    }
