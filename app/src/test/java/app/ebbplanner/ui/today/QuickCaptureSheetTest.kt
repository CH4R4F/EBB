@file:Suppress("MaxLineLength")

package app.ebbplanner.ui.today

import android.provider.Settings
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
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
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
@Config(sdk = [28], qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class QuickCaptureSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate.now()

    @Before
    fun use24HourClock() {
        Settings.System.putString(
            ApplicationProvider.getApplicationContext<android.content.Context>().contentResolver,
            Settings.System.TIME_12_24,
            "24",
        )
    }

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
        composeRule.onNodeWithText("Save").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("Add").fetchSemanticsNodes().isEmpty())
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

    @Test
    fun imeEnter_savesClearsAndKeepsSheetOpen() {
        val captured = mutableListOf<QuickCaptureResolved>()
        mountEditor(onResolved = { captured += it })
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym")
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performImeAction()
        composeRule.waitForIdle()
        assertEquals(1, captured.size)
        assertEquals("gym", captured.single().title)
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).assertTextEquals("")
        composeRule.onNodeWithTag(QUICK_CAPTURE_ADD_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun nextFreeSlot_recomputesWhenDurationChanges() {
        mountEditor(
            nowMinutes = 10 * 60,
            occupied =
                listOf(
                    10 * 60 to 60,
                    11 * 60 + 15 to 30,
                    12 * 60 + 20 to 30,
                ),
        )
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym")
        tapTag(QUICK_CAPTURE_NEXT_FREE_TAG)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Timeline, today 11:00").assertIsDisplayed()
        tapTag(quickCaptureDurationTag(45))
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Timeline, today 12:50").assertIsDisplayed()
    }

    @Test
    fun noFreeSlot_showsDisabledChipAndTomorrowOption() {
        val captured = mutableListOf<QuickCaptureResolved>()
        mountEditor(
            nowMinutes = 22 * 60,
            occupied = listOf(22 * 60 to 60),
            occupiedTomorrow = listOf(7 * 60 to 60),
            windowStart = 7 * 60,
            windowEnd = 23 * 60,
            onResolved = { captured += it },
        )
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym")
        tapTag(quickCaptureDurationTag(90))
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No free slot today").assertIsDisplayed()
        composeRule.onNodeWithTag(QUICK_CAPTURE_NEXT_FREE_TAG, useUnmergedTree = true).assertIsNotEnabled()
        composeRule.onNodeWithText("Tomorrow, 08:00").assertIsDisplayed()
        tapTag(QUICK_CAPTURE_TOMORROW_FREE_TAG)
        composeRule.waitForIdle()
        tapTag(QUICK_CAPTURE_ADD_BUTTON_TAG)
        composeRule.waitForIdle()
        assertEquals(today.plusDays(1), captured.single().date)
        assertEquals("08:00", captured.single().startLocalTime)
        assertEquals(TaskEntity.PLACEMENT_SCHEDULED, captured.single().placement)
    }

    @Test
    fun tomorrowChip_hiddenWhenTomorrowHasNoFit() {
        mountEditor(
            nowMinutes = 22 * 60,
            occupied = listOf(22 * 60 to 60),
            occupiedTomorrow = listOf(7 * 60 to 16 * 60),
            windowStart = 7 * 60,
            windowEnd = 23 * 60,
        )
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym")
        tapTag(quickCaptureDurationTag(90))
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No free slot today").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag(QUICK_CAPTURE_TOMORROW_FREE_TAG).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun nextFree_durationPastWindow_clearsSlotAndShowsTomorrow() {
        mountEditor(
            nowMinutes = 23 * 60,
            occupied = emptyList(),
            occupiedTomorrow = emptyList(),
            windowStart = 7 * 60,
            windowEnd = 24 * 60,
        )
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym")
        tapTag(QUICK_CAPTURE_NEXT_FREE_TAG)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Timeline, today 23:00").assertIsDisplayed()
        tapTag(quickCaptureDurationTag(90))
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No free slot today").assertIsDisplayed()
        composeRule.onNodeWithText("Tomorrow, 07:00").assertIsDisplayed()
    }

    @Test
    fun destination_12Hour_usesUnpaddedClock() {
        Settings.System.putString(
            ApplicationProvider.getApplicationContext<android.content.Context>().contentResolver,
            Settings.System.TIME_12_24,
            "12",
        )
        mountEditor()
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym 18:00")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Timeline, today 6:00 PM").assertIsDisplayed()
    }

    @Test
    fun nonTodayDate_usedForDestinationAndSave() {
        val viewed = LocalDate.of(2027, 3, 15)
        val captured = mutableListOf<QuickCaptureResolved>()
        mountEditor(defaultDate = viewed, onResolved = { captured += it })
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG, useUnmergedTree = true).performTextInput("gym 18:00")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Timeline, 15 Mar 18:00").assertIsDisplayed()
        tapTag(QUICK_CAPTURE_ADD_BUTTON_TAG)
        composeRule.waitForIdle()
        assertEquals(viewed, captured.single().date)
        assertEquals("18:00", captured.single().startLocalTime)
    }

    private fun mountEditor(
        saveTarget: QuickCaptureSaveTarget = QuickCaptureSaveTarget.Today,
        nowMinutes: Int = 10 * 60,
        defaultDate: LocalDate = today,
        occupied: List<Pair<Int, Int>> = emptyList(),
        occupiedTomorrow: List<Pair<Int, Int>> = emptyList(),
        windowStart: Int = 0,
        windowEnd: Int = 24 * 60,
        onResolved: (QuickCaptureResolved) -> Unit = {},
    ) {
        composeRule.setContent {
            EbbTheme {
                QuickCaptureEditor(
                    defaultDate = defaultDate,
                    saveTarget = saveTarget,
                    nowMinutes = nowMinutes,
                    occupied = occupied,
                    occupiedTomorrow = occupiedTomorrow,
                    windowStart = windowStart,
                    windowEnd = windowEnd,
                    onSave = { draft ->
                        onResolved(resolveQuickCapture(draft, defaultDate, saveTarget))
                    },
                    onCancel = {},
                )
            }
        }
        composeRule.waitForIdle()
    }
}
