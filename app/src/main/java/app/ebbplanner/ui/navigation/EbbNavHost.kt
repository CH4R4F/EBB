package app.ebbplanner.ui.navigation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import app.ebbplanner.ui.AppEntryViewModel
import app.ebbplanner.ui.main.MainShell
import app.ebbplanner.ui.onboarding.OnboardingCalendarScreen
import app.ebbplanner.ui.onboarding.OnboardingDayShapeRoute
import app.ebbplanner.ui.onboarding.OnboardingDoneRoute
import app.ebbplanner.ui.onboarding.OnboardingExactAlarmsScreen
import app.ebbplanner.ui.onboarding.OnboardingNotificationsScreen
import app.ebbplanner.ui.onboarding.OnboardingPreferencesRoute
import app.ebbplanner.ui.onboarding.OnboardingPromiseScreen
import app.ebbplanner.ui.onboarding.OnboardingTryItRoute
import app.ebbplanner.ui.onboarding.OnboardingViewModel
import app.ebbplanner.ui.onboarding.OnboardingWelcomeScreen

@Suppress("LongMethod")
@Composable
fun EbbNavHost(
    pendingFocusTaskId: String? = null,
    onFocusDeepLinkConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val appViewModel: AppEntryViewModel = hiltViewModel()
    val onboardingComplete by appViewModel.onboardingComplete.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val startDestination =
        if (onboardingComplete) {
            EbbDestinations.MAIN
        } else {
            EbbDestinations.ONBOARDING_GRAPH
        }

    fun skipToNotifications() {
        navController.navigate(EbbDestinations.ONBOARDING_NOTIFICATIONS)
    }

    fun afterNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            navController.navigate(EbbDestinations.ONBOARDING_EXACT_ALARMS)
        } else {
            navController.navigate(EbbDestinations.ONBOARDING_CALENDAR) {
                popUpTo(EbbDestinations.ONBOARDING_NOTIFICATIONS) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        navigation(
            startDestination = EbbDestinations.ONBOARDING_WELCOME,
            route = EbbDestinations.ONBOARDING_GRAPH,
        ) {
            composable(EbbDestinations.ONBOARDING_WELCOME) {
                OnboardingWelcomeScreen(
                    onGetStarted = { navController.navigate(EbbDestinations.ONBOARDING_PROMISE) },
                    onRestoreBackup = {
                        appViewModel.completeOnboardingFromRestore()
                        navController.navigate(EbbDestinations.MAIN) {
                            popUpTo(EbbDestinations.ONBOARDING_GRAPH) { inclusive = true }
                        }
                    },
                )
            }
            composable(EbbDestinations.ONBOARDING_PROMISE) {
                OnboardingPromiseScreen(
                    onContinue = { navController.navigate(EbbDestinations.ONBOARDING_DAY_SHAPE) },
                )
            }
            composable(EbbDestinations.ONBOARDING_DAY_SHAPE) {
                OnboardingDayShapeRoute(
                    onContinue = { navController.navigate(EbbDestinations.ONBOARDING_PREFERENCES) },
                    onSkip = { navController.navigate(EbbDestinations.ONBOARDING_PREFERENCES) },
                    viewModel = hiltViewModel(activity),
                )
            }
            composable(EbbDestinations.ONBOARDING_PREFERENCES) {
                OnboardingPreferencesRoute(
                    onContinue = { navController.navigate(EbbDestinations.ONBOARDING_TRY_IT) },
                    onSkip = { navController.navigate(EbbDestinations.ONBOARDING_TRY_IT) },
                    viewModel = hiltViewModel(activity),
                )
            }
            composable(EbbDestinations.ONBOARDING_TRY_IT) {
                OnboardingTryItRoute(
                    onSkip = { skipToNotifications() },
                    onNext = { skipToNotifications() },
                )
            }
            composable(EbbDestinations.ONBOARDING_NOTIFICATIONS) {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel(activity)
                val notificationPermissionLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                        onboardingViewModel.setRemindersEnabled(granted)
                        afterNotifications()
                    }
                OnboardingNotificationsScreen(
                    onAllow = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onboardingViewModel.setRemindersEnabled(true)
                            afterNotifications()
                        }
                    },
                    onSkip = {
                        onboardingViewModel.setRemindersEnabled(false)
                        afterNotifications()
                    },
                )
            }
            composable(EbbDestinations.ONBOARDING_EXACT_ALARMS) {
                OnboardingExactAlarmsScreen(
                    onOpenSettings = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent =
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = "package:${context.packageName}".toUri()
                                }
                            context.startActivity(intent)
                        }
                        navController.navigate(EbbDestinations.ONBOARDING_CALENDAR)
                    },
                    onSkip = { navController.navigate(EbbDestinations.ONBOARDING_CALENDAR) },
                    showExactAlarmStep = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                )
            }
            composable(EbbDestinations.ONBOARDING_CALENDAR) {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel(activity)
                val calendarPermissionLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                        onboardingViewModel.setLinkedCalendarCount(if (granted) 1 else 0)
                        navController.navigate(EbbDestinations.ONBOARDING_DONE)
                    }
                OnboardingCalendarScreen(
                    onAllow = { calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR) },
                    onSkip = {
                        onboardingViewModel.setLinkedCalendarCount(0)
                        navController.navigate(EbbDestinations.ONBOARDING_DONE)
                    },
                )
            }
            composable(EbbDestinations.ONBOARDING_DONE) {
                OnboardingDoneRoute(
                    onOpenMyDay = {
                        navController.navigate(EbbDestinations.MAIN) {
                            popUpTo(EbbDestinations.ONBOARDING_GRAPH) { inclusive = true }
                        }
                    },
                    viewModel = hiltViewModel(activity),
                )
            }
        }
        composable(EbbDestinations.MAIN) {
            MainShell(
                pendingFocusTaskId = pendingFocusTaskId,
                onFocusDeepLinkConsumed = onFocusDeepLinkConsumed,
            )
        }
    }
}
