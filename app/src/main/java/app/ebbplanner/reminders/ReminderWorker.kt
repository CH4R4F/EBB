package app.ebbplanner.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters,
        private val notifier: ReminderNotifier,
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            val taskId = inputData.getString(KEY_TASK_ID)
            val title = inputData.getString(KEY_TITLE)
            return if (taskId.isNullOrBlank() || title.isNullOrBlank()) {
                Result.failure()
            } else {
                notifier.showTaskReminder(taskId, title)
                Result.success()
            }
        }

        companion object {
            const val KEY_TASK_ID = "task_id"
            const val KEY_TITLE = "title"
        }
    }
