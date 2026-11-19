@file:Suppress("NewLineAtEndOfFile", "MaxLineLength")

package app.ebbplanner.ui.week

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.R
import app.ebbplanner.data.calendar.CalendarEventsReader
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.capture.QuickCaptureDraft
import app.ebbplanner.domain.capture.QuickCaptureResolved
import app.ebbplanner.domain.capture.resolveQuickCapture
import app.ebbplanner.domain.routines.RoutineInstanceGenerator
import app.ebbplanner.domain.time.WeekAnchor
import app.ebbplanner.ui.today.QuickCaptureSaveTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeekViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val taskRepository: TaskRepository,
        private val calendarEventsReader: CalendarEventsReader,
        private val routineInstanceGenerator: RoutineInstanceGenerator,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        init {
            viewModelScope.launch {
                routineInstanceGenerator.ensureRollingWindow()
            }
        }

        private val weekStartDay = MutableStateFlow(UserPreferencesRepository.WEEK_START_USE_LOCALE)
        private val weekPageOffset = MutableStateFlow(0)
        private val weekStart = MutableStateFlow(WeekAnchor.weekStartForPage(LocalDate.now(), weekStartDay.value, 0))
        private val loading = MutableStateFlow(true)
        private val captureDate = MutableStateFlow(LocalDate.now())

        private val rangeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())

        init {
            viewModelScope.launch {
                userPreferencesRepository.weekStartDay.collect { stored ->
                    weekStartDay.value = stored
                    applyWeekAnchor()
                }
            }
        }

        private fun applyWeekAnchor() {
            weekStart.value =
                WeekAnchor.weekStartForPage(
                    LocalDate.now(),
                    weekStartDay.value,
                    weekPageOffset.value,
                )
        }

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val presentation: StateFlow<WeekPresentationState> =
            weekStart
                .flatMapLatest { start ->
                    val end = start.plusDays(6)
                    combine(
                        taskRepository.observeTasksBetween(start, end).onStart { loading.value = true },
                        weekStart,
                        captureDate,
                    ) { tasks, anchor, selected ->
                        loading.value = false
                        val today = LocalDate.now()
                        val rangeLabel = formatWeekRange(anchor, end)
                        WeekPresentationState(
                            rangeLabel = rangeLabel,
                            selectedDate = selected,
                            days =
                                (0..6).map { offset ->
                                    val day = anchor.plusDays(offset.toLong())
                                    val dayTasks =
                                        tasks.filter {
                                            it.date == day.toString() &&
                                                it.placement != TaskEntity.PLACEMENT_INBOX &&
                                                it.status != TaskEntity.STATUS_DONE
                                        }
                                    val eventCount = calendarEventsReader.eventsForDay(day).size
                                    val taskCount = dayTasks.size
                                    val summary = formatDaySummary(taskCount, eventCount)
                                    val plannedMinutes = dayTasks.sumOf { it.durationMin }
                                    val load =
                                        when {
                                            taskCount == 0 -> "" to 0f
                                            plannedMinutes >= 360 -> {
                                                context.getString(R.string.week_load_full) to 1f
                                            }
                                            plannedMinutes >= 180 -> {
                                                context.getString(R.string.week_load_balanced) to (plannedMinutes / 360f).coerceIn(0.2f, 1f)
                                            }
                                            else -> {
                                                context.getString(R.string.week_load_light) to (plannedMinutes / 180f).coerceIn(0.15f, 0.6f)
                                            }
                                        }
                                    WeekDayRow(
                                        date = day,
                                        weekday = day.format(weekdayFormatter),
                                        dayNumber = day.dayOfMonth.toString(),
                                        summary = summary,
                                        loadLabel = load.first,
                                        loadProgress = load.second,
                                        loadFull = plannedMinutes >= 360,
                                        eventCount = eventCount,
                                        today = day == today,
                                        open = taskCount == 0 && eventCount == 0,
                                        selected = day == selected,
                                    )
                                },
                            mode =
                                if (tasks.isEmpty()) {
                                    WeekVisualMode.Empty
                                } else {
                                    WeekVisualMode.Portrait
                                },
                        )
                    }
                }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    WeekPresentationState(mode = WeekVisualMode.Loading),
                )

        fun previousWeek() {
            weekPageOffset.update { it - 1 }
            applyWeekAnchor()
        }

        fun nextWeek() {
            weekPageOffset.update { it + 1 }
            applyWeekAnchor()
        }

        fun jumpToThisWeek() {
            weekPageOffset.value = 0
            applyWeekAnchor()
            captureDate.value = LocalDate.now()
        }

        fun selectDay(date: LocalDate) {
            captureDate.value = date
        }

        fun captureTask(
            draft: QuickCaptureDraft,
            onSaved: (TaskEntity, QuickCaptureResolved) -> Unit = { _, _ -> },
        ) {
            if (draft.raw.isBlank()) return
            viewModelScope.launch {
                val resolved = resolveQuickCapture(draft, captureDate.value, QuickCaptureSaveTarget.Week)
                if (resolved.title.isBlank()) return@launch
                val task =
                    taskRepository.addQuickCapture(
                        title = resolved.title,
                        date = resolved.date,
                        placement = resolved.placement,
                        startLocalTime = resolved.startLocalTime,
                        durationMin = resolved.durationMin,
                    )
                onSaved(task, resolved)
            }
        }

        private fun formatDaySummary(
            taskCount: Int,
            eventCount: Int,
        ): String {
            if (taskCount == 0 && eventCount == 0) {
                return context.getString(R.string.week_day_open)
            }
            val tasksPart =
                context.resources.getQuantityString(
                    R.plurals.week_task_count,
                    taskCount,
                    taskCount,
                )
            val eventsPart =
                context.resources.getQuantityString(
                    R.plurals.week_event_count,
                    eventCount,
                    eventCount,
                )
            return when {
                taskCount == 0 -> eventsPart
                eventCount == 0 -> tasksPart
                else -> context.getString(R.string.week_day_summary, tasksPart, eventsPart)
            }
        }

        private fun formatWeekRange(
            start: LocalDate,
            end: LocalDate,
        ): String = formatWeekRangeLabel(start, end)
    }
