package app.ebbplanner.ui.today

import app.ebbplanner.ui.WithFontScale

import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.unit.dp
import app.ebbplanner.data.local.entity.TaskEntity
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
@Config(sdk = [28], qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class TimelinePillTapTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun timeLeftPill_taps_doNotMarkTaskDone_scale_100() = tapPillHorizontalSweep(1f)

    @Test
    fun timeLeftPill_taps_doNotMarkTaskDone_scale_200() = tapPillHorizontalSweep(2f)

    private fun tapPillHorizontalSweep(fontScale: Float) {
        val today = LocalDate.of(2025, 11, 17)
        val now = 10 * 60 + 2
        val mapped =
            TodayPresentationMapper.from(
                TodayUiState(
                    date = today,
                    scheduledTasks = listOf(task(today, "running", "10:00", 45)),
                    isLoading = false,
                ),
                now,
                fontScale,
            )
        val state =
            mapped.copy(
                showNowLine = true,
                nowMinutes = now,
                blocks = mapped.blocks.map { it.copy(isCurrent = true, minutesLeftLabel = "12 min left") },
            )
        var doneCount = 0
        composeRule.setContent {
            WithFontScale(fontScale) {
                EbbTheme {
                    TodayTimeline(
                        state = state,
                        modifier = Modifier.height(600.dp),
                        onMarkTaskDone = { doneCount++ },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val pill = composeRule.onNodeWithTag(timelineTimeLeftPillTag("running"), useUnmergedTree = true)
        pill.performTouchInput {
            for (fraction in listOf(0.1f, 0.5f, 0.9f, 0.99f)) {
                val x = left + (right - left) * fraction
                val y = top + (bottom - top) * 0.5f
                click(Offset(x, y))
            }
        }
        composeRule.waitForIdle()
        assertTrue(doneCount == 0)
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

