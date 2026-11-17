package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.day.DayPlanningUseCase
import app.ebbplanner.domain.day.OverlapResolutionType
import app.ebbplanner.domain.day.OverlapSituation
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

data class OverlapUiState(
    val situation: OverlapSituation? = null,
    val selected: OverlapResolutionType = OverlapResolutionType.PUSH_LATER,
    val background: TodayPresentationState = TodayPresentationState(),
)

@HiltViewModel
class OverlapHelperViewModel
    @Inject
    constructor(
        private val dayPlanning: DayPlanningUseCase,
        private val taskRepository: TaskRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val clock: EbbClock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OverlapUiState())
        val uiState: StateFlow<OverlapUiState> = _uiState.asStateFlow()
        private val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

        init {
            viewModelScope.launch { refresh() }
        }

        private suspend fun refresh() {
            val bed = userPreferencesRepository.dayBedMinutes.first()
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
                    clock.nowMinutes(),
                )
            val situation = dayPlanning.analyzeOverlap(clock.today(), bed)
            val defaultOption =
                situation?.options?.firstOrNull()?.type ?: OverlapResolutionType.KEEP_BOTH
            _uiState.value =
                OverlapUiState(
                    situation = situation,
                    selected = defaultOption,
                    background = background,
                )
        }

        fun selectOption(type: OverlapResolutionType) {
            _uiState.value = _uiState.value.copy(selected = type)
        }

        fun apply(onComplete: () -> Unit) {
            viewModelScope.launch {
                val bed = userPreferencesRepository.dayBedMinutes.first()
                dayPlanning.applyOverlap(clock.today(), _uiState.value.selected, bed)
                onComplete()
            }
        }
    }