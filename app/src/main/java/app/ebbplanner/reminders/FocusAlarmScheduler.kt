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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusAlarmScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val alarmManager: AlarmManager,
        private val alarmGrant: ExactAlarmGrant,
    ) {
        fun schedulePhaseEnd(
            taskId: String,
            title: String,
            triggerAtMs: Long,
            kind: FocusAlarmKind,
        ) {
            cancel()
            val delay = triggerAtMs - System.currentTimeMillis()
            if (delay <= 0L) return
            if (alarmGrant.isGranted()) {
                try {
                    scheduleExact(taskId, title, triggerAtMs, kind)
                    return
                } catch (_: SecurityException) {
                    // fall through to WorkManager
                }
            }
            scheduleWork(taskId, title, delay, kind)
        }

        fun scheduleTimesUp(
            taskId: String,
            title: String,
            triggerAtMs: Long,
            isBreak: Boolean = false,
        ) {
            schedulePhaseEnd(
                taskId,
                title,
                triggerAtMs,
                when {
                    isBreak -> FocusAlarmKind.INTERVAL_BREAK
                    else -> FocusAlarmKind.TIMES_UP
                },
            )
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
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK)
        }

        private fun scheduleExact(
            taskId: String,
            title: String,
            triggerAtMs: Long,
            kind: FocusAlarmKind,
        ) {
            val intent =
                Intent(context, FocusTimesUpReceiver::class.java).apply {
                    putExtra(EXTRA_TASK_ID, taskId)
                    putExtra(EXTRA_TASK_TITLE, title)
                    putExtra(EXTRA_ALARM_KIND, kind.name)
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

        private fun scheduleWork(
            taskId: String,
            title: String,
            delayMs: Long,
            kind: FocusAlarmKind,
        ) {
            val work =
                OneTimeWorkRequestBuilder<FocusAlarmWorker>()
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            FocusAlarmWorker.KEY_TASK_ID to taskId,
                            FocusAlarmWorker.KEY_TASK_TITLE to title,
                            FocusAlarmWorker.KEY_ALARM_KIND to kind.name,
                        ),
                    )
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK,
                ExistingWorkPolicy.REPLACE,
                work,
            )
        }

        companion object {
            const val EXTRA_TASK_ID = "focus_task_id"
            const val EXTRA_TASK_TITLE = "focus_task_title"
            const val EXTRA_IS_BREAK = "focus_is_break"
            const val EXTRA_ALARM_KIND = "focus_alarm_kind"
            private const val REQUEST_CODE = 42_002
            private const val UNIQUE_WORK = "ebb-focus-alarm"
        }
    }

enum class FocusAlarmKind {
    TIMES_UP,
    INTERVAL_BREAK,
    INTERVAL_WORK,
    MANUAL_BREAK_END,
}