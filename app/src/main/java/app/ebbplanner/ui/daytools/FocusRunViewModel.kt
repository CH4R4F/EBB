package app.ebbplanner.ui.daytools

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.FocusSessionStore
import app.ebbplanner.data.preferences.PersistedFocusSession
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.focus.FocusPhase
import app.ebbplanner.domain.focus.FocusTickDelayer
import app.ebbplanner.domain.focus.FocusTimerController
import app.ebbplanner.domain.time.EbbClock
import app.ebbplanner.reminders.FocusAlarmScheduler
import app.ebbplanner.reminders.FocusNotifier
import app.ebbplanner.ui.focus.FocusPresentationState
import app.ebbplanner.ui.focus.FocusStepRow
import app.ebbplanner.ui.focus.FocusVisualMode
import app.ebbplanner.ui.time.ClockFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
        private val focusSessionStore: FocusSessionStore,
        private val focusAlarmScheduler: FocusAlarmScheduler,
    ) : ViewModel() {
        private val taskId: String = savedStateHandle.get<String>("taskId").orEmpty()
        private val timer = FocusTimerController()
        private var tickJob: Job? = null

        private var timerModeIndex = 0
        private var showRing = true
        private var hideNumbers = false
        private var fixedMinutes = 25
        private var cachedBlockDuration = TaskEntity.DEFAULT_DURATION_MIN
        private var cachedStartLocalTime: String? = null

        private val _state = MutableStateFlow(FocusPresentationState(mode = FocusVisualMode.Setup, taskId = taskId))
        val state: StateFlow<FocusPresentationState> = _state.asStateFlow()

        init {
            viewModelScope.launch {
                restoreOrLoad()
            }
        }

        private suspend fun restoreOrLoad() {
            val saved = focusSessionStore.session.first()
            if (saved != null && saved.taskId == taskId) {
                timerModeIndex = saved.timerMode
                showRing = saved.showRing
                hideNumbers = saved.hideNumbers
                fixedMinutes = saved.fixedMinutes
                timer.restore(
                    FocusPhase.valueOf(saved.phase),
                    saved.endAtMs,
                    saved.totalMs,
                    saved.pausedRemainingMs,
                    clock.millis(),
                )
                if (timer.phase() == FocusPhase.Running || timer.phase() == FocusPhase.Break) {
                    startTicking()
                }
            }
            refreshTaskHeader()
        }

        fun setTimerMode(index: Int) {
            timerModeIndex = index
            refreshSetupCopy(blockDuration = cachedBlockDuration, startLocalTime = cachedStartLocalTime)
        }

        fun setShowRing(ring: Boolean) {
            showRing = ring
            mergeTimerIntoState()
        }

        fun setHideNumbers(hide: Boolean) {
            hideNumbers = hide
            mergeTimerIntoState()
        }

        fun setFixedMinutes(minutes: Int) {
            fixedMinutes = minutes.coerceIn(5, 120)
            refreshSetupCopy(blockDuration = cachedBlockDuration, startLocalTime = cachedStartLocalTime)
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
            val subtasks = taskRepository.listSubtasks(taskId)
            val steps =
                subtasks.map {
                    FocusStepRow(title = it.title, done = it.done)
                }
            val doneSteps = steps.count { it.done }
            val subtitle =
                buildString {
                    task.startLocalTime?.let { start ->
                        val startMin = ClockFormat.parseMinutes(start) ?: 0
                        val end = startMin + task.durationMin
                        append("${ClockFormat.minutesToClock(startMin)}–${ClockFormat.minutesToClock(end)}")
                    } ?: append("${task.durationMin} min")
                    if (steps.isNotEmpty()) append(" · steps $doneSteps/${steps.size}")
                }
            cachedBlockDuration = task.durationMin
            cachedStartLocalTime = task.startLocalTime
            refreshSetupCopy(blockDuration = task.durationMin, startLocalTime = task.startLocalTime)
            _state.value =
                _state.value.copy(
                    title = task.title,
                    subtitle = subtitle,
                    runningTitle = task.title,
                    taskId = taskId,
                    steps = steps,
                    showSteps = steps.isNotEmpty(),
                )
            mergeTimerIntoState()
        }

        private fun refreshSetupCopy(
            blockDuration: Int,
            startLocalTime: String? = null,
        ) {
            val blockSubtitle =
                if (startLocalTime != null) {
                    val startMin = ClockFormat.parseMinutes(startLocalTime) ?: clock.nowMinutes()
                    val endMin = startMin + blockDuration
                    "${blockDuration} min · ends ${ClockFormat.minutesToClock(endMin)}"
                } else {
                    "${blockDuration} min"
                }
            _state.value =
                _state.value.copy(
                    timerModeIndex = timerModeIndex,
                    showRing = showRing,
                    hideNumbers = hideNumbers,
                    fixedMinutes = fixedMinutes,
                    blockOptionSubtitle = blockSubtitle,
                    fixedOptionSubtitle = "$fixedMinutes min selected · tap start",
                )
        }

        fun startFocus() {
            viewModelScope.launch {
                val task = taskRepository.getTask(taskId) ?: return@launch
                val nowMs = clock.millis()
                when (timerModeIndex) {
                    1 -> timer.startFixedMinutes(nowMs, fixedMinutes)
                    2 -> timer.startFixedMinutes(nowMs, 12 * 60)
                    3 -> timer.startFixedMinutes(nowMs, 25)
                    else -> {
                        val start = task.startLocalTime?.let { ClockFormat.parseMinutes(it) }
                        val endMinutes = (start ?: clock.nowMinutes()) + task.durationMin
                        val blockEndMs =
                            nowMs + (endMinutes - clock.nowMinutes()).coerceAtLeast(1) * 60_000L
                        timer.startUntilBlockEnd(nowMs, blockEndMs)
                    }
                }
                scheduleAlarm()
                startTicking()
                mergeTimerIntoState()
                persistSession()
            }
        }

        fun justFiveMinutes() {
            val nowMs = clock.millis()
            timer.startFixedMinutes(nowMs, 5)
            scheduleAlarm()
            startTicking()
            mergeTimerIntoState()
            viewModelScope.launch { persistSession() }
        }

        fun pause() {
            timer.pause(clock.millis())
            focusAlarmScheduler.cancel()
            mergeTimerIntoState()
            viewModelScope.launch { persistSession() }
        }

        fun resume() {
            timer.resume(clock.millis())
            scheduleAlarm()
            startTicking()
            mergeTimerIntoState()
            viewModelScope.launch { persistSession() }
        }

        fun addFive() {
            timer.addMinutes(clock.millis(), 5)
            scheduleAlarm()
            mergeTimerIntoState()
            viewModelScope.launch { persistSession() }
        }

        fun markDone(onComplete: (String?) -> Unit = {}) {
            viewModelScope.launch {
                val today = clock.today()
                taskRepository.markTaskDone(taskId)
                val nextId = taskRepository.nextIncompleteTaskAfter(today, taskId)
                timer.markDone()
                focusNotifier.cancel()
                focusAlarmScheduler.cancel()
                focusSessionStore.clear()
                tickJob?.cancel()
                onComplete(nextId)
            }
        }

        fun distracted() {
            timer.resetToSetup()
            tickJob?.cancel()
            focusNotifier.cancel()
            focusAlarmScheduler.cancel()
            viewModelScope.launch { focusSessionStore.clear() }
            mergeTimerIntoState()
        }

        fun timesUpAddFive() {
            timer.addMinutes(clock.millis(), 5)
            scheduleAlarm()
            startTicking()
            mergeTimerIntoState()
            viewModelScope.launch { persistSession() }
        }

        fun timesUpAddFifteen() {
            timer.addMinutes(clock.millis(), 15)
            scheduleAlarm()
            startTicking()
            mergeTimerIntoState()
            viewModelScope.launch { persistSession() }
        }

        fun timesUpShiftDay() {
            viewModelScope.launch {
                taskRepository.shiftRestOfDayFromNotification(clock.today(), taskId, 15)
                timer.resetToSetup()
                focusNotifier.cancel()
                focusAlarmScheduler.cancel()
                focusSessionStore.clear()
                mergeTimerIntoState()
            }
        }

        fun takeBreak() {
            val nowMs = clock.millis()
            timer.startBreak(nowMs, 5)
            scheduleAlarm()
            startTicking()
            mergeTimerIntoState()
            viewModelScope.launch { persistSession() }
        }

        fun close() {
            tickJob?.cancel()
            viewModelScope.launch { persistSession() }
        }

        private fun scheduleAlarm() {
            val snap = timer.snapshot(clock.millis())
            if (snap.phase == FocusPhase.Running || snap.phase == FocusPhase.Break) {
                val endAt = clock.millis() + snap.remainingMs
                focusAlarmScheduler.scheduleTimesUp(taskId, _state.value.runningTitle, endAt)
            }
        }

        private fun startTicking() {
            tickJob?.cancel()
            tickJob =
                viewModelScope.launch {
                    var done = false
                    while (!done && isActive &&
                        (timer.phase() == FocusPhase.Running || timer.phase() == FocusPhase.Break)
                    ) {
                        tickDelayer.delayMs(TICK_INTERVAL_MS)
                        if (timer.phase() == FocusPhase.Running || timer.phase() == FocusPhase.Break) {
                            val wasBreak = timer.phase() == FocusPhase.Break
                            val ended = timer.tick(clock.millis())
                            mergeTimerIntoState()
                            persistSession()
                            if (ended) {
                                if (wasBreak) {
                                    onBreakEnded()
                                } else {
                                    onTimesUp()
                                }
                                done = true
                            }
                        }
                    }
                }
        }

        private fun onBreakEnded() {
            focusAlarmScheduler.cancel()
            _state.value = _state.value.copy(mode = FocusVisualMode.BreakEnded)
            viewModelScope.launch { focusSessionStore.clear() }
        }

        private fun onTimesUp() {
            val title = _state.value.runningTitle.ifBlank { _state.value.title }
            focusNotifier.showTimesUp(taskId, title)
            focusAlarmScheduler.cancel()
            viewModelScope.launch { persistSession() }
        }

        private suspend fun persistSession() {
            val snap = timer.snapshot(clock.millis())
            val phase = timer.phase()
            if (phase == FocusPhase.Done || phase == FocusPhase.Setup) {
                focusSessionStore.clear()
                return
            }
            val endAt =
                when (phase) {
                    FocusPhase.Paused -> clock.millis() + snap.remainingMs
                    FocusPhase.Running, FocusPhase.Break -> clock.millis() + snap.remainingMs
                    else -> 0L
                }
            focusSessionStore.save(
                PersistedFocusSession(
                    taskId = taskId,
                    phase = phase.name,
                    endAtMs = endAt,
                    totalMs = snap.totalMs,
                    pausedRemainingMs = if (phase == FocusPhase.Paused) snap.remainingMs else 0L,
                    timerMode = timerModeIndex,
                    showRing = showRing,
                    hideNumbers = hideNumbers,
                    fixedMinutes = fixedMinutes,
                ),
            )
        }

        private fun mergeTimerIntoState() {
            val snap = timer.snapshot(clock.millis())
            val mode =
                when (snap.phase) {
                    FocusPhase.Setup -> FocusVisualMode.Setup
                    FocusPhase.Running ->
                        if (showRing) FocusVisualMode.RunningRing else FocusVisualMode.RunningBar
                    FocusPhase.Paused ->
                        if (showRing) FocusVisualMode.RunningRing else FocusVisualMode.RunningBar
                    FocusPhase.Break ->
                        if (showRing) FocusVisualMode.RunningRing else FocusVisualMode.RunningBar
                    FocusPhase.TimesUp -> FocusVisualMode.TimesUp
                    FocusPhase.Done -> FocusVisualMode.Setup
                }
            val progress = timer.progress(clock.millis())
            val topBar =
                when (snap.phase) {
                    FocusPhase.Break -> "Break · 5 min"
                    FocusPhase.TimesUp ->
                        when (timerModeIndex) {
                            1 -> "Fixed · $fixedMinutes min"
                            2 -> "Count up"
                            3 -> "Intervals"
                            else -> "Until block ends"
                        }
                    else ->
                        when (timerModeIndex) {
                            1 -> "Fixed · $fixedMinutes min"
                            else -> "Focus"
                        }
                }
            val remainingSubtitle =
                if (snap.totalMs > 0) {
                    "left of ${formatDurationLabel(snap.totalMs)}"
                } else {
                    ""
                }
            _state.value =
                _state.value.copy(
                    mode = mode,
                    timerLabel = snap.timerLabel,
                    paused = snap.paused,
                    ringProgress = progress,
                    showRing = showRing,
                    hideNumbers = hideNumbers,
                    timerModeIndex = timerModeIndex,
                    topBarLabel = topBar,
                    runningRemainingSubtitle = remainingSubtitle,
                    timesUpDetail = topBar,
                )
        }

        private fun formatDurationLabel(totalMs: Long): String {
            val min = ((totalMs + 59_999) / 60_000).toInt()
            val h = min / 60
            val m = min % 60
            return if (h > 0) "$h h ${m} min" else "$m min"
        }

        companion object {
            private const val TICK_INTERVAL_MS = 1_000L
        }
    }