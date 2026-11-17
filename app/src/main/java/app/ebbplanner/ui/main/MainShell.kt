package app.ebbplanner.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.ebbplanner.ui.components.EbbBottomNavigation
import app.ebbplanner.ui.inbox.InboxScreen
import app.ebbplanner.ui.navigation.MainTab
import app.ebbplanner.ui.routines.RoutinesScreen
import app.ebbplanner.ui.today.TodayScreen
import app.ebbplanner.ui.week.WeekScreen

@Composable
fun MainShell() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: MainTab.Today.route
    val selected =
        MainTab.entries.firstOrNull { it.route == currentRoute } ?: MainTab.Today

    Scaffold(
        bottomBar = {
            EbbBottomNavigation(
                selected = selected,
                onTabSelected = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Today.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(MainTab.Today.route) { TodayScreen() }
            composable(MainTab.Inbox.route) { InboxScreen() }
            composable(MainTab.Week.route) { WeekScreen() }
            composable(MainTab.Routines.route) { RoutinesScreen() }
        }
    }
}
