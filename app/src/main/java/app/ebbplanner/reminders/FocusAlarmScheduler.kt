package app.ebbplanner.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusAlarmScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        fun scheduleTimesUp(
            taskId: String,
            title: String,
            triggerAtMs: Long,
        ) {
            val intent =
                Intent(context, FocusTimesUpReceiver::class.java).apply {
                    putExtra(EXTRA_TASK_ID, taskId)
                    putExtra(EXTRA_TASK_TITLE, title)
                }
            val pending =
                PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pending)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pending)
            }
        }

        fun cancel() {
            val intent = Intent(context, FocusTimesUpReceiver::class.java)
            val pending =
                PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
                )
            pending?.let { alarmManager.cancel(it) }
        }

        companion object {
            const val EXTRA_TASK_ID = "focus_task_id"
            const val EXTRA_TASK_TITLE = "focus_task_title"
            private const val REQUEST_CODE = 42_002
        }
    }