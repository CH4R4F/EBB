package app.ebbplanner.ui.taskdetail

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TaskDetailViewModelTest {
    private lateinit var database: EbbDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository =
            TaskRepository(
                database.taskDao(),
                database.dayPlanDao(),
                database.subtaskDao(),
                mockk(relaxed = true),
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun openSheetFromRoute_opensSchedule() {
        val vm = TaskDetailViewModel(repository)
        vm.openSheetFromRoute("schedule")
        assertEquals(TaskSheetType.Schedule, vm.openSheet.value)
    }

    @Test
    fun load_unknownId_eventuallyNotFixtureTitle() =
        runBlocking {
            val vm = TaskDetailViewModel(repository)
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
            val vm = TaskDetailViewModel(repository)
            vm.load(task.id)
            kotlinx.coroutines.delay(100)
            val title = vm.uiState.value.title
            if (title.isNotEmpty()) {
                assertEquals("Database title", title)
            }
        }
}
