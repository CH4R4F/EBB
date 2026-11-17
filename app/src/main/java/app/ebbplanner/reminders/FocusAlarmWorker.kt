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
            val kind =
                inputData.getString(KEY_ALARM_KIND)?.let {
                    runCatching { FocusAlarmKind.valueOf(it) }.getOrNull()
                } ?: if (inputData.getBoolean(KEY_IS_BREAK, false)) {
                    FocusAlarmKind.INTERVAL_BREAK
                } else {
                    FocusAlarmKind.TIMES_UP
                }
            if (taskId.isBlank()) return Result.failure()
            when (kind) {
                FocusAlarmKind.INTERVAL_BREAK -> focusNotifier.showBreakTime(taskId, title.ifBlank { "Focus" })
                FocusAlarmKind.INTERVAL_WORK -> focusNotifier.showBackToWork(taskId, title.ifBlank { "Focus" })
                FocusAlarmKind.MANUAL_BREAK_END -> focusNotifier.showBreakEnded(taskId, title.ifBlank { "Focus" })
                FocusAlarmKind.TIMES_UP -> focusNotifier.showTimesUp(taskId, title.ifBlank { "Focus" })
            }
            return Result.success()
        }

        companion object {
            const val KEY_TASK_ID = "focus_task_id"
            const val KEY_TASK_TITLE = "focus_task_title"
            const val KEY_IS_BREAK = "focus_is_break"
            const val KEY_ALARM_KIND = "focus_alarm_kind"
        }
    }