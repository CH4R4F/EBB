package app.ebbplanner.ui.inbox

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.MainDispatcherRule
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class InboxViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: EbbDatabase
    private lateinit var repository: TaskRepository
    private lateinit var prefs: app.ebbplanner.data.preferences.UserPreferencesRepository

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database =
            Room.inMemoryDatabaseBuilder(context, EbbDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        prefs = mockk(relaxed = true)
        every { prefs.remindersDefault } returns kotlinx.coroutines.flow.flowOf(false)
        val reminderSync = mockk<app.ebbplanner.reminders.TaskReminderSync>(relaxed = true)
        coEvery { reminderSync.syncTask(any()) } returns Unit
        coEvery { reminderSync.cancelTask(any()) } returns Unit
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
    fun longPressThenToday_movesTaskOutOfInbox() =
        runBlocking {
            val task =
                repository.addQuickCapture(
                    "Buy stamps",
                    LocalDate.now(),
                    TaskEntity.PLACEMENT_INBOX,
                )
            val vm =
                InboxViewModel(
                    ApplicationProvider.getApplicationContext(),
                    repository,
                    prefs,
                )
            vm.enterMultiselect(task.id)
            val selected = vm.presentation.first { it.visualMode == InboxVisualMode.Multiselect }
            assertEquals(1, selected.tasks.count { it.selected })
            vm.moveSelectedToToday()
            kotlinx.coroutines.delay(200)
            assertEquals(TaskEntity.PLACEMENT_ANYTIME, repository.getTask(task.id)?.placement)
            assertEquals(LocalDate.now().toString(), repository.getTask(task.id)?.date)
        }

    @Test
    fun clearMultiselect_exitsMode() =
        runBlocking {
            val task =
                repository.addQuickCapture(
                    "Note",
                    LocalDate.now(),
                    TaskEntity.PLACEMENT_INBOX,
                )
            val vm =
                InboxViewModel(
                    ApplicationProvider.getApplicationContext(),
                    repository,
                    prefs,
                )
            vm.enterMultiselect(task.id)
            assertEquals(
                InboxVisualMode.Multiselect,
                vm.presentation.first { it.visualMode == InboxVisualMode.Multiselect }.visualMode,
            )
            vm.clearMultiselect()
            assertEquals(
                InboxVisualMode.Normal,
                vm.presentation.first { it.visualMode == InboxVisualMode.Normal }.visualMode,
            )
        }

    @Test
    fun letGoThenUndo_restoresExactlyThoseTasks() =
        runBlocking {
            val keep =
                repository.addQuickCapture(
                    "Keep me",
                    LocalDate.now(),
                    TaskEntity.PLACEMENT_INBOX,
                )
            val gone =
                repository.addQuickCapture(
                    "Let me go",
                    LocalDate.now(),
                    TaskEntity.PLACEMENT_INBOX,
                )
            val extra =
                repository.addQuickCapture(
                    "Also go",
                    LocalDate.now(),
                    TaskEntity.PLACEMENT_INBOX,
                )
            val vm =
                InboxViewModel(
                    ApplicationProvider.getApplicationContext(),
                    repository,
                    prefs,
                )
            vm.enterMultiselect(gone.id)
            vm.toggleSelected(extra.id)
            vm.letGoSelected()
            kotlinx.coroutines.delay(200)
            assertEquals(null, repository.getTask(gone.id))
            assertEquals(null, repository.getTask(extra.id))
            assertEquals("Keep me", repository.getTask(keep.id)?.title)
            vm.undoLetGo(listOf(gone.id, extra.id))
            kotlinx.coroutines.delay(200)
            assertEquals("Let me go", repository.getTask(gone.id)?.title)
            assertEquals("Also go", repository.getTask(extra.id)?.title)
            assertEquals("Keep me", repository.getTask(keep.id)?.title)
        }
}
