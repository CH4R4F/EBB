package app.ebbplanner.ui.today

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import app.ebbplanner.MainDispatcherRule
import app.ebbplanner.data.calendar.CalendarEventsReader
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TodayViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: EbbDatabase
    private lateinit var taskRepository: TaskRepository
    private lateinit var preferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        taskRepository = TaskRepository(database.taskDao(), database.dayPlanDao())
        preferencesRepository = UserPreferencesRepository(context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun uiState_groupsScheduledAndAnytimeTasks() =
        runTest {
            preferencesRepository.setSeedSampleTasks(false)
            val today = LocalDate.now()
            val now = System.currentTimeMillis()
            database.taskDao().insert(
                TaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Email",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = today.toString(),
                    startLocalTime = "11:00",
                    durationMin = 20,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            database.taskDao().insert(
                TaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Stretch",
                    placement = TaskEntity.PLACEMENT_ANYTIME,
                    date = today.toString(),
                    createdAt = now,
                    updatedAt = now,
                ),
            )

            val context: Context = ApplicationProvider.getApplicationContext()
            val viewModel =
                TodayViewModel(
                    taskRepository,
                    preferencesRepository,
                    CalendarEventsReader(context),
                )
            advanceUntilIdle()
            viewModel.uiState.test {
                var state = awaitItem()
                while (state.scheduledTasks.isEmpty()) {
                    state = awaitItem()
                }
                assertTrue(state.scheduledTasks.any { it.title == "Email" })
                assertTrue(state.anytimeTasks.any { it.title == "Stretch" })
                assertEquals(20, state.minutesPlanned)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
