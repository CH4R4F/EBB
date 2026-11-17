package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.day.DayPlanningUseCase
import app.ebbplanner.domain.time.EbbClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlanDayTaskRow(
    val id: String,
    val title: String,
    val estimate: String,
    val selected: Boolean = false,
)

data class PlanDayUiState(
    val tasks: List<PlanDayTaskRow> = emptyList(),
    val hasCandidates: Boolean = false,
    val unplacedTitles: List<String> = emptyList(),
)

@HiltViewModel
class PlanDayViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val dayPlanning: DayPlanningUseCase,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val clock: EbbClock,
    ) : ViewModel() {
        private val selectedIds = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())
        private val selectionTouched = kotlinx.coroutines.flow.MutableStateFlow(false)
        private val unplacedTitles = kotlinx.coroutines.flow.MutableStateFlow<List<String>>(emptyList())

        val uiState: StateFlow<PlanDayUiState> =
            combine(
                taskRepository.observeInboxTasks(),
                taskRepository.observeTasksForDate(clock.today()),
                selectedIds,
                selectionTouched,
                unplacedTitles,
            ) { inbox, today, picked, touched, unplaced ->
                val anytime =
                    today.filter {
                        it.placement == TaskEntity.PLACEMENT_ANYTIME &&
                            it.status != TaskEntity.STATUS_DONE
                    }
                val distinct = (inbox + anytime).distinctBy { it.id }
                val rows =
                    distinct.map { task ->
                        PlanDayTaskRow(
                            id = task.id,
                            title = task.title,
                            estimate = "${task.durationMin} min",
                            selected = touched && picked.contains(task.id),
                        )
                    }
                PlanDayUiState(
                    tasks = rows,
                    hasCandidates = rows.isNotEmpty(),
                    unplacedTitles = unplaced,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanDayUiState())

        fun toggleTask(id: String) {
            selectionTouched.value = true
            selectedIds.value =
                selectedIds.value.toMutableSet().apply {
                    if (contains(id)) remove(id) else add(id)
                }
        }

        fun applyPlan(onComplete: () -> Unit) {
            viewModelScope.launch {
                val wake = userPreferencesRepository.dayWakeMinutes.first()
                val bed = userPreferencesRepository.dayBedMinutes.first()
                val now = clock.nowMinutes()
                val ids =
                    if (selectionTouched.value) {
                        uiState.value.tasks.filter { it.selected }.map { it.id }
                    } else {
                        emptyList()
                    }
                val result = dayPlanning.scheduleTasksOnDay(clock.today(), ids, wake, bed, now)
                val unplaced =
                    result.unplacedTaskIds.mapNotNull { id ->
                        taskRepository.getTask(id)?.title
                    }
                unplacedTitles.value = unplaced
                if (unplaced.isEmpty()) {
                    onComplete()
                }
            }
        }

        fun clearUnplaced() {
            unplacedTitles.value = emptyList()
        }
    }
