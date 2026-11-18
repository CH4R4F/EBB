package app.ebbplanner.ui.week

import app.ebbplanner.ui.WithFontScale

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.assert
import app.ebbplanner.R
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WeekThisWeekSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
  @Config(qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun todayButton_f100_hasOnClick_andInvokesCallback() = assertTodayButton(1f)

    @Test
    @Config(qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun todayButton_f200_hasOnClick_andInvokesCallback() = assertTodayButton(2f)

    private fun assertTodayButton(fontScale: Float) {
        val expected = RuntimeEnvironment.getApplication().getString(R.string.week_this_week)
        var jumped = false
        composeRule.setContent {
            WithFontScale(fontScale) {
                EbbTheme {
                    WeekScreenDesign(
                        state = weekPortrait(),
                        showBottomNav = false,
                        onThisWeek = { jumped = true },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val node = composeRule.onNodeWithTag(WEEK_THIS_WEEK_BUTTON_TAG, useUnmergedTree = false)
        node.assert(hasClickAction())
        val config = node.fetchSemanticsNode().config
        assertNotNull(config.getOrNull(SemanticsActions.OnClick))
        assertEquals(expected, config.getOrNull(SemanticsProperties.ContentDescription)?.singleOrNull())
        val mergedText = config.getOrNull(SemanticsProperties.Text)?.joinToString().orEmpty()
        assertTrue(mergedText.isEmpty() || mergedText == expected)
        assertTrue(!mergedText.equals("today", ignoreCase = true))
        jumped = false
        node.performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        assertTrue(jumped)
    }
}

