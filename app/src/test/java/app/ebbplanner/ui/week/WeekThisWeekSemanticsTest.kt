package app.ebbplanner.ui.week

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import app.ebbplanner.R
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WeekThisWeekSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun todayButton_mergedSemantics_exposesOnlyContentDescription() {
        val expected = RuntimeEnvironment.getApplication().getString(R.string.week_this_week)
        composeRule.setContent {
            EbbTheme {
                WeekScreenDesign(state = weekPortrait(), showBottomNav = false)
            }
        }
        composeRule.waitForIdle()
        val node = composeRule.onNodeWithTag(WEEK_THIS_WEEK_BUTTON_TAG, useUnmergedTree = false)
        val config = node.fetchSemanticsNode().config
        assertEquals(expected, config.getOrNull(SemanticsProperties.ContentDescription)?.singleOrNull())
        val mergedText = config.getOrNull(SemanticsProperties.Text)?.joinToString().orEmpty()
        assertTrue(mergedText.isEmpty() || mergedText == expected)
        assertTrue(!mergedText.equals("today", ignoreCase = true))
    }
}