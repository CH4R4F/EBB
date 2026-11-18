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
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
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
class TodayTimelineTapTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val tapHeights = listOf(0.15f, 0.5f, 0.9f)

    @Test
    fun visibleBlocks_allHeights_openOwnTask_scale_100_fourLanes() {
        tapEveryVisibleHeight(
            fontScale = 1f,
            count = 4,
            timelineHeight = 820.dp,
        )
    }

    @Test
    fun visibleBlocks_allHeights_openOwnTask_scale_100_fiveLanes() {
        tapEveryVisibleHeight(
            fontScale = 1f,
            count = 5,
            timelineHeight = 820.dp,
        )
    }

    @Test
    fun visibleBlocks_allHeights_openOwnTask_scale_100_sixLanes() {
        tapEveryVisibleHeight(
            fontScale = 1f,
            count = 6,
            timelineHeight = 900.dp,
        )
    }

    @Test
    fun visibleBlocks_allHeights_openOwnTask_scale_200_fiveLanes() {
        tapEveryVisibleHeight(
            fontScale = 2f,
            count = 5,
            timelineHeight = 900.dp,
        )
    }

    @Test
    fun visibleBlocks_allHeights_openOwnTask_scale_200_sixLanes() {
        tapEveryVisibleHeight(
            fontScale = 2f,
            count = 6,
            timelineHeight = 960.dp,
        )
    }

    @Test
    fun runningCluster_atScale100_centerTapOpensRunningTaskOnly() {
        exerciseRunningCluster(1f)
    }

    @Test
    fun runningCluster_atScale200_centerTapOpensRunningTaskOnly() {
        exerciseRunningCluster(2f)
    }

    private fun exerciseRunningCluster(scale: Float) {
            val today = LocalDate.of(2025, 11, 17)
            val now = 10 * 60 + 1
            val tasks =
                listOf(
                    task(today, "running", "10:00", 5),
                    task(today, "b", "10:02", 5),
                    task(today, "c", "10:04", 5),
                )
            val mapped =
                TodayPresentationMapper.from(
                    TodayUiState(date = today, scheduledTasks = tasks, isLoading = false),
                    now,
                    scale,
                )
            val state =
                mapped.copy(
                    showNowLine = true,
                    nowMinutes = now,
                    blocks =
                        mapped.blocks.map {
                            if (it.id == "running") {
                                it.copy(isCurrent = true, minutesLeftLabel = "3 min left")
                            } else {
                                it
                            }
                        },
                )
            var opened: String? = null
            composeRule.setContent {
                WithFontScale(scale) {
                    EbbTheme {
                        TodayTimeline(
                            state = state,
                            modifier = Modifier.height(720.dp),
                            onTaskClick = { opened = it },
                        )
                    }
                }
            }
            composeRule.waitForIdle()
            visiblePlacementIds(state, scale).forEach { id ->
                tapHeights.forEach { fraction ->
                    composeRule.runOnIdle { opened = null }
                    tapTaskAt(id, fraction)
                    composeRule.runOnIdle { assertEquals("scale=$scale id=$id y=$fraction", id, opened) }
                }
            }
    }

    @Test
    fun kiloLima_fullWidth_allHeights_scale_200() {
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
                        modifier = Modifier.height(900.dp),
                        onTaskClick = { opened = it },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val screenW =
            RuntimeEnvironment.getApplication().resources.configuration.screenWidthDp.dp
        val trackWidth = robolectricTimelineTrackWidth(screenW.value.toInt(), 2f, false)
        assertTrue(
            buildTimelineOverlapPlan(
                mapped.blocks,
                TimelineLayoutMetrics(fontScale = 2f),
                trackWidth,
                mapped.timelineStartHour,
            ).overflow.isEmpty(),
        )
        listOf("kilo", "lima").forEach { id ->
            tapHeights.forEach { fraction ->
                composeRule.runOnIdle { opened = null }
                tapTaskAt(id, fraction)
                composeRule.runOnIdle { assertEquals(id, opened) }
            }
        }
    }

    @Test
    fun overflowSheet_listsWholeGroup() {
        val today = LocalDate.of(2025, 11, 17)
        val tasks =
            (1..5).map { i ->
                task(today, "lane-$i", "10:${((i - 1) * 5).toString().padStart(2, '0')}", 30)
            }
        val mapped =
            TodayPresentationMapper.from(
                TodayUiState(date = today, scheduledTasks = tasks, isLoading = false),
                10 * 60,
                1f,
            )
        composeRule.setContent {
            WithFontScale(1f) {
                EbbTheme {
                    TodayTimeline(
                        state = mapped,
                        modifier = Modifier.height(820.dp),
                        onTaskClick = {},
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val cluster = overflowCluster(mapped, 1f)
        composeRule.onNodeWithTag(timelineOverflowChipTag(cluster.groupKey), useUnmergedTree = true).performTouchInput {
            click(Offset(centerX, centerY))
        }
        composeRule.waitForIdle()
        cluster.allGroupBlocks.forEach { block ->
            composeRule.onNodeWithTag(timelineOverflowSheetRowTag(block.id), useUnmergedTree = true).assertIsDisplayed()
        }
    }

  @Test
    fun compactLane_threeLetterTitle_visible() {
        val today = LocalDate.of(2025, 11, 17)
        val tasks =
            (1..4).map { i ->
                TaskEntity(
                    id = "lane-$i",
                    title = if (i == 1) "Abc" else "lane-$i",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = today.toString(),
                    startLocalTime = "10:${((i - 1) * 5).toString().padStart(2, '0')}",
                    durationMin = 30,
                    createdAt = 1,
                    updatedAt = 1,
                )
            }
        val mapped =
            TodayPresentationMapper.from(
                TodayUiState(date = today, scheduledTasks = tasks, isLoading = false),
                10 * 60,
                1f,
            )
        composeRule.setContent {
            WithFontScale(1f) {
                EbbTheme {
                    TodayTimeline(
                        state = mapped,
                        modifier = Modifier.height(820.dp),
                        onTaskClick = {},
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Abc", useUnmergedTree = true).assertIsDisplayed()
    }

    private fun tapEveryVisibleHeight(
        fontScale: Float,
        count: Int,
        timelineHeight: Dp,
    ) {
        val today = LocalDate.of(2025, 11, 17)
        val tasks =
            (0 until count).map { i ->
                task(today, "lane-${'a' + i}", "10:${(i * 5).toString().padStart(2, '0')}", 30)
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
                        modifier = Modifier.height(timelineHeight),
                        onTaskClick = { opened = it },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val visible = visiblePlacementIds(mapped, fontScale)
        assertTrue("expected visible lanes", visible.isNotEmpty())
        visible.forEach { id ->
            tapHeights.forEach { fraction ->
                composeRule.runOnIdle { opened = null }
                tapTaskAt(id, fraction)
                composeRule.runOnIdle {
                    assertEquals("scale=$fontScale lanes=$count id=$id at $fraction", id, opened)
                }
            }
        }
    }

    private fun visiblePlacementIds(
        state: TodayPresentationState,
        fontScale: Float,
    ): List<String> {
        val screenW =
            RuntimeEnvironment.getApplication().resources.configuration.screenWidthDp.dp
        val trackWidth = robolectricTimelineTrackWidth(screenW.value.toInt(), fontScale, false)
        val plan =
            buildTimelineOverlapPlan(
                state.blocks,
                TimelineLayoutMetrics(fontScale = fontScale),
                trackWidth,
                state.timelineStartHour,
            )
        return plan.placements.map { it.first.id }
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

    private fun tapTaskAt(
        taskId: String,
        heightFraction: Float,
    ) {
        val node = composeRule.onNodeWithTag(timelineTaskTestTag(taskId), useUnmergedTree = true)
        node.assertIsDisplayed()
        node.performTouchInput {
            val y = top + (bottom - top) * heightFraction
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