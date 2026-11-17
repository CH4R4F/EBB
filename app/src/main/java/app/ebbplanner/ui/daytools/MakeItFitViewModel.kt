package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.domain.day.DayPlanningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MakeItFitViewModel
    @Inject
    constructor(
        private val dayPlanning: DayPlanningUseCase,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        fun applyMoves(onComplete: () -> Unit) {
            viewModelScope.launch {
                val bed = userPreferencesRepository.dayBedMinutes.first()
                val now = LocalTime.now().hour * 60 + LocalTime.now().minute
                val available = (bed - now).coerceAtLeast(0)
                dayPlanning.makeDayFit(LocalDate.now(), available)
                onComplete()
            }
        }
    }
