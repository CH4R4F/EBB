package app.ebbplanner.domain.routines

import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.RoutineOccurrenceRepair
import app.ebbplanner.data.preferences.RoutineRepairPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepairState
    @Inject
    constructor(
        private val database: EbbDatabase,
        private val repairPreferences: RoutineRepairPreferences,
    ) {
        suspend fun isRepairPending(): Boolean {
            val prefsPending = repairPreferences.repairPending.first()
            val metaPending =
                RoutineOccurrenceRepair.isPendingInMeta(database.openHelper.readableDatabase)
            return prefsPending || metaPending
        }
    }