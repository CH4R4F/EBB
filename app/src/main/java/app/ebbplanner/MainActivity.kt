package app.ebbplanner

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.reminders.FocusNotifier
import app.ebbplanner.ui.navigation.EbbNavHost
import app.ebbplanner.ui.theme.EbbTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    private val focusDeepLinkTaskId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        focusDeepLinkTaskId.value = intentFocusTaskId(intent)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            val useMaterialYou by userPreferencesRepository.useMaterialYou.collectAsStateWithLifecycle(
                initialValue = false,
            )
            val pendingFocus by focusDeepLinkTaskId
            EbbTheme(dynamicColor = useMaterialYou) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                ) {
                    EbbNavHost(
                        pendingFocusTaskId = pendingFocus,
                        onFocusDeepLinkConsumed = { focusDeepLinkTaskId.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        focusDeepLinkTaskId.value = intentFocusTaskId(intent)
    }

    private fun intentFocusTaskId(intent: Intent?): String? =
        intent?.getStringExtra(FocusNotifier.EXTRA_OPEN_FOCUS)?.takeIf { it.isNotBlank() }
}