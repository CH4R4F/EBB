package app.ebbplanner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.ebbPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ebb_user_preferences",
)

@Singleton
class UserPreferencesRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.ebbPreferencesDataStore

        val onboardingComplete: Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[KEY_ONBOARDING_COMPLETE] ?: false
            }

        val dayWakeMinutes: Flow<Int> =
            dataStore.data.map { prefs ->
                prefs[KEY_DAY_WAKE_MINUTES] ?: DEFAULT_WAKE_MINUTES
            }

        val dayBedMinutes: Flow<Int> =
            dataStore.data.map { prefs ->
                prefs[KEY_DAY_BED_MINUTES] ?: DEFAULT_BED_MINUTES
            }

        val seedSampleTasks: Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[KEY_SEED_SAMPLE_TASKS] ?: false
            }

        val useMaterialYou: Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[KEY_USE_MATERIAL_YOU] ?: false
            }

        suspend fun setOnboardingComplete(complete: Boolean) {
            dataStore.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETE] = complete }
        }

        suspend fun setDayWakeMinutes(minutes: Int) {
            dataStore.edit { prefs -> prefs[KEY_DAY_WAKE_MINUTES] = minutes }
        }

        suspend fun setDayBedMinutes(minutes: Int) {
            dataStore.edit { prefs -> prefs[KEY_DAY_BED_MINUTES] = minutes }
        }

        suspend fun setSeedSampleTasks(enabled: Boolean) {
            dataStore.edit { prefs -> prefs[KEY_SEED_SAMPLE_TASKS] = enabled }
        }

        suspend fun setUseMaterialYou(enabled: Boolean) {
            dataStore.edit { prefs -> prefs[KEY_USE_MATERIAL_YOU] = enabled }
        }

        suspend fun setBreatherMinutes(minutes: Int) {
            dataStore.edit { prefs -> prefs[KEY_BREATHER_MINUTES] = minutes }
        }

        suspend fun setCelebrationStyle(style: String) {
            dataStore.edit { prefs -> prefs[KEY_CELEBRATION_STYLE] = style }
        }

        companion object {
            private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
            private val KEY_DAY_WAKE_MINUTES = intPreferencesKey("day_wake_minutes")
            private val KEY_DAY_BED_MINUTES = intPreferencesKey("day_bed_minutes")
            private val KEY_SEED_SAMPLE_TASKS = booleanPreferencesKey("seed_sample_tasks")
            private val KEY_USE_MATERIAL_YOU = booleanPreferencesKey("use_material_you")
            private val KEY_BREATHER_MINUTES = intPreferencesKey("breather_minutes")
            private val KEY_CELEBRATION_STYLE = stringPreferencesKey("celebration_style")
            const val DEFAULT_WAKE_MINUTES = 7 * 60
            const val DEFAULT_BED_MINUTES = 23 * 60
        }
    }
