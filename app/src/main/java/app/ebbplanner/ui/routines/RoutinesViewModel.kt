package app.ebbplanner.ui.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RoutinesViewModel
    @Inject
    constructor(
        taskRepository: TaskRepository,
    ) : ViewModel() {
        val presentation: StateFlow<RoutinesPresentationState> =
            taskRepository
                .observeTasksForDate(LocalDate.now())
                .map { tasks ->
                    val cards =
                        tasks
                            .filter { it.placement == app.ebbplanner.data.local.entity.TaskEntity.PLACEMENT_SCHEDULED }
                            .map {
                                RoutineCard(
                                    title = it.title,
                                    schedule = it.startLocalTime ?: "Today",
                                    meta = "${it.durationMin} min",
                                    icon = it.iconKey ?: "task",
                                )
                            }
                    if (cards.isEmpty()) {
                        routinesEmpty()
                    } else {
                        routinesPopulated().copy(routines = cards)
                    }
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), routinesEmpty())
    }
