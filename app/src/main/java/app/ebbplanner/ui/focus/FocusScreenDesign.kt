@file:Suppress("LongMethod", "MaxLineLength", "NewLineAtEndOfFile", "MagicNumber")

package app.ebbplanner.ui.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.components.EbbJoinedSegmentedControl
import app.ebbplanner.ui.components.EbbRadioDot
import app.ebbplanner.ui.components.EbbSegmentSelectionStyle
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
    val taskId: String = "",
    val title: String = "Write project brief",
    val subtitle: String = "10:00–11:15 · steps 2/5",
    val mode: FocusVisualMode = FocusVisualMode.Setup,
    val timerLabel: String = "33:00",
    val runningTitle: String = "Emails",
    val topBarLabel: String = "Focus",
    val paused: Boolean = false,
    val ringProgress: Float = 0.45f,
    val showRing: Boolean = true,
)

@Composable
fun FocusScreenDesign(
    state: FocusPresentationState,
    modifier: Modifier = Modifier,
    callbacks: FocusCallbacks = FocusCallbacks(),
) {
    when (state.mode) {
        FocusVisualMode.TimesUpLockscreen -> FocusTimesUpLockscreen(state.runningTitle)
        else ->
            Column(modifier = modifier.fillMaxSize()) {
                FocusTopBar(state.topBarLabel, onClose = callbacks.onClose, onMenu = callbacks.onMenu)
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
                    FocusVisualMode.Setup -> FocusSetupBody(callbacks)
                    FocusVisualMode.RunningRing ->
                        FocusRunningBody(
                            state,
                            ring = state.showRing,
                            modifier = Modifier.weight(1f),
                            ringProgress = state.ringProgress,
                            callbacks = callbacks,
                        )
                    FocusVisualMode.RunningBar ->
                        FocusRunningBody(
                            state,
                            ring = false,
                            modifier = Modifier.weight(1f),
                            ringProgress = state.ringProgress,
                            callbacks = callbacks,
                        )
                    FocusVisualMode.TimesUp ->
                        FocusTimesUpInApp(
                            state,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            callbacks = callbacks,
                        )
                    FocusVisualMode.Hyperfocus -> FocusHyperfocusBody(state, modifier = Modifier.weight(1f).fillMaxWidth())
                    FocusVisualMode.BreakEnded -> FocusBreakEndedBody(Modifier.weight(1f))
                    FocusVisualMode.TimesUpLockscreen -> Unit
                }
            }
    }
}

@Composable
private fun FocusTopBar(
    label: String,
    onClose: () -> Unit,
    onMenu: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClose) { EbbSymbol("close_fullscreen", fontSize = 22.sp) }
        Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = EbbTypography.labelL)
        IconButton(onClick = onMenu) { EbbSymbol("more_vert", fontSize = 22.sp) }
    }
}

