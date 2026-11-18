@file:Suppress("LongMethod", "MagicNumber", "MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.taskdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import app.ebbplanner.R
import app.ebbplanner.ui.components.EbbListLeadingIcon
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreenDesign(
    state: TaskDetailPresentationState,
    modifier: Modifier = Modifier,
    callbacks: TaskDetailCallbacks = TaskDetailCallbacks(),
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TaskDetailTopBar(
                done = state.done,
                onClose = callbacks.onClose,
                onMarkDone = callbacks.onMarkDone,
                onDeleteTask = callbacks.onDeleteTask,
            )
            when (state.mode) {
                TaskDetailMode.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading…", style = EbbTypography.bodyM)
                    }
                    return
                }
                TaskDetailMode.NotFound -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Task not found", style = EbbTypography.titleM)
                            Text(
                                "It may have been deleted.",
                                style = EbbTypography.bodyM,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = EbbSpacing.s2),
                            )
                        }
                    }
                    return
                }
                else -> Unit
            }
            state.recurringBanner?.let { banner ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        banner,
                        modifier = Modifier.padding(EbbSpacing.s3),
                        style = EbbTypography.bodyM,
                    )
                }
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = EbbSpacing.s4)
                        .padding(bottom = 96.dp),
            ) {
                TitleRow(state, onMarkDone = callbacks.onMarkDone)
                DetailListRow(
                    icon = "event",
                    line1 = state.whenLabel,
                    line2 = state.dateLabel,
                    tall = true,
                    onClick = callbacks.onOpenSchedule,
                )
                EstimateRow(
                    state.estimate,
                    onClick = callbacks.onOpenDuration,
                    onDecrease = callbacks.onEstimateDecrease,
                    onIncrease = callbacks.onEstimateIncrease,
                )
                PinnedRow(state.pinned, onPinnedChange = callbacks.onPinnedChange)
                EnergySegmentedRow(state.energySelected, onEnergyChange = callbacks.onEnergyChange)
                ColorIconRow(state, onClick = callbacks.onOpenColorIcon)
                DetailListRow(
                    icon = "notifications",
                    line1 = state.remindersLabel,
                    line2 = "Reminders",
                    tall = true,
                    onClick = callbacks.onOpenReminders,
                )
                DetailListRow(
                    icon = "repeat",
                    line1 = state.repeatLabel,
                    line2 = "Repeat",
                    tall = true,
                    onClick = callbacks.onOpenRepeat,
                )
                DetailListRow(
                    icon = "air",
                    line1 = "Breather after",
                    line2 = state.breatherSubtitle,
                    tall = true,
                    onClick = callbacks.onApplyBreather,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = EbbSpacing.s2))
                StepsHeader(state.subtaskProgress)
                state.subtasks.forEach { SubtaskRow(it, onToggle = callbacks.onToggleSubtask) }
                AddStepRow(onClick = callbacks.onOpenBreakDown)
                if (state.notes.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = EbbSpacing.s2))
                    Text("Notes", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(state.notes, style = EbbTypography.bodyM, modifier = Modifier.padding(top = EbbSpacing.s2))
                }
            }
        }
        TaskDetailBottomBar(
            onStartFocus = callbacks.onStartFocus,
            onOpenSchedule = callbacks.onOpenSchedule,
            onOpenBreakDown = callbacks.onOpenBreakDown,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun TaskDetailTopBar(
    done: Boolean,
    onClose: () -> Unit = {},
    onMarkDone: () -> Unit = {},
    onDeleteTask: () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = EbbSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
            EbbSymbol("close", fontSize = 22.sp)
        }
        IconButton(onClick = onMarkDone, modifier = Modifier.size(48.dp)) {
            EbbSymbol(
                if (done) "check_circle" else "radio_button_unchecked",
                fontSize = 22.sp,
                filled = done,
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(48.dp)) {
                EbbSymbol("more_vert", fontSize = 22.sp)
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.task_delete)) },
                    onClick = {
                        menuExpanded = false
                        onDeleteTask()
                    },
                )
            }
        }
    }
}

@Composable
private fun TitleRow(
    state: TaskDetailPresentationState,
    onMarkDone: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s3),
        modifier = Modifier.padding(vertical = EbbSpacing.s2),
    ) {
        TaskCompletionRing(done = state.done, onClick = onMarkDone)
        Text(
            state.title,
            style = EbbTypography.headlineM,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TaskCompletionRing(
    done: Boolean,
    onClick: () -> Unit = {},
) {
    val color =
        if (done) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }
    Surface(
        onClick = onClick,
        modifier = Modifier.size(28.dp),
        shape = CircleShape,
        border = BorderStroke(2.dp, color),
        color = MaterialTheme.colorScheme.background,
    ) {}
}

@Composable
private fun DetailListRow(
    icon: String,
    line1: String,
    line2: String,
    tall: Boolean = false,
    onClick: () -> Unit = {},
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(if (tall) 72.dp else 56.dp)
                .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbListLeadingIcon(icon = icon)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = EbbSpacing.s3),
        ) {
            Text(line1, style = EbbTypography.bodyL)
            Text(line2, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        EbbSymbol("chevron_right", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EstimateRow(
    estimate: String,
    onClick: () -> Unit = {},
    onDecrease: () -> Unit = {},
    onIncrease: () -> Unit = {},
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbListLeadingIcon(icon = "timer")
        Column(modifier = Modifier.weight(1f).padding(horizontal = EbbSpacing.s3)) {
            Text(estimate, style = EbbTypography.bodyL)
            Text("Estimate", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1)) {
            RoundAdjustButton("remove", onClick = onDecrease)
            RoundAdjustButton("add", onClick = onIncrease)
        }
    }
}

@Composable
private fun RoundAdjustButton(
    icon: String,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            EbbSymbol(icon, fontSize = 20.sp)
        }
    }
}

