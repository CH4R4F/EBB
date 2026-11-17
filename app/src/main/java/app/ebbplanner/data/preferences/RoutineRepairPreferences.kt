package app.ebbplanner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.routineRepairDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "routine_repair",
)

@Singleton
class RoutineRepairPreferences
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.routineRepairDataStore

        val repairPending: Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[KEY_REPAIR_PENDING] ?: false
            }

        suspend fun setRepairPending(pending: Boolean) {
            dataStore.edit { prefs ->
                prefs[KEY_REPAIR_PENDING] = pending
            }
        }

        companion object {
            private val KEY_REPAIR_PENDING = booleanPreferencesKey("routine_repair_pending")
        }
    }