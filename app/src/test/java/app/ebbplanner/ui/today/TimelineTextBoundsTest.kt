package app.ebbplanner.ui.today

import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertFalse
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
class TimelineTextBoundsTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val durations = listOf(5, 15, 25, 30, 60)

    @Test
    fun laneContentWidth_twoLanes_scale100_matchesExpectation() {
        val metrics = TimelineLayoutMetrics(1f)
        val track = timelineTrackWidthDp(411.dp, metrics, compactHourLabels = false)
        val lane = track / 2 - 2.dp
        val contentW = timelineBlockContentWidth(lane, metrics, compactLane = true)
        assertTrue("2-lane content width was $contentW at 411dp", contentW >= 90.dp)
    }

    @Test
    fun noTextClipped_scale100_oneLane() = assertNoClipped(1f, 1)

    @Test
    fun noTextClipped_scale100_twoLanes() = assertNoClipped(1f, 2)

    @Test
    fun noTextClipped_scale100_threeLanes() = assertNoClipped(1f, 3)

    @Test
    fun noTextClipped_scale200_oneLane() = assertNoClipped(2f, 1)

    @Test
    fun noTextClipped_scale200_twoLanes() = assertNoClipped(2f, 2)

    @Test
    fun noTextClipped_scale200_threeLanes() = assertNoClipped(2f, 3)

    private fun assertNoClipped(
        scale: Float,
        lanes: Int,
    ) {
        val specs = listOf("Call mum", "Pay rent", "Buy stamps", "Plan week", "Gym class")
        val pairs = specs.take(lanes).mapIndexed { i, title -> title to durations[i] }
        val (ids, state) = mount(scale, lanes, pairs)
        val trackWidth = timelineTrackWidthDp(411.dp, TimelineLayoutMetrics(scale), compactHourLabels = false)
        val visible =
            buildTimelineOverlapPlan(
                state.blocks,
                TimelineLayoutMetrics(scale),
                trackWidth,
                state.timelineStartHour,
            ).placements.map { it.first.id }
        visible.forEach { id ->
            val block = composeRule.onNodeWithTag(timelineTaskTestTag(id), useUnmergedTree = true)
            val blockRect = block.fetchSemanticsNode().boundsInRoot
            val titleRect =
                composeRule
                    .onNodeWithTag(timelineTaskTitleTag(id), useUnmergedTree = true)
                    .fetchSemanticsNode()
                    .boundsInRoot
            assertContains(blockRect, titleRect, "title $id")
            val ringRect =
                composeRule
                    .onNodeWithTag(timelineRingSlotTag(id), useUnmergedTree = true)
                    .fetchSemanticsNode()
                    .boundsInRoot
            assertFalse("ring overlaps title for $id", ringRect.overlaps(titleRect))
            if (id == "t0") {
                val pillRect =
                    composeRule.onNodeWithTag(timelineTimeLeftPillTag(id), useUnmergedTree = true)
                        .fetchSemanticsNode().boundsInRoot
                assertContains(blockRect, pillRect, "pill $id")
            }
        }
    }

    private fun mount(
        scale: Float,
        lanes: Int,
        pairs: List<Pair<String, Int>>,
    ): Pair<List<String>, TodayPresentationState> {
        val today = LocalDate.of(2025, 11, 17)
        val now = 10 * 60 + 3
        val tasks =
            pairs.mapIndexed { index, (title, duration) ->
                val id = "t$index"
                TaskEntity(
                    id = id,
                    title = title,
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = today.toString(),
                    startLocalTime = "10:00",
                    durationMin = duration,
                    createdAt = 1,
                    updatedAt = 1,
                )
            }
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
                    mapped.blocks.mapIndexed { index, block ->
                        block.copy(
                            isCurrent = index == 0,
                            minutesLeftLabel = if (index == 0) "${block.durationMin - 2} min left" else block.minutesLeftLabel,
                            metaDuration = "${block.durationMin} min",
                            metaTimeRange = "10:00–10:${block.durationMin.toString().padStart(2, '0')}",
                            hasReminder = block.title == "Gym class",
                            pinned = block.title.contains("Dentist", ignoreCase = true),
                            short = block.durationMin <= 15,
                        )
                    },
            )
        composeRule.setContent {
            WithFontScale(scale) {
                EbbTheme {
                    TodayTimeline(
                        state = state,
                        modifier = Modifier.height(1200.dp),
                        onTaskClick = {},
                    )
                }
            }
        }
        composeRule.waitForIdle()
        return tasks.map { it.id } to state
    }

    private fun assertContains(
        outer: Rect,
        inner: Rect,
        label: String,
    ) {
        assertTrue(
            "$label clipped: inner=$inner outer=$outer",
            inner.left >= outer.left - 0.5f &&
                inner.top >= outer.top - 0.5f &&
                inner.right <= outer.right + 0.5f &&
                inner.bottom <= outer.bottom + 0.5f,
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