package app.ebbplanner.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.reminders.TaskReminderSync
import io.mockk.coEvery
import io.mockk.coVerify
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
        repository = TaskRepository(database.taskDao(), database.dayPlanDao(), reminderSync)
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
    fun deleteTask_softDeletes() =
        runTest {
            val today = LocalDate.of(2025, 11, 17)
            val task = repository.addQuickCapture("Temp", today, TaskEntity.PLACEMENT_ANYTIME)
            repository.deleteTask(task.id)
            assertEquals(null, repository.getTask(task.id))
        }
}
