package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.theme.EbbTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28])
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
        val mapped = TodayPresentationMapper.from(uiState, now)
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
            EbbTheme {
                TodayTimeline(
                    state = state,
                    modifier = Modifier.height(900.dp),
                    onTaskClick = { opened = it },
                )
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