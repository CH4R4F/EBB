package app.ebbplanner.ui.today

import app.ebbplanner.ui.WithFontScale

import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import app.ebbplanner.ui.text.LocalTextOverflowReports
import androidx.compose.ui.unit.dp
import app.ebbplanner.ui.theme.EbbTheme
import androidx.compose.ui.test.onNodeWithText
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
class TimelineNowBadgeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun nowBadge_noVisualOverflow_scale100() = assertBadge(1f, 10 * 60 + 42, "10:42", is24Hour = true)

    @Test
    fun nowBadge_noVisualOverflow_scale200() = assertBadge(2f, 15 * 60 + 47, "15:47", is24Hour = true)

    @Test
    fun nowBadge_12h_compact_scale200() = assertBadge(2f, 8 * 60 + 5, "8:05", is24Hour = false)

    private fun assertBadge(
        fontScale: Float,
        nowMin: Int,
        expectedPrefix: String,
        is24Hour: Boolean,
    ) {
        android.provider.Settings.System.putString(
            androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
                .contentResolver,
            android.provider.Settings.System.TIME_12_24,
            if (is24Hour) "24" else "12",
        )
        val today = LocalDate.of(2025, 11, 17)
        val state =
            TodayPresentationMapper
                .from(
                    TodayUiState(date = today, isLoading = false, is24Hour = is24Hour),
                    nowMin,
                    fontScale,
                ).copy(showNowLine = true, nowMinutes = nowMin)
        val overflow = mutableMapOf<String, Boolean>()
        composeRule.setContent {
            CompositionLocalProvider(LocalTextOverflowReports provides overflow) {
                WithFontScale(fontScale) {
                    EbbTheme {
                        TodayTimeline(state = state, modifier = Modifier.height(900.dp))
                    }
                }
            }
        }
        composeRule.waitForIdle()
        assertEquals(false, overflow[TIMELINE_NOW_BADGE_TAG])
        composeRule.onNodeWithText(expectedPrefix, useUnmergedTree = true).assertExists()
    }
}

