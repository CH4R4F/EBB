package app.ebbplanner.domain.focus

import kotlin.math.max

enum class FocusPhase {
    Setup,
    Running,
    Paused,
    TimesUp,
    Done,
    Break,
}

enum class FocusTickResult {
    None,
    IntervalBreak,
    IntervalWork,
    SessionEnded,
}

data class FocusTimerSnapshot(
    val phase: FocusPhase,
    val remainingMs: Long,
    val totalMs: Long,
    val timerLabel: String,
    val paused: Boolean,
)

data class FocusTimerPersistedState(
    val phase: FocusPhase,
    val endAtMs: Long,
    val totalMs: Long,
    val pausedRemainingMs: Long,
    val countUp: Boolean,
    val intervalMode: Boolean,
    val countUpStartedAtMs: Long,
    val intervalCyclesCompleted: Int,
    val phaseStartedAtMs: Long,
)

class FocusTimerController {
    private var phase: FocusPhase = FocusPhase.Setup
    private var endAtMs: Long = 0L
    private var totalMs: Long = 0L
    private var pausedRemainingMs: Long = 0L
    private var countUp: Boolean = false
    private var countUpStartedAtMs: Long = 0L
    private var intervalMode: Boolean = false
    private var intervalCyclesCompleted: Int = 0
    private var phaseStartedAtMs: Long = 0L
    private val intervalWorkMs = 25 * 60_000L
    private val intervalBreakMs = 5 * 60_000L

    fun isCountUp(): Boolean = countUp

    fun isIntervalMode(): Boolean = intervalMode

    fun snapshot(nowMs: Long): FocusTimerSnapshot {
        val remaining = remainingMs(nowMs)
        return FocusTimerSnapshot(
            phase = phase,
            remainingMs = remaining,
            totalMs = totalMs,
            timerLabel = formatMs(remaining),
            paused = phase == FocusPhase.Paused,
        )
    }

    fun phase(): FocusPhase = phase

    fun startUntilBlockEnd(
        nowMs: Long,
        blockEndMs: Long,
    ) {
        clearModes()
        val remaining = max(0L, blockEndMs - nowMs)
        if (remaining <= 0L) {
            enterTimesUp()
            return
        }
        totalMs = remaining
        endAtMs = nowMs + remaining
        phase = FocusPhase.Running
        phaseStartedAtMs = nowMs
    }

    fun startFixedMinutes(
        nowMs: Long,
        minutes: Int,
    ) {
        clearModes()
        val duration = minutes.coerceAtLeast(1) * 60_000L
        totalMs = duration
        endAtMs = nowMs + duration
        phase = FocusPhase.Running
        phaseStartedAtMs = nowMs
    }

    fun startCountUp(nowMs: Long) {
        clearModes()
        countUp = true
        countUpStartedAtMs = nowMs
        totalMs = 0L
        endAtMs = Long.MAX_VALUE
        phase = FocusPhase.Running
        phaseStartedAtMs = nowMs
    }

    fun startIntervals(nowMs: Long) {
        clearModes()
        intervalMode = true
        intervalCyclesCompleted = 0
        totalMs = intervalWorkMs
        endAtMs = nowMs + intervalWorkMs
        phase = FocusPhase.Running
        phaseStartedAtMs = nowMs
    }

    fun pause(nowMs: Long) {
        when {
            countUp && phase == FocusPhase.Running -> {
                pausedRemainingMs = nowMs - countUpStartedAtMs
                phase = FocusPhase.Paused
            }
            phase == FocusPhase.Running -> {
                pausedRemainingMs = max(0L, endAtMs - nowMs)
                phase = FocusPhase.Paused
            }
            else -> Unit
        }
    }

    fun resume(nowMs: Long) {
        when {
            countUp && phase == FocusPhase.Paused -> {
                countUpStartedAtMs = nowMs - pausedRemainingMs
                phase = FocusPhase.Running
            }
            phase == FocusPhase.Paused -> {
                endAtMs = nowMs + pausedRemainingMs
                phase = FocusPhase.Running
            }
            else -> Unit
        }
    }

    fun addMinutes(
        nowMs: Long,
        minutes: Int,
    ) {
        if (countUp) {
            countUpStartedAtMs -= minutes * 60_000L
            return
        }
        val extra = minutes * 60_000L
        when (phase) {
            FocusPhase.Running -> {
                endAtMs += extra
                totalMs += extra
            }
            FocusPhase.Paused -> {
                pausedRemainingMs += extra
                totalMs += extra
            }
            FocusPhase.TimesUp -> {
                clearModes()
                totalMs = extra
                endAtMs = nowMs + extra
                phase = FocusPhase.Running
                phaseStartedAtMs = nowMs
            }
            else -> Unit
        }
    }

