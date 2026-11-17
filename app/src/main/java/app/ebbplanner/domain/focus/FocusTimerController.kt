package app.ebbplanner.domain.focus

import kotlin.math.max

enum class FocusPhase {
    Setup,
    Running,
    Paused,
    TimesUp,
    Done,
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

    fun snapshot(nowMs: Long): FocusTimerSnapshot =
        FocusTimerSnapshot(
            phase = phase,
            remainingMs = remainingMs(nowMs),
            totalMs = totalMs,
            timerLabel = formatMs(remainingMs(nowMs)),
            paused = phase == FocusPhase.Paused,
        )

    fun phase(): FocusPhase = phase

    fun startUntilBlockEnd(
        nowMs: Long,
        blockEndMs: Long,
    ) {
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
        val duration = minutes.coerceAtLeast(1) * 60_000L
        totalMs = duration
        endAtMs = nowMs + duration
        phase = FocusPhase.Running
    }

    fun pause(nowMs: Long) {
        if (phase != FocusPhase.Running) return
        pausedRemainingMs = max(0L, endAtMs - nowMs)
        phase = FocusPhase.Paused
    }

    fun resume(nowMs: Long) {
        if (phase != FocusPhase.Paused) return
        endAtMs = nowMs + pausedRemainingMs
        phase = FocusPhase.Running
    }

    fun addMinutes(
        nowMs: Long,
        minutes: Int,
    ) {
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
                totalMs = extra
                endAtMs = nowMs + extra
                phase = FocusPhase.Running
            }
            else -> Unit
        }
    }

    fun tick(nowMs: Long): Boolean {
        if (phase != FocusPhase.Running) return false
        val expired = nowMs >= endAtMs
        if (expired) {
            enterTimesUp()
        }
        return expired
    }

    fun markDone() {
        phase = FocusPhase.Done
    }

    fun resetToSetup() {
        phase = FocusPhase.Setup
        endAtMs = 0L
        totalMs = 0L
        pausedRemainingMs = 0L
    }

    fun enterTimesUp() {
        phase = FocusPhase.TimesUp
    }

    private fun remainingMs(nowMs: Long): Long =
        when (phase) {
            FocusPhase.Running -> max(0L, endAtMs - nowMs)
            FocusPhase.Paused -> pausedRemainingMs
            FocusPhase.TimesUp -> 0L
            else -> totalMs
        }

    fun progress(nowMs: Long): Float {
        if (totalMs <= 0L) return 0f
        val left = remainingMs(nowMs).toFloat()
        return 1f - (left / totalMs.toFloat()).coerceIn(0f, 1f)
    }

    private fun formatMs(ms: Long): String {
        val totalSec = (ms + 999) / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%d:%02d".format(min, sec)
    }
}
