package app.ebbplanner.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {
    @Inject lateinit var rescheduleUseCase: ReminderRescheduleUseCase

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            ACTION_SNOOZE -> rescheduleUseCase.snoozeTask(intent.getStringExtra(EXTRA_TASK_ID))
            ACTION_DONE, ACTION_SHIFT -> Unit
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val ACTION_DONE = "app.ebbplanner.reminder.DONE"
        const val ACTION_SNOOZE = "app.ebbplanner.reminder.SNOOZE"
        const val ACTION_SHIFT = "app.ebbplanner.reminder.SHIFT"
    }
}
