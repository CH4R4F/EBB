package app.ebbplanner.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TodayUiState(
    val date: LocalDate = LocalDate.now(),
    val dateLabel: String = "",
    val wakeMinutes: Int = UserPreferencesRepository.DEFAULT_WAKE_MINUTES,
    val bedMinutes: Int = UserPreferencesRepository.DEFAULT_BED_MINUTES,
    val scheduledTasks: List<TaskEntity> = emptyList(),
    val anytimeTasks: List<TaskEntity> = emptyList(),
    val minutesPlanned: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class TodayViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        private val selectedDate = MutableStateFlow(LocalDate.now())
        private val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val uiState: StateFlow<TodayUiState> =
            combine(
                selectedDate,
                selectedDate.flatMapLatest { taskRepository.observeTasksForDate(it) },
                userPreferencesRepository.dayWakeMinutes,
                userPreferencesRepository.dayBedMinutes,
            ) { date, tasks, wake, bed ->
                val scheduled =
                    tasks.filter {
                        it.placement == TaskEntity.PLACEMENT_SCHEDULED && it.startLocalTime != null
                    }
                val anytime = tasks.filter { it.placement == TaskEntity.PLACEMENT_ANYTIME }
                TodayUiState(
                    date = date,
                    dateLabel = date.format(dateFormatter),
                    wakeMinutes = wake,
                    bedMinutes = bed,
                    scheduledTasks = scheduled,
                    anytimeTasks = anytime,
                    minutesPlanned = scheduled.sumOf { it.durationMin },
                    isLoading = false,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

        init {
            viewModelScope.launch {
                val seedEnabled = userPreferencesRepository.seedSampleTasks.first()
                if (seedEnabled) {
                    taskRepository.ensureSampleDataForTodayIfEmpty(LocalDate.now())
                }
            }
        }

        fun refreshForDate(date: LocalDate) {
            selectedDate.value = date
        }

        fun captureTask(title: String) {
            if (title.isBlank()) return
            viewModelScope.launch {
                taskRepository.addQuickCapture(
                    title = title,
                    date = selectedDate.value,
                    placement = TaskEntity.PLACEMENT_ANYTIME,
                )
            }
        }
    }
