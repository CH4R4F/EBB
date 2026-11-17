@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {
    @Inject lateinit var rescheduleUseCase: ReminderRescheduleUseCase

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pendingResult = goAsync()
        val taskId = intent.getStringExtra(EXTRA_TASK_ID)
        val title = intent.getStringExtra(EXTRA_TITLE)
        receiverScope.launch {
            try {
                when (intent.action) {
                    ACTION_SNOOZE -> {
                        rescheduleUseCase.dismissNotification(taskId)
                        rescheduleUseCase.snoozeTask(taskId, title)
                    }
                    ACTION_DONE -> {
                        rescheduleUseCase.markDoneFromNotification(taskId)
                    }
                    ACTION_SHIFT -> {
                        rescheduleUseCase.shiftDayFromNotification(taskId)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TITLE = "title"
        const val ACTION_DONE = "app.ebbplanner.reminder.DONE"
        const val ACTION_SNOOZE = "app.ebbplanner.reminder.SNOOZE"
        const val ACTION_SHIFT = "app.ebbplanner.reminder.SHIFT"
    }
}