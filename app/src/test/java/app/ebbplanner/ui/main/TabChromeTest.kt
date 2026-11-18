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
    fun inboxDesign_omitsBottomNavByDefault() {
        composeRule.setContent {
            EbbTheme { InboxScreenDesign(state = InboxPresentationState()) }
        }
        composeRule.onAllNodesWithText("Today", substring = true).assertCountEquals(0)
    }

    @Test
    fun weekDesign_omitsBottomNavByDefault() {
        composeRule.setContent {
            EbbTheme { WeekScreenDesign(state = WeekPresentationState(mode = WeekVisualMode.Empty)) }
        }
        composeRule.onAllNodesWithText("Today", substring = true).assertCountEquals(0)
    }

    @Test
    fun routinesDesign_omitsBottomNavByDefault() {
        composeRule.setContent {
            EbbTheme {
                RoutinesScreenDesign(state = RoutinesPresentationState(mode = RoutinesVisualMode.Empty))
            }
        }
        composeRule.onAllNodesWithText("Today", substring = true).assertCountEquals(0)
    }

    @Test
    fun bottomNavPreview_showsTodayTab() {
        composeRule.setContent {
            EbbTheme { EbbBottomNavigation(selected = MainTab.Today) }
        }
        composeRule.onAllNodesWithText("Today", substring = true).assertCountEquals(1)
    }
}