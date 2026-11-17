package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.domain.day.DayPlanningUseCase
import app.ebbplanner.domain.day.MakeItFitPlan
import app.ebbplanner.domain.time.EbbClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MakeItFitUiState(
    val plan: MakeItFitPlan? = null,
    val selectedMoveIds: Set<String> = emptySet(),
)

@HiltViewModel
class MakeItFitViewModel
    @Inject
    constructor(
        private val dayPlanning: DayPlanningUseCase,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val clock: EbbClock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MakeItFitUiState())
        val uiState: StateFlow<MakeItFitUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch { refresh() }
        }

        private suspend fun refresh() {
            val bed = userPreferencesRepository.dayBedMinutes.first()
            val now = clock.nowMinutes()
            val plan = dayPlanning.buildMakeItFitPlan(clock.today(), now, bed)
            _uiState.value =
                MakeItFitUiState(
                    plan = plan,
                    selectedMoveIds = plan.moves.filter { it.selected }.map { it.taskId }.toSet(),
                )
        }

        fun toggleMove(taskId: String) {
            val cur = _uiState.value.selectedMoveIds.toMutableSet()
            if (cur.contains(taskId)) cur.remove(taskId) else cur.add(taskId)
            _uiState.value = _uiState.value.copy(selectedMoveIds = cur)
        }

        fun applyMoves(onComplete: () -> Unit) {
            viewModelScope.launch {
                val bed = userPreferencesRepository.dayBedMinutes.first()
                val now = clock.nowMinutes()
                dayPlanning.applyMakeItFit(clock.today(), now, bed, _uiState.value.selectedMoveIds)
                onComplete()
            }
        }
    }
