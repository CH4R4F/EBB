package app.ebbplanner.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.ebbplanner.R
import app.ebbplanner.ui.navigation.MainTab
import app.ebbplanner.ui.today.TodayScreen

@Composable
fun MainShell() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: MainTab.Today.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon(), contentDescription = tab.label) },
                        label = { Text(mainTabLabel(tab)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Today.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(MainTab.Today.route) { TodayScreen() }
            composable(MainTab.Inbox.route) { InboxPlaceholderScreen() }
            composable(MainTab.Week.route) { WeekPlaceholderScreen() }
            composable(MainTab.Routines.route) { RoutinesPlaceholderScreen() }
        }
    }
}

@Composable
private fun MainTab.icon(): ImageVector =
    when (this) {
        MainTab.Today -> Icons.Outlined.Today
        MainTab.Inbox -> Icons.Outlined.Inbox
        MainTab.Week -> Icons.Outlined.CalendarMonth
        MainTab.Routines -> Icons.Outlined.Repeat
    }

@Composable
private fun mainTabLabel(tab: MainTab): String =
    when (tab) {
        MainTab.Today -> stringResource(R.string.tab_today)
        MainTab.Inbox -> stringResource(R.string.tab_inbox)
        MainTab.Week -> stringResource(R.string.tab_week)
        MainTab.Routines -> stringResource(R.string.tab_routines)
    }
