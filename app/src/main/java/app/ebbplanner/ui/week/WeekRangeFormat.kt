package app.ebbplanner.ui.week

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val NBSP = '\u00A0'

fun formatWeekRangeLabel(
    start: LocalDate,
    end: LocalDate,
): String {
    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
    return if (start.month == end.month && start.year == end.year) {
        "${start.dayOfMonth}$NBSP–$NBSP${end.dayOfMonth}$NBSP${end.format(monthFormatter)}"
    } else {
        val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        val startPart = start.format(dayMonth).replace(' ', NBSP)
        val endPart = end.format(dayMonth).replace(' ', NBSP)
        "$startPart$NBSP–$NBSP$endPart"
    }
}

internal const val WEEK_RANGE_LABEL_TAG = "week_range_label"

internal const val WEEK_TOP_BAR_TAG = "week_top_bar"