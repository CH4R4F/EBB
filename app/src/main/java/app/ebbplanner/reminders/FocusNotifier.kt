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
class FocusNotifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        init {
            ensureChannel()
        }

        fun showTimesUp(
            taskId: String,
            title: String,
        ) {
            ensureChannel()
            val open =
                PendingIntent.getActivity(
                    context,
                    NOTIFICATION_ID,
                    Intent(context, MainActivity::class.java).apply {
                        putExtra(EXTRA_OPEN_FOCUS, taskId)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            val notification =
                NotificationCompat
                    .Builder(context, CHANNEL_FOCUS)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(context.getString(R.string.focus_times_up_title, title))
                    .setContentText(context.getString(R.string.focus_times_up_body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setContentIntent(open)
                    .build()
            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        fun cancel() {
            notificationManager.cancel(NOTIFICATION_ID)
        }

        private fun ensureChannel() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val channel =
                NotificationChannel(
                    CHANNEL_FOCUS,
                    context.getString(R.string.focus_channel_name),
                    NotificationManager.IMPORTANCE_HIGH,
                )
            notificationManager.createNotificationChannel(channel)
        }

        companion object {
            const val CHANNEL_FOCUS = "ebb_focus_timer"
            const val NOTIFICATION_ID = 42_001
            const val EXTRA_OPEN_FOCUS = "open_focus_task_id"

            fun focusRouteFor(taskId: String): String = "flow/focus/$taskId"
        }
    }
