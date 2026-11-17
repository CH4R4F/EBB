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
    val onPinnedChange: (Boolean) -> Unit = {},
    val onEnergyChange: (Int) -> Unit = {},
    val onEstimateDecrease: () -> Unit = {},
    val onEstimateIncrease: () -> Unit = {},
    val onToggleSubtask: (String) -> Unit = {},
    val onDeleteTask: () -> Unit = {},
    val onOpenRepeat: () -> Unit = {},
    val onApplyBreather: () -> Unit = {},
)
