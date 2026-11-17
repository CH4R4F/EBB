@file:Suppress("MaxLineLength", "TooManyFunctions", "NewLineAtEndOfFile")

package app.ebbplanner.ui.routines

private fun baseRoutines(runningMorning: Boolean = false): List<RoutineCard> =
    listOf(
        RoutineCard(
            "Morning",
            if (runningMorning) "Weekdays 07:30 · started 07:34" else "Weekdays 07:30",
            "45 min · 6 steps",
            "wb_sunny",
            running = runningMorning,
            stepLabel = if (runningMorning) "Step 3 of 6" else null,
            progress = if (runningMorning) 0.5f else 0f,
        ),
        RoutineCard("Work start-up", "Weekdays 08:45", "20 min · 5 steps", "work"),
        RoutineCard("Leaving the house", "On demand", "8 min · 5 steps", "door_front"),
    )

private fun recurring(): List<RecurringTaskRow> =
    listOf(
        RecurringTaskRow("Take meds", "Daily 08:00", "medication"),
        RecurringTaskRow("Water plants", "Every 3 days after done", "water_drop"),
        RecurringTaskRow("Pay rent", "Monthly on the 1st · carry over", "payments"),
        RecurringTaskRow("Bins out", "Weekly on Tue 20:00", "delete"),
    )

fun routinesPopulated(): RoutinesPresentationState = RoutinesPresentationState(routines = baseRoutines(), recurring = recurring())

fun routinesEmpty(): RoutinesPresentationState =
    RoutinesPresentationState(
        routines = emptyList(),
        recurring = emptyList(),
        mode = RoutinesVisualMode.Empty,
    )

fun routinesRunning(): RoutinesPresentationState =
    RoutinesPresentationState(
        routines = baseRoutines(runningMorning = true),
        recurring = recurring(),
        mode = RoutinesVisualMode.Running,
    )
