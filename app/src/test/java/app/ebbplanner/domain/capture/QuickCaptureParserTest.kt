package app.ebbplanner.domain.capture

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class QuickCaptureParserTest {
    private val today = LocalDate.of(2026, 11, 16)

    @Test
    fun parsesTomorrowTimeAndDuration() {
        val result = QuickCaptureParser.parse("Dentist tomorrow 3pm 45min", today)
        assertEquals(today.plusDays(1), result.targetDate)
        assertEquals(LocalTime.of(15, 0), result.startTime)
        assertEquals(45, result.durationMin)
        assertEquals("Dentist", result.title)
    }

    @Test
    fun parses24HourTime() {
        val result = QuickCaptureParser.parse("Standup at 09:30 15 min", today)
        assertEquals(LocalTime.of(9, 30), result.startTime)
        assertEquals(15, result.durationMin)
    }

    @Test
    fun leavesTitleWhenNoTokens() {
        val result = QuickCaptureParser.parse("Buy milk", today)
        assertEquals("Buy milk", result.title)
        assertEquals(null, result.startTime)
    }
}
