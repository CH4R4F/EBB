package app.ebbplanner.ui.time

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockFormatTest {
    @Test
    fun minutesToClockDisplay_24h_zeroPads() {
        assertEquals("09:00", ClockFormat.minutesToClockDisplay(9 * 60, is24Hour = true))
        assertEquals("00:05", ClockFormat.minutesToClockDisplay(5, is24Hour = true))
        assertEquals("14:30", ClockFormat.minutesToClockDisplay(14 * 60 + 30, is24Hour = true))
    }

    @Test
    fun minutesToClockDisplay_12h_zeroPads() {
        assertEquals("09:00 AM", ClockFormat.minutesToClockDisplay(9 * 60, is24Hour = false))
        assertEquals("12:00 AM", ClockFormat.minutesToClockDisplay(0, is24Hour = false))
        assertEquals("12:00 PM", ClockFormat.minutesToClockDisplay(12 * 60, is24Hour = false))
        assertEquals("02:30 PM", ClockFormat.minutesToClockDisplay(14 * 60 + 30, is24Hour = false))
    }
}
