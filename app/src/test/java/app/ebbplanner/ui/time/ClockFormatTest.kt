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
    fun minutesToClockDisplay_12h_noZeroPad() {
        assertEquals("9:00 AM", ClockFormat.minutesToClockDisplay(9 * 60, is24Hour = false))
        assertEquals("12:00 AM", ClockFormat.minutesToClockDisplay(0, is24Hour = false))
        assertEquals("12:00 PM", ClockFormat.minutesToClockDisplay(12 * 60, is24Hour = false))
        assertEquals("2:30 PM", ClockFormat.minutesToClockDisplay(14 * 60 + 30, is24Hour = false))
        assertEquals("8:05 PM", ClockFormat.minutesToClockDisplay(20 * 60 + 5, is24Hour = false))
    }

    @Test
    fun localTimeToClockDisplay_matchesMinutesInBothModes() {
        assertEquals("20:05", ClockFormat.localTimeToClockDisplay("20:05", is24Hour = true))
        assertEquals("8:05 PM", ClockFormat.localTimeToClockDisplay("20:05", is24Hour = false))
        assertEquals("09:00", ClockFormat.localTimeToClockDisplay("09:00", is24Hour = true))
        assertEquals("9:00 AM", ClockFormat.localTimeToClockDisplay("09:00", is24Hour = false))
    }
}
