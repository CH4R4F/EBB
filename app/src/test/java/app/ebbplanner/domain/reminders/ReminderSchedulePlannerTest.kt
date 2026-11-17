package app.ebbplanner.domain.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSchedulePlannerTest {
    private val now = 1_000_000L

    @Test
    fun usesExactWhenGranted() {
        val request =
            ReminderRequest("t1", now + 60_000, ReminderKind.TaskStart, "Email", exact = true)
        val planned = ReminderSchedulePlanner.plan(listOf(request), exactAlarmsGranted = true, now)
        assertEquals(ReminderDeliveryMode.ExactAlarm, planned.single().delivery)
    }

    @Test
    fun fallsBackToWorkManagerWhenExactDenied() {
        val request =
            ReminderRequest("t1", now + 60_000, ReminderKind.TaskStart, "Email", exact = true)
        val planned = ReminderSchedulePlanner.plan(listOf(request), exactAlarmsGranted = false, now)
        assertEquals(ReminderDeliveryMode.InexactWorkManager, planned.single().delivery)
        assertNotNullWorkId(planned.single())
    }

    @Test
    fun dropsPastReminders() {
        val request =
            ReminderRequest("t1", now - 1, ReminderKind.TaskStart, "Late", exact = true)
        val planned = ReminderSchedulePlanner.plan(listOf(request), exactAlarmsGranted = true, now)
        assertTrue(planned.isEmpty())
    }

    private fun assertNotNullWorkId(reminder: ScheduledReminder) {
        assertTrue(reminder.workRequestId != null)
    }
}
