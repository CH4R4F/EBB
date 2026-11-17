package app.ebbplanner.ui.taskdetail

data class TaskDetailCallbacks(
    val onClose: () -> Unit = {},
    val onMarkDone: () -> Unit = {},
    val onStartFocus: () -> Unit = {},
    val onOpenReminders: () -> Unit = {},
    val onOpenDuration: () -> Unit = {},
    val onOpenBreakDown: () -> Unit = {},
    val onOpenSchedule: () -> Unit = {},
    val onOpenColorIcon: () -> Unit = {},
)
