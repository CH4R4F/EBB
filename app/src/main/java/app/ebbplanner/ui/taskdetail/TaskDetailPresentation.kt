package app.ebbplanner.ui.taskdetail

data class TaskSubtaskRow(
    val title: String,
    val estimate: String,
    val done: Boolean,
)

data class TaskDetailPresentationState(
    val title: String = "Write project brief",
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
    val breatherLabel: String = "Default (5 min)",
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
    val selectedMinutes: Int = 75,
    val options: List<Int> = listOf(15, 30, 45, 60, 75, 90, 120),
    val customText: String = "",
    val promptTitle: String = "How long will this take?",
)
