package app.ebbplanner.domain.day

import app.ebbplanner.ui.time.ClockFormat
import java.time.LocalDate

data class ShiftedSchedule(
    val date: LocalDate,
    val startLocalTime: String,
    val crossedMidnight: Boolean,
)

object DayTimeShift {
    /** Adds [offsetMinutes] to [startMinutes] on [date], rolling to the next calendar day past midnight. */
    fun shiftStart(
        date: LocalDate,
        startMinutes: Int,
        offsetMinutes: Int,
    ): ShiftedSchedule {
        var day = date
        var total = startMinutes + offsetMinutes
        var crossed = false
        while (total >= 24 * 60) {
            total -= 24 * 60
            day = day.plusDays(1)
            crossed = true
        }
        while (total < 0) {
            total += 24 * 60
            day = day.minusDays(1)
        }
        return ShiftedSchedule(
            date = day,
            startLocalTime = ClockFormat.minutesToLocalTime(total),
            crossedMidnight = crossed,
        )
    }
}