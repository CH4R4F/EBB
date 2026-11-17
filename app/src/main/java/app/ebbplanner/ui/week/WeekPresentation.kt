@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.week

data class WeekDayRow(
    val weekday: String,
    val dayNumber: String,
    val summary: String,
    val loadLabel: String,
    val loadProgress: Float,
    val loadFull: Boolean = false,
    val eventCount: Int = 0,
    val today: Boolean = false,
    val open: Boolean = false,
)

data class WeekPresentationState(
    val rangeLabel: String = "16–22 Nov",
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
