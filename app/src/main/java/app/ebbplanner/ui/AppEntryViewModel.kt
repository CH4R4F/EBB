package app.ebbplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.domain.routines.RoutineOccurrenceRepairRunner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppEntryViewModel
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val routineOccurrenceRepairRunner: RoutineOccurrenceRepairRunner,
    ) : ViewModel() {
        init {
            viewModelScope.launch {
                routineOccurrenceRepairRunner.repairThenGenerateIfNeeded()
            }
        }

        val onboardingComplete: StateFlow<Boolean> =
            userPreferencesRepository.onboardingComplete
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

        fun completeOnboardingFromRestore() {
            viewModelScope.launch {
                userPreferencesRepository.setOnboardingComplete(true)
                userPreferencesRepository.setSeedSampleTasks(false)
            }
        }
    }
