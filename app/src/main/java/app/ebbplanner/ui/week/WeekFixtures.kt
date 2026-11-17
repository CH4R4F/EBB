@file:Suppress("MagicNumber", "MaxLineLength", "TooManyFunctions", "NewLineAtEndOfFile")

package app.ebbplanner.ui.week

import java.time.LocalDate

private fun weekDaysPopulated(): List<WeekDayRow> {
    val base = LocalDate.of(2025, 11, 16)
    return listOf(
        WeekDayRow(base, "Mon", "16", "Write project brief · Call bank · Dentist 15:30", "Balanced", 0.6f, eventCount = 2, today = true),
        WeekDayRow(base.plusDays(1), "Tue", "17", "Invoice client · Gym", "Light", 0.3f, eventCount = 1),
        WeekDayRow(base.plusDays(2), "Wed", "18", "Team workshop · Expense report · Call gran", "Full", 0.9f, loadFull = true, eventCount = 3),
        WeekDayRow(base.plusDays(3), "Thu", "19", "Groceries", "Light", 0.2f),
        WeekDayRow(base.plusDays(4), "Fri", "20", "Send slides · Coffee with Sam", "Balanced", 0.55f, eventCount = 1),
        WeekDayRow(base.plusDays(5), "Sat", "21", "Call gran", "Light", 0.15f),
        WeekDayRow(base.plusDays(6), "Sun", "22", "Weekly reset", "Light", 0.1f),
    )
}

fun weekPortrait(): WeekPresentationState = WeekPresentationState(days = weekDaysPopulated())

fun weekLandscape(): WeekPresentationState =
    WeekPresentationState(
        days = weekDaysPopulated(),
        mode = WeekVisualMode.Landscape,
    )

fun weekEmpty(): WeekPresentationState =
    WeekPresentationState(
        days = emptyList(),
        mode = WeekVisualMode.Empty,
        bannerMessage = "This week is still open — add something when you're ready.",
    )

fun weekDrag(): WeekPresentationState =
    WeekPresentationState(
        days = weekDaysPopulated(),
        mode = WeekVisualMode.Drag,
        dragLabel = "Call bank → Tue 11:00",
    )