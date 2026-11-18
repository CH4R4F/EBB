package app.ebbplanner.ui.week

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeekRangeFormatTest {
    @Test
    fun sameMonth_usesNonBreakingSpaces() {
        val label =
            formatWeekRangeLabel(
                LocalDate.of(2025, 11, 14),
                LocalDate.of(2025, 11, 20),
            )
        assertTrue(label.contains('\u00A0'))
        assertTrue(label.contains("14"))
        assertTrue(label.contains("20"))
        assertTrue(label.contains("Nov"))
        assertEquals(-1, label.indexOf(" "))
    }
}