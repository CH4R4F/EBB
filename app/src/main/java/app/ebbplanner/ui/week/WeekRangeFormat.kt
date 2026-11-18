package app.ebbplanner.ui.week

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val NBSP = '\u00A0'
private const val EN_DASH = '\u2013'

fun formatWeekRangeLabel(
    start: LocalDate,
    end: LocalDate,
): String {
    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
    return if (start.month == end.month && start.year == end.year) {
        val month = end.format(monthFormatter)
        "${start.dayOfMonth}$EN_DASH${end.dayOfMonth}$NBSP$month"
    } else {
        val startMonth = start.format(monthFormatter)
        val endMonth = end.format(monthFormatter)
        "${start.dayOfMonth}$NBSP$startMonth$EN_DASH${end.dayOfMonth}$NBSP$endMonth"
    }
}

internal const val WEEK_RANGE_LABEL_TAG = "week_range_label"

internal const val WEEK_TOP_BAR_TAG = "week_top_bar"

internal const val WEEK_THIS_WEEK_BUTTON_TAG = "week_this_week_button"