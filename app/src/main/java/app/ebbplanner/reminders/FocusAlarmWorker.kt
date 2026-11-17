package app.ebbplanner.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FocusAlarmWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val focusNotifier: FocusNotifier,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val taskId = inputData.getString(KEY_TASK_ID).orEmpty()
            val title = inputData.getString(KEY_TASK_TITLE).orEmpty()
            val isBreak = inputData.getBoolean(KEY_IS_BREAK, false)
            if (taskId.isBlank()) return Result.failure()
            if (isBreak) {
                focusNotifier.showBreakEnded(taskId, title.ifBlank { "Focus" })
            } else {
                focusNotifier.showTimesUp(taskId, title.ifBlank { "Focus" })
            }
            return Result.success()
        }

        companion object {
            const val KEY_TASK_ID = "focus_task_id"
            const val KEY_TASK_TITLE = "focus_task_title"
            const val KEY_IS_BREAK = "focus_is_break"
        }
    }