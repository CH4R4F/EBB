package app.ebbplanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.ui.components.weekStartDayLabel
import app.ebbplanner.ui.time.ClockFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val materialYou: Boolean = false,
    val remindersDefault: Boolean = true,
    val wakeLabel: String = "",
    val bedLabel: String = "",
    val weekStartDay: Int = UserPreferencesRepository.WEEK_START_USE_LOCALE,
    val weekStartLabel: String = "",
)

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val preferences: UserPreferencesRepository,
    ) : ViewModel() {
        val uiState: StateFlow<SettingsUiState> =
            combine(
                preferences.useMaterialYou,
                preferences.remindersDefault,
                preferences.dayWakeMinutes,
                preferences.dayBedMinutes,
                preferences.weekStartDay,
            ) { materialYou, reminders, wake, bed, weekStart ->
                SettingsUiState(
                    materialYou = materialYou,
                    remindersDefault = reminders,
                    wakeLabel = ClockFormat.minutesToClock(wake),
                    bedLabel = ClockFormat.minutesToClock(bed),
                    weekStartDay = weekStart,
                    weekStartLabel = weekStartDayLabel(weekStart),
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

        fun setMaterialYou(enabled: Boolean) {
            viewModelScope.launch { preferences.setUseMaterialYou(enabled) }
        }

        fun setRemindersDefault(enabled: Boolean) {
            viewModelScope.launch { preferences.setRemindersDefault(enabled) }
        }

        fun setWeekStartDay(day: Int) {
            viewModelScope.launch { preferences.setWeekStartDay(day) }
        }
    }
