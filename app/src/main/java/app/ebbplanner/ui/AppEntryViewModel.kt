package app.ebbplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppEntryViewModel
    @Inject
    constructor(
        userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        val onboardingComplete: StateFlow<Boolean> =
            userPreferencesRepository.onboardingComplete
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    }
