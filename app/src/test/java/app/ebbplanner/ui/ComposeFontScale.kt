package app.ebbplanner.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Composable
fun WithFontScale(
    scale: Float,
    content: @Composable () -> Unit,
) {
    val current = LocalConfiguration.current
    val currentDensity = LocalDensity.current
    val config = Configuration(current).apply { fontScale = scale }
    val density =
        Density(
            density = currentDensity.density,
            fontScale = scale,
        )
    CompositionLocalProvider(
        LocalConfiguration provides config,
        LocalDensity provides density,
    ) {
        content()
    }
}
