package app.ebbplanner.ui.today

import app.ebbplanner.ui.WithFontScale

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.ui.text.LocalTextOverflowReports
import app.ebbplanner.ui.theme.EbbTheme
import app.ebbplanner.ui.time.ClockFormat
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
class TimelineNowBadgeMinuteSweepTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun nowBadge_f200_keyTimes_fullTextAndNoOverflow() = sweepBadgeMinutes(2f, listOf(10 * 60, 20 * 60, 23 * 60 + 59))

    @Test
    fun nowBadge_f100_everyHour_onTheHourAnd59_noOverflow() = sweepBadgeMinutes(1f, hourBoundaryMinutes())

    @Test
    fun nowBadge_f200_everyHour_onTheHourAnd59_noOverflow() = sweepBadgeMinutes(2f, hourBoundaryMinutes())

    @Test
    fun nowBadge_f200_everyMinute_24h_fullTextAndNoOverflow() {
        setClock24Hour(true)
        sweepBadgeMinutes(2f, (0 until 1440).toList(), is24Hour = true)
    }

    @Test
    fun nowBadge_f200_everyMinute_12h_fullTextAndNoOverflow() {
        setClock24Hour(false)
        sweepBadgeMinutes(2f, (0 until 1440).toList(), is24Hour = false)
    }

    private fun hourBoundaryMinutes(): List<Int> =
        buildList {
            for (hour in 0 until 24) {
                add(hour * 60)
                add(hour * 60 + 59)
            }
        }

    private fun sweepBadgeMinutes(
        fontScale: Float,
        minutes: List<Int>,
        is24Hour: Boolean = true,
    ) {
        setClock24Hour(is24Hour)
        val today = LocalDate.of(2025, 11, 17)
        var nowMin by mutableIntStateOf(minutes.first())
        val overflow = mutableMapOf<String, Boolean>()
        composeRule.setContent {
            CompositionLocalProvider(LocalTextOverflowReports provides overflow) {
                WithFontScale(fontScale) {
                    EbbTheme {
                        val state =
                            TodayPresentationMapper
                                .from(
                                    TodayUiState(date = today, isLoading = false, is24Hour = is24Hour),
                                    nowMin,
                                    fontScale,
                                ).copy(showNowLine = true, nowMinutes = nowMin)
                        TodayTimeline(state = state, modifier = Modifier.height(900.dp))
                    }
                }
            }
        }
        minutes.forEach { minute ->
            overflow.clear()
            composeRule.runOnIdle { nowMin = minute }
            composeRule.waitForIdle()
            val expected = ClockFormat.minutesToClockCompact(minute, is24Hour)
            assertTrue(
                "overflow at scale=$fontScale minute=$minute ($expected) 24h=$is24Hour",
                overflow[TIMELINE_NOW_BADGE_TAG] != true,
            )
            composeRule.onNodeWithText(expected, useUnmergedTree = true).assertExists()
        }
    }

    private fun setClock24Hour(enabled: Boolean) {
        Settings.System.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.System.TIME_12_24,
            if (enabled) "24" else "12",
        )
    }
}

