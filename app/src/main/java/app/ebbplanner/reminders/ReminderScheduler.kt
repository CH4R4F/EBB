package app.ebbplanner.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.ebbplanner.domain.reminders.ReminderDeliveryMode
import app.ebbplanner.domain.reminders.ReminderRequest
import app.ebbplanner.domain.reminders.ReminderSchedulePlanner
import app.ebbplanner.domain.reminders.ScheduledReminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val alarmManager: AlarmManager,
    ) {
        fun rescheduleAll(
            requests: List<ReminderRequest>,
            exactAlarmsGranted: Boolean,
        ) {
            val planned =
                ReminderSchedulePlanner.plan(
                    requests = requests,
                    exactAlarmsGranted = exactAlarmsGranted,
                    nowEpochMillis = System.currentTimeMillis(),
                )
            planned.forEach { scheduleOne(it) }
        }

        fun scheduleTask(
            request: ReminderRequest,
            exactAlarmsGranted: Boolean,
        ) {
            val planned =
                ReminderSchedulePlanner.plan(
                    requests = listOf(request),
                    exactAlarmsGranted = exactAlarmsGranted,
                    nowEpochMillis = System.currentTimeMillis(),
                ).firstOrNull() ?: return
            scheduleOne(planned)
        }

        private fun scheduleOne(reminder: ScheduledReminder) {
            when (reminder.delivery) {
                ReminderDeliveryMode.ExactAlarm -> scheduleExact(reminder.request)
                ReminderDeliveryMode.InexactWorkManager -> scheduleWork(reminder)
            }
        }

        private fun scheduleExact(request: ReminderRequest) {
            val intent =
                Intent(context, ReminderAlarmReceiver::class.java).apply {
                    putExtra(ReminderAlarmReceiver.EXTRA_TASK_ID, request.taskId)
                    putExtra(ReminderAlarmReceiver.EXTRA_TITLE, request.title)
                }
            val pending =
                PendingIntent.getBroadcast(
                    context,
                    alarmRequestCode(request.taskId),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    request.fireAtEpochMillis,
                    pending,
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    request.fireAtEpochMillis,
                    pending,
                )
            }
        }

        private fun scheduleWork(reminder: ScheduledReminder) {
            val delay = (reminder.request.fireAtEpochMillis - System.currentTimeMillis()).coerceAtLeast(0)
            val work =
                OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            ReminderWorker.KEY_TASK_ID to reminder.request.taskId,
                            ReminderWorker.KEY_TITLE to reminder.request.title,
                        ),
                    )
                    .addTag(workTag(reminder.request.taskId))
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName(reminder.request.taskId),
                ExistingWorkPolicy.REPLACE,
                work,
            )
        }

        fun cancelForTask(taskId: String) {
            val alarmIntent =
                Intent(context, ReminderAlarmReceiver::class.java).apply {
                    putExtra(ReminderAlarmReceiver.EXTRA_TASK_ID, taskId)
                }
            val pending =
                PendingIntent.getBroadcast(
                    context,
                    alarmRequestCode(taskId),
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            alarmManager.cancel(pending)
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName(taskId))
        }

        companion object {
            fun uniqueWorkName(taskId: String): String = "ebb-reminder-$taskId"

            fun workTag(taskId: String): String = "ebb-reminder-tag-$taskId"

            fun alarmRequestCode(taskId: String): Int = taskId.hashCode()
        }
    }
