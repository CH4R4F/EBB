package app.ebbplanner.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import javax.inject.Inject

@HiltViewModel
class WeekViewModel
    @Inject
    constructor(
        taskRepository: TaskRepository,
    ) : ViewModel() {
        private val weekStart = MutableStateFlow(mondayOf(LocalDate.now()))

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val presentation: StateFlow<WeekPresentationState> =
            weekStart
                .flatMapLatest { start ->
                    val end = start.plusDays(6)
                    combine(
                        taskRepository.observeTasksBetween(start, end),
                        weekStart,
                    ) { tasks, anchor ->
                        val today = LocalDate.now()
                        WeekPresentationState(
                            rangeLabel =
                                "${anchor.dayOfMonth}–${end.dayOfMonth} " +
                                    anchor.month.name.take(3).lowercase(),
                            days =
                                (0..6).map { offset ->
                                    val day = anchor.plusDays(offset.toLong())
                                    val dayTasks = tasks.filter { it.date == day.toString() }
                                    WeekDayRow(
                                        weekday = day.format(DateTimeFormatter.ofPattern("EEE")),
                                        dayNumber = day.dayOfMonth.toString(),
                                        summary = dayTasks.joinToString(" · ") { it.title }.ifBlank { "Open" },
                                        loadLabel = if (dayTasks.isEmpty()) "Light" else "Balanced",
                                        loadProgress = (dayTasks.size / 6f).coerceIn(0f, 1f),
                                        eventCount = dayTasks.size,
                                        today = day == today,
                                        open = dayTasks.isEmpty(),
                                    )
                                },
                        )
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), weekPortrait())

        fun previousWeek() {
            weekStart.update { it.minusWeeks(1) }
        }

        fun nextWeek() {
            weekStart.update { it.plusWeeks(1) }
        }

        private fun mondayOf(date: LocalDate): LocalDate {
            val fields = WeekFields.ISO
            return date.with(fields.dayOfWeek(), 1)
        }
    }
