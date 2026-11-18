package app.ebbplanner.ui.week

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WeekRangeLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val range =
        formatWeekRangeLabel(
            LocalDate.of(2025, 11, 14),
            LocalDate.of(2025, 11, 20),
        )

    @Test
    @Config(qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_f100_411dp_fullyVisible() = assertRangeVisible(1f)

    @Test
    @Config(qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_f200_411dp_fullyVisible() = assertRangeVisible(2f)

    @Test
    @Config(qualifiers = "w360dp-h640dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_f100_360dp_fullyVisible() = assertRangeVisible(1f)

    @Test
    @Config(qualifiers = "w360dp-h640dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_f200_360dp_fullyVisible() = assertRangeVisible(2f)

    private fun assertRangeVisible(fontScale: Float) {
        composeRule.setContent {
            WithFontScale(fontScale) {
                EbbTheme {
                    WeekScreenDesign(
                        state = weekPortrait().copy(rangeLabel = range),
                        showBottomNav = false,
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val bar = composeRule.onNodeWithTag(WEEK_TOP_BAR_TAG)
        val rangeNode = composeRule.onNodeWithTag(WEEK_RANGE_LABEL_TAG)
        val barRect = bar.fetchSemanticsNode().boundsInRoot
        val rangeRect = rangeNode.fetchSemanticsNode().boundsInRoot
        assertContains(barRect, rangeRect)
        val text =
            rangeNode.fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)?.joinToString() ?: ""
        assertTrue(text.contains("14"))
        assertTrue(text.contains("20"))
        assertTrue(text.contains("Nov"))
        assertTrue("range must not split the end day from month", text.contains("20") && text.contains("Nov"))
    }

    private fun assertContains(
        outer: Rect,
        inner: Rect,
    ) {
        assertTrue(
            "range clipped: inner=$inner outer=$outer",
            inner.left >= outer.left - 1f &&
                inner.top >= outer.top - 1f &&
                inner.right <= outer.right + 1f &&
                inner.bottom <= outer.bottom + 1f,
        )
    }
}

@Composable
private fun WithFontScale(
    scale: Float,
    content: @Composable () -> Unit,
) {
    val base = RuntimeEnvironment.getApplication().resources.configuration
    val config = Configuration(base).apply { fontScale = scale }
    CompositionLocalProvider(LocalConfiguration provides config) {
        content()
    }
}