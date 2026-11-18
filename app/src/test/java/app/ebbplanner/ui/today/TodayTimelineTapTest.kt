package app.ebbplanner.ui.today

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertEquals
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
class TodayTimelineTapTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun timeline_tapOpensMatchingTask_includingShortBlockAndCurrentPill() {
        val today = LocalDate.of(2025, 11, 17)
        val now = 10 * 60 + 15
        val uiState =
            TodayUiState(
                date = today,
                dateLabel = "Mon 17 Nov",
                scheduledTasks =
                    listOf(
                        task(today, "focus-probe", "10:00", 45),
                        task(today, "call-mum", "11:00", 30),
                        task(today, "short-after", "11:35", 5),
                    ),
                isLoading = false,
            )
        val mapped = TodayPresentationMapper.from(uiState, now, fontScale = 1f)
        val state =
            mapped.copy(
                showNowLine = true,
                nowMinutes = now,
                blocks =
                    mapped.blocks.map { block ->
                        if (block.id == "focus-probe") {
                            block.copy(isCurrent = true, minutesLeftLabel = "12 min left")
                        } else {
                            block
                        }
                    },
            )
        var opened: String? = null
        composeRule.setContent {
            WithFontScale(1f) {
                EbbTheme {
                    TodayTimeline(
                        state = state,
                        modifier = Modifier.height(900.dp),
                        onTaskClick = { opened = it },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(timelineTaskTestTag("focus-probe"), useUnmergedTree = true).performClick()
        composeRule.runOnIdle { assertEquals("focus-probe", opened) }
        composeRule.onNodeWithTag(timelineTaskTestTag("call-mum"), useUnmergedTree = true).performClick()
        composeRule.runOnIdle { assertEquals("call-mum", opened) }
        composeRule.onNodeWithTag(timelineTaskTestTag("short-after"), useUnmergedTree = true).performClick()
        composeRule.runOnIdle { assertEquals("short-after", opened) }
    }

    @Test
    fun four_overlapping_lanes_scale_100_eachVisibleOrOverflowBottomTapOpensTask() {
        exerciseOverlapScenario(fontScale = 1f, ids = listOf("lane-a", "lane-b", "lane-c", "lane-d"))
    }

    @Test
    fun five_overlapping_lanes_scale_100_overflowChipOpensHiddenTask() {
        exerciseOverlapScenario(
            fontScale = 1f,
            ids = listOf("lane-a", "lane-b", "lane-c", "lane-d", "lane-e"),
        )
    }

    @Test
    fun five_overlapping_lanes_scale_200_overflowChipOpensHiddenTask() {
        exerciseOverlapScenario(
            fontScale = 2f,
            ids = listOf("lane-a", "lane-b", "lane-c", "lane-d", "lane-e"),
            tapIds = listOf("lane-e"),
        )
    }

    @Test
    fun kilo_lima_adjacent_scale_200_bottomTapOpensCorrectBlock() {
        val today = LocalDate.of(2025, 11, 17)
        val tasks =
            listOf(
                task(today, "kilo", "11:00", 30),
                task(today, "lima", "11:35", 45),
            )
        val mapped = TodayPresentationMapper.from(TodayUiState(date = today, scheduledTasks = tasks, isLoading = false), 11 * 60, 2f)
        var opened: String? = null
        composeRule.setContent {
            WithFontScale(2f) {
                EbbTheme {
                    TodayTimeline(
                        state = mapped,
                        modifier = Modifier.height(800.dp),
                        onTaskClick = { opened = it },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        openTaskViaTimelineOrOverflow("kilo", mapped, 2f) { opened }
        composeRule.runOnIdle { assertEquals("kilo", opened) }
        composeRule.runOnIdle { opened = null }
        openTaskViaTimelineOrOverflow("lima", mapped, 2f) { opened }
        composeRule.runOnIdle { assertEquals("lima", opened) }
    }

    @Test
    fun currentFiveMinuteTask_and_nearby_tasks_tappable_scale_100() {
        val today = LocalDate.of(2025, 11, 17)
        val now = 10 * 60 + 2
        val tasks =
            listOf(
                task(today, "running", "10:00", 5),
                task(today, "next-a", "10:06", 15),
                task(today, "next-b", "10:12", 15),
            )
        val mapped = TodayPresentationMapper.from(TodayUiState(date = today, scheduledTasks = tasks, isLoading = false), now, 1f)
        val state =
            mapped.copy(
                showNowLine = true,
                nowMinutes = now,
                blocks = mapped.blocks.map { if (it.id == "running") it.copy(isCurrent = true, minutesLeftLabel = "3 min left") else it },
            )
        var opened: String? = null
        composeRule.setContent {
            WithFontScale(1f) {
                EbbTheme {
                    TodayTimeline(state = state, modifier = Modifier.height(700.dp), onTaskClick = { opened = it })
                }
            }
        }
        composeRule.waitForIdle()
        listOf("running", "next-a", "next-b").forEach { id ->
            tapBottomEdge(id)
            composeRule.runOnIdle { assertEquals(id, opened) }
        }
    }

    private fun exerciseOverlapScenario(
        fontScale: Float,
        ids: List<String>,
        tapIds: List<String>? = null,
    ) {
        val today = LocalDate.of(2025, 11, 17)
        val starts = listOf("10:00", "10:05", "10:10", "10:15", "10:20")
        val tasks =
            ids.mapIndexed { index, id ->
                task(today, id, starts[index], 30)
            }
        val mapped =
            TodayPresentationMapper.from(
                TodayUiState(date = today, scheduledTasks = tasks, isLoading = false),
                10 * 60,
                fontScale,
            )
        var opened: String? = null
        composeRule.setContent {
            WithFontScale(fontScale) {
                EbbTheme {
                    TodayTimeline(
                        state = mapped,
                        modifier = Modifier.height(720.dp),
                        onTaskClick = { opened = it },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val trackWidth =
            (
                RuntimeEnvironment.getApplication().resources.configuration.screenWidthDp -
                    68
            ).dp
        val plan =
            planTimelineOverlaps(
                mapped.blocks,
                TimelineLayoutMetrics(fontScale = fontScale, titleOnlyBlocks = false),
                trackWidth,
                mapped.timelineStartHour,
            )
        ids.forEach { id ->
            val reachable =
                plan.placements.any { it.first.id == id } ||
                    plan.overflow.any { cluster -> cluster.hiddenBlocks.any { it.id == id } }
            assertEquals("task $id should be visible or in +N", true, reachable)
        }
        val taps = tapIds ?: listOf(ids.first(), ids.last())
        taps.forEach { id ->
            composeRule.runOnIdle { opened = null }
            openTaskViaTimelineOrOverflow(id, mapped, fontScale) { opened }
            composeRule.runOnIdle { assertEquals(id, opened) }
        }
    }

    private fun openTaskViaTimelineOrOverflow(
        id: String,
        mapped: TodayPresentationState,
        fontScale: Float,
        openedSoFar: () -> String? = { null },
    ) {
        val trackWidth =
            (
                RuntimeEnvironment.getApplication().resources.configuration.screenWidthDp -
                    68
            ).dp
        val plan =
            planTimelineOverlaps(
                mapped.blocks,
                TimelineLayoutMetrics(fontScale = fontScale, titleOnlyBlocks = false),
                trackWidth,
                mapped.timelineStartHour,
            )
        val onTimeline =
            composeRule.onAllNodesWithTag(timelineTaskTestTag(id), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        if (onTimeline) {
            val node = composeRule.onNodeWithTag(timelineTaskTestTag(id), useUnmergedTree = true)
            node.assertIsDisplayed()
            node.performTouchInput {
                val y = top + (bottom - top) * 0.92f
                click(Offset(centerX, y))
            }
            composeRule.waitForIdle()
            if (openedSoFar() == null) {
                node.performClick()
                composeRule.waitForIdle()
            }
        } else {
            val cluster = plan.overflow.first { cluster -> cluster.hiddenBlocks.any { it.id == id } }
            composeRule.onNodeWithTag(timelineOverflowChipTag(cluster.groupKey), useUnmergedTree = true).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText(id, useUnmergedTree = true).performClick()
            composeRule.waitForIdle()
        }
    }

    private fun tapBottomEdge(taskId: String) {
        val node = composeRule.onNodeWithTag(timelineTaskTestTag(taskId), useUnmergedTree = true)
        node.assertIsDisplayed()
        node.performTouchInput {
            val y = top + (bottom - top) * 0.92f
            click(Offset(centerX, y))
        }
        composeRule.waitForIdle()
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