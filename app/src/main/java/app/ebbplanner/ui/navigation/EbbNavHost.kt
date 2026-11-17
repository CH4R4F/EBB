package app.ebbplanner.ui.navigation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import app.ebbplanner.ui.onboarding.OnboardingTryItScreen
import app.ebbplanner.ui.onboarding.OnboardingWelcomeScreen

@Suppress("LongMethod")
@Composable
fun EbbNavHost() {
    val navController = rememberNavController()
    val appViewModel: AppEntryViewModel = hiltViewModel()
    val onboardingComplete by appViewModel.onboardingComplete.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            navController.navigate(EbbDestinations.ONBOARDING_EXACT_ALARMS)
        }

    val startDestination =
        if (onboardingComplete) {
            EbbDestinations.MAIN
        } else {
            EbbDestinations.ONBOARDING_WELCOME
        }

    fun skipToNotifications() {
        navController.navigate(EbbDestinations.ONBOARDING_NOTIFICATIONS)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(EbbDestinations.ONBOARDING_WELCOME) {
            OnboardingWelcomeScreen(
                onGetStarted = { navController.navigate(EbbDestinations.ONBOARDING_PROMISE) },
                onRestoreBackup = {
                    navController.navigate(EbbDestinations.MAIN) {
                        popUpTo(EbbDestinations.ONBOARDING_WELCOME) { inclusive = true }
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
            )
        }
        composable(EbbDestinations.ONBOARDING_PREFERENCES) {
            OnboardingPreferencesRoute(
                onContinue = { navController.navigate(EbbDestinations.ONBOARDING_TRY_IT) },
                onSkip = { navController.navigate(EbbDestinations.ONBOARDING_TRY_IT) },
            )
        }
        composable(EbbDestinations.ONBOARDING_TRY_IT) {
            OnboardingTryItScreen(
                onSkip = { skipToNotifications() },
                onNext = { skipToNotifications() },
            )
        }
        composable(EbbDestinations.ONBOARDING_NOTIFICATIONS) {
            OnboardingNotificationsScreen(
                onAllow = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        navController.navigate(EbbDestinations.ONBOARDING_EXACT_ALARMS)
                    }
                },
                onSkip = { navController.navigate(EbbDestinations.ONBOARDING_EXACT_ALARMS) },
            )
        }
        composable(EbbDestinations.ONBOARDING_EXACT_ALARMS) {
            OnboardingExactAlarmsScreen(
                onOpenSettings = {
                    val intent =
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                    context.startActivity(intent)
                    navController.navigate(EbbDestinations.ONBOARDING_CALENDAR)
                },
                onSkip = { navController.navigate(EbbDestinations.ONBOARDING_CALENDAR) },
            )
        }
        composable(EbbDestinations.ONBOARDING_CALENDAR) {
            OnboardingCalendarScreen(
                onAllow = { navController.navigate(EbbDestinations.ONBOARDING_DONE) },
                onSkip = { navController.navigate(EbbDestinations.ONBOARDING_DONE) },
            )
        }
        composable(EbbDestinations.ONBOARDING_DONE) {
            OnboardingDoneRoute(
                onOpenMyDay = {
                    navController.navigate(EbbDestinations.MAIN) {
                        popUpTo(EbbDestinations.ONBOARDING_WELCOME) { inclusive = true }
                    }
                },
            )
        }
        composable(EbbDestinations.MAIN) {
            MainShell()
        }
    }
}
