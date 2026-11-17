package app.ebbplanner.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import app.ebbplanner.MainActivity
import app.ebbplanner.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        init {
            ensureChannels()
        }

        fun showTaskReminder(
            taskId: String,
            title: String,
            body: String? = null,
        ) {
            ensureChannels()
            val openApp =
                PendingIntent.getActivity(
                    context,
                    taskId.hashCode(),
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            val done =
                actionPendingIntent(taskId, title, ReminderActionReceiver.ACTION_DONE, 1)
            val snooze =
                actionPendingIntent(taskId, title, ReminderActionReceiver.ACTION_SNOOZE, 2)
            val shift =
                actionPendingIntent(taskId, title, ReminderActionReceiver.ACTION_SHIFT, 3)

            val notification =
                NotificationCompat
                    .Builder(context, CHANNEL_TASKS)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(body ?: context.getString(R.string.reminder_task_body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(openApp)
                    .addAction(0, context.getString(R.string.reminder_action_done), done)
                    .addAction(0, context.getString(R.string.reminder_action_snooze), snooze)
                    .addAction(0, context.getString(R.string.reminder_action_shift), shift)
                    .build()
            notificationManager.notify(taskId.hashCode(), notification)
        }

        private fun actionPendingIntent(
            taskId: String,
            title: String,
            action: String,
            requestCode: Int,
        ): PendingIntent {
            val intent =
                Intent(context, ReminderActionReceiver::class.java).apply {
                    this.action = action
                    putExtra(ReminderActionReceiver.EXTRA_TASK_ID, taskId)
                    putExtra(ReminderActionReceiver.EXTRA_TITLE, title)
                }
            return PendingIntent.getBroadcast(
                context,
                taskId.hashCode() + requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun ensureChannels() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val tasks =
                NotificationChannel(
                    CHANNEL_TASKS,
                    context.getString(R.string.reminder_channel_tasks),
                    NotificationManager.IMPORTANCE_HIGH,
                )
            val breather =
                NotificationChannel(
                    CHANNEL_BREATHER,
                    context.getString(R.string.reminder_channel_breather),
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            notificationManager.createNotificationChannel(tasks)
            notificationManager.createNotificationChannel(breather)
        }

        companion object {
            const val CHANNEL_TASKS = "ebb_task_reminders"
            const val CHANNEL_BREATHER = "ebb_breather"
        }
    }
