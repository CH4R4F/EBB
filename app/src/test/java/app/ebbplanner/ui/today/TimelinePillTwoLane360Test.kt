package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.WithFontScale
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w360dp-h640dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class TimelinePillTwoLane360Test {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun twoLanes_f100_chooses4mLeftWithoutEllipsis() = assertPill(1f)

    @Test
    fun twoLanes_f130_chooses4mLeftWithoutEllipsis() = assertPill(1.3f)

    @Test
    fun twoLanes_f200_chooses4mLeftWithoutEllipsis() = assertPill(2f)

    private fun assertPill(fontScale: Float) {
        val today = LocalDate.of(2025, 11, 17)
        val now = 10 * 60 + 2
        val tasks =
            listOf(
                TaskEntity(
                    id = "a",
                    title = "Running",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = today.toString(),
                    startLocalTime = "10:00",
                    durationMin = 45,
                    createdAt = 1,
                    updatedAt = 1,
                ),
                TaskEntity(
                    id = "b",
                    title = "Standup",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = today.toString(),
                    startLocalTime = "10:15",
                    durationMin = 15,
                    createdAt = 1,
                    updatedAt = 1,
                ),
            )
        val mapped =
            TodayPresentationMapper.from(
                TodayUiState(date = today, scheduledTasks = tasks, isLoading = false),
                now,
                fontScale,
            )
        val state =
            mapped.copy(
                showNowLine = true,
                nowMinutes = now,
                blocks =
                    mapped.blocks.map {
                        it.copy(isCurrent = it.id == "a", minutesLeftLabel = "4 min left")
                    },
            )
        val chosen = mutableMapOf<String, String>()
        val ellipsized = mutableMapOf<String, Boolean>()
        composeRule.setContent {
            CompositionLocalProvider(
                LocalTimelinePillChosenLabels provides chosen,
                LocalTimelinePillLineEllipsized provides ellipsized,
            ) {
                WithFontScale(fontScale) {
                    EbbTheme {
                        TodayTimeline(state = state, modifier = Modifier.height(800.dp))
                    }
                }
            }
        }
        composeRule.waitForIdle()
        val label = chosen["a"] ?: error("missing pill label")
        assertTrue("unexpected label $label at scale $fontScale", label == "4m left" || (fontScale == 1.3f && label == "4m"))
        assertEquals(false, ellipsized["a"])
    }
}