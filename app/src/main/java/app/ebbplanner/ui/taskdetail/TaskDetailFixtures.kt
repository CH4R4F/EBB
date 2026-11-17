@file:Suppress("MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.taskdetail

fun taskDetailExisting(): TaskDetailPresentationState =
    TaskDetailPresentationState(
        pinned = false,
        subtasks =
            listOf(
                TaskSubtaskRow("Gather last year's numbers", "10 min", true),
                TaskSubtaskRow("Outline 3 sections", "10 min", true),
                TaskSubtaskRow("Write the first draft", "30 min", false),
                TaskSubtaskRow("Ask Sam for feedback", "5 min", false),
                TaskSubtaskRow("Final read-through", "10 min", false),
            ),
        notes = "Brief for the spring campaign. Keep it to one page.",
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
            TaskSubtaskRow("Step $i", "5 min", i <= 3)
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
