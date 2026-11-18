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
                    RoutineOccurrenceRepair.isPendingInMeta(database.openHelper.readableDatabase)
                var pending = prefsPending || metaPending
                if (pending) {
                    val priorAttempts = repairPreferences.repairAttempts.first()
                    if (priorAttempts >= MAX_REPAIR_ATTEMPTS) {
                        abandonPendingRepair(priorAttempts)
                        pending = false
                    } else {
                        try {
                            database.withTransaction {
                                val db = database.openHelper.writableDatabase
                                RoutineOccurrenceRepair.executeRepair(db)
                                RoutineOccurrenceRepair.clearPendingInMeta(db)
                            }
                            repairPreferences.setRepairPending(false)
                            repairPreferences.resetRepairAttempts()
                            pending = false
                        } catch (t: Throwable) {
                            val attempts = repairPreferences.incrementRepairAttempts()
                            Log.e(TAG, "Routine occurrence repair failed at app start (attempt $attempts).", t)
                            RoutineOccurrenceRepair.setPendingInMeta(database.openHelper.writableDatabase)
                            repairPreferences.setRepairPending(true)
                            if (attempts >= MAX_REPAIR_ATTEMPTS) {
                                abandonPendingRepair(attempts, t)
                                pending = false
                            } else {
                                return@withExclusive
                            }
                        }
                    }
                }
                if (!pending && !repairState.isRepairPending()) {
                    routineInstanceGenerator.drainDeferredThenEnsureRollingWindowLocked()
                }
            }
        }

        private suspend fun abandonPendingRepair(
            attempts: Int,
            cause: Throwable? = null,
        ) {
            Log.e(
                TAG,
                "Abandoning routine repair after $attempts failed attempts; allowing generation to proceed.",
                cause,
            )
            database.withTransaction {
                RoutineOccurrenceRepair.clearPendingInMeta(database.openHelper.writableDatabase)
            }
            repairPreferences.setRepairPending(false)
            repairPreferences.resetRepairAttempts()
        }

        companion object {
            private const val TAG = "RoutineOccurrenceRepairRunner"
            const val MAX_REPAIR_ATTEMPTS = 3
        }
    }