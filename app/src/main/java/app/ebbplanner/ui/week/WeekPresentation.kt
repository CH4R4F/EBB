@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.week

import java.time.LocalDate

data class WeekDayRow(
    val date: LocalDate = LocalDate.now(),
    val weekday: String,
    val dayNumber: String,
    val summary: String,
    val loadLabel: String,
    val loadProgress: Float,
    val loadFull: Boolean = false,
    val eventCount: Int = 0,
    val today: Boolean = false,
    val open: Boolean = false,
    val selected: Boolean = false,
)

data class WeekPresentationState(
    val rangeLabel: String = "16\u201322\u00A0Nov",
    val selectedDate: LocalDate = LocalDate.now(),
    val days: List<WeekDayRow> = emptyList(),
    val mode: WeekVisualMode = WeekVisualMode.Portrait,
    val bannerMessage: String? = null,
    val dragLabel: String? = null,
)

enum class WeekVisualMode {
    Portrait,
    Landscape,
    Empty,
    Drag,
    Loading,
}
