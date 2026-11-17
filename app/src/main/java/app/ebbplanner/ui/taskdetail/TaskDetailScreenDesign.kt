@file:Suppress("LongMethod")

package app.ebbplanner.ui.taskdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun TaskDetailScreenDesign(
    state: TaskDetailPresentationState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = EbbSpacing.s1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                EbbSymbol("close", fontSize = 22.sp)
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                EbbSymbol("more_vert", fontSize = 22.sp)
            }
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = EbbSpacing.s4),
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s3)) {
                TaskRing()
                Text(state.title, style = EbbTypography.headlineM, modifier = Modifier.weight(1f))
            }
            DetailRow("event", state.whenLabel, state.dateLabel)
            DetailRow("timer", state.estimate, "Estimate")
            DetailRow("bolt", "High", "Energy", energy = state.energySelected)
            DetailRow("palette", state.colorLabel, "Color & icon")
            DetailRow("notifications", state.remindersLabel, "Reminders")
            DetailRow("repeat", state.repeatLabel, "Repeat")
            HorizontalDivider(modifier = Modifier.padding(vertical = EbbSpacing.s3))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Steps ", style = EbbTypography.titleM)
                Text(
                    state.subtaskProgress,
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.subtasks.forEach { sub ->
                SubtaskRow(sub)
            }
            if (state.notes.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = EbbSpacing.s3))
                Text("Notes", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(state.notes, style = EbbTypography.bodyM, modifier = Modifier.padding(top = EbbSpacing.s2))
            }
        }
        TaskDetailBottomBar()
    }
}

@Composable
private fun TaskRing() {
    Surface(
        modifier = Modifier.size(28.dp),
        shape = EbbShapes.full,
        border =
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.outline,
            ),
    ) {}
}

@Composable
private fun DetailRow(
    icon: String,
    line1: String,
    line2: String,
    energy: Int? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = EbbSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbSymbol(icon, fontSize = 20.sp, modifier = Modifier.padding(end = EbbSpacing.s3))
        Column(modifier = Modifier.weight(1f)) {
            Text(line1, style = EbbTypography.bodyL)
            Text(line2, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (energy != null) {
            Text("High", style = EbbTypography.labelL)
        } else {
            EbbSymbol("chevron_right", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SubtaskRow(row: TaskSubtaskRow) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbSymbol(
            if (row.done) "check_box" else "check_box_outline_blank",
            fontSize = 20.sp,
            filled = row.done,
            modifier = Modifier.padding(end = EbbSpacing.s2),
        )
        Text(row.title, style = EbbTypography.bodyL, modifier = Modifier.weight(1f))
        Text(row.estimate, style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TaskDetailBottomBar() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(EbbSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        Surface(
            shape = EbbShapes.full,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Text("Schedule…", modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp))
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                EbbSymbol("play_arrow", fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary)
                Text("Start focus", color = MaterialTheme.colorScheme.onPrimary, style = EbbTypography.labelL)
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

@Composable
fun EstimatePickerDesign(
    state: EstimatePickerState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(EbbSpacing.s4),
    ) {
        Text("Estimate", style = EbbTypography.titleL)
        Row(
            modifier = Modifier.padding(top = EbbSpacing.s3),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            state.options.forEach { min ->
                val sel = min == state.selectedMinutes
                Surface(
                    shape = EbbShapes.full,
                    color =
                        if (sel) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                ) {
                    Text(
                        "$min min",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = EbbTypography.labelL,
                    )
                }
            }
        }
    }
}
