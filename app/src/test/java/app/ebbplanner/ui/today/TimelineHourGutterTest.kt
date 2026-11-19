package app.ebbplanner.ui.today

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.ui.WithFontScale
import app.ebbplanner.ui.text.LocalTextOverflowReports
import app.ebbplanner.ui.theme.EbbTheme
import app.ebbplanner.ui.time.ClockFormat
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
@Config(sdk = [28], qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class TimelineHourGutterTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun hourGutter_f200_12h_usesTwelveHourLabelsNot24h() {
        Settings.System.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.System.TIME_12_24,
            "12",
        )
        val today = LocalDate.of(2025, 11, 17)
        val overflow = mutableMapOf<String, Boolean>()
        val state =
            TodayPresentationMapper
                .from(
                    TodayUiState(date = today, isLoading = false, is24Hour = false),
                    nowMinutes = 10 * 60,
                    fontScale = 2f,
                ).copy(showNowLine = false, nowMinutes = null)
        composeRule.setContent {
            CompositionLocalProvider(LocalTextOverflowReports provides overflow) {
                WithFontScale(2f) {
                    EbbTheme {
                        TodayTimeline(state = state, modifier = Modifier.height(1600.dp))
                    }
                }
            }
        }
        composeRule.waitForIdle()
        assertEquals(false, overflow[TIMELINE_HOUR_LABEL_TAG] == true)
        composeRule.onNodeWithText("13", useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText("23", useUnmergedTree = true).assertDoesNotExist()
        val onePm = ClockFormat.hourGutterLabel(13, is24Hour = false, withMeridian = true)
        val one = ClockFormat.hourGutterLabel(13, is24Hour = false, withMeridian = false)
        val shown =
            composeRule.onAllNodesWithText(onePm, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText(one, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        assertTrue("expected 12h gutter '$onePm' or '$one' at f200", shown)
        composeRule.onNodeWithText("1 PM", useUnmergedTree = true).assertExists()
    }
}
