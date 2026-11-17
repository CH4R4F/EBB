package app.ebbplanner.domain.routines

import android.util.Log
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.RoutineOccurrenceRepair
import app.ebbplanner.data.preferences.RoutineRepairPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineOccurrenceRepairRunner
    @Inject
    constructor(
        private val database: EbbDatabase,
        private val repairPreferences: RoutineRepairPreferences,
        private val routineInstanceGenerator: RoutineInstanceGenerator,
    ) {
        suspend fun repairThenGenerateIfNeeded() {
            val prefsPending = repairPreferences.repairPending.first()
            val metaPending = RoutineOccurrenceRepair.isPendingInMeta(database.openHelper.writableDatabase)
            if (!prefsPending && !metaPending) {
                routineInstanceGenerator.ensureRollingWindow()
                return
            }
            val db = database.openHelper.writableDatabase
            try {
                RoutineOccurrenceRepair.executeRepair(db)
                RoutineOccurrenceRepair.clearPendingInMeta(db)
                repairPreferences.setRepairPending(false)
            } catch (t: Throwable) {
                Log.e(TAG, "Routine occurrence repair failed at app start; will retry later.", t)
                RoutineOccurrenceRepair.setPendingInMeta(db)
                repairPreferences.setRepairPending(true)
                return
            }
            routineInstanceGenerator.ensureRollingWindow()
        }

        companion object {
            private const val TAG = "RoutineOccurrenceRepairRunner"
        }
    }