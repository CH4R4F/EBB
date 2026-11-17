package app.ebbplanner.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class CelebrationStyle {
    Calm,
    Lively,
    Off,
}

data class OnboardingUiState(
    val wakeMinutes: Int = UserPreferencesRepository.DEFAULT_WAKE_MINUTES,
    val bedMinutes: Int = UserPreferencesRepository.DEFAULT_BED_MINUTES,
    val breatherMinutes: Int = 5,
    val celebrationStyle: CelebrationStyle = CelebrationStyle.Calm,
    val addSampleTasks: Boolean = false,
    val remindersEnabled: Boolean = true,
    val linkedCalendarCount: Int = 0,
)

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val taskRepository: TaskRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        fun setWakeMinutes(minutes: Int) {
            _uiState.update { it.copy(wakeMinutes = minutes.coerceIn(0, 24 * 60 - 1)) }
        }

        fun setBedMinutes(minutes: Int) {
            _uiState.update { it.copy(bedMinutes = minutes.coerceIn(0, 24 * 60 - 1)) }
        }

        fun setBreatherMinutes(minutes: Int) {
            _uiState.update { it.copy(breatherMinutes = minutes) }
        }

        fun setCelebrationStyle(style: CelebrationStyle) {
            _uiState.update { it.copy(celebrationStyle = style) }
        }

        fun setAddSampleTasks(enabled: Boolean) {
            _uiState.update { it.copy(addSampleTasks = enabled) }
        }

        fun setRemindersEnabled(enabled: Boolean) {
            _uiState.update { it.copy(remindersEnabled = enabled) }
        }

        fun setLinkedCalendarCount(count: Int) {
            _uiState.update { it.copy(linkedCalendarCount = count.coerceAtLeast(0)) }
        }

        fun completeOnboarding(onComplete: () -> Unit) {
            viewModelScope.launch {
                persistOnboardingChoices()
                onComplete()
            }
        }

        suspend fun persistOnboardingChoices() {
            val state = _uiState.value
            userPreferencesRepository.setDayWakeMinutes(state.wakeMinutes)
            userPreferencesRepository.setDayBedMinutes(state.bedMinutes)
            userPreferencesRepository.setBreatherMinutes(state.breatherMinutes)
            userPreferencesRepository.setCelebrationStyle(state.celebrationStyle.name)
            userPreferencesRepository.setSeedSampleTasks(state.addSampleTasks)
            userPreferencesRepository.setOnboardingComplete(true)
            if (state.addSampleTasks) {
                taskRepository.seedSampleTasksForToday(LocalDate.now())
            }
        }
    }
