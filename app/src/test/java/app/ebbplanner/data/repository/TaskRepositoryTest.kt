package app.ebbplanner.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.reminders.TaskReminderSync
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskRepositoryTest {
    private lateinit var database: EbbDatabase
    private lateinit var repository: TaskRepository
    private lateinit var reminderSync: TaskReminderSync

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        reminderSync = mockk(relaxed = true)
        coEvery { reminderSync.syncTask(any()) } returns Unit
        coEvery { reminderSync.cancelTask(any()) } returns Unit
        val prefs = mockk<app.ebbplanner.data.preferences.UserPreferencesRepository>(relaxed = true)
        every { prefs.remindersDefault } returns kotlinx.coroutines.flow.flowOf(true)
        repository =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                reminderSync,
                prefs,
                mockk(relaxed = true),
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedSampleTasksForToday_seedsTasks() =
        runTest {
            val today = LocalDate.of(2025, 11, 17)
            repository.seedSampleTasksForToday(today)
            val tasks = repository.observeTasksForDate(today).first()
            assertTrue(tasks.isNotEmpty())
            assertTrue(tasks.any { it.placement == TaskEntity.PLACEMENT_SCHEDULED })
            assertTrue(tasks.any { it.placement == TaskEntity.PLACEMENT_ANYTIME })
        }

    @Test
    fun addQuickCapture_insertsTask() =
        runTest {
            val today = LocalDate.of(2025, 11, 17)
            val task = repository.addQuickCapture("Walk the dog", today, TaskEntity.PLACEMENT_ANYTIME)
            assertEquals("Walk the dog", task.title)
            val stored = repository.getTask(task.id)
            assertNotNull(stored)
            coVerify { reminderSync.syncTask(task.id) }
        }

    @Test
    fun scheduleTask_movesToScheduledAndSyncs() =
        runTest {
            val today = LocalDate.of(2025, 11, 17)
            val task = repository.addQuickCapture("Door", today, TaskEntity.PLACEMENT_ANYTIME)
            repository.scheduleTask(task.id, TaskEntity.PLACEMENT_SCHEDULED, today, "14:00")
            val stored = repository.getTask(task.id)
            assertEquals(TaskEntity.PLACEMENT_SCHEDULED, stored?.placement)
            assertEquals("14:00", stored?.startLocalTime)
            coVerify { reminderSync.syncTask(task.id) }
        }

    @Test
    fun addSubtask_persists() =
        runTest {
            val today = LocalDate.now()
            val task = repository.addQuickCapture("Write", today, TaskEntity.PLACEMENT_INBOX)
            repository.addSubtask(task.id, "Open doc")
            assertEquals(1, repository.listSubtasks(task.id).size)
        }

    @Test
    fun shiftRestOfDay_offsetsUnpinnedFutureTasks() =
        runTest {
            val today = LocalDate.of(2025, 11, 17)
            val now = System.currentTimeMillis()
            val pinned = repository.addQuickCapture("Pinned", today, TaskEntity.PLACEMENT_SCHEDULED, "09:00")
            repository.updateTaskFields(pinned.id) { it.copy(pinned = true) }
            val movable = repository.addQuickCapture("Move", today, TaskEntity.PLACEMENT_SCHEDULED, "10:00")
            repository.shiftRestOfDay(today, 15, fromMinutes = 9 * 60 + 30)
            assertEquals("09:00", repository.getTask(pinned.id)?.startLocalTime)
            assertEquals("10:15", repository.getTask(movable.id)?.startLocalTime)
        }

    @Test
    fun scheduleTask_moveToAnotherDay_resyncsReminder() =
        runTest {
            val today = LocalDate.of(2025, 11, 17)
            val tomorrow = today.plusDays(1)
            val task =
                repository.addQuickCapture(
                    title = "Move me",
                    date = today,
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    startLocalTime = "09:00",
                )
            coVerify { reminderSync.syncTask(task.id) }
            repository.scheduleTask(task.id, TaskEntity.PLACEMENT_SCHEDULED, tomorrow, "10:00")
            coVerify(atLeast = 2) { reminderSync.syncTask(task.id) }
            assertEquals(tomorrow.toString(), repository.getTask(task.id)?.date)
        }

    @Test
    fun deleteTask_softDeletes() =
        runTest {
            val today = LocalDate.of(2025, 11, 17)
            val task = repository.addQuickCapture("Temp", today, TaskEntity.PLACEMENT_ANYTIME)
            repository.deleteTask(task.id)
            assertEquals(null, repository.getTask(task.id))
            coVerify { reminderSync.cancelTask(task.id) }
        }

    @Test
    fun scheduleTask_sameDateTime_keepsReminderEnabledAndAlarm() =
        runTest {
            val date = LocalDate.now().plusDays(1)
            val (repo, _) = reminderAwareRepo(defaultReminders = false)
            val task =
                repo.addQuickCapture(
                    title = "Call",
                    date = date,
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    startLocalTime = "21:40",
                    reminderEnabled = true,
                )
            val before = database.reminderDao().getAll().single { it.taskId == task.id }
            assertEquals(true, repo.getTask(task.id)?.reminderEnabled)
            assertNotNull(pendingAlarm(task.id))
            repo.scheduleTask(task.id, TaskEntity.PLACEMENT_SCHEDULED, date, "21:40")
            assertEquals(true, repo.getTask(task.id)?.reminderEnabled)
            val after = database.reminderDao().getAll().single { it.taskId == task.id }
            assertEquals(before.id, after.id)
            assertEquals(before.fireAtEpochMillis, after.fireAtEpochMillis)
            assertEquals(app.ebbplanner.domain.reminders.ReminderKind.TaskStart.name, after.kind)
            assertNotNull(pendingAlarm(task.id))
        }

    @Test
    fun scheduleTask_unscheduledToScheduled_appliesDefaultOff() =
        runTest {
            val date = LocalDate.now().plusDays(1)
            val (repo, _) = reminderAwareRepo(defaultReminders = false)
            val task = repo.addQuickCapture("Note", date, TaskEntity.PLACEMENT_ANYTIME)
            repo.scheduleTask(task.id, TaskEntity.PLACEMENT_SCHEDULED, date, "10:00")
            assertEquals(false, repo.getTask(task.id)?.reminderEnabled)
            assertTrue(database.reminderDao().getAll().none { it.taskId == task.id })
        }

    @Test
    fun scheduleTask_unscheduledToScheduled_appliesDefaultOn() =
        runTest {
            val date = LocalDate.now().plusDays(1)
            val (repo, _) = reminderAwareRepo(defaultReminders = true)
            val task = repo.addQuickCapture("Note", date, TaskEntity.PLACEMENT_ANYTIME)
            repo.scheduleTask(task.id, TaskEntity.PLACEMENT_SCHEDULED, date, "10:00")
            assertEquals(true, repo.getTask(task.id)?.reminderEnabled)
            assertEquals(1, database.reminderDao().getAll().count { it.taskId == task.id })
            assertNotNull(pendingAlarm(task.id))
        }

    @Test
    fun scheduleTask_timeChange_keepsReminderAndReschedulesAlarm() =
        runTest {
            val date = LocalDate.now().plusDays(1)
            val (repo, _) = reminderAwareRepo(defaultReminders = false)
            val task =
                repo.addQuickCapture(
                    title = "Call",
                    date = date,
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    startLocalTime = "21:40",
                    reminderEnabled = true,
                )
            val before = database.reminderDao().getAll().single { it.taskId == task.id }
            repo.scheduleTask(task.id, TaskEntity.PLACEMENT_SCHEDULED, date, "22:10")
            assertEquals(true, repo.getTask(task.id)?.reminderEnabled)
            val after = database.reminderDao().getAll().single { it.taskId == task.id }
            assertEquals(app.ebbplanner.domain.reminders.ReminderKind.TaskStart.name, after.kind)
            assertTrue(after.fireAtEpochMillis > before.fireAtEpochMillis)
            assertNotNull(pendingAlarm(task.id))
        }

    @Test
    fun scheduleTask_alreadyScheduledReminderOff_staysOffOnTimeChange() =
        runTest {
            val date = LocalDate.now().plusDays(1)
            val (repo, _) = reminderAwareRepo(defaultReminders = true)
            val task =
                repo.addQuickCapture(
                    title = "Call",
                    date = date,
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    startLocalTime = "21:40",
                    reminderEnabled = false,
                )
            repo.scheduleTask(task.id, TaskEntity.PLACEMENT_SCHEDULED, date, "22:00")
            assertEquals(false, repo.getTask(task.id)?.reminderEnabled)
            assertTrue(database.reminderDao().getAll().none { it.taskId == task.id })
        }

    private fun reminderAwareRepo(defaultReminders: Boolean): Pair<TaskRepository, app.ebbplanner.reminders.ReminderScheduler> {
        val context: Context = ApplicationProvider.getApplicationContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val scheduler = app.ebbplanner.reminders.ReminderScheduler(context, alarmManager)
        val grant =
            mockk<app.ebbplanner.reminders.ExactAlarmGrant> {
                coEvery { isGranted() } returns true
            }
        val sync =
            app.ebbplanner.reminders.TaskReminderSync(
                database.taskDao(),
                database.reminderDao(),
                scheduler,
                grant,
            )
        val localPrefs = mockk<app.ebbplanner.data.preferences.UserPreferencesRepository>(relaxed = true)
        every { localPrefs.remindersDefault } returns kotlinx.coroutines.flow.flowOf(defaultReminders)
        val repo =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                sync,
                localPrefs,
                mockk(relaxed = true),
            )
        return repo to scheduler
    }

    private fun pendingAlarm(taskId: String): android.app.PendingIntent? {
        val context: Context = ApplicationProvider.getApplicationContext()
        val intent =
            android.content.Intent(context, app.ebbplanner.reminders.ReminderAlarmReceiver::class.java).apply {
                putExtra(app.ebbplanner.reminders.ReminderAlarmReceiver.EXTRA_TASK_ID, taskId)
            }
        return android.app.PendingIntent.getBroadcast(
            context,
            app.ebbplanner.reminders.ReminderScheduler.alarmRequestCode(taskId),
            intent,
            android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
