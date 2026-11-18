package app.ebbplanner.ui.week

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeekRangeFormatTest {
    @Test
    fun sameMonth_compactWithNbspBeforeMonth() {
        val label =
            formatWeekRangeLabel(
                LocalDate.of(2025, 11, 14),
                LocalDate.of(2025, 11, 20),
            )
        assertEquals("14\u201320\u00A0Nov", label)
    }

    @Test
    fun crossMonth_compactEnDash() {
        val label =
            formatWeekRangeLabel(
                LocalDate.of(2025, 10, 31),
                LocalDate.of(2025, 11, 6),
            )
        assertEquals("31\u00A0Oct\u20136\u00A0Nov", label)
    }
}