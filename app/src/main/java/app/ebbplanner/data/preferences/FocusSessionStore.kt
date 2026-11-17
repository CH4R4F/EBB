package app.ebbplanner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.focusSessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_session")

data class PersistedFocusSession(
    val taskId: String,
    val phase: String,
    val endAtMs: Long,
    val totalMs: Long,
    val pausedRemainingMs: Long,
    val timerMode: Int,
    val showRing: Boolean,
    val hideNumbers: Boolean,
    val fixedMinutes: Int,
    val countUp: Boolean,
    val intervalMode: Boolean,
    val countUpStartedAtMs: Long,
    val intervalCyclesCompleted: Int,
    val phaseStartedAtMs: Long,
    val manualBreak: Boolean = false,
)

@Singleton
class FocusSessionStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        val session: Flow<PersistedFocusSession?> =
            context.focusSessionDataStore.data.map { prefs ->
                val taskId = prefs[TASK_ID] ?: return@map null
                if (taskId.isBlank()) return@map null
                PersistedFocusSession(
                    taskId = taskId,
                    phase = prefs[PHASE] ?: "Setup",
                    endAtMs = prefs[END_AT_MS] ?: 0L,
                    totalMs = prefs[TOTAL_MS] ?: 0L,
                    pausedRemainingMs = prefs[PAUSED_REMAINING_MS] ?: 0L,
                    timerMode = prefs[TIMER_MODE] ?: TIMER_MODE_BLOCK,
                    showRing = prefs[SHOW_RING] ?: true,
                    hideNumbers = prefs[HIDE_NUMBERS] ?: false,
                    fixedMinutes = prefs[FIXED_MINUTES] ?: 25,
                    countUp = prefs[COUNT_UP] ?: false,
                    intervalMode = prefs[INTERVAL_MODE] ?: false,
                    countUpStartedAtMs = prefs[COUNT_UP_STARTED_AT_MS] ?: 0L,
                    intervalCyclesCompleted = prefs[INTERVAL_CYCLES_COMPLETED] ?: 0,
                    phaseStartedAtMs = prefs[PHASE_STARTED_AT_MS] ?: 0L,
                    manualBreak = prefs[MANUAL_BREAK] ?: false,
                )
            }

        suspend fun save(session: PersistedFocusSession) {
            context.focusSessionDataStore.edit { prefs ->
                prefs[TASK_ID] = session.taskId
                prefs[PHASE] = session.phase
                prefs[END_AT_MS] = session.endAtMs
                prefs[TOTAL_MS] = session.totalMs
                prefs[PAUSED_REMAINING_MS] = session.pausedRemainingMs
                prefs[TIMER_MODE] = session.timerMode
                prefs[SHOW_RING] = session.showRing
                prefs[HIDE_NUMBERS] = session.hideNumbers
                prefs[FIXED_MINUTES] = session.fixedMinutes
                prefs[COUNT_UP] = session.countUp
                prefs[INTERVAL_MODE] = session.intervalMode
                prefs[COUNT_UP_STARTED_AT_MS] = session.countUpStartedAtMs
                prefs[INTERVAL_CYCLES_COMPLETED] = session.intervalCyclesCompleted
                prefs[PHASE_STARTED_AT_MS] = session.phaseStartedAtMs
                prefs[MANUAL_BREAK] = session.manualBreak
            }
        }

        suspend fun clear() {
            context.focusSessionDataStore.edit { it.clear() }
        }

        companion object {
            const val TIMER_MODE_BLOCK = 0
            const val TIMER_MODE_FIXED = 1
            const val TIMER_MODE_COUNT_UP = 2
            const val TIMER_MODE_INTERVALS = 3

            private val TASK_ID = stringPreferencesKey("task_id")
            private val PHASE = stringPreferencesKey("phase")
            private val END_AT_MS = longPreferencesKey("end_at_ms")
            private val TOTAL_MS = longPreferencesKey("total_ms")
            private val PAUSED_REMAINING_MS = longPreferencesKey("paused_remaining_ms")
            private val TIMER_MODE = intPreferencesKey("timer_mode")
            private val SHOW_RING = booleanPreferencesKey("show_ring")
            private val HIDE_NUMBERS = booleanPreferencesKey("hide_numbers")
            private val FIXED_MINUTES = intPreferencesKey("fixed_minutes")
            private val COUNT_UP = booleanPreferencesKey("count_up")
            private val INTERVAL_MODE = booleanPreferencesKey("interval_mode")
            private val COUNT_UP_STARTED_AT_MS = longPreferencesKey("count_up_started_at_ms")
            private val INTERVAL_CYCLES_COMPLETED = intPreferencesKey("interval_cycles_completed")
            private val PHASE_STARTED_AT_MS = longPreferencesKey("phase_started_at_ms")
            private val MANUAL_BREAK = booleanPreferencesKey("manual_break")
        }
    }