package app.ebbplanner.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.calendar.CalendarEventsReader
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.capture.QuickCaptureParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
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
    val calendarEvents: List<TodayCalendarEventUi> = emptyList(),
    val minutesPlanned: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class TodayViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val calendarEventsReader: CalendarEventsReader,
    ) : ViewModel() {
        private val selectedDate = MutableStateFlow(LocalDate.now())
        private val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val uiState: StateFlow<TodayUiState> =
            combine(
                selectedDate,
                selectedDate.flatMapLatest { taskRepository.observeTasksForDate(it) },
                selectedDate.flatMapLatest { date -> flowOf(calendarEventsReader.eventsForDay(date)) },
                userPreferencesRepository.dayWakeMinutes,
                userPreferencesRepository.dayBedMinutes,
            ) { date, tasks, calendarSnapshots, wake, bed ->
                val scheduled =
                    tasks.filter {
                        it.placement == TaskEntity.PLACEMENT_SCHEDULED && it.startLocalTime != null
                    }
                val anytime = tasks.filter { it.placement == TaskEntity.PLACEMENT_ANYTIME }
                val calendarEvents =
                    calendarSnapshots.map { snapshot -> snapshot.toTodayUi() }
                TodayUiState(
                    date = date,
                    dateLabel = date.format(dateFormatter),
                    wakeMinutes = wake,
                    bedMinutes = bed,
                    scheduledTasks = scheduled,
                    anytimeTasks = anytime,
                    calendarEvents = calendarEvents,
                    minutesPlanned = scheduled.sumOf { it.durationMin },
                    isLoading = false,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

        private val minuteTick =
            flow {
                while (true) {
                    emit(Unit)
                    delay(60_000)
                }
            }

        val presentationState: StateFlow<TodayPresentationState> =
            combine(
                uiState,
                minuteTick.onStart { emit(Unit) },
            ) { state, _ ->
                TodayPresentationMapper.from(state, currentNowMinutes())
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayPresentationState())

        fun markTaskDone(taskId: String) {
            viewModelScope.launch {
                taskRepository.markTaskDone(taskId)
            }
        }

        private fun currentNowMinutes(): Int {
            val now = java.time.LocalTime.now()
            return now.hour * 60 + now.minute
        }

        fun refreshForDate(date: LocalDate) {
            selectedDate.value = date
        }

        fun goToPreviousDay() {
            selectedDate.value = selectedDate.value.minusDays(1)
        }

        fun goToNextDay() {
            selectedDate.value = selectedDate.value.minusDays(-1)
        }

        fun captureTask(raw: String) {
            if (raw.isBlank()) return
            viewModelScope.launch {
                val parsed = QuickCaptureParser.parse(raw, selectedDate.value)
                val date = parsed.targetDate ?: selectedDate.value
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val start =
                    parsed.startTime?.format(timeFormatter)
                val placement =
                    if (start != null) {
                        TaskEntity.PLACEMENT_SCHEDULED
                    } else {
                        TaskEntity.PLACEMENT_ANYTIME
                    }
                val remindersOn = userPreferencesRepository.remindersDefault.first()
                val reminderEnabled =
                    if (placement == TaskEntity.PLACEMENT_SCHEDULED && start != null) {
                        remindersOn
                    } else {
                        false
                    }
                taskRepository.addQuickCapture(
                    title = parsed.title,
                    date = date,
                    placement = placement,
                    startLocalTime = start,
                    durationMin = parsed.durationMin ?: TaskEntity.DEFAULT_DURATION_MIN,
                    reminderEnabled = reminderEnabled,
                )
            }
        }
    }
