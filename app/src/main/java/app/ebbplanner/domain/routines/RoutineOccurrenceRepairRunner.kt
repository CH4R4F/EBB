package app.ebbplanner.domain.routines

import android.util.Log
import androidx.room.withTransaction
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
        private val generationGate: RoutineGenerationGate,
        private val repairState: RoutineRepairState,
    ) {
        suspend fun repairThenGenerateIfNeeded() {
            generationGate.withExclusive {
                val prefsPending = repairPreferences.repairPending.first()
                val metaPending =
                    RoutineOccurrenceRepair.isPendingInMeta(database.openHelper.writableDatabase)
                val pending = prefsPending || metaPending
                if (pending) {
                    try {
                        database.withTransaction {
                            val db = database.openHelper.writableDatabase
                            RoutineOccurrenceRepair.executeRepair(db)
                            RoutineOccurrenceRepair.clearPendingInMeta(db)
                        }
                        repairPreferences.setRepairPending(false)
                    } catch (t: Throwable) {
                        Log.e(TAG, "Routine occurrence repair failed at app start; will retry later.", t)
                        RoutineOccurrenceRepair.setPendingInMeta(database.openHelper.writableDatabase)
                        repairPreferences.setRepairPending(true)
                        return@withExclusive
                    }
                }
                if (!repairState.isRepairPending()) {
                    routineInstanceGenerator.ensureRollingWindowLocked()
                }
            }
        }

        companion object {
            private const val TAG = "RoutineOccurrenceRepairRunner"
        }
    }