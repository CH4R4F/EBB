package app.ebbplanner.ui.daytools

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.focus.FocusPhase
import app.ebbplanner.domain.focus.FocusTickDelayer
import app.ebbplanner.domain.focus.FocusTimerController
import app.ebbplanner.domain.time.EbbClock
import app.ebbplanner.reminders.FocusNotifier
import app.ebbplanner.ui.focus.FocusPresentationState
import app.ebbplanner.ui.focus.FocusVisualMode
import app.ebbplanner.ui.time.ClockFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FocusRunViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val taskRepository: TaskRepository,
        private val clock: EbbClock,
        private val focusNotifier: FocusNotifier,
        private val tickDelayer: FocusTickDelayer,
    ) : ViewModel() {
        private val taskId: String = savedStateHandle.get<String>("taskId").orEmpty()
        private val timer = FocusTimerController()
        private var tickJob: Job? = null

        private val _state = MutableStateFlow(FocusPresentationState(mode = FocusVisualMode.Setup))
        val state: StateFlow<FocusPresentationState> = _state.asStateFlow()

        init {
            viewModelScope.launch {
                refreshTaskHeader()
            }
        }

        private suspend fun refreshTaskHeader() {
            val task = taskRepository.getTask(taskId)
            if (task == null) {
                _state.value =
                    FocusPresentationState(
                        mode = FocusVisualMode.Setup,
                        title = "Task not found",
                        taskId = taskId,
                    )
                return
            }
            val subtitle =
                task.startLocalTime?.let { start ->
                    val startMin = ClockFormat.parseMinutes(start) ?: 0
                    val end = startMin + task.durationMin
                    "${ClockFormat.minutesToClock(startMin)}–${ClockFormat.minutesToClock(end)}"
                } ?: "${task.durationMin} min"
            _state.value =
                _state.value.copy(
                    title = task.title,
                    subtitle = subtitle,
                    runningTitle = task.title,
                    taskId = taskId,
                )
            mergeTimerIntoState()
        }

        fun startFocus() {
            viewModelScope.launch {
                val task = taskRepository.getTask(taskId) ?: return@launch
                val nowMs = clock.millis()
                val start = task.startLocalTime?.let { ClockFormat.parseMinutes(it) }
                val endMinutes = (start ?: clock.nowMinutes()) + task.durationMin
                val blockEndMs =
                    nowMs + (endMinutes - clock.nowMinutes()).coerceAtLeast(1) * 60_000L
                timer.startUntilBlockEnd(nowMs, blockEndMs)
                startTicking()
                mergeTimerIntoState()
            }
        }

        fun justFiveMinutes() {
            val nowMs = clock.millis()
            timer.startFixedMinutes(nowMs, 5)
            startTicking()
            mergeTimerIntoState()
        }

        fun pause() {
            timer.pause(clock.millis())
            mergeTimerIntoState()
        }

        fun resume() {
            timer.resume(clock.millis())
            startTicking()
            mergeTimerIntoState()
        }

        fun addFive() {
            timer.addMinutes(clock.millis(), 5)
            mergeTimerIntoState()
        }

        fun markDone() {
            viewModelScope.launch {
                taskRepository.markTaskDone(taskId)
                timer.markDone()
                focusNotifier.cancel()
                tickJob?.cancel()
                mergeTimerIntoState()
            }
        }

        fun distracted() {
            timer.resetToSetup()
            tickJob?.cancel()
            focusNotifier.cancel()
            mergeTimerIntoState()
        }

        fun timesUpAddFive() {
            timer.addMinutes(clock.millis(), 5)
            startTicking()
            mergeTimerIntoState()
        }

        fun timesUpAddFifteen() {
            timer.addMinutes(clock.millis(), 15)
            startTicking()
            mergeTimerIntoState()
        }

        fun timesUpShiftDay() {
            viewModelScope.launch {
                taskRepository.shiftRestOfDayFromNotification(LocalDate.now(), taskId, 15)
                timer.resetToSetup()
                focusNotifier.cancel()
                mergeTimerIntoState()
            }
        }

        fun takeBreak() {
            timer.resetToSetup()
            focusNotifier.cancel()
            mergeTimerIntoState()
        }

        fun close() {
            tickJob?.cancel()
            focusNotifier.cancel()
        }

        private fun startTicking() {
            tickJob?.cancel()
            tickJob =
                viewModelScope.launch {
                    var done = false
                    while (!done && isActive && timer.phase() == FocusPhase.Running) {
                        tickDelayer.delayMs(TICK_INTERVAL_MS)
                        if (timer.phase() == FocusPhase.Running) {
                            val timesUp = timer.tick(clock.millis())
                            mergeTimerIntoState()
                            if (timesUp) {
                                onTimesUp()
                                done = true
                            }
                        }
                    }
                }
        }

        companion object {
            private const val TICK_INTERVAL_MS = 1_000L
        }

        private fun onTimesUp() {
            val title = _state.value.runningTitle.ifBlank { _state.value.title }
            focusNotifier.showTimesUp(taskId, title)
        }

        private fun mergeTimerIntoState() {
            val snap = timer.snapshot(clock.millis())
            val mode =
                when (snap.phase) {
                    FocusPhase.Setup -> FocusVisualMode.Setup
                    FocusPhase.Running -> FocusVisualMode.RunningRing
                    FocusPhase.Paused -> FocusVisualMode.RunningRing
                    FocusPhase.TimesUp -> FocusVisualMode.TimesUp
                    FocusPhase.Done -> FocusVisualMode.Setup
                }
            val progress = timer.progress(clock.millis())
            _state.value =
                _state.value.copy(
                    mode = mode,
                    timerLabel = snap.timerLabel,
                    paused = snap.paused,
                    ringProgress = progress,
                    showRing = true,
                )
        }
    }
