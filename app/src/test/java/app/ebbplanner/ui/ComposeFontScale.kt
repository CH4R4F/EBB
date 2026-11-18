package app.ebbplanner.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import org.robolectric.RuntimeEnvironment

@Composable
fun WithFontScale(
    scale: Float,
    content: @Composable () -> Unit,
) {
    val app = RuntimeEnvironment.getApplication()
    val base = app.resources.configuration
    val config = Configuration(base).apply { fontScale = scale }
    @Suppress("DEPRECATION")
    app.resources.updateConfiguration(config, app.resources.displayMetrics)
    val density =
        Density(
            density = app.resources.displayMetrics.density,
            fontScale = scale,
        )
    CompositionLocalProvider(
        LocalConfiguration provides config,
        LocalDensity provides density,
    ) {
        content()
    }
}