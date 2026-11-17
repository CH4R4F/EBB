package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.day.DayPlanningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PlanDayTaskRow(
    val id: String,
    val title: String,
    val estimate: String,
    val selected: Boolean = true,
)

data class PlanDayUiState(
    val tasks: List<PlanDayTaskRow> = emptyList(),
)

@HiltViewModel
class PlanDayViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val dayPlanning: DayPlanningUseCase,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        private val selected = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())

        val uiState: StateFlow<PlanDayUiState> =
            combine(
                taskRepository.observeInboxTasks(),
                taskRepository.observeTasksForDate(LocalDate.now()),
                selected,
            ) { inbox, today, picked ->
                val anytime =
                    today.filter {
                        it.placement == TaskEntity.PLACEMENT_ANYTIME &&
                            it.status != TaskEntity.STATUS_DONE
                    }
                val distinct = (inbox + anytime).distinctBy { it.id }
                val allIds = distinct.map { it.id }.toSet()
                val effectivePick = if (picked.isEmpty()) allIds else picked
                val rows =
                    distinct.map { task ->
                        PlanDayTaskRow(
                            id = task.id,
                            title = task.title,
                            estimate = "${task.durationMin} min",
                            selected = effectivePick.contains(task.id),
                        )
                    }
                PlanDayUiState(tasks = rows)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanDayUiState())

        fun toggleTask(id: String) {
            selected.value =
                selected.value.toMutableSet().apply {
                    if (contains(id)) remove(id) else add(id)
                }
        }

        fun applyPlan(onComplete: () -> Unit) {
            viewModelScope.launch {
                val wake = userPreferencesRepository.dayWakeMinutes.first()
                val ids =
                    uiState.value.tasks
                        .filter { it.selected }
                        .map { it.id }
                dayPlanning.scheduleTasksOnDay(LocalDate.now(), ids, wake)
                onComplete()
            }
        }
    }
