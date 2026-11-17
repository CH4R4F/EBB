package app.ebbplanner.ui.daytools

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.MainDispatcherRule
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.day.DayPlanningUseCase
import app.ebbplanner.domain.time.MutableEbbClock
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PlanDayViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var database: EbbDatabase
    private lateinit var repository: TaskRepository
    private lateinit var prefs: UserPreferencesRepository
    private lateinit var clock: MutableEbbClock
    private lateinit var useCase: DayPlanningUseCase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        prefs = UserPreferencesRepository(context)
        repository =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                mockk(relaxed = true),
                prefs,
            )
        clock = MutableEbbClock(Instant.parse("2026-11-17T10:00:00Z"))
        useCase = DayPlanningUseCase(repository, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun emptySelection_placesNothing() =
        runTest {
            val now = System.currentTimeMillis()
            database.taskDao().insert(
                TaskEntity(
                    id = "inbox1",
                    title = "Inbox",
                    placement = TaskEntity.PLACEMENT_INBOX,
                    durationMin = 15,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            val vm = PlanDayViewModel(repository, useCase, prefs, clock)
            advanceUntilIdle()
            assertFalse(vm.uiState.value.tasks.first().selected)
            vm.applyPlan {}
            advanceUntilIdle()
            assertEquals(TaskEntity.PLACEMENT_INBOX, repository.getTask("inbox1")?.placement)
        }
}