    fun tick(nowMs: Long): FocusTickResult {
        if (countUp) return FocusTickResult.None
        if (phase != FocusPhase.Running && phase != FocusPhase.Break) return FocusTickResult.None
        val expired = nowMs >= endAtMs
        if (!expired) return FocusTickResult.None
        if (intervalMode) {
            if (phase == FocusPhase.Running) {
                intervalCyclesCompleted++
                if (intervalCyclesCompleted >= INTERVAL_CYCLES) {
                    enterTimesUp()
                    return FocusTickResult.SessionEnded
                }
                totalMs = intervalBreakMs
                endAtMs = nowMs + intervalBreakMs
                phase = FocusPhase.Break
                phaseStartedAtMs = nowMs
                return FocusTickResult.IntervalBreak
            }
            if (phase == FocusPhase.Break) {
                totalMs = intervalWorkMs
                endAtMs = nowMs + intervalWorkMs
                phase = FocusPhase.Running
                phaseStartedAtMs = nowMs
                return FocusTickResult.IntervalWork
            }
        }
        if (phase == FocusPhase.Break) {
            phase = FocusPhase.Setup
            endAtMs = 0L
            totalMs = 0L
            return FocusTickResult.SessionEnded
        }
        enterTimesUp()
        return FocusTickResult.SessionEnded
    }

    fun markDone() {
        phase = FocusPhase.Done
    }

    fun resetToSetup() {
        phase = FocusPhase.Setup
        endAtMs = 0L
        totalMs = 0L
        pausedRemainingMs = 0L
        phaseStartedAtMs = 0L
        clearModes()
    }

    fun startBreak(
        nowMs: Long,
        minutes: Int = 5,
    ) {
        intervalMode = false
        countUp = false
        val duration = minutes.coerceAtLeast(1) * 60_000L
        totalMs = duration
        endAtMs = nowMs + duration
        phase = FocusPhase.Break
        phaseStartedAtMs = nowMs
    }

    fun exportPersistedState(): FocusTimerPersistedState =
        FocusTimerPersistedState(
            phase = phase,
            endAtMs = endAtMs,
            totalMs = totalMs,
            pausedRemainingMs = pausedRemainingMs,
            countUp = countUp,
            intervalMode = intervalMode,
            countUpStartedAtMs = countUpStartedAtMs,
            intervalCyclesCompleted = intervalCyclesCompleted,
            phaseStartedAtMs = phaseStartedAtMs,
        )

    fun restoreFromPersisted(
        saved: FocusTimerPersistedState,
        nowMs: Long,
    ) {
        phase = saved.phase
        endAtMs = saved.endAtMs
        totalMs = saved.totalMs
        pausedRemainingMs = saved.pausedRemainingMs
        countUp = saved.countUp
        intervalMode = saved.intervalMode
        countUpStartedAtMs = saved.countUpStartedAtMs
        intervalCyclesCompleted = saved.intervalCyclesCompleted
        phaseStartedAtMs = saved.phaseStartedAtMs
        when (phase) {
            FocusPhase.Running, FocusPhase.Break ->
                if (!countUp && nowMs >= endAtMs && endAtMs != Long.MAX_VALUE) {
                    if (intervalMode && phase == FocusPhase.Running) {
                        intervalCyclesCompleted++
                        if (intervalCyclesCompleted >= INTERVAL_CYCLES) {
                            enterTimesUp()
                        } else {
                            totalMs = intervalBreakMs
                            endAtMs = nowMs + intervalBreakMs
                            phase = FocusPhase.Break
                        }
                    } else if (phase == FocusPhase.Break && intervalMode) {
                        totalMs = intervalWorkMs
                        endAtMs = nowMs + intervalWorkMs
                        phase = FocusPhase.Running
                    } else if (phase == FocusPhase.Break) {
                        this.phase = FocusPhase.Setup
                        this.endAtMs = 0L
                        this.totalMs = 0L
                    } else {
                        enterTimesUp()
                    }
                }
            else -> Unit
        }
    }

    fun restore(
        phase: FocusPhase,
        endAtMs: Long,
        totalMs: Long,
        pausedRemainingMs: Long,
        nowMs: Long,
    ) {
        restoreFromPersisted(
            FocusTimerPersistedState(
                phase = phase,
                endAtMs = endAtMs,
                totalMs = totalMs,
                pausedRemainingMs = pausedRemainingMs,
                countUp = false,
                intervalMode = false,
                countUpStartedAtMs = 0L,
                intervalCyclesCompleted = 0,
                phaseStartedAtMs = nowMs,
            ),
            nowMs,
        )
    }

    fun enterTimesUp() {
        phase = FocusPhase.TimesUp
    }

    private fun remainingMs(nowMs: Long): Long =
        when {
            countUp ->
                when (phase) {
                    FocusPhase.Running -> max(0L, nowMs - countUpStartedAtMs)
                    FocusPhase.Paused -> pausedRemainingMs
                    else -> 0L
                }
            else ->
                when (phase) {
                    FocusPhase.Running, FocusPhase.Break -> max(0L, endAtMs - nowMs)
                    FocusPhase.Paused -> pausedRemainingMs
                    FocusPhase.TimesUp -> 0L
                    else -> totalMs
                }
        }

    fun progress(nowMs: Long): Float {
        if (countUp) {
            val elapsed = remainingMs(nowMs).toFloat()
            return ((elapsed % 60_000f) / 60_000f).coerceIn(0f, 1f)
        }
        if (totalMs <= 0L) return 0f
        val left = remainingMs(nowMs).toFloat()
        return 1f - (left / totalMs.toFloat()).coerceIn(0f, 1f)
    }

    private fun clearModes() {
        countUp = false
        intervalMode = false
        countUpStartedAtMs = 0L
        intervalCyclesCompleted = 0
    }

    private fun formatMs(ms: Long): String {
        val totalSec = (ms + 999) / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%d:%02d".format(min, sec)
    }

    companion object {
        const val INTERVAL_CYCLES = 4
    }
}