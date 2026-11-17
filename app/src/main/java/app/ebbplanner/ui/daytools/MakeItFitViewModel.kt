package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.day.DayPlanningUseCase
import app.ebbplanner.domain.day.MakeItFitPlan
import app.ebbplanner.domain.time.EbbClock
import app.ebbplanner.ui.today.TodayPresentationMapper
import app.ebbplanner.ui.today.TodayPresentationState
import app.ebbplanner.ui.today.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MakeItFitUiState(
    val plan: MakeItFitPlan? = null,
    val selectedMoveIds: Set<String> = emptySet(),
    val afterPlannedMinutes: Int = 0,
    val background: TodayPresentationState = TodayPresentationState(),
)

@HiltViewModel
class MakeItFitViewModel
    @Inject
    constructor(
        private val dayPlanning: DayPlanningUseCase,
        private val taskRepository: TaskRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val clock: EbbClock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MakeItFitUiState())
        val uiState: StateFlow<MakeItFitUiState> = _uiState.asStateFlow()
        private val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

        init {
            viewModelScope.launch { refresh() }
        }

        private suspend fun refresh() {
            val bed = userPreferencesRepository.dayBedMinutes.first()
            val now = clock.nowMinutes()
            val plan = dayPlanning.buildMakeItFitPlan(clock.today(), now, bed)
            val tasks = taskRepository.observeTasksForDate(clock.today()).first()
            val scheduled =
                tasks.filter {
                    it.placement == TaskEntity.PLACEMENT_SCHEDULED && it.startLocalTime != null
                }
            val anytime = tasks.filter { it.placement == TaskEntity.PLACEMENT_ANYTIME }
            val background =
                TodayPresentationMapper.from(
                    TodayUiState(
                        date = clock.today(),
                        dateLabel = clock.today().format(dateFormatter),
                        scheduledTasks = scheduled,
                        anytimeTasks = anytime,
                        isLoading = false,
                    ),
                    now,
                )
            val selected = plan.moves.filter { it.selected }.map { it.taskId }.toSet()
            _uiState.value =
                MakeItFitUiState(
                    plan = plan,
                    selectedMoveIds = selected,
                    afterPlannedMinutes = dayPlanning.afterPlannedMinutes(plan, selected),
                    background = background,
                )
        }

        fun toggleMove(taskId: String) {
            val cur = _uiState.value
            val ids = cur.selectedMoveIds.toMutableSet()
            if (ids.contains(taskId)) ids.remove(taskId) else ids.add(taskId)
            val after =
                cur.plan?.let { dayPlanning.afterPlannedMinutes(it, ids) } ?: 0
            _uiState.value = cur.copy(selectedMoveIds = ids, afterPlannedMinutes = after)
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