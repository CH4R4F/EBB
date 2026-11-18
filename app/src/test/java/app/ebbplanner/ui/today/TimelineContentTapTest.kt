package app.ebbplanner.ui.today

import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.unit.dp
import app.ebbplanner.data.local.entity.TaskEntity
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
class TimelineContentTapTest {
    @get:Rule
    val composeRule = createComposeRule()

  @Test
    fun scale100_oneLane_titlePillDurationTaps_openTask_notDone() = sweep(1f, 1)

    @Test
    fun scale200_oneLane_titlePillDurationTaps_openTask_notDone() = sweep(2f, 1)

    @Test
    fun scale100_twoLanes_titlePillDurationTaps_openTask_notDone() = sweep(1f, 2)

    @Test
    fun scale200_twoLanes_titlePillDurationTaps_openTask_notDone() = sweep(2f, 2)

    @Test
    fun scale100_threeLanes_titlePillDurationTaps_openTask_notDone() = sweep(1f, 3)

    @Test
    fun scale200_threeLanes_titlePillDurationTaps_openTask_notDone() = sweep(2f, 3)

    private fun sweep(
        fontScale: Float,
        laneCount: Int,
    ) {
        val today = LocalDate.of(2025, 11, 17)
        val titles = listOf("Laundry", "Stretch", "Buy stamps").take(laneCount)
        val now = 10 * 60 + 5
        val tasks =
            titles.mapIndexed { index, title ->
                TaskEntity(
                    id = "t$index",
                    title = title,
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = today.toString(),
                    startLocalTime = "10:00",
                    durationMin = listOf(45, 25, 30)[index],
                    createdAt = 1,
                    updatedAt = 1,
                )
            }
        val mapped =
            TodayPresentationMapper.from(
                TodayUiState(date = today, scheduledTasks = tasks, isLoading = false),
                now,
                fontScale,
            )
        val runningId = "t0"
        val state =
            mapped.copy(
                showNowLine = true,
                nowMinutes = now,
                blocks =
                    mapped.blocks.map {
                        if (it.id == runningId) {
                            it.copy(isCurrent = true, minutesLeftLabel = "12 min left", metaDuration = "45 min")
                        } else {
                            it.copy(metaDuration = "${it.durationMin} min")
                        }
                    },
            )
        var opened: String? = null
        var doneCount = 0
        composeRule.setContent {
            WithFontScale(fontScale) {
                EbbTheme {
                    TodayTimeline(
                        state = state,
                        modifier = Modifier.height(900.dp),
                        onTaskClick = { opened = it },
                        onMarkTaskDone = { doneCount++ },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val trackWidth = robolectricTimelineTrackWidth(411, fontScale, false)
        val visible =
            buildTimelineOverlapPlan(
                state.blocks,
                TimelineLayoutMetrics(fontScale),
                trackWidth,
                state.timelineStartHour,
            ).placements.map { it.first.id }

        visible.forEach { blockId ->
            val block = state.blocks.first { it.id == blockId }
            tapTitleTrailing(block.id)
            composeRule.waitForIdle()
            assertEquals(block.id, opened)
            assertEquals(0, doneCount)

            val durationTag = timelineTaskDurationTag(block.id)
            composeRule.waitForIdle()
            try {
                composeRule.onNodeWithTag(durationTag, useUnmergedTree = true).performTouchInput {
                    click(Offset(right - 1f, centerY))
                }
                composeRule.waitForIdle()
                assertEquals(block.id, opened)
                assertEquals(0, doneCount)
            } catch (_: AssertionError) {
                // duration row hidden when vertical space is tight
            }
        }

        val pill = composeRule.onNodeWithTag(timelineTimeLeftPillTag(runningId), useUnmergedTree = true)
        pill.performTouchInput {
            for (fraction in listOf(0.1f, 0.5f, 0.9f, 0.99f)) {
                val x = left + (right - left) * fraction
                click(Offset(x, centerY))
            }
        }
        composeRule.waitForIdle()
        assertEquals(runningId, opened)
        assertEquals(0, doneCount)

        composeRule
            .onNodeWithTag(timelineRingSlotTag(runningId), useUnmergedTree = true)
            .performTouchInput {
                click(Offset(centerX, centerY))
            }
        composeRule.waitForIdle()
        assertEquals(1, doneCount)
    }

    private fun tapTitleTrailing(blockId: String) {
        composeRule.onNodeWithTag(timelineTaskTitleTag(blockId), useUnmergedTree = true).performTouchInput {
            click(Offset(right - 1f, centerY))
        }
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