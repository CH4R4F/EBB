package app.ebbplanner.ui.today

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.theme.EbbTheme
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
class TodayScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun todayScreen_showsHeaderAndTimeline() {
        val task =
            TaskEntity(
                id = "t1",
                title = "Focus block",
                placement = TaskEntity.PLACEMENT_SCHEDULED,
                date = "2025-11-17",
                startLocalTime = "10:00",
                durationMin = 30,
                createdAt = 0L,
                updatedAt = 0L,
            )
        val state =
            TodayUiState(
                date = LocalDate.of(2025, 11, 17),
                dateLabel = "Mon 17 Nov",
                minutesPlanned = 45,
                scheduledTasks = listOf(task),
                isLoading = false,
            )
        composeRule.setContent {
            EbbTheme {
                TodayScreenContent(state = state)
            }
        }
        composeRule.onNodeWithTag(TODAY_DATE_HEADER_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Mon 17 Nov").assertIsDisplayed()
        composeRule.onNodeWithTag(TODAY_TIMELINE_TAG).assertIsDisplayed()
    }
}
