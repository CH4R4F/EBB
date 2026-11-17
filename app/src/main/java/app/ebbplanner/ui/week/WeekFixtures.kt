@file:Suppress("MagicNumber", "MaxLineLength", "TooManyFunctions", "NewLineAtEndOfFile")

package app.ebbplanner.ui.week

private fun weekDaysPopulated(): List<WeekDayRow> =
    listOf(
        WeekDayRow("Mon", "16", "Write project brief · Call bank · Dentist 15:30", "Balanced", 0.6f, eventCount = 2, today = true),
        WeekDayRow("Tue", "17", "Invoice client · Gym", "Light", 0.3f, eventCount = 1),
        WeekDayRow("Wed", "18", "Team workshop · Expense report · Call gran", "Full", 0.9f, loadFull = true, eventCount = 3),
        WeekDayRow("Thu", "19", "Groceries", "Light", 0.2f),
        WeekDayRow("Fri", "20", "Send slides · Coffee with Sam", "Balanced", 0.55f, eventCount = 1),
        WeekDayRow("Sat", "21", "Call gran", "Light", 0.15f),
        WeekDayRow("Sun", "22", "Weekly reset", "Light", 0.1f),
    )

fun weekPortrait(): WeekPresentationState = WeekPresentationState(days = weekDaysPopulated())

fun weekLandscape(): WeekPresentationState =
    WeekPresentationState(
        days = weekDaysPopulated(),
        mode = WeekVisualMode.Landscape,
    )

fun weekEmpty(): WeekPresentationState =
    WeekPresentationState(
        rangeLabel = "23–29 Nov",
        days = weekDaysPopulated().map { it.copy(summary = "Open", open = true, loadProgress = 0f) },
        mode = WeekVisualMode.Empty,
        bannerMessage = "A quiet week so far. Tap a day to plan it.",
    )

fun weekDrag(): WeekPresentationState =
    WeekPresentationState(
        days = weekDaysPopulated(),
        mode = WeekVisualMode.Drag,
        dragLabel = "Call bank → Wed",
    )
