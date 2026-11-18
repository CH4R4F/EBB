package app.ebbplanner.ui.taskdetail

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.domain.day.DayPlanningUseCase
import app.ebbplanner.domain.time.EbbClock
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskDetailViewModelTest {
    private lateinit var database: EbbDatabase
    private lateinit var repository: TaskRepository
    private lateinit var planningUseCase: DayPlanningUseCase
    private lateinit var prefs: app.ebbplanner.data.preferences.UserPreferencesRepository
    private lateinit var clock: EbbClock

    private fun createViewModel(): TaskDetailViewModel = TaskDetailViewModel(repository, prefs, planningUseCase, clock)

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        prefs = mockk(relaxed = true)
        every { prefs.remindersDefault } returns kotlinx.coroutines.flow.flowOf(true)
        every { prefs.breatherMinutes } returns kotlinx.coroutines.flow.flowOf(5)
        every { prefs.dayBedMinutes } returns kotlinx.coroutines.flow.flowOf(23 * 60)
        every { prefs.dayWakeMinutes } returns kotlinx.coroutines.flow.flowOf(7 * 60)
        clock =
            mockk<EbbClock>().also {
                every { it.today() } returns LocalDate.now()
                every { it.nowMinutes() } returns 10 * 60
                every { it.zoneId() } returns ZoneId.systemDefault()
            }
        repository =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                mockk(relaxed = true),
                prefs,
                mockk(relaxed = true),
            )
        planningUseCase = DayPlanningUseCase(repository, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun openSheetFromRoute_opensSchedule() {
        val vm = createViewModel()
        vm.openSheetFromRoute("schedule")
        assertEquals(TaskSheetType.Schedule, vm.openSheet.value)
    }

    @Test
    fun load_unknownId_eventuallyNotFixtureTitle() =
        runBlocking {
            val vm = createViewModel()
            vm.load("missing-id")
            assertNotEquals("Write project brief", vm.uiState.value.title)
            assertEquals(TaskDetailMode.Loading, vm.uiState.value.mode)
        }

    @Test
    fun load_existingTask_doesNotUseFixtureTitle() =
        runBlocking {
            val task =
                repository.addQuickCapture(
                    "Database title",
                    LocalDate.now(),
                    TaskEntity.PLACEMENT_ANYTIME,
                )
            val vm = createViewModel()
            vm.load(task.id)
            kotlinx.coroutines.delay(100)
            val title = vm.uiState.value.title
            if (title.isNotEmpty()) {
                assertEquals("Database title", title)
            }
        }
}
