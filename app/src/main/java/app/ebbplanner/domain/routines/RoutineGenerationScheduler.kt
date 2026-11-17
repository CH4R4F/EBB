package app.ebbplanner.domain.routines

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RoutineGenerationScheduler {
    private const val UNIQUE_NAME = "ebb-routine-generation"

    fun schedule(context: Context) {
        val request =
            PeriodicWorkRequestBuilder<RoutineGenerationWorker>(1, TimeUnit.DAYS)
                .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun runNow(context: Context) {
        val request =
            androidx.work.OneTimeWorkRequestBuilder<RoutineGenerationWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}