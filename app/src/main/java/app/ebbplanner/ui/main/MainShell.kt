package app.ebbplanner.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.ebbplanner.reminders.FocusNotifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.ebbplanner.ui.components.EbbBottomNavigation
import app.ebbplanner.ui.inbox.InboxScreen
import app.ebbplanner.ui.navigation.AppRoutes
import app.ebbplanner.ui.navigation.FocusRunScreen
import app.ebbplanner.ui.navigation.MainTab
import app.ebbplanner.ui.navigation.MakeItFitScreen
import app.ebbplanner.ui.navigation.OverlapHelperScreen
import app.ebbplanner.ui.navigation.PlanDayScreen
import app.ebbplanner.ui.navigation.ShiftDayScreen
import app.ebbplanner.ui.navigation.TaskDetailRoute
import app.ebbplanner.ui.routines.RoutinesScreen
import app.ebbplanner.ui.settings.SettingsScreen
import app.ebbplanner.ui.taskdetail.TaskSheetType
import app.ebbplanner.ui.today.TodayScreen
import app.ebbplanner.ui.week.WeekScreen

@Suppress("LongMethod")
@Composable
fun MainShell() {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    LaunchedEffect(activity.intent) {
        val taskId = activity.intent?.getStringExtra(FocusNotifier.EXTRA_OPEN_FOCUS).orEmpty()
        if (taskId.isNotBlank()) {
            navController.navigate(AppRoutes.focusRun(taskId)) {
                launchSingleTop = true
            }
            activity.intent?.removeExtra(FocusNotifier.EXTRA_OPEN_FOCUS)
        }
    }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: MainTab.Today.route
    val selected =
        MainTab.entries.firstOrNull { currentRoute?.startsWith(it.route) == true } ?: MainTab.Today
    val showBottomBar =
        MainTab.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
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
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Today.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(MainTab.Today.route) {
                TodayScreen(
                    onOpenPlanDay = { navController.navigate(AppRoutes.PLAN_DAY) },
                    onOpenShiftDay = { navController.navigate(AppRoutes.SHIFT_DAY) },
                    onOpenOverlap = { navController.navigate(AppRoutes.OVERLAP_HELPER) },
                    onOpenMakeItFit = { navController.navigate(AppRoutes.MAKE_IT_FIT) },
                    onOpenTask = { id -> navController.navigate(AppRoutes.taskDetail(id)) },
                    onOpenTaskSchedule = { id ->
                        val sheet = TaskSheetType.toRouteArg(TaskSheetType.Schedule)
                        navController.navigate(AppRoutes.taskDetail(id, sheet))
                    },
                    onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) },
                )
            }
            composable(MainTab.Inbox.route) {
                InboxScreen(onOpenTask = { id -> navController.navigate(AppRoutes.taskDetail(id)) })
            }
            composable(MainTab.Week.route) { WeekScreen() }
            composable(MainTab.Routines.route) {
                RoutinesScreen(
                    onCreateRoutine = { id -> navController.navigate(AppRoutes.routineEdit(id)) },
                )
            }
            composable(
                route = AppRoutes.ROUTINE_EDIT,
                arguments = listOf(navArgument("routineId") { type = NavType.StringType }),
            ) {
                app.ebbplanner.ui.routines.RoutineEditorScreen(
                    onClose = { navController.popBackStack() },
                )
            }
            composable(AppRoutes.SHIFT_DAY) { ShiftDayScreen(navController = navController) }
            composable(AppRoutes.PLAN_DAY) { PlanDayScreen(navController = navController) }
            composable(AppRoutes.OVERLAP_HELPER) { OverlapHelperScreen(navController = navController) }
            composable(AppRoutes.MAKE_IT_FIT) { MakeItFitScreen(navController = navController) }
            composable(AppRoutes.SETTINGS) {
                SettingsScreen(onClose = { navController.popBackStack() })
            }
            composable(
                route = AppRoutes.FOCUS_RUN,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType }),
            ) {
                FocusRunScreen(navController = navController)
            }
            composable(
                route = AppRoutes.TASK_DETAIL,
                arguments =
                    listOf(
                        navArgument("taskId") { type = NavType.StringType },
                        navArgument("sheet") {
                            type = NavType.StringType
                            defaultValue = "none"
                        },
                    ),
            ) { entry ->
                val taskId = entry.arguments?.getString("taskId").orEmpty()
                val sheet = entry.arguments?.getString("sheet").orEmpty()
                TaskDetailRoute(
                    taskId = taskId,
                    initialSheet = sheet,
                    navController = navController,
                )
            }
        }
    }
}
