package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.domain.day.DayPlanningUseCase
import app.ebbplanner.domain.day.OverlapResolutionType
import app.ebbplanner.domain.day.OverlapSituation
import app.ebbplanner.domain.time.EbbClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OverlapUiState(
    val situation: OverlapSituation? = null,
    val selected: OverlapResolutionType = OverlapResolutionType.PUSH_LATER,
)

@HiltViewModel
class OverlapHelperViewModel
    @Inject
    constructor(
        private val dayPlanning: DayPlanningUseCase,
        private val clock: EbbClock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OverlapUiState())
        val uiState: StateFlow<OverlapUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch { refresh() }
        }

        private suspend fun refresh() {
            val situation = dayPlanning.analyzeOverlap(clock.today())
            _uiState.value =
                OverlapUiState(
                    situation = situation,
                    selected = situation?.options?.firstOrNull()?.type ?: OverlapResolutionType.PUSH_LATER,
                )
        }

        fun selectOption(type: OverlapResolutionType) {
            _uiState.value = _uiState.value.copy(selected = type)
        }

        fun apply(onComplete: () -> Unit) {
            viewModelScope.launch {
                dayPlanning.applyOverlap(clock.today(), _uiState.value.selected)
                onComplete()
            }
        }
    }
