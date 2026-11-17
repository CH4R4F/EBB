@file:Suppress("LongMethod", "MaxLineLength", "NewLineAtEndOfFile", "MagicNumber")

package app.ebbplanner.ui.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.components.EbbRadioDot
import app.ebbplanner.ui.components.EbbSegmentedChips
import app.ebbplanner.ui.components.EbbSwitchRow
import app.ebbplanner.ui.components.FocusProgressRing
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

enum class FocusVisualMode {
    Setup,
    RunningRing,
    RunningBar,
    TimesUp,
    TimesUpLockscreen,
    Hyperfocus,
    BreakEnded,
}

data class FocusPresentationState(
    val title: String = "Write project brief",
    val subtitle: String = "10:00–11:15 · steps 2/5",
    val mode: FocusVisualMode = FocusVisualMode.Setup,
    val timerLabel: String = "33:00",
    val runningTitle: String = "Emails",
    val topBarLabel: String = "Focus",
)

@Composable
fun FocusScreenDesign(
    state: FocusPresentationState,
    modifier: Modifier = Modifier,
) {
    when (state.mode) {
        FocusVisualMode.TimesUpLockscreen -> FocusTimesUpLockscreen(state.runningTitle)
        else ->
            Column(modifier = modifier.fillMaxSize()) {
                FocusTopBar(state.topBarLabel)
                if (state.mode == FocusVisualMode.Setup) {
                    Text(
                        state.title,
                        style = EbbTypography.headlineM,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    Text(
                        state.subtitle,
                        style = EbbTypography.bodyM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                when (state.mode) {
                    FocusVisualMode.Setup -> FocusSetupBody()
                    FocusVisualMode.RunningRing -> FocusRunningBody(state, ring = true)
                    FocusVisualMode.RunningBar -> FocusRunningBody(state, ring = false)
                    FocusVisualMode.TimesUp -> FocusTimesUpInApp(state)
                    FocusVisualMode.Hyperfocus -> FocusHyperfocusBody(state)
                    FocusVisualMode.BreakEnded -> FocusBreakEndedBody()
                    FocusVisualMode.TimesUpLockscreen -> Unit
                }
            }
    }
}

@Composable
private fun FocusTopBar(label: String) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {}) { EbbSymbol("close_fullscreen", fontSize = 22.sp) }
        Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = EbbTypography.labelL)
        IconButton(onClick = {}) { EbbSymbol("more_vert", fontSize = 22.sp) }
    }
}

@Composable
private fun FocusSetupBody() {
    Column(Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = EbbSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Timer",
                style = EbbTypography.labelL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = EbbSpacing.s4),
            )
            TimerOption("hourglass_bottom", "Until block ends", "33 min · ends 11:15", selected = true)
            TimerOption("timer", "Fixed length", "5 · 15 · 25 · 45 · 60 min", selected = false)
            TimerOption("trending_up", "Count up", "Open-ended, for just starting", selected = false)
            TimerOption("restart_alt", "Intervals", "25 work / 5 break × 4", selected = false)
            Text(
                "Show as",
                style = EbbTypography.labelL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = EbbSpacing.s4),
            )
            EbbSegmentedChips(
                labels = listOf("Ring", "Bar"),
                icons = listOf(null, "linear_scale"),
                selectedIndex = 0,
                selectedUsesTertiary = true,
            )
            EbbSwitchRow("Hide numbers", "Just the shape, less clock-watching", checked = false)
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EbbSymbol("play_arrow", fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary)
                    Text(
                        "Start focus",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = EbbTypography.labelL,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            Surface(
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EbbSymbol("timer", fontSize = 20.sp)
                    Text("Just 5 minutes", style = EbbTypography.labelL, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun TimerOption(
    icon: String,
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
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            EbbSymbol(icon, fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(title, style = EbbTypography.bodyL)
                Text(subtitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            EbbRadioDot(selected = selected)
        }
    }
}

@Composable
private fun FocusRunningBody(
    state: FocusPresentationState,
    ring: Boolean,
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            state.title,
            style = EbbTypography.headlineM,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        if (ring) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                FocusProgressRing(progress = 0.45f, label = state.timerLabel, subtitle = "left of 1 h 15 min")
            }
        } else {
            Column(
                Modifier.fillMaxWidth().weight(1f).padding(horizontal = EbbSpacing.s6),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.timerLabel, style = EbbTypography.headlineL, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text(
                    "remaining",
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                LinearProgressIndicator(
                    progress = { 0.55f },
                    modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s4).height(12.dp).clip(EbbShapes.full),
                )
            }
        }
        FocusStepsCard()
        FocusControlRow()
    }
}

