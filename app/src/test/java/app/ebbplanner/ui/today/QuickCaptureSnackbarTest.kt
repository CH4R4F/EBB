@file:Suppress("MaxLineLength")

package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.DpRect
import app.ebbplanner.ui.theme.EbbTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class QuickCaptureSnackbarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun savedSnackbar_usesLongDurationNotIndefinite() =
        runTest {
            val host = SnackbarHostState()
            val job =
                launch {
                    host.showQuickCaptureSavedSnackbar("Saved to Anytime", "Show")
                }
            testScheduler.runCurrent()
            assertEquals(SnackbarDuration.Long, host.currentSnackbarData!!.visuals.duration)
            job.cancel()
        }

    @Test
    fun rapidSaves_replaceCurrentSnackbar() =
        runTest {
            val host = SnackbarHostState()
            val job1 =
                launch {
                    host.showQuickCaptureSavedSnackbar("one", "Show")
                }
            testScheduler.runCurrent()
            assertEquals("one", host.currentSnackbarData!!.visuals.message)
            val job2 =
                launch {
                    host.showQuickCaptureSavedSnackbar("two", "Show")
                }
            testScheduler.runCurrent()
            assertEquals("two", host.currentSnackbarData!!.visuals.message)
            job1.cancel()
            job2.cancel()
        }

    @Test
    fun saveNotifier_coalescesCountMessage() {
        val resources =
            androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>().resources
        val notifier = QuickCaptureSaveNotifier()
        notifier.record("a")
        assertEquals("Saved to Anytime", notifier.message(resources, "Saved to Anytime"))
        notifier.record("b")
        notifier.record("c")
        assertEquals("Saved 3 tasks", notifier.message(resources, "Saved to Anytime"))
        assertEquals("c", notifier.lastTaskId)
    }

    @Test
    fun savedSnackbar_dismissesOnItsOwn() {
        composeRule.mainClock.autoAdvance = false
        val hostState = SnackbarHostState()
        composeRule.setContent {
            val scope = rememberCoroutineScope()
            EbbTheme {
                TodayScreenDesign(
                    state = TodayPresentationState(showFab = true, visualMode = TodayVisualMode.Empty),
                    modifier = Modifier.fillMaxSize(),
                    showBottomNav = false,
                    snackbarHostState = hostState,
                    onFabClick = {
                        scope.launch {
                            hostState.showQuickCaptureSavedSnackbar("Saved to Anytime", "Show")
                        }
                    },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(QUICK_CAPTURE_FAB_TAG).performTouchInput {
            click(Offset(width / 2f, height / 2f))
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Show").assertIsDisplayed()
        composeRule.mainClock.advanceTimeBy(10_500)
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithText("Show").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun snackbar_doesNotOverlapFab_andFabOpensNewCapture() {
        composeRule.mainClock.autoAdvance = false
        var captureOpens = 0
        var openedTaskId: String? = null
        composeRule.setContent {
            val hostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            var showCapture by remember { mutableStateOf(false) }
            EbbTheme {
                TodayScreenDesign(
                    state = TodayPresentationState(showFab = true, visualMode = TodayVisualMode.Empty),
                    modifier = Modifier.fillMaxSize(),
                    showBottomNav = false,
                    snackbarHostState = hostState,
                    onFabClick = {
                        captureOpens++
                        showCapture = true
                    },
                )
                if (showCapture) {
                    QuickCaptureEditor(
                        defaultDate = LocalDate.of(2026, 11, 19),
                        onSave = {
                            showCapture = false
                            scope.launch {
                                val result =
                                    hostState.showQuickCaptureSavedSnackbar("Saved to Anytime", "Show")
                                if (result == SnackbarResult.ActionPerformed) {
                                    openedTaskId = "previous"
                                }
                            }
                        },
                        onCancel = { showCapture = false },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(QUICK_CAPTURE_FAB_TAG).performTouchInput {
            click(Offset(width / 2f, height / 2f))
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym")
        composeRule.onNodeWithTag(QUICK_CAPTURE_ADD_BUTTON_TAG).performTouchInput {
            click(Offset(width / 2f, height / 2f))
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Show").assertIsDisplayed()
        val fab = composeRule.onNodeWithTag(QUICK_CAPTURE_FAB_TAG).getBoundsInRoot()
        val show = composeRule.onNodeWithText("Show").getBoundsInRoot()
        assertFalse("FAB must not intersect snackbar Show action", fab.intersects(show))
        val snackbar = composeRule.onNodeWithTag(QUICK_CAPTURE_SNACKBAR_HOST_TAG).getBoundsInRoot()
        assertFalse("FAB must not intersect snackbar host", fab.intersects(snackbar))
        val fabPx = composeRule.onNodeWithTag(QUICK_CAPTURE_FAB_TAG).fetchSemanticsNode().boundsInRoot
        composeRule.onRoot().performTouchInput {
            click(Offset(fabPx.center.x, fabPx.center.y))
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        assertEquals(2, captureOpens)
        assertNull(openedTaskId)
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    private fun DpRect.intersects(other: DpRect): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top
}
