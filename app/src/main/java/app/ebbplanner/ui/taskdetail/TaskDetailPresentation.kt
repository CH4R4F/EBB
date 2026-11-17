package app.ebbplanner.ui.taskdetail

import app.ebbplanner.data.local.entity.TaskEntity

data class TaskSubtaskRow(
    val title: String,
    val estimate: String,
    val done: Boolean,
)

data class TaskDetailPresentationState(
    val id: String = "",
    val title: String = "",
    val scheduleLabel: String = "",
    val whenLabel: String = "",
    val dateLabel: String = "",
    val estimate: String = "",
    val pinned: Boolean = false,
    val energySelected: Int = 3,
    val colorLabel: String = "",
    val colorIconKey: String = "task",
    val colorKey: String = "slate",
    val remindersLabel: String = "",
    val repeatLabel: String = "Doesn't repeat",
    val breatherSubtitle: String = "Default (5 min)",
    val recurringBanner: String? = null,
    val subtasks: List<TaskSubtaskRow> = emptyList(),
    val subtaskProgress: String = "",
    val notes: String = "",
    val mode: TaskDetailMode = TaskDetailMode.Loading,
    val done: Boolean = false,
    val durationMin: Int = TaskEntity.DEFAULT_DURATION_MIN,
    val remindersEnabled: Boolean = false,
)

enum class TaskDetailMode {
    Loading,
    NotFound,
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
