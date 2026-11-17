package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.domain.day.DayPlanningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OverlapHelperViewModel
    @Inject
    constructor(
        private val dayPlanning: DayPlanningUseCase,
    ) : ViewModel() {
        fun applyPushLater(onComplete: () -> Unit) {
            viewModelScope.launch {
                dayPlanning.resolveFirstOverlapPushLater(LocalDate.now())
                onComplete()
            }
        }
    }
