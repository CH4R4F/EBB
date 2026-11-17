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

class FocusTimerController {
    private var phase: FocusPhase = FocusPhase.Setup
    private var endAtMs: Long = 0L
    private var totalMs: Long = 0L
    private var pausedRemainingMs: Long = 0L
    private var countUp: Boolean = false
    private var countUpStartedAtMs: Long = 0L
    private var intervalMode: Boolean = false
    private val intervalWorkMs = 25 * 60_000L
    private val intervalBreakMs = 5 * 60_000L

    fun isCountUp(): Boolean = countUp

    fun snapshot(nowMs: Long): FocusTimerSnapshot {
        val remaining = remainingMs(nowMs)
        val label =
            if (countUp) {
                formatMs(remaining)
            } else {
                formatMs(remaining)
            }
        return FocusTimerSnapshot(
            phase = phase,
            remainingMs = remaining,
            totalMs = totalMs,
            timerLabel = label,
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
    }

    fun startCountUp(nowMs: Long) {
        clearModes()
        countUp = true
        countUpStartedAtMs = nowMs
        totalMs = 0L
        endAtMs = Long.MAX_VALUE
        phase = FocusPhase.Running
    }

    fun startIntervals(nowMs: Long) {
        clearModes()
        intervalMode = true
        totalMs = intervalWorkMs
        endAtMs = nowMs + intervalWorkMs
        phase = FocusPhase.Running
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
                totalMs = intervalBreakMs
                endAtMs = nowMs + intervalBreakMs
                phase = FocusPhase.Break
                return FocusTickResult.IntervalBreak
            }
            if (phase == FocusPhase.Break) {
                totalMs = intervalWorkMs
                endAtMs = nowMs + intervalWorkMs
                phase = FocusPhase.Running
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
    }

    fun restore(
        phase: FocusPhase,
        endAtMs: Long,
        totalMs: Long,
        pausedRemainingMs: Long,
        nowMs: Long,
    ) {
        this.phase = phase
        this.endAtMs = endAtMs
        this.totalMs = totalMs
        this.pausedRemainingMs = pausedRemainingMs
        when (phase) {
            FocusPhase.Running, FocusPhase.Break ->
                if (nowMs >= endAtMs && endAtMs != Long.MAX_VALUE) {
                    if (phase == FocusPhase.Break) {
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
    }

    private fun formatMs(ms: Long): String {
        val totalSec = (ms + 999) / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%d:%02d".format(min, sec)
    }
}