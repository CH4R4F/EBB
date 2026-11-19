package app.ebbplanner.ui.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.calendar.CalendarEventsReader
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.capture.QuickCaptureDraft
import app.ebbplanner.domain.capture.QuickCaptureResolved
import app.ebbplanner.domain.capture.resolveQuickCapture
import app.ebbplanner.ui.time.ClockFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val tomorrowScheduledTasks: List<TaskEntity> = emptyList(),
    val tomorrowEvents: List<TodayCalendarEventUi> = emptyList(),
    val minutesPlanned: Int = 0,
    val isLoading: Boolean = true,
    val is24Hour: Boolean = true,
)

@HiltViewModel
class TodayViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val taskRepository: TaskRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val calendarEventsReader: CalendarEventsReader,
    ) : ViewModel() {
        private val selectedDate = MutableStateFlow(LocalDate.now())
        private val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val uiState: StateFlow<TodayUiState> =
            combine(
                selectedDate.flatMapLatest { date ->
                    combine(
                        taskRepository.observeTasksForDate(date),
                        taskRepository.observeTasksForDate(date.plusDays(1)),
                        flowOf(
                            calendarEventsReader.eventsForDay(date) to
                                calendarEventsReader.eventsForDay(date.plusDays(1)),
                        ),
                    ) { todayTasks, tomorrowTasks, events ->
                        Triple(todayTasks, tomorrowTasks, events)
                    }
                },
                selectedDate,
                userPreferencesRepository.dayWakeMinutes,
                userPreferencesRepository.dayBedMinutes,
            ) { dayData, date, wake, bed ->
                val (tasks, tomorrowTasks, eventsPair) = dayData
                val scheduled =
                    tasks.filter {
                        it.placement == TaskEntity.PLACEMENT_SCHEDULED && it.startLocalTime != null
                    }
                val anytime = tasks.filter { it.placement == TaskEntity.PLACEMENT_ANYTIME }
                val calendarEvents = eventsPair.first.map { snapshot -> snapshot.toTodayUi() }
                val tomorrowScheduled =
                    tomorrowTasks.filter {
                        it.placement == TaskEntity.PLACEMENT_SCHEDULED && it.startLocalTime != null
                    }
                val tomorrowEvents = eventsPair.second.map { snapshot -> snapshot.toTodayUi() }
                TodayUiState(
                    date = date,
                    dateLabel = date.format(dateFormatter),
                    wakeMinutes = wake,
                    bedMinutes = bed,
                    scheduledTasks = scheduled,
                    anytimeTasks = anytime,
                    calendarEvents = calendarEvents,
                    tomorrowScheduledTasks = tomorrowScheduled,
                    tomorrowEvents = tomorrowEvents,
                    minutesPlanned = scheduled.sumOf { it.durationMin },
                    isLoading = false,
                    is24Hour = ClockFormat.isSystem24Hour(context),
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

        fun captureTask(
            draft: QuickCaptureDraft,
            onSaved: (TaskEntity, QuickCaptureResolved) -> Unit = { _, _ -> },
        ) {
            if (draft.raw.isBlank()) return
            viewModelScope.launch {
                val resolved = resolveQuickCapture(draft, selectedDate.value, QuickCaptureSaveTarget.Today)
                if (resolved.title.isBlank()) return@launch
                val remindersOn = userPreferencesRepository.remindersDefault.first()
                val reminderEnabled =
                    resolved.placement == TaskEntity.PLACEMENT_SCHEDULED && resolved.startLocalTime != null && remindersOn
                val task =
                    taskRepository.addQuickCapture(
                        title = resolved.title,
                        date = resolved.date,
                        placement = resolved.placement,
                        startLocalTime = resolved.startLocalTime,
                        durationMin = resolved.durationMin,
                        reminderEnabled = reminderEnabled,
                    )
                onSaved(task, resolved)
            }
        }
    }
