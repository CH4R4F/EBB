package app.ebbplanner.ui.taskdetail

data class TaskSubtaskRow(
    val title: String,
    val estimate: String,
    val done: Boolean,
)

data class TaskDetailPresentationState(
    val id: String = "",
    val title: String = "Write project brief",
    val scheduleLabel: String = "",
    val whenLabel: String = "Today · 10:00–11:15",
    val dateLabel: String = "Mon 16 Nov",
    val estimate: String = "1 h 15 min",
    val pinned: Boolean = false,
    val energySelected: Int = 3,
    val colorLabel: String = "Amber · Edit note",
    val colorIconKey: String = "edit_note",
    val colorKey: String = "amber",
    val remindersLabel: String = "At start · 5 min before",
    val repeatLabel: String = "Doesn't repeat",
    val breatherSubtitle: String = "Default (5 min)",
    val recurringBanner: String? = null,
    val subtasks: List<TaskSubtaskRow> = emptyList(),
    val subtaskProgress: String = "2/5",
    val notes: String = "",
    val mode: TaskDetailMode = TaskDetailMode.Existing,
    val done: Boolean = false,
)

enum class TaskDetailMode {
    New,
    Existing,
    Done,
    Recurring,
    ManySubtasks,
}

enum class EstimatePickerMode {
    Presets,
    Custom,
    BigPrompt,
}

data class EstimatePickerState(
    val mode: EstimatePickerMode = EstimatePickerMode.Presets,
    val sheetTitle: String = "How long might it take?",
    val sheetSubtitle: String? = "A guess is fine. You can change it later.",
    val presetOptionsMinutes: List<Int> = listOf(5, 10, 15, 30, 45, 60, 90, 120),
    val selectedPresetMinutes: Int = 30,
    val customHours: Int = 1,
    val customMinutes: Int = 20,
    val hourAbove: String = "0",
    val hourBelow: String = "2",
    val minuteAbove: String = "15",
    val minuteBelow: String = "25",
    val doneLabel: String = "Done",
)
