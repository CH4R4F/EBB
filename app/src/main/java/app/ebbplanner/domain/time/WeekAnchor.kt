package app.ebbplanner.domain.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object WeekAnchor {
    fun startOfWeek(date: LocalDate, storedWeekStart: Int): LocalDate {
        val first: DayOfWeek = WeekStart.resolveDayOfWeek(storedWeekStart)
        return date.with(TemporalAdjusters.previousOrSame(first))
    }

    fun weekStartForPage(
        today: LocalDate,
        storedWeekStart: Int,
        pageOffsetFromToday: Int,
    ): LocalDate = startOfWeek(today, storedWeekStart).plusWeeks(pageOffsetFromToday.toLong())
}