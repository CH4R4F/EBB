package app.ebbplanner.ui.week

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import app.ebbplanner.ui.text.LocalTextOverflowReports
import androidx.compose.ui.test.onNodeWithTag
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertEquals
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

    private val sameMonth =
        formatWeekRangeLabel(
            LocalDate.of(2025, 11, 14),
            LocalDate.of(2025, 11, 20),
        )
    private val crossMonth =
        formatWeekRangeLabel(
            LocalDate.of(2025, 10, 31),
            LocalDate.of(2025, 11, 6),
        )

    @Test
    @Config(qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_sameMonth_f100_411_noOverflow() = assertRangeVisible(1f, sameMonth)

    @Test
    @Config(qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_sameMonth_f130_411_noOverflow() = assertRangeVisible(1.3f, sameMonth)

    @Test
    @Config(qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_sameMonth_f200_411_noOverflow() = assertRangeVisible(2f, sameMonth)

    @Test
    @Config(qualifiers = "w360dp-h640dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_sameMonth_f100_360_noOverflow() = assertRangeVisible(1f, sameMonth)

    @Test
    @Config(qualifiers = "w360dp-h640dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_sameMonth_f130_360_noOverflow() = assertRangeVisible(1.3f, sameMonth)

    @Test
    @Config(qualifiers = "w360dp-h640dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_sameMonth_f200_360_noOverflow() = assertRangeVisible(2f, sameMonth)

    @Test
    @Config(qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_crossMonth_f100_411_noOverflow() = assertRangeVisible(1f, crossMonth)

    @Test
    @Config(qualifiers = "w360dp-h640dp-normal-long-notround-any-420dpi-keyshidden-nonav")
    fun weekRange_crossMonth_f200_360_noOverflow() = assertRangeVisible(2f, crossMonth)

    private fun assertRangeVisible(
        fontScale: Float,
        rangeLabel: String,
    ) {
        val overflow = mutableMapOf<String, Boolean>()
        composeRule.setContent {
            CompositionLocalProvider(LocalTextOverflowReports provides overflow) {
                WithFontScale(fontScale) {
                    EbbTheme {
                        WeekScreenDesign(
                            state = weekPortrait().copy(rangeLabel = rangeLabel),
                            showBottomNav = false,
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        val bar = composeRule.onNodeWithTag(WEEK_TOP_BAR_TAG)
        val rangeNode = composeRule.onNodeWithTag(WEEK_RANGE_LABEL_TAG)
        val barRect = bar.fetchSemanticsNode().boundsInRoot
        val rangeRect = rangeNode.fetchSemanticsNode().boundsInRoot
        assertContains(barRect, rangeRect)
        assertTrue("range width ${rangeRect.width} > bar ${barRect.width}", rangeRect.width <= barRect.width + 1f)
        assertEquals(false, overflow[WEEK_RANGE_LABEL_TAG])
        val text =
            rangeNode.fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)?.joinToString() ?: ""
        assertTrue(text.contains("Nov"))
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