@Composable
private fun PinnedRow(
    pinned: Boolean,
    onPinnedChange: (Boolean) -> Unit = {},
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbListLeadingIcon(icon = "keep", filled = pinned)
        Column(modifier = Modifier.weight(1f).padding(horizontal = EbbSpacing.s3)) {
            Text("Pinned", style = EbbTypography.bodyL)
            Text("Stays put when you shift your day", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = pinned,
            onCheckedChange = onPinnedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
        )
    }
}

@Composable
private fun EnergySegmentedRow(
    selected: Int,
    onEnergyChange: (Int) -> Unit = {},
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbListLeadingIcon(icon = "bolt")
        Column(modifier = Modifier.weight(1f).padding(horizontal = EbbSpacing.s3)) {
            Text("Energy", style = EbbTypography.bodyL)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1)) {
            EnergyChip("Low", 1, selected == 1, onClick = { onEnergyChange(1) })
            EnergyChip("Med", 2, selected == 2, onClick = { onEnergyChange(2) })
            EnergyChip("High", 3, selected == 3, onClick = { onEnergyChange(3) })
        }
    }
}

@Composable
private fun EnergyChip(
    label: String,
    level: Int,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            EnergyBars(level)
            Text(label, style = EbbTypography.labelM)
        }
    }
}

@Composable
private fun EnergyBars(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.Bottom) {
        listOf(4.dp, 6.dp, 8.dp).forEachIndexed { index, h ->
            Box(
                modifier =
                    Modifier
                        .size(width = 3.dp, height = h)
                        .background(
                            if (index < level) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            EbbShapes.xs,
                        ),
            )
        }
    }
}

@Composable
private fun ColorIconRow(
    state: TaskDetailPresentationState,
    onClick: () -> Unit = {},
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbListLeadingIcon(icon = "palette")
        Column(modifier = Modifier.weight(1f).padding(horizontal = EbbSpacing.s3)) {
            Text(state.colorLabel, style = EbbTypography.bodyL)
            Text("Color & icon", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
            modifier = Modifier.size(40.dp),
            shape = EbbShapes.md,
            color = EbbTaskColors.tint(state.colorKey, dark),
        ) {
            Box(contentAlignment = Alignment.Center) {
                EbbSymbol(state.colorIconKey, fontSize = 20.sp)
            }
        }
        EbbSymbol("chevron_right", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StepsHeader(progress: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = EbbSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Steps ", style = EbbTypography.titleM)
        Text(progress, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        Text(
            "Break it down",
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.primary,
        )
        EbbSymbol("call_split", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SubtaskRow(
    row: TaskSubtaskRow,
    onToggle: (String) -> Unit = {},
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = { if (row.id.isNotBlank()) onToggle(row.id) },
            modifier = Modifier.size(24.dp),
            shape = EbbShapes.xs,
            color = if (row.done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            border =
                if (row.done) {
                    null
                } else {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                },
        ) {
            if (row.done) {
                Box(contentAlignment = Alignment.Center) {
                    EbbSymbol("check", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        Text(
            row.title,
            style = EbbTypography.bodyL,
            modifier = Modifier.weight(1f).padding(horizontal = EbbSpacing.s2),
        )
        Text(row.estimate, style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        EbbSymbol(
            "drag_indicator",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = EbbSpacing.s2),
        )
    }
}

@Composable
private fun AddStepRow(onClick: () -> Unit = {}) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbSymbol("add", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "Add a step",
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = EbbSpacing.s2),
        )
    }
}

@Composable
private fun TaskDetailBottomBar(
    onStartFocus: () -> Unit = {},
    onOpenSchedule: () -> Unit = {},
    onOpenBreakDown: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            Surface(
                onClick = onOpenSchedule,
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
                ) {
                    EbbSymbol("event_available", fontSize = 18.sp)
                    Text("Schedule…", style = EbbTypography.labelL)
                }
            }
            IconButton(onClick = onOpenBreakDown, modifier = Modifier.size(48.dp)) {
                EbbSymbol("spa", fontSize = 22.sp)
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            Surface(
                onClick = onStartFocus,
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.primary,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
                ) {
                    EbbSymbol("play_arrow", fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary)
                    Text(
                        "Start focus",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = EbbTypography.labelL,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDetailAppPreview(
    darkTheme: Boolean,
    state: TaskDetailPresentationState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        TaskDetailScreenDesign(state = state)
    }
}
