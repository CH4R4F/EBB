@file:Suppress("LongMethod", "MagicNumber")

package app.ebbplanner.ui.inbox

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import app.ebbplanner.ui.WithFontScale
import app.ebbplanner.ui.components.EBB_SHEET_CANCEL_TAG
import app.ebbplanner.ui.components.EBB_SHEET_CONFIRM_TAG
import app.ebbplanner.ui.text.LocalTextOverflowReports
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w360dp-h640dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class InboxMultiselectBarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun actionBar_f100_360dp_minTouchNoClipAndTaps() = assertActionBar(1f)

    @Test
    fun actionBar_f130_360dp_minTouchNoClipAndTaps() = assertActionBar(1.3f)

    @Test
    fun actionBar_f200_360dp_minTouchNoClipAndTaps() = assertActionBar(2f)

    @Test
    fun letGo_oneSelected_noConfirm() {
        var letGo = false
        composeRule.setContent {
            EbbTheme {
                InboxScreenDesign(
                    state = inboxMultiselectOne(),
                    modifier = Modifier.fillMaxSize(),
                    onLetGo = { letGo = true },
                )
            }
        }
        openOverflow()
        tap(INBOX_MULTISELECT_LET_GO_TAG)
        composeRule.onNodeWithTag(INBOX_LET_GO_CONFIRM_TAG).assertDoesNotExist()
        assertTrue(letGo)
    }

    @Test
    fun letGo_twoSelected_confirmsThenRuns() {
        var letGo = false
        composeRule.setContent {
            EbbTheme {
                InboxScreenDesign(
                    state = inboxMultiselect(),
                    modifier = Modifier.fillMaxSize(),
                    onLetGo = { letGo = true },
                )
            }
        }
        openOverflow()
        tap(INBOX_MULTISELECT_LET_GO_TAG)
        composeRule.onNodeWithTag(INBOX_LET_GO_CONFIRM_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Let go 2 tasks?").assertIsDisplayed()
        assertFalse(letGo)
        tap(EBB_SHEET_CONFIRM_TAG)
        assertTrue(letGo)
    }

    @Test
    fun letGo_twoSelected_cancelLeavesTasks() {
        var letGo = false
        composeRule.setContent {
            EbbTheme {
                InboxScreenDesign(
                    state = inboxMultiselect(),
                    modifier = Modifier.fillMaxSize(),
                    onLetGo = { letGo = true },
                )
            }
        }
        openOverflow()
        tap(INBOX_MULTISELECT_LET_GO_TAG)
        tap(EBB_SHEET_CANCEL_TAG)
        composeRule.onNodeWithTag(INBOX_LET_GO_CONFIRM_TAG).assertDoesNotExist()
        assertFalse(letGo)
    }

    @Test
    fun systemBack_exitsMultiselect() {
        var done = false
        var dispatcher: androidx.activity.OnBackPressedDispatcher? = null
        composeRule.setContent {
            dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
            EbbTheme {
                InboxScreenDesign(
                    state = inboxMultiselect(),
                    modifier = Modifier.fillMaxSize(),
                    onMultiselectDone = { done = true },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { dispatcher!!.onBackPressed() }
        composeRule.waitForIdle()
        assertTrue(done)
    }

    private fun assertActionBar(fontScale: Float) {
        val overflow = mutableMapOf<String, Boolean>()
        var today = false
        var tomorrow = false
        var estimate = false
        var energy = false
        var letGo = false
        composeRule.setContent {
            CompositionLocalProvider(LocalTextOverflowReports provides overflow) {
                WithFontScale(fontScale) {
                    EbbTheme {
                        InboxScreenDesign(
                            state = inboxMultiselectOne(),
                            modifier = Modifier.fillMaxSize(),
                            onMoveToToday = { today = true },
                            onMoveToTomorrow = { tomorrow = true },
                            onSetDuration = { estimate = true },
                            onSetEnergy = { energy = true },
                            onLetGo = { letGo = true },
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        val onBar =
            if (fontScale >= 1.3f) {
                listOf(
                    INBOX_MULTISELECT_TODAY_TAG,
                    INBOX_MULTISELECT_TOMORROW_TAG,
                    INBOX_MULTISELECT_OVERFLOW_TAG,
                )
            } else {
                listOf(
                    INBOX_MULTISELECT_TODAY_TAG,
                    INBOX_MULTISELECT_TOMORROW_TAG,
                    INBOX_MULTISELECT_ESTIMATE_TAG,
                    INBOX_MULTISELECT_ENERGY_TAG,
                    INBOX_MULTISELECT_OVERFLOW_TAG,
                )
            }
        onBar.forEach { tag -> assertActionFits(tag, overflow, fontScale) }
        tap(INBOX_MULTISELECT_TODAY_TAG)
        tap(INBOX_MULTISELECT_TOMORROW_TAG)
        openOverflow()
        val inMenu =
            if (fontScale >= 1.3f) {
                listOf(
                    INBOX_MULTISELECT_ESTIMATE_TAG,
                    INBOX_MULTISELECT_ENERGY_TAG,
                    INBOX_MULTISELECT_LET_GO_TAG,
                )
            } else {
                listOf(INBOX_MULTISELECT_LET_GO_TAG)
            }
        inMenu.forEach { tag -> assertActionFits(tag, overflow, fontScale) }
        openOverflow()
        tapAction(INBOX_MULTISELECT_ESTIMATE_TAG, fontScale >= 1.3f)
        composeRule.onNode(hasTextExactly("15 min") and hasClickAction()).performTouchInput { click(center) }
        composeRule.waitForIdle()
        tapAction(INBOX_MULTISELECT_ENERGY_TAG, fontScale >= 1.3f)
        composeRule.onNode(hasTextExactly("Low") and hasClickAction()).performTouchInput { click(center) }
        composeRule.waitForIdle()
        tapAction(INBOX_MULTISELECT_LET_GO_TAG, overflow = true)
        assertTrue("today tap at f$fontScale", today)
        assertTrue("tomorrow tap at f$fontScale", tomorrow)
        assertTrue("estimate tap at f$fontScale", estimate)
        assertTrue("energy tap at f$fontScale", energy)
        assertTrue("let go tap at f$fontScale", letGo)
    }

    private fun tapAction(
        tag: String,
        overflow: Boolean,
    ) {
        if (overflow) openOverflow()
        tap(tag)
    }

    private fun assertActionFits(
        tag: String,
        overflow: Map<String, Boolean>,
        fontScale: Float,
    ) {
        val node = composeRule.onNodeWithTag(tag, useUnmergedTree = true)
        node.assertIsDisplayed()
        val bounds = node.getBoundsInRoot()
        assertTrue("$tag width ${bounds.width} at f$fontScale", bounds.width >= 48.dp)
        assertTrue("$tag height ${bounds.height} at f$fontScale", bounds.height >= 48.dp)
        assertFalse("$tag clipped/ellipsis at f$fontScale", overflow[tag] == true)
    }

    private fun openOverflow() {
        composeRule
            .onNodeWithTag(INBOX_MULTISELECT_OVERFLOW_TAG, useUnmergedTree = true)
            .performTouchInput { click(center) }
        composeRule.waitForIdle()
    }

    private fun tap(tag: String) {
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).performTouchInput { click(center) }
        composeRule.waitForIdle()
    }
}
