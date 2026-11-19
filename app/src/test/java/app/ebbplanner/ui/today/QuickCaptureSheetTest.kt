@file:Suppress("MaxLineLength")

package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.domain.capture.QuickCaptureResolved
import app.ebbplanner.domain.capture.resolveQuickCapture
import app.ebbplanner.ui.WithFontScale
import app.ebbplanner.ui.components.EBB_SHEET_CANCEL_LABEL_TAG
import app.ebbplanner.ui.components.EBB_SHEET_CONFIRM_LABEL_TAG
import app.ebbplanner.ui.text.LocalTextOverflowReports
import app.ebbplanner.ui.theme.EbbTheme
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class QuickCaptureSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate.of(2026, 11, 19)

    @Test
    fun quickCapture_hasSingleFieldAndSaveButton() {
        composeRule.setContent {
            EbbTheme {
                QuickCaptureSheetContent(
                    state = QuickCapturePresentationState(mode = QuickCaptureMode.Empty, destinationLabel = "Anytime (no time set)"),
                    showDragHandle = false,
                    interactive = true,
                    text = "",
                )
            }
        }
        composeRule.onAllNodesWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG).assertCountEquals(1)
        composeRule.onAllNodesWithTag(QUICK_CAPTURE_ADD_BUTTON_TAG).assertCountEquals(1)
    }

    @Test
    fun save_disabledWhenEmpty() {
        composeRule.setContent {
            EbbTheme {
                QuickCaptureEditor(
                    onSave = {},
                    onCancel = {},
                    defaultDate = today,
                    nowMinutes = 10 * 60,
                )
            }
        }
        composeRule.onNodeWithTag(QUICK_CAPTURE_ADD_BUTTON_TAG).assertIsNotEnabled()
        composeRule.onNodeWithText("Anytime (no time set)").assertIsDisplayed()
    }

    @Test
    fun parsedText_updatesPreviewAndDestination() {
        mountEditor()
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym 18:00 45m")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(QUICK_CAPTURE_PREVIEW_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Today 18:00 · 45 min", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag(QUICK_CAPTURE_DESTINATION_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Timeline, today 18:00").assertIsDisplayed()
    }

    @Test
    fun settingTime_viaNow_routesToTimeline() {
        val captured = mutableListOf<QuickCaptureResolved>()
        mountEditor(nowMinutes = 18 * 60, onResolved = { captured += it })
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Anytime (no time set)").assertIsDisplayed()
        tapTag(QUICK_CAPTURE_NOW_TAG)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Timeline, today 18:00").assertIsDisplayed()
        tapTag(QUICK_CAPTURE_ADD_BUTTON_TAG)
        composeRule.waitForIdle()
        assertEquals(TaskEntity.PLACEMENT_SCHEDULED, captured.single().placement)
        assertEquals("18:00", captured.single().startLocalTime)
    }

    @Test
    fun noTime_routesToAnytime() {
        val captured = mutableListOf<QuickCaptureResolved>()
        mountEditor(onResolved = { captured += it })
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Anytime (no time set)").assertIsDisplayed()
        tapTag(QUICK_CAPTURE_ADD_BUTTON_TAG)
        composeRule.waitForIdle()
        assertEquals(TaskEntity.PLACEMENT_ANYTIME, captured.single().placement)
    }

    @Test
    fun override_beatsParsedTime() {
        val captured = mutableListOf<QuickCaptureResolved>()
        mountEditor(nowMinutes = 9 * 60 + 30, onResolved = { captured += it })
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym 18:00")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Timeline, today 18:00").assertIsDisplayed()
        tapTag(QUICK_CAPTURE_CLEAR_TIME_TAG)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Anytime (no time set)").assertIsDisplayed()
        tapTag(QUICK_CAPTURE_NOW_TAG)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Timeline, today 09:30").assertIsDisplayed()
        tapTag(QUICK_CAPTURE_ADD_BUTTON_TAG)
        composeRule.waitForIdle()
        assertEquals("09:30", captured.single().startLocalTime)
        assertEquals(TaskEntity.PLACEMENT_SCHEDULED, captured.single().placement)
    }

    @Test
    fun durationOverride_beatsParsed() {
        val captured = mutableListOf<QuickCaptureResolved>()
        mountEditor(onResolved = { captured += it })
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym 18:00 45m")
        composeRule.waitForIdle()
        tapTag(quickCaptureDurationTag(90))
        tapTag(QUICK_CAPTURE_ADD_BUTTON_TAG)
        composeRule.waitForIdle()
        assertEquals(90, captured.single().durationMin)
        assertEquals("18:00", captured.single().startLocalTime)
    }

    @Test
    fun destinationLine_inbox() {
        mountEditor(saveTarget = QuickCaptureSaveTarget.Inbox)
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("note")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Inbox").assertIsDisplayed()
    }

    @Test
    fun snackbarShow_invokesCallback() {
        var shown = false
        composeRule.setContent {
            val host = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            var visible by remember { mutableStateOf(true) }
            EbbTheme {
                Box(Modifier.fillMaxSize()) {
                    if (visible) {
                        QuickCaptureEditor(
                            defaultDate = today,
                            nowMinutes = 10 * 60,
                            onSave = { _ ->
                                scope.launch {
                                    val result = host.showSnackbar("Saved to Anytime", "Show")
                                    if (result == SnackbarResult.ActionPerformed) shown = true
                                }
                            },
                            onCancel = { visible = false },
                        )
                    }
                    SnackbarHost(host, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym")
        tapTag(QUICK_CAPTURE_ADD_BUTTON_TAG)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Show").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Show").performTouchInput {
            click(Offset(width / 2f, height / 2f))
        }
        composeRule.waitForIdle()
        assertTrue(shown)
    }

    @Test
    fun actionLabels_noOverflowAtFontScale100() = assertActionLabelsDoNotOverflow(1f)

    @Test
    fun actionLabels_noOverflowAtFontScale130() = assertActionLabelsDoNotOverflow(1.3f)

    @Test
    fun actionLabels_noOverflowAtFontScale200() = assertActionLabelsDoNotOverflow(2f)

    private fun assertActionLabelsDoNotOverflow(scale: Float) {
        val overflow = mutableMapOf<String, Boolean>()
        composeRule.setContent {
            CompositionLocalProvider(LocalTextOverflowReports provides overflow) {
                WithFontScale(scale) {
                    EbbTheme {
                        QuickCaptureSheetContent(
                            state =
                                QuickCapturePresentationState(
                                    destinationLabel = "Anytime (no time set)",
                                    showTodayShortcuts = true,
                                    saveEnabled = true,
                                    text = "gym",
                                ),
                            showDragHandle = false,
                            interactive = true,
                            text = "gym",
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        assertFalse("confirm overflow at f$scale", overflow[EBB_SHEET_CONFIRM_LABEL_TAG] == true)
        assertFalse("cancel overflow at f$scale", overflow[EBB_SHEET_CANCEL_LABEL_TAG] == true)
    }

    private fun tapTag(tag: String) {
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).performTouchInput {
            click(Offset(width / 2f, height / 2f))
        }
    }

    private fun mountEditor(
        saveTarget: QuickCaptureSaveTarget = QuickCaptureSaveTarget.Today,
        nowMinutes: Int = 10 * 60,
        onResolved: (QuickCaptureResolved) -> Unit = {},
    ) {
        composeRule.setContent {
            EbbTheme {
                QuickCaptureEditor(
                    defaultDate = today,
                    saveTarget = saveTarget,
                    nowMinutes = nowMinutes,
                    nextFreeSlotMinutes = nowMinutes + 30,
                    onSave = { draft ->
                        onResolved(resolveQuickCapture(draft, today, saveTarget))
                    },
                    onCancel = {},
                )
            }
        }
        composeRule.waitForIdle()
    }
}