@Composable
private fun FocusStepsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4),
        shape = EbbShapes.md,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Steps 2/5", style = EbbTypography.titleM, modifier = Modifier.weight(1f))
                EbbSymbol("expand_less", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StepRow("Write the first draft")
            StepRow("Ask Sam for feedback")
        }
    }
}

@Composable
private fun StepRow(label: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(20.dp), shape = CircleShape, border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)) {}
        Text(label, style = EbbTypography.bodyL, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun FocusControlRow() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s3),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        FocusControl("pause", "Pause")
        FocusControl("+5", "5 more min", textOnly = true)
        FocusControl("check", "Done", filled = true)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        EbbSymbol("psychology_alt", fontSize = 18.sp)
        Text(
            "I got distracted",
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
        )
    }
}

@Composable
private fun FocusControl(
    icon: String,
    label: String,
    filled: Boolean = false,
    textOnly: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (textOnly) {
                    Text(icon, style = EbbTypography.labelL)
                } else {
                    EbbSymbol(
                        icon,
                        fontSize = 32.sp,
                        color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Text(
            label,
            style = EbbTypography.labelM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun FocusTimesUpInApp(state: FocusPresentationState) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(
                state.runningTitle,
                style = EbbTypography.headlineM,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                FocusProgressRing(progress = 1f, label = "0:00", subtitle = "Time's up")
            }
            Spacer(Modifier.height(80.dp))
        }
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp),
                shape = EbbShapes.lg,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    EbbSymbol("hourglass_empty", fontSize = 32.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(
                        buildAnnotatedString {
                            append("Time's up for ")
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append(state.runningTitle) }
                            append(".")
                        },
                        style = EbbTypography.headlineM,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        "How's it going?",
                        style = EbbTypography.bodyL,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Surface(
                        shape = EbbShapes.full,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    ) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) {
                            EbbSymbol("check", fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary)
                            Text(
                                "Done",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = EbbTypography.labelL,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(Modifier.weight(1f), shape = EbbShapes.full, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                            Text(
                                "5 more min",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp),
                                style = EbbTypography.labelL,
                            )
                        }
                        Surface(Modifier.weight(1f), shape = EbbShapes.full, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                            Text(
                                "15 more min",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp),
                                style = EbbTypography.labelL,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusTimesUpLockscreen(taskTitle: String) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text("10:42", style = EbbTypography.headlineL)
            Spacer(Modifier.height(48.dp))
            EbbSymbol("hourglass_empty", fontSize = 48.sp, color = MaterialTheme.colorScheme.secondary)
            Text(
                "Time's up",
                style = EbbTypography.headlineL,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(taskTitle, style = EbbTypography.titleL, color = MaterialTheme.colorScheme.primary)
            Surface(
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            ) {
                Text(
                    "Done",
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 14.dp),
                    style = EbbTypography.labelL,
                )
            }
        }
    }
}

@Composable
private fun FocusHyperfocusBody(state: FocusPresentationState) {
    Box(Modifier.fillMaxSize()) {
        FocusRunningBody(state.copy(timerLabel = "48:12"), ring = true)
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
            Surface(shape = EbbShapes.lg, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.padding(24.dp)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Still going?", style = EbbTypography.headlineM)
                    Text(
                        "You've been at this a while. Stretch or keep going?",
                        style = EbbTypography.bodyL,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                    Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary) {
                        Text(
                            "Keep going",
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        )
                    }
                    Text(
                        "Take a breather",
                        style = EbbTypography.labelL,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusBreakEndedBody() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Surface(Modifier.size(180.dp), shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                EbbSymbol("self_improvement", fontSize = 56.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Text("Break's over", style = EbbTypography.headlineL, modifier = Modifier.padding(top = 24.dp))
        Text(
            "Ready for round 3 of 4? Or call it here — both are fine.",
            style = EbbTypography.bodyL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        Spacer(Modifier.weight(1f))
        Column(
            Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Back to work",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(vertical = 14.dp),
                    style = EbbTypography.labelL,
                )
            }
            Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "2 more minutes",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 14.dp),
                    style = EbbTypography.labelL,
                )
            }
            Text(
                "End session",
                style = EbbTypography.labelL,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
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
