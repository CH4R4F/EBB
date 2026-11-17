@file:Suppress("NewLineAtEndOfFile", "MaxLineLength")

package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.ui.focus.FocusPresentationState
import app.ebbplanner.ui.focus.FocusVisualMode
import app.ebbplanner.ui.time.ClockFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FocusRunViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) : ViewModel() {
        val state: StateFlow<FocusPresentationState> =
            taskRepository.observeTasksForDate(LocalDate.now())
                .map { tasks ->
                    val now = java.time.LocalTime.now().hour * 60 + java.time.LocalTime.now().minute
                    val scheduled =
                        tasks.filter {
                            it.placement == TaskEntity.PLACEMENT_SCHEDULED &&
                                it.startLocalTime != null &&
                                it.status != TaskEntity.STATUS_DONE
                        }
                    val current =
                        scheduled.firstOrNull { task ->
                            val start = ClockFormat.parseMinutes(task.startLocalTime ?: return@firstOrNull false) ?: return@firstOrNull false
                            start <= now && start + task.durationMin > now
                        }
                    val next =
                        scheduled
                            .mapNotNull { task ->
                                val start = task.startLocalTime?.let { ClockFormat.parseMinutes(it) } ?: return@mapNotNull null
                                task to start
                            }
                            .filter { (_, start) -> start >= now }
                            .minByOrNull { (_, start) -> start }
                            ?.first
                    val chosen = current ?: next
                    val subtitle =
                        chosen?.startLocalTime?.let { start ->
                            val startMin = ClockFormat.parseMinutes(start) ?: 0
                            val end = startMin + chosen.durationMin
                            "${ClockFormat.minutesToClock(startMin)}–${ClockFormat.minutesToClock(end)}"
                        } ?: ""
                    FocusPresentationState(
                        mode = FocusVisualMode.Setup,
                        title = chosen?.title ?: "No task lined up",
                        subtitle = subtitle.ifBlank { chosen?.let { "${it.durationMin} min" } ?: "" },
                    )
                }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    FocusPresentationState(mode = FocusVisualMode.Setup),
                )
    }