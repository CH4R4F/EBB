package app.ebbplanner.domain.routines

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RoutineGenerationWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val generator: RoutineInstanceGenerator,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            generator.ensureRollingWindow(RoutineInstanceGenerator.DEFAULT_WINDOW_DAYS)
            return Result.success()
        }
    }