package app.ebbplanner.ui.today

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.WithFontScale
import app.ebbplanner.ui.text.LocalTextOverflowReports
import app.ebbplanner.ui.theme.EbbTheme
import app.ebbplanner.ui.time.ClockFormat
import org.junit.Assert.assertFalse
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
class TimelineTimeRangeLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun range_12h_solo_f100_noEllipsis() = assertRange(1f, lanes = 1, start = 22 * 60 + 45, duration = 90)

    @Test
    fun range_12h_solo_f200_noEllipsis() = assertRange(2f, lanes = 1, start = 22 * 60 + 45, duration = 45)

    @Test
    fun range_12h_solo30_f200_noEllipsis() = assertRange(2f, lanes = 1, start = 13 * 60, duration = 30)

    @Test
    fun range_12h_twoLane_f100_noEllipsis() = assertRange(1f, lanes = 2, start = 13 * 60, duration = 60)

    @Test
    fun range_12h_twoLane_f200_noEllipsis() = assertRange(2f, lanes = 2, start = 13 * 60, duration = 75)

    private fun assertRange(
        fontScale: Float,
        lanes: Int,
        start: Int,
        duration: Int,
    ) {
        setClock24Hour(false)
        val today = LocalDate.of(2025, 11, 17)
        val startClock = ClockFormat.minutesToLocalTime(start)
        val tasks =
            List(lanes) { index ->
                TaskEntity(
                    id = "r$index",
                    title = if (index == 0) "Deep work" else "Standup",
                    placement = TaskEntity.PLACEMENT_SCHEDULED,
                    date = today.toString(),
                    startLocalTime = startClock,
                    durationMin = duration,
                    createdAt = 1,
                    updatedAt = 1,
                )
            }
        val state =
            TodayPresentationMapper.from(
                TodayUiState(
                    date = today,
                    scheduledTasks = tasks,
                    isLoading = false,
                    is24Hour = false,
                ),
                nowMinutes = 9 * 60,
                fontScale = fontScale,
            )
        val overflow = mutableMapOf<String, Boolean>()
        composeRule.setContent {
            CompositionLocalProvider(LocalTextOverflowReports provides overflow) {
                WithFontScale(fontScale) {
                    EbbTheme {
                        TodayTimeline(state = state, modifier = Modifier.height(1600.dp))
                    }
                }
            }
        }
        composeRule.waitForIdle()
        val compact = ClockFormat.rangeDisplay(start, start + duration, is24Hour = false)
        val startLabel = ClockFormat.minutesToClockDisplay(start, is24Hour = false)
        val compactStart = ClockFormat.minutesToClockCompact(start, is24Hour = false)
        val allowed = setOf(compact, startLabel, compactStart)
        val visibleTags =
            tasks.map { timelineTaskRangeTag(it.id) }.filter { tag ->
                composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            }
        assertTrue("no range labels at f$fontScale lanes=$lanes", visibleTags.isNotEmpty())
        visibleTags.forEach { tag ->
            val node = composeRule.onNodeWithTag(tag, useUnmergedTree = true)
            val text =
                node.fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }
            assertFalse("$tag overflow at f$fontScale lanes=$lanes text=$text", overflow[tag] == true)
            assertFalse("$tag ellipsis at f$fontScale text=$text", text.contains("…") || text.contains("..."))
            assertTrue(
                "$tag expected compact or start. text=$text allowed=$allowed",
                text in allowed,
            )
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
