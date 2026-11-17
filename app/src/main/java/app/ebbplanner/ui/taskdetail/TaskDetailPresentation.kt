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
    val energySelected: Int = 3,
    val colorLabel: String = "Amber · Edit note",
    val remindersLabel: String = "At start · 5 min before",
    val repeatLabel: String = "Doesn't repeat",
    val subtasks: List<TaskSubtaskRow> = emptyList(),
    val subtaskProgress: String = "2/5",
    val notes: String = "",
    val mode: TaskDetailMode = TaskDetailMode.Existing,
)

enum class TaskDetailMode {
    New,
    Existing,
    Done,
    Recurring,
}