@Composable
private fun FocusSetupBody(callbacks: FocusCallbacks) {
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
            EbbJoinedSegmentedControl(
                labels = listOf("Ring", "Bar"),
                icons = listOf(null, "linear_scale"),
                selectedIndex = 0,
                selectionStyle = EbbSegmentSelectionStyle.SecondaryContainer,
            )
            EbbSwitchRow("Hide numbers", "Just the shape, less clock-watching", checked = false)
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = callbacks.onStartFocus,
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            ) {
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
                onClick = callbacks.onJustFiveMinutes,
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EbbSymbol("timer", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Just 5 minutes",
                        style = EbbTypography.labelL,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp),
                    )
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
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
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
    timerSubtitle: String = "left of 1 h 15 min",
    modifier: Modifier = Modifier,
    ringProgress: Float = 0.45f,
    ringDiameter: Dp = 272.dp,
    callbacks: FocusCallbacks = FocusCallbacks(),
) {
    Column(modifier.fillMaxSize()) {
        Text(
            state.title,
            style = EbbTypography.headlineM,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        if (ring) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                FocusProgressRing(
                    progress = ringProgress,
                    label = state.timerLabel,
                    subtitle = timerSubtitle,
                    diameter = ringDiameter,
                    strokeWidth = 12.dp,
                )
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
                    progress = { ringProgress },
                    modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s4).height(12.dp).clip(EbbShapes.full),
                )
            }
        }
        FocusStepsCard()
        FocusControlRow(state, callbacks)
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
        Surface(Modifier.size(20.dp), shape = EbbShapes.sm, border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)) {}
        Text(label, style = EbbTypography.bodyL, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun FocusControlRow(
    state: FocusPresentationState,
    callbacks: FocusCallbacks,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s3),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        FocusControl(
            if (state.paused) "play_arrow" else "pause",
            if (state.paused) "Resume" else "Pause",
            onClick = if (state.paused) callbacks.onResume else callbacks.onPause,
        )
        FocusControl("+5", "5 more min", textOnly = true, onClick = callbacks.onAddFive)
        FocusControl("check", "Done", filled = true, onClick = callbacks.onDone)
    }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = callbacks.onDistracted),
        horizontalArrangement = Arrangement.Center,
    ) {
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
    onClick: () -> Unit = {},
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
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
private fun FocusTimesUpInApp(
    state: FocusPresentationState,
    modifier: Modifier = Modifier,
    callbacks: FocusCallbacks = FocusCallbacks(),
) {
    val runningState =
        state.copy(
            title = state.runningTitle,
            timerLabel = "0:00",
            topBarLabel = "Fixed · 25 min",
        )
    Box(modifier.fillMaxSize()) {
        FocusRunningBody(
            runningState,
            ring = true,
            timerSubtitle = "Time's up",
            modifier = Modifier.fillMaxSize(),
            ringProgress = 1f,
            ringDiameter = 272.dp,
        )
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)))
        Surface(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 212.dp, start = 24.dp, end = 24.dp),
            shape = EbbShapes.lg,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(Modifier.padding(24.dp).fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                        onClick = callbacks.onTimesUpDone,
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
                }
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = callbacks.onTimesUpAddFive,
                        shape = EbbShapes.full,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text("5 more min", modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = EbbTypography.labelL)
                    }
                    Surface(
                        onClick = callbacks.onTimesUpAddFifteen,
                        shape = EbbShapes.full,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text("15 more min", modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = EbbTypography.labelL)
                    }
                }
                Row(
                    Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(Modifier.clickable(onClick = callbacks.onTimesUpShiftDay)) {
                        EbbSymbol("more_time", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Shift my day", color = MaterialTheme.colorScheme.primary, style = EbbTypography.labelL)
                    }
                    Row(Modifier.clickable(onClick = callbacks.onTimesUpBreak)) {
                        EbbSymbol("coffee", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Take a break", color = MaterialTheme.colorScheme.primary, style = EbbTypography.labelL)
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusTimesUpLockscreen(taskTitle: String) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("10:15", style = EbbTypography.headlineL, fontWeight = FontWeight.Light)
            Text(
                "Mon 16 Nov",
                style = EbbTypography.bodyL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                EbbSymbol("hourglass_empty", fontSize = 32.sp, color = MaterialTheme.colorScheme.primaryContainer)
                Text(
                    buildAnnotatedString {
                        append("Time's up for ")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append(taskTitle) }
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
                    Surface(
                        Modifier.weight(1f),
                        shape = EbbShapes.full,
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        Text(
                            "5 more min",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp),
                            style = EbbTypography.labelL,
                        )
                    }
                    Surface(
                        Modifier.weight(1f),
                        shape = EbbShapes.full,
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        Text(
                            "15 more min",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp),
                            style = EbbTypography.labelL,
                        )
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                        EbbSymbol("more_time", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Shift my day",
                            color = MaterialTheme.colorScheme.primary,
                            style = EbbTypography.labelL,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                        EbbSymbol("coffee", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Take a break",
                            color = MaterialTheme.colorScheme.primary,
                            style = EbbTypography.labelL,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        }
        Column(Modifier.padding(bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            EbbSymbol("lock", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Unlock to open Ebb",
                style = EbbTypography.labelM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun FocusHyperfocusBody(
    state: FocusPresentationState,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        FocusRunningBody(
            state.copy(title = "Design poster", timerLabel = "1:30:04", topBarLabel = "Count up"),
            ring = true,
            timerSubtitle = "focused so far",
        )
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            shape = EbbShapes.lg,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    EbbSymbol("water_drop", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("You've been at this for 90 minutes.", style = EbbTypography.titleM)
                        Text("Water? Stretch?", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Wrap up",
                        color = MaterialTheme.colorScheme.primary,
                        style = EbbTypography.labelL,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                    Text(
                        "Break",
                        color = MaterialTheme.colorScheme.primary,
                        style = EbbTypography.labelL,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                    Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                        Text(
                            "Keep going 15",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = EbbTypography.labelL,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusBreakEndedBody(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
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
