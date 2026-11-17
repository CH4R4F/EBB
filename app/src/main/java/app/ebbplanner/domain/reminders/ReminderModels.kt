package app.ebbplanner.domain.reminders

data class ReminderRequest(
    val taskId: String,
    val fireAtEpochMillis: Long,
    val kind: ReminderKind,
    val title: String,
    val exact: Boolean,
)

enum class ReminderKind {
    TaskStart,
    Breather,
}

enum class ReminderDeliveryMode {
    ExactAlarm,
    InexactWorkManager,
}

data class ScheduledReminder(
    val request: ReminderRequest,
    val delivery: ReminderDeliveryMode,
    val workRequestId: String? = null,
)
