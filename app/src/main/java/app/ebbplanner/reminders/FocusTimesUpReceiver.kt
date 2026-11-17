package app.ebbplanner.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FocusTimesUpReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val taskId = intent.getStringExtra(FocusAlarmScheduler.EXTRA_TASK_ID).orEmpty()
        val title = intent.getStringExtra(FocusAlarmScheduler.EXTRA_TASK_TITLE).orEmpty()
        if (taskId.isBlank()) return
        val kind =
            intent.getStringExtra(FocusAlarmScheduler.EXTRA_ALARM_KIND)?.let {
                runCatching { FocusAlarmKind.valueOf(it) }.getOrNull()
            } ?: if (intent.getBooleanExtra(FocusAlarmScheduler.EXTRA_IS_BREAK, false)) {
                FocusAlarmKind.INTERVAL_BREAK
            } else {
                FocusAlarmKind.TIMES_UP
            }
        val notifier = FocusNotifier(context)
        when (kind) {
            FocusAlarmKind.INTERVAL_BREAK -> notifier.showBreakTime(taskId, title.ifBlank { "Focus" })
            FocusAlarmKind.INTERVAL_WORK -> notifier.showBackToWork(taskId, title.ifBlank { "Focus" })
            FocusAlarmKind.TIMES_UP -> notifier.showTimesUp(taskId, title.ifBlank { "Focus" })
        }
    }
}