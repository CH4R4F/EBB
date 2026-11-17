@file:Suppress("NewLineAtEndOfFile", "MaxLineLength")

package app.ebbplanner.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeekViewModel
    @Inject
    constructor(
        taskRepository: TaskRepository,
    ) : ViewModel() {
        private val weekStart = MutableStateFlow(startOfWeek(LocalDate.now()))
        private val loading = MutableStateFlow(true)

        private val rangeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val presentation: StateFlow<WeekPresentationState> =
            weekStart
                .flatMapLatest { start ->
                    val end = start.plusDays(6)
                    combine(
                        taskRepository.observeTasksBetween(start, end).onStart { loading.value = true },
                        weekStart,
                    ) { tasks, anchor ->
                        loading.value = false
                        val today = LocalDate.now()
                        val rangeLabel = formatWeekRange(anchor, end)
                        WeekPresentationState(
                            rangeLabel = rangeLabel,
                            days =
                                (0..6).map { offset ->
                                    val day = anchor.plusDays(offset.toLong())
                                    val dayTasks =
                                        tasks.filter {
                                            it.date == day.toString() &&
                                                it.placement != TaskEntity.PLACEMENT_INBOX
                                        }
                                    val taskCount = dayTasks.size
                                    val summary =
                                        when (taskCount) {
                                            0 -> "Open"
                                            1 -> "1 task · 0 events"
                                            else -> "$taskCount tasks · 0 events"
                                        }
                                    WeekDayRow(
                                        weekday = day.format(weekdayFormatter),
                                        dayNumber = day.dayOfMonth.toString(),
                                        summary = summary,
                                        loadLabel = if (dayTasks.isEmpty()) "Light" else "Balanced",
                                        loadProgress = (dayTasks.size / 6f).coerceIn(0f, 1f),
                                        eventCount = 0,
                                        today = day == today,
                                        open = dayTasks.isEmpty(),
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
            weekStart.update { it.minusWeeks(1) }
        }

        fun nextWeek() {
            weekStart.update { it.plusWeeks(1) }
        }

        fun jumpToThisWeek() {
            weekStart.value = startOfWeek(LocalDate.now())
        }

        private fun startOfWeek(date: LocalDate): LocalDate {
            val fields = WeekFields.of(Locale.getDefault())
            return date.with(fields.dayOfWeek(), fields.firstDayOfWeek.value.toLong())
        }

        private fun formatWeekRange(
            start: LocalDate,
            end: LocalDate,
        ): String {
            val startLabel = start.format(rangeFormatter)
            val endLabel = end.format(rangeFormatter)
            return "$startLabel – $endLabel"
        }
    }