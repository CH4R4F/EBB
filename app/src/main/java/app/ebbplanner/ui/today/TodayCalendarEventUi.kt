@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.today

import app.ebbplanner.data.calendar.CalendarEventSnapshot
import java.time.Instant
import java.time.ZoneId

data class TodayCalendarEventUi(
    val id: String,
    val title: String,
    val startMinutes: Int,
    val durationMin: Int,
    val calendarLabel: String,
    val colorKey: String = "blue",
    val pinned: Boolean = true,
)

fun CalendarEventSnapshot.toTodayUi(zoneId: ZoneId = ZoneId.systemDefault()): TodayCalendarEventUi {
    val start = Instant.ofEpochMilli(startMillis).atZone(zoneId)
    val durationMin = ((endMillis - startMillis) / 60_000L).toInt().coerceAtLeast(1)
    return TodayCalendarEventUi(
        id = id.toString(),
        title = title,
        startMinutes = start.hour * 60 + start.minute,
        durationMin = durationMin,
        calendarLabel = calendarName.ifBlank { "Calendar" },
    )
}

fun TodayCalendarEventUi.toPresentation(): TodayCalendarEvent =
    TodayCalendarEvent(
        id = id,
        title = title,
        startMinutes = startMinutes,
        durationMin = durationMin,
        calendarLabel = calendarLabel,
        colorKey = colorKey,
        pinned = pinned,
    )
