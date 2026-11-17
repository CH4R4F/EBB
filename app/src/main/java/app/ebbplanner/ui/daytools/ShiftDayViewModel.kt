@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.daytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.day.ShiftPreviewBlock
import app.ebbplanner.domain.time.EbbClock
import app.ebbplanner.ui.today.TodayPresentationMapper
import app.ebbplanner.ui.today.TodayPresentationState
import app.ebbplanner.ui.today.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ShiftDayUiState(
    val background: TodayPresentationState = TodayPresentationState(),
    val chipIndex: Int = 1,
    val shiftMinutes: Int = 15,
    val fromNow: Boolean = false,
    val previewBefore: List<ShiftPreviewBlock> = emptyList(),
    val previewAfter: List<ShiftPreviewBlock> = emptyList(),
)

@HiltViewModel
class ShiftDayViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val clock: EbbClock,
    ) : ViewModel() {
        private val chipIndex = MutableStateFlow(1)
        private val customMinutes = MutableStateFlow<Int?>(null)
        private val fromNow = MutableStateFlow(false)
        private val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

        val uiState: StateFlow<ShiftDayUiState> =
            combine(
                taskRepository.observeTasksForDate(clock.today()),
                chipIndex,
                customMinutes,
                fromNow,
            ) { tasks, chip, custom, startFromNow ->
                val now = clock.nowMinutes()
                val scheduled =
                    tasks.filter {
                        it.placement == TaskEntity.PLACEMENT_SCHEDULED && it.startLocalTime != null
                    }
                val anytime = tasks.filter { it.placement == TaskEntity.PLACEMENT_ANYTIME }
                val background =
                    TodayPresentationMapper.from(
                        TodayUiState(
                            date = clock.today(),
                            dateLabel = clock.today().format(dateFormatter),
                            scheduledTasks = scheduled,
                            anytimeTasks = anytime,
                            isLoading = false,
                        ),
                        now,
                    )
                val shift = resolveShiftMinutes(chip, custom)
                val movable =
                    scheduled.filter { task ->
                        val start =
                            app.ebbplanner.ui.time.ClockFormat.parseMinutes(task.startLocalTime ?: return@filter false)
                                ?: return@filter false
                        !task.pinned && start >= now
                    }
                val before = movable.map { task -> toPreviewBlock(task, 0) }
                val after = movable.map { task -> toPreviewBlock(task, shift) }
                ShiftDayUiState(
                    background = background,
                    chipIndex = chip,
                    shiftMinutes = shift,
                    fromNow = startFromNow,
                    previewBefore = before,
                    previewAfter = after,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShiftDayUiState())

        fun selectChip(index: Int) {
            chipIndex.value = index
            if (index == 4) {
                fromNow.value = true
            } else if (index != 5) {
                fromNow.value = false
            }
        }

        fun setCustomMinutes(minutes: Int) {
            customMinutes.value = minutes
            chipIndex.value = 5
        }

        fun applyShift(onComplete: () -> Unit) {
            viewModelScope.launch {
                val state = uiState.value
                val from =
                    if (state.fromNow) {
                        clock.nowMinutes()
                    } else {
                        clock.nowMinutes()
                    }
                taskRepository.shiftRestOfDay(clock.today(), state.shiftMinutes, from)
                onComplete()
            }
        }

        private fun resolveShiftMinutes(
            chip: Int,
            custom: Int?,
        ): Int =
            when (chip) {
                0 -> 5
                1 -> 15
                2 -> 30
                3 -> 60
                4 -> 0
                else -> custom ?: 15
            }

        private fun toPreviewBlock(
            task: TaskEntity,
            offset: Int,
        ): ShiftPreviewBlock {
            val start =
                app.ebbplanner.ui.time.ClockFormat.parseMinutes(task.startLocalTime ?: "09:00") ?: 0
            return ShiftPreviewBlock(
                id = task.id,
                title = task.title,
                colorKey = task.colorKey,
                startMinutes = start + offset,
                durationMin = task.durationMin,
                pinned = task.pinned,
                isEvent = false,
            )
        }
    }
