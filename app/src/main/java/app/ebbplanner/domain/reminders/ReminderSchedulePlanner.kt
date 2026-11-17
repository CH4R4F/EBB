package app.ebbplanner.domain.reminders

/**
 * Pure scheduling decisions: exact when allowed, otherwise WorkManager fallback.
 */
object ReminderSchedulePlanner {
    fun plan(
        requests: List<ReminderRequest>,
        exactAlarmsGranted: Boolean,
        nowEpochMillis: Long,
    ): List<ScheduledReminder> =
        requests
            .filter { it.fireAtEpochMillis > nowEpochMillis }
            .map { request ->
                if (exactAlarmsGranted && request.exact) {
                    ScheduledReminder(request, ReminderDeliveryMode.ExactAlarm)
                } else {
                    ScheduledReminder(
                        request,
                        ReminderDeliveryMode.InexactWorkManager,
                        workRequestId = "reminder-${request.taskId}-${request.fireAtEpochMillis}",
                    )
                }
            }

    fun remindersNeedingReschedule(
        scheduled: List<ScheduledReminder>,
        nowEpochMillis: Long,
    ): List<ScheduledReminder> = scheduled.filter { it.request.fireAtEpochMillis > nowEpochMillis }

    fun cancelObsolete(
        scheduled: List<ScheduledReminder>,
        activeTaskIds: Set<String>,
    ): List<ScheduledReminder> = scheduled.filter { it.request.taskId in activeTaskIds }
}
