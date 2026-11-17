@file:Suppress("MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.taskdetail

fun taskDetailExisting(): TaskDetailPresentationState =
    TaskDetailPresentationState(
        title = "Write project brief",
        whenLabel = "Today · 10:00–11:15",
        dateLabel = "Mon 16 Nov",
        estimate = "1 h 15 min",
        pinned = false,
        energySelected = 3,
        colorLabel = "Amber · Edit note",
        colorIconKey = "edit_note",
        colorKey = "amber",
        remindersLabel = "At start · 5 min before",
        remindersEnabled = true,
        subtaskProgress = "2/5",
        subtasks =
            listOf(
                TaskSubtaskRow(title = "Gather last year's numbers", estimate = "10 min", done = true),
                TaskSubtaskRow(title = "Outline 3 sections", estimate = "10 min", done = true),
                TaskSubtaskRow(title = "Write the first draft", estimate = "30 min", done = false),
                TaskSubtaskRow(title = "Ask Sam for feedback", estimate = "5 min", done = false),
                TaskSubtaskRow(title = "Final read-through", estimate = "10 min", done = false),
            ),
        notes = "Brief for the spring campaign. Keep it to one page.",
        mode = TaskDetailMode.Existing,
    )

fun taskDetailNew(): TaskDetailPresentationState =
    TaskDetailPresentationState(
        title = "New task",
        whenLabel = "Inbox",
        dateLabel = "No date",
        estimate = "15 min",
        energySelected = 2,
        colorLabel = "Slate",
        colorKey = "slate",
        subtaskProgress = "0/0",
        subtasks = emptyList(),
        mode = TaskDetailMode.New,
    )

fun taskDetailDone(): TaskDetailPresentationState =
    taskDetailExisting().copy(
        mode = TaskDetailMode.Done,
        done = true,
    )

fun taskDetailRecurring(): TaskDetailPresentationState =
    taskDetailExisting().copy(
        mode = TaskDetailMode.Recurring,
        recurringBanner = "Repeats weekdays",
        repeatLabel = "Weekdays",
    )

fun taskDetailManySubtasks(): TaskDetailPresentationState {
    val subs =
        (1..22).map { i ->
            TaskSubtaskRow(title = "Step $i", estimate = "5 min", done = i <= 3)
        }
    return taskDetailExisting().copy(
        mode = TaskDetailMode.ManySubtasks,
        subtaskProgress = "3/22",
        subtasks = subs,
    )
}

fun estimatePickerPresets(): EstimatePickerState = EstimatePickerState(mode = EstimatePickerMode.Presets)

fun estimatePickerCustom(): EstimatePickerState =
    EstimatePickerState(
        mode = EstimatePickerMode.Custom,
        sheetSubtitle = null,
        customHours = 1,
        customMinutes = 20,
        hourAbove = "0",
        hourBelow = "2",
        minuteAbove = "15",
        minuteBelow = "25",
    )

fun estimatePickerBigPrompt(): EstimatePickerState =
    EstimatePickerState(
        mode = EstimatePickerMode.BigPrompt,
        sheetSubtitle = null,
        customHours = 5,
        customMinutes = 0,
        hourAbove = "4",
        hourBelow = "6",
        minuteAbove = "",
        minuteBelow = "5",
        doneLabel = "Keep 5 h",
    )
