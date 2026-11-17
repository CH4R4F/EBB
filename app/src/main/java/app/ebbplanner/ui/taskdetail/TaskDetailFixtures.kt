package app.ebbplanner.ui.taskdetail

fun taskDetailExisting(): TaskDetailPresentationState =
    TaskDetailPresentationState(
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

fun estimatePickerDefault(): EstimatePickerState =
    EstimatePickerState(
        selectedMinutes = 75,
        options = listOf(15, 30, 45, 60, 75, 90, 120),
    )

data class EstimatePickerState(
    val selectedMinutes: Int,
    val options: List<Int>,
)
