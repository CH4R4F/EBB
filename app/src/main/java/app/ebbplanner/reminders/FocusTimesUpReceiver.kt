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
        FocusNotifier(context).showTimesUp(taskId, title.ifBlank { "Focus" })
    }
}