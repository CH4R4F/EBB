package app.ebbplanner.ui.main

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import app.ebbplanner.ui.components.EbbBottomNavigation
import app.ebbplanner.ui.inbox.InboxScreenDesign
import app.ebbplanner.ui.inbox.InboxPresentationState
import app.ebbplanner.ui.navigation.MainTab
import app.ebbplanner.ui.routines.RoutinesPresentationState
import app.ebbplanner.ui.routines.RoutinesScreenDesign
import app.ebbplanner.ui.routines.RoutinesVisualMode
import app.ebbplanner.ui.theme.EbbTheme
import app.ebbplanner.ui.week.WeekPresentationState
import app.ebbplanner.ui.week.WeekScreenDesign
import app.ebbplanner.ui.week.WeekVisualMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28])
class TabChromeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tabDesigns_omitBottomNavByDefault() {
        val navLabel = "Today"
        composeRule.setContent {
            EbbTheme {
                EbbBottomNavigation(selected = MainTab.Today)
            }
        }
        val alone = composeRule.onAllNodesWithText(navLabel, substring = true).fetchSemanticsNodes().size

        composeRule.setContent {
            EbbTheme {
                InboxScreenDesign(state = InboxPresentationState())
            }
        }
        val withInbox =
            composeRule.onAllNodesWithText(navLabel, substring = true).fetchSemanticsNodes().size
        assert(withInbox == alone)

        composeRule.setContent {
            EbbTheme {
                WeekScreenDesign(state = WeekPresentationState(mode = WeekVisualMode.Empty))
            }
        }
        val withWeek =
            composeRule.onAllNodesWithText(navLabel, substring = true).fetchSemanticsNodes().size
        assert(withWeek == alone)

        composeRule.setContent {
            EbbTheme {
                RoutinesScreenDesign(state = RoutinesPresentationState(mode = RoutinesVisualMode.Empty))
            }
        }
        val withRoutines =
            composeRule.onAllNodesWithText(navLabel, substring = true).fetchSemanticsNodes().size
        assert(withRoutines == alone)
    }
}