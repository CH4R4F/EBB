package app.ebbplanner.domain.focus

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusTimerControllerTest {
    private val controller = FocusTimerController()
    private val t0 = 1_000_000L

    @Test
    fun justFiveMinutes_countsDownToTimesUp() {
        controller.startFixedMinutes(t0, 5)
        var timesUp = false
        for (i in 0 until 5) {
            timesUp = controller.tick(t0 + i * 60_000L)
        }
        timesUp = controller.tick(t0 + 5 * 60_000L)
        assertTrue(timesUp)
        assertEquals(FocusPhase.TimesUp, controller.phase())
    }

    @Test
    fun pause_resume_extendsRemaining() {
        controller.startFixedMinutes(t0, 10)
        controller.pause(t0 + 2 * 60_000L)
        val paused = controller.snapshot(t0 + 2 * 60_000L)
        assertEquals(FocusPhase.Paused, paused.phase)
        controller.resume(t0 + 2 * 60_000L)
        controller.addMinutes(t0 + 2 * 60_000L, 5)
        val snap = controller.snapshot(t0 + 2 * 60_000L)
        assertTrue(snap.remainingMs >= 12 * 60_000L)
    }
}
