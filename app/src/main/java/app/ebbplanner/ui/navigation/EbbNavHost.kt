package app.ebbplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.ebbplanner.ui.AppEntryViewModel
import app.ebbplanner.ui.main.MainShell
import app.ebbplanner.ui.onboarding.OnboardingDoneScreen
import app.ebbplanner.ui.onboarding.OnboardingPreferencesScreen
import app.ebbplanner.ui.onboarding.OnboardingPromiseScreen
import app.ebbplanner.ui.onboarding.OnboardingWelcomeScreen

@Composable
fun EbbNavHost() {
    val navController = rememberNavController()
    val appViewModel: AppEntryViewModel = hiltViewModel()
    val onboardingComplete by appViewModel.onboardingComplete.collectAsStateWithLifecycle()

    val startDestination =
        if (onboardingComplete) {
            EbbDestinations.MAIN
        } else {
            EbbDestinations.ONBOARDING_WELCOME
        }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(EbbDestinations.ONBOARDING_WELCOME) {
            OnboardingWelcomeScreen(
                onGetStarted = {
                    navController.navigate(EbbDestinations.ONBOARDING_PROMISE)
                },
                onRestoreBackup = {
                    navController.navigate(EbbDestinations.MAIN) {
                        popUpTo(EbbDestinations.ONBOARDING_WELCOME) { inclusive = true }
                    }
                },
            )
        }
        composable(EbbDestinations.ONBOARDING_PROMISE) {
            OnboardingPromiseScreen(
                onContinue = { navController.navigate(EbbDestinations.ONBOARDING_PREFERENCES) },
            )
        }
        composable(EbbDestinations.ONBOARDING_PREFERENCES) {
            OnboardingPreferencesScreen(
                onContinue = { navController.navigate(EbbDestinations.ONBOARDING_DONE) },
            )
        }
        composable(EbbDestinations.ONBOARDING_DONE) {
            OnboardingDoneScreen(
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
