package app.ebbplanner.ui.today

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28])
class QuickCaptureSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun quickCapture_hasSingleFieldAndAddButton() {
        var saved: String? = null
        composeRule.setContent {
            EbbTheme {
                QuickCaptureSheetContent(
                    state = QuickCapturePresentationState(mode = QuickCaptureMode.Empty),
                    showDragHandle = false,
                    interactive = true,
                    text = "",
                    onTextChange = {},
                    onAdd = {},
                )
            }
        }
        composeRule.onAllNodesWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG).assertCountEquals(1)
        composeRule.onAllNodesWithTag(QUICK_CAPTURE_ADD_BUTTON_TAG).assertCountEquals(1)
        composeRule.setContent {
            EbbTheme {
                QuickCaptureSheetContent(
                    state = QuickCapturePresentationState(mode = QuickCaptureMode.Typing, text = "Pay rent"),
                    showDragHandle = false,
                    interactive = true,
                    text = "Pay rent",
                    onTextChange = {},
                    onAdd = { saved = "Pay rent" },
                )
            }
        }
        composeRule.onNodeWithTag(QUICK_CAPTURE_ADD_BUTTON_TAG).performClick()
        composeRule.runOnIdle {
            assert(saved == "Pay rent")
        }
    }

    @Test
    fun quickCapture_interactive_savesFromField() {
        var draft = ""
        var saved = ""
        composeRule.setContent {
            EbbTheme {
                QuickCaptureSheetContent(
                    state = QuickCapturePresentationState(mode = QuickCaptureMode.Typing, text = draft),
                    showDragHandle = false,
                    interactive = true,
                    text = draft,
                    onTextChange = { draft = it },
                    onAdd = { saved = draft },
                )
            }
        }
        composeRule.onNodeWithTag(QUICK_CAPTURE_TEXT_FIELD_TAG).performTextInput("One task only")
        composeRule.onNodeWithTag(QUICK_CAPTURE_ADD_BUTTON_TAG).performClick()
        composeRule.runOnIdle {
            assert(saved == "One task only")
        }
    }
}