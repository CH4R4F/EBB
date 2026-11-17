package app.ebbplanner.domain.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun parses24HourTimeWithDuration() {
        val result = QuickCaptureParser.parse("Standup at 09:30 15 min", today)
        assertEquals(LocalTime.of(9, 30), result.startTime)
        assertEquals(15, result.durationMin)
        assertEquals("Standup", result.title)
    }

    @Test
    fun workoutDurationIsNotParsedAsTime() {
        val result = QuickCaptureParser.parse("workout 30 min", today)
        assertNull(result.startTime)
        assertEquals(30, result.durationMin)
        assertEquals("workout", result.title)
    }

    @Test
    fun bareChapterCountIsNotTime() {
        val result = QuickCaptureParser.parse("read 2 chapters", today)
        assertNull(result.startTime)
        assertNull(result.durationMin)
        assertEquals("read 2 chapters", result.title)
    }

    @Test
    fun bareAppleCountWithTomorrowIsNotTime() {
        val result = QuickCaptureParser.parse("buy 3 apples tomorrow", today)
        assertEquals(today.plusDays(1), result.targetDate)
        assertNull(result.startTime)
        assertEquals("buy 3 apples", result.title)
    }

    @Test
    fun tomorrowAloneRemainsTitle() {
        val result = QuickCaptureParser.parse("tomorrow", today)
        assertEquals(today.plusDays(1), result.targetDate)
        assertEquals("tomorrow", result.title)
    }

    @Test
    fun leavesTitleWhenNoTokens() {
        val result = QuickCaptureParser.parse("Buy milk", today)
        assertEquals("Buy milk", result.title)
        assertNull(result.startTime)
    }
}
