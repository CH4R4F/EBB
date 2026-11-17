package app.ebbplanner.ui.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTheme

/** Same root as [app.ebbplanner.MainActivity]: token theme, no Material You, token background. */
@Composable
fun OnboardingAppPreview(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    EbbTheme(darkTheme = darkTheme, dynamicColor = false) {
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = EbbSpacing.s6),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            content()
        }
    }
}

@Composable
fun onboardingTitleStyle() =
    app.ebbplanner.ui.theme.EbbTypography.onboardingTitle.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
