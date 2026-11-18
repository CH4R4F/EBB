package app.ebbplanner.ui.today

import app.ebbplanner.ui.WithFontScale

import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.text.LocalTextOverflowReports
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
@Config(sdk = [28], qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class TimelineNowBadgeLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun nowBadge_f100_staysInGutter_centeredOnLine() = assertBadgeLayout(1f, 10 * 60 + 42)

    @Test
    fun nowBadge_f200_staysInGutter_centeredOnLine() = assertBadgeLayout(2f, 15 * 60 + 47)

    private fun assertBadgeLayout(
        fontScale: Float,
        nowMin: Int,
    ) {
        val today = LocalDate.of(2025, 11, 17)
        val tasks =
            listOf(
                TaskEntity(
                    id = "running",
                    title = "Running",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = today.toString(),
                    startLocalTime = "10:00",
                    durationMin = 45,
                    createdAt = 1,
                    updatedAt = 1,
                ),
            )
        val mapped =
            TodayPresentationMapper.from(
                TodayUiState(date = today, scheduledTasks = tasks, isLoading = false),
                nowMin,
                fontScale,
            )
        val state =
            mapped.copy(
                showNowLine = true,
                nowMinutes = nowMin,
                blocks =
                    mapped.blocks.map {
                        it.copy(isCurrent = true, minutesLeftLabel = "12 min left")
                    },
            )
        val overflow = mutableMapOf<String, Boolean>()
        var railPx = 0f
        var lineCenterYPx = 0f
        composeRule.setContent {
            WithFontScale(fontScale) {
                val rail = robolectricTimelineHourRail(fontScale, false)
                val mapping =
                    buildTimelineTimeMapping(
                        state.blocks,
                        TimelineLayoutMetrics(fontScale),
                        state.timelineStartHour,
                        state.timelineEndHour,
                    )
                val lineY = mapping.y(nowMin)
                val badgeH = nowBadgeHeightDp(TimelineLayoutMetrics(fontScale))
                val density = LocalDensity.current
                railPx = with(density) { rail.toPx() }
                lineCenterYPx = with(density) { lineY.toPx() }
                CompositionLocalProvider(LocalTextOverflowReports provides overflow) {
                    EbbTheme {
                        TodayTimeline(state = state, modifier = Modifier.height(900.dp))
                    }
                }
            }
        }
        composeRule.waitForIdle()
        assertEquals(false, overflow[TIMELINE_NOW_BADGE_TAG])
        val badge = composeRule.onNodeWithTag(TIMELINE_NOW_BADGE_TAG, useUnmergedTree = true)
        val badgeRect = badge.fetchSemanticsNode().boundsInRoot
        assertTrue("badge overlaps task track", badgeRect.right <= railPx + 2f)
        val block = composeRule.onNodeWithTag(timelineTaskTestTag("running"), useUnmergedTree = true)
        val blockRect = block.fetchSemanticsNode().boundsInRoot
        assertNoHorizontalOverlap(badgeRect, blockRect)
        val badgeCenterY = (badgeRect.top + badgeRect.bottom) / 2f
        assertTrue(
            "badge center $badgeCenterY not on line $lineCenterYPx",
            kotlin.math.abs(badgeCenterY - lineCenterYPx) <= with(composeRule.density) { 1.dp.toPx() },
        )
    }

    private fun assertNoHorizontalOverlap(
        badge: Rect,
        block: Rect,
    ) {
        assertTrue(
            "badge $badge intersects block $block",
            badge.right <= block.left || block.right <= badge.left,
        )
    }
}

