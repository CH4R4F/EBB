package app.ebbplanner.ui.taskdetail

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.components.EBB_SHEET_CONFIRM_TAG
import app.ebbplanner.ui.theme.EbbTheme
import app.ebbplanner.ui.time.ClockFormat
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
@Config(sdk = [28], qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class TaskScheduleSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun useSelection_withoutChanges_keepsExistingDateAndTime() {
        val existingDate = LocalDate.of(2026, 3, 15)
        val existingStart = "14:30"
        var savedPlacement: String? = null
        var savedDate: LocalDate? = null
        var savedStart: String? = null
        composeRule.setContent {
            EbbTheme {
                ScheduleSheetPanel(
                    taskTitle = "Write brief",
                    suggestions = emptyList(),
                    existingDate = existingDate,
                    existingStartLocalTime = existingStart,
                    onSave = { placement, date, time ->
                        savedPlacement = placement
                        savedDate = date
                        savedStart = time
                    },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("15 Mar").assertIsDisplayed()
        val is24Hour =
            android.text.format.DateFormat.is24HourFormat(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            )
        composeRule
            .onNodeWithText(ClockFormat.minutesToClockDisplay(14 * 60 + 30, is24Hour))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(EBB_SHEET_CONFIRM_TAG).performTouchInput {
            click(Offset(width / 2f, height / 2f))
        }
        composeRule.waitForIdle()
        assertEquals(TaskEntity.PLACEMENT_SCHEDULED, savedPlacement)
        assertEquals(existingDate, savedDate)
        assertEquals(existingStart, savedStart)
    }
}
