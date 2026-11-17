package app.ebbplanner.reminders

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.reminders.ReminderKind
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ReminderRescheduleUseCaseTest {
    private lateinit var database: EbbDatabase
    private lateinit var taskRepository: TaskRepository
    private lateinit var reminderSync: TaskReminderSync
    private lateinit var useCase: ReminderRescheduleUseCase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        val scheduler = mockk<ReminderScheduler>(relaxed = true)
        val alarmGrant = mockk<ExactAlarmGrant> { coEvery { isGranted() } returns true }
        reminderSync = TaskReminderSync(database.taskDao(), database.reminderDao(), scheduler, alarmGrant)
        val prefs = mockk<app.ebbplanner.data.preferences.UserPreferencesRepository>(relaxed = true)
        io.mockk.every { prefs.remindersDefault } returns kotlinx.coroutines.flow.flowOf(true)
        taskRepository =
            TaskRepository(database.taskDao(), database.dayPlanDao(), database.subtaskDao(), reminderSync, prefs)
        useCase = ReminderRescheduleUseCase(context, reminderSync, taskRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun snooze_persistsReminderRow() =
        runTest {
            val today = LocalDate.now().toString()
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            database.taskDao().insert(
                TaskEntity(
                    id = id,
                    title = "Call",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = today,
                    startLocalTime = "14:00",
                    durationMin = 15,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            useCase.snoozeTask(id, "Call")
            val saved = database.reminderDao().getAll()
            assertEquals(1, saved.size)
            assertEquals(ReminderKind.Snooze.name, saved.first().kind)
            assertTrue(saved.first().fireAtEpochMillis > now)
        }

    @Test
    fun shiftDay_shiftsRemainingTasks() =
        runTest {
            val today = LocalDate.now()
            val now = System.currentTimeMillis()
            val pinnedId = UUID.randomUUID().toString()
            val movableId = UUID.randomUUID().toString()
            database.taskDao().insertAll(
                listOf(
                    TaskEntity(
                        id = pinnedId,
                        title = "Pinned",
                        placement = TaskEntity.PLACEMENT_SCHEDULED,
                        date = today.toString(),
                        startLocalTime = "15:00",
                        pinned = true,
                        createdAt = now,
                        updatedAt = now,
                    ),
                    TaskEntity(
                        id = movableId,
                        title = "Later",
                        placement = TaskEntity.PLACEMENT_SCHEDULED,
                        date = today.toString(),
                        startLocalTime = "15:30",
                        createdAt = now,
                        updatedAt = now,
                    ),
                ),
            )
            useCase.shiftDayFromNotification(movableId)
            assertEquals("15:00", database.taskDao().getById(pinnedId)?.startLocalTime)
            assertEquals("15:45", database.taskDao().getById(movableId)?.startLocalTime)
        }
}
