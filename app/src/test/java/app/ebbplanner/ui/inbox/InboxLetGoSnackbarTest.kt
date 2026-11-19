package app.ebbplanner.ui.inbox

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.MainDispatcherRule
import app.ebbplanner.data.local.EbbDatabase
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.data.repository.TaskRepository
import app.ebbplanner.ui.theme.EbbTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w360dp-h640dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class InboxLetGoSnackbarTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeRule = createComposeRule()

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
    fun letGo_snackbarUndo_restoresSelection() {
        val first =
            kotlinx.coroutines.runBlocking {
                repository.addQuickCapture("Stamps", LocalDate.now(), TaskEntity.PLACEMENT_INBOX)
            }
        val second =
            kotlinx.coroutines.runBlocking {
                repository.addQuickCapture("Dentist", LocalDate.now(), TaskEntity.PLACEMENT_INBOX)
            }
        val vm =
            InboxViewModel(
                ApplicationProvider.getApplicationContext(),
                repository,
                prefs,
            )
        composeRule.setContent {
            EbbTheme {
                InboxScreen(viewModel = vm)
            }
        }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag(inboxTaskTestTag(first.id), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        vm.enterMultiselect(first.id)
        vm.toggleSelected(second.id)
        composeRule.waitForIdle()
        composeRule
            .onNodeWithTag(INBOX_MULTISELECT_OVERFLOW_TAG, useUnmergedTree = true)
            .performTouchInput { click(center) }
        composeRule.waitForIdle()
        composeRule
            .onNodeWithTag(INBOX_MULTISELECT_LET_GO_TAG, useUnmergedTree = true)
            .performTouchInput { click(center) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Let go 2 tasks?").assertIsDisplayed()
        composeRule
            .onNodeWithTag(app.ebbplanner.ui.components.EBB_SHEET_CONFIRM_TAG)
            .performTouchInput { click(center) }
        composeRule.waitForIdle()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Let go 2 tasks", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Undo").assertIsDisplayed().performTouchInput { click(center) }
        composeRule.waitUntil(5_000) {
            kotlinx.coroutines.runBlocking {
                repository.getTask(first.id) != null && repository.getTask(second.id) != null
            }
        }
        composeRule.runOnIdle {
            kotlinx.coroutines.runBlocking {
                assertNotNull(repository.getTask(first.id))
                assertNotNull(repository.getTask(second.id))
                assertEquals("Stamps", repository.getTask(first.id)?.title)
                assertEquals("Dentist", repository.getTask(second.id)?.title)
            }
        }
    }

    @Test
    fun letGoSnackbar_longDuration_autoDismisses() {
        val host = SnackbarHostState()
        composeRule.setContent {
            EbbTheme {
                SnackbarHost(hostState = host)
            }
            LaunchedEffect(Unit) {
                host.showLetGoSnackbar("Let go 1 task", "Undo")
            }
        }
        composeRule.waitForIdle()
        assertEquals(SnackbarDuration.Long, host.currentSnackbarData!!.visuals.duration)
        composeRule.onNodeWithText("Let go 1 task").assertIsDisplayed()
        composeRule.mainClock.autoAdvance = true
        composeRule.mainClock.advanceTimeBy(10_500)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Let go 1 task").assertDoesNotExist()
        assertEquals(null, host.currentSnackbarData)
    }
}
