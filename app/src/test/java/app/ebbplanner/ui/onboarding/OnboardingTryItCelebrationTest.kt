package app.ebbplanner.ui.onboarding

import app.ebbplanner.domain.capture.QuickCaptureParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class OnboardingTryItCelebrationTest {
    private val today = LocalDate.of(2026, 11, 17)

    @Test
    fun parseBuyStampsTomorrowTime_forCelebrationCopy() {
        val parsed = QuickCaptureParser.parse("Buy stamps tomorrow 2:30pm", today)
        assertEquals("Buy stamps", parsed.title)
        assertNotNull(parsed.startTime)
        assertEquals(14, parsed.startTime?.hour)
        assertEquals(30, parsed.startTime?.minute)
    }
}
