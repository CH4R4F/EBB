@file:Suppress("LongMethod", "MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

enum class FocusVisualMode {
    Setup,
    RunningRing,
    TimesUp,
    Hyperfocus,
    BreakEnded,
}

data class FocusPresentationState(
    val title: String = "Write project brief",
    val subtitle: String = "10:00–11:15 · steps 2/5",
    val mode: FocusVisualMode = FocusVisualMode.Setup,
    val timerLabel: String = "33:00",
    val message: String? = null,
)

@Composable
fun FocusScreenDesign(
    state: FocusPresentationState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { EbbSymbol("close_fullscreen", fontSize = 22.sp) }
            Text("Focus", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = EbbTypography.labelL)
            IconButton(onClick = {}) { EbbSymbol("more_vert", fontSize = 22.sp) }
        }
        Text(state.title, style = EbbTypography.headlineM, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(
            state.subtitle,
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        when (state.mode) {
            FocusVisualMode.Setup -> FocusSetupBody()
            FocusVisualMode.RunningRing -> FocusRingBody(state.timerLabel)
            FocusVisualMode.TimesUp ->
                FocusMessageBody(
                    "Time's up",
                    "Nice work. Mark done or take 5?",
                    primary = "Done",
                    secondary = "5 more min",
                )
            FocusVisualMode.Hyperfocus ->
                FocusMessageBody(
                    "Still going?",
                    "You've been at this a while. Stretch or keep going?",
                    primary = "Keep going",
                    secondary = "Take a breather",
                )
            FocusVisualMode.BreakEnded -> FocusMessageBody("Break over", "Ready to jump back in?", primary = "Resume", secondary = null)
        }
    }
}

@Composable
private fun FocusSetupBody() {
    Column(modifier = Modifier.padding(EbbSpacing.s4), verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
        Text("Timer", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TimerOption("Until block ends", "33 min · ends 11:15", selected = true)
        TimerOption("Fixed length", "5 · 15 · 25 · 45 · 60 min", selected = false)
        TimerOption("Count up", "Open-ended, for just starting", selected = false)
        TimerOption("Intervals", "25 work / 5 break × 4", selected = false)
        Surface(
            shape = EbbShapes.full,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = EbbSpacing.s4),
        ) {
            Text(
                "Start focus",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                style = EbbTypography.labelL,
            )
        }
    }
}

@Composable
private fun TimerOption(
    title: String,
    subtitle: String,
    selected: Boolean,
) {
    Surface(
        shape = EbbShapes.md,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            EbbSymbol("timer", fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f).padding(horizontal = EbbSpacing.s2)) {
                Text(title, style = EbbTypography.bodyL)
                Text(subtitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FocusRingBody(label: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(EbbSpacing.s6),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(200.dp),
            shape = CircleShape,
            border = BorderStroke(8.dp, MaterialTheme.colorScheme.primary),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(label, style = EbbTypography.headlineL)
                Text("remaining", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FocusMessageBody(
    title: String,
    body: String,
    primary: String,
    secondary: String?,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(EbbSpacing.s6),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = EbbTypography.headlineM)
        Text(body, style = EbbTypography.bodyL, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = EbbSpacing.s3))
        Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary) {
            Text(primary, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
        }
        secondary?.let {
            Text(
                it,
                style = EbbTypography.labelL,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = EbbSpacing.s3),
            )
        }
    }
}

@Composable
fun FocusAppPreview(
    darkTheme: Boolean,
    state: FocusPresentationState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        FocusScreenDesign(state = state)
    }
}
