package app.ebbplanner

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.ui.navigation.EbbNavHost
import app.ebbplanner.ui.theme.EbbTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            val useMaterialYou by userPreferencesRepository.useMaterialYou.collectAsStateWithLifecycle(
                initialValue = false,
            )
            EbbTheme(dynamicColor = useMaterialYou) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EbbNavHost()
                }
            }
        }
    }
}
