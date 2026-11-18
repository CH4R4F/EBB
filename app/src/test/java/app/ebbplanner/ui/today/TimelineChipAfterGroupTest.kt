package app.ebbplanner.ui.today

import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class TimelineChipAfterGroupTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun chipTap_afterGroup_10min_gap_scale_100() = exerciseChipAfterSolo(10, 1f)

    @Test
    fun chipTap_afterGroup_30min_gap_scale_100() = exerciseChipAfterSolo(30, 1f)

    @Test
    fun chipTap_afterGroup_45min_gap_scale_100() = exerciseChipAfterSolo(45, 1f)

    @Test
    fun chipTap_afterGroup_10min_gap_scale_200() = exerciseChipAfterSolo(10, 2f)

    @Test
    fun chipTap_afterGroup_30min_gap_scale_200() = exerciseChipAfterSolo(30, 2f)

    @Test
    fun chipTap_afterGroup_45min_gap_scale_200() = exerciseChipAfterSolo(45, 2f)

    private fun exerciseChipAfterSolo(
        gapMin: Int,
        fontScale: Float,
    ) {
        val today = LocalDate.of(2025, 11, 17)
        val overlap =
            (1..5).map { i ->
                task(today, "lane-$i", "10:${((i - 1) * 5).toString().padStart(2, '0')}", 30)
            }
        val groupEnd = 10 * 60 + 20
        val soloStart = groupEnd + gapMin
        val soloHour = soloStart / 60
        val soloMin = soloStart % 60
        val solo =
            task(
                today,
                "solo-after",
                "$soloHour:${soloMin.toString().padStart(2, '0')}",
                30,
            )
        val mapped =
            TodayPresentationMapper.from(
                TodayUiState(date = today, scheduledTasks = overlap + solo, isLoading = false),
                10 * 60,
                fontScale,
            )
        var doneIds = mutableListOf<String>()
        composeRule.setContent {
            WithFontScale(fontScale) {
                EbbTheme {
                    TodayTimeline(
                        state = mapped,
                        modifier = Modifier.height(1200.dp),
                        onTaskClick = {},
                        onMarkTaskDone = { doneIds.add(it) },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val cluster = overflowCluster(mapped, fontScale)
        val chip =
            composeRule.onNodeWithTag(timelineOverflowChipTag(cluster.groupKey), useUnmergedTree = true)
        chip.assertIsDisplayed()
        chip.performTouchInput {
            click(Offset(centerX, centerY))
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(timelineOverflowSheetRowTag(cluster.hiddenBlocks.first().id)).assertIsDisplayed()
        assertFalse("gap=$gapMin: chip tap must not mark done", doneIds.isNotEmpty())
    }

    private fun overflowCluster(
        state: TodayPresentationState,
        fontScale: Float,
    ): TimelineOverflowCluster {
        val screenW =
            RuntimeEnvironment.getApplication().resources.configuration.screenWidthDp.dp
        val trackWidth = robolectricTimelineTrackWidth(screenW.value.toInt(), fontScale, false)
        return buildTimelineOverlapPlan(
            state.blocks,
            TimelineLayoutMetrics(fontScale = fontScale),
            trackWidth,
            state.timelineStartHour,
        ).overflow.first()
    }

    private fun task(
        date: LocalDate,
        id: String,
        start: String,
        durationMin: Int,
    ): TaskEntity =
        TaskEntity(
            id = id,
            title = id,
            placement = TaskEntity.PLACEMENT_SCHEDULED,
            date = date.toString(),
            startLocalTime = start,
            durationMin = durationMin,
            createdAt = 1,
            updatedAt = 1,
        )
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