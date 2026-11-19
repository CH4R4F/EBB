package app.ebbplanner.ui.inbox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class InboxScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longPress_entersMultiselect() {
        var longPressed: String? = null
        var clicked: String? = null
        composeRule.setContent {
            EbbTheme {
                InboxScreenDesign(
                    state = inboxPopulated(),
                    modifier = Modifier.fillMaxSize(),
                    onTaskClick = { clicked = it },
                    onTaskLongPress = { longPressed = it },
                )
            }
        }
        composeRule
            .onNodeWithTag(inboxTaskTestTag("1"), useUnmergedTree = true)
            .assertIsDisplayed()
            .performTouchInput { longClick() }
        composeRule.waitForIdle()
        assertEquals("1", longPressed)
        assertEquals(null, clicked)
    }

    @Test
    fun done_exitsMultiselect() {
        var done = false
        composeRule.setContent {
            EbbTheme {
                InboxScreenDesign(
                    state = inboxMultiselect(),
                    modifier = Modifier.fillMaxSize(),
                    onMultiselectDone = { done = true },
                )
            }
        }
        composeRule.onNodeWithText("2 selected").assertIsDisplayed()
        composeRule.onNodeWithTag(INBOX_MULTISELECT_TODAY_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(INBOX_MULTISELECT_DONE_TAG).performTouchInput {
            click(center)
        }
        composeRule.waitForIdle()
        assertTrue(done)
    }

    @Test
    fun tapInMultiselect_togglesSelectionNotOpen() {
        val toggled = mutableListOf<String>()
        var opened: String? = null
        composeRule.setContent {
            EbbTheme {
                InboxScreenDesign(
                    state = inboxMultiselect(),
                    modifier = Modifier.fillMaxSize(),
                    onTaskClick = { toggled += it },
                    onTaskLongPress = {},
                )
            }
        }
        composeRule
            .onNodeWithTag(inboxTaskTestTag("2"), useUnmergedTree = true)
            .performTouchInput { click(center) }
        composeRule.waitForIdle()
        assertEquals(listOf("2"), toggled)
        assertEquals(null, opened)
    }
}
