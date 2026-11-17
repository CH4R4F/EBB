package app.ebbplanner.ui.taskdetail

enum class TaskSheetType {
    Reminder,
    Duration,
    BreakDown,
    Schedule,
    ColorIcon,
    ;

    companion object {
        fun fromRoute(value: String?): TaskSheetType? =
            when (value?.lowercase()) {
                "reminder", "t5" -> Reminder
                "duration", "t3", "t6" -> Duration
                "breakdown", "t7" -> BreakDown
                "schedule", "t8" -> Schedule
                "color", "t4" -> ColorIcon
                else -> null
            }

        fun toRouteArg(type: TaskSheetType): String =
            when (type) {
                Reminder -> "reminder"
                Duration -> "duration"
                BreakDown -> "breakdown"
                Schedule -> "schedule"
                ColorIcon -> "color"
            }
    }
}
