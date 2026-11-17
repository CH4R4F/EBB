@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.ui.today.TodayPresentationMapper
import app.ebbplanner.ui.today.TodayPresentationState
import app.ebbplanner.ui.today.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ShiftDayViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) : ViewModel() {
        val shiftMinutes: Int = 15
        private val today = LocalDate.now()
        private val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

        val backgroundState: StateFlow<TodayPresentationState> =
            taskRepository.observeTasksForDate(today)
                .map { tasks ->
                    val scheduled =
                        tasks.filter {
                            it.placement == TaskEntity.PLACEMENT_SCHEDULED && it.startLocalTime != null
                        }
                    val anytime = tasks.filter { it.placement == TaskEntity.PLACEMENT_ANYTIME }
                    val now = LocalTime.now().hour * 60 + LocalTime.now().minute
                    val ui =
                        TodayUiState(
                            date = today,
                            dateLabel = today.format(dateFormatter),
                            scheduledTasks = scheduled,
                            anytimeTasks = anytime,
                            isLoading = false,
                        )
                    TodayPresentationMapper.from(ui, now)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayPresentationState())

        fun applyShift(onComplete: () -> Unit) {
            viewModelScope.launch {
                val from = LocalTime.now().hour * 60 + LocalTime.now().minute
                taskRepository.shiftRestOfDay(today, shiftMinutes, from)
                onComplete()
            }
        }
    }
