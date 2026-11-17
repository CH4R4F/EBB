@file:Suppress("LongMethod", "MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
fun ReminderPickerDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(title = "Reminders", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
            SheetToggleRow("Remind at start", "10:00", on = true)
            Text("Heads-up before", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SheetChips(listOf("5 min", "10 min", "15 min", "30 min", "60 min"), selected = 0)
            SheetToggleRow("At end", "Time to wrap up", on = false)
            Text("Style", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StyleSegment(selectedGentle = true)
            SheetToggleRow("Keep reminding until I respond", "Every 10 min, up to 3 times", on = true)
        }
    }
}

@Composable
fun RepeatEditorDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(title = "Repeat", modifier = modifier) {
        Column {
            SheetChips(listOf("Doesn't repeat", "Daily", "Weekdays", "Weekly", "Monthly", "Custom"), selected = 2)
            Text(
                "After you mark done",
                style = EbbTypography.labelL,
                modifier = Modifier.padding(top = EbbSpacing.s3),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SheetChips(listOf("Wait", "Next occurrence", "Ask me"), selected = 1)
        }
    }
}

@Composable
fun BreakItDownDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(title = "Break it down", modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("No steps yet", style = EbbTypography.bodyL, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(top = EbbSpacing.s3),
            ) {
                Text("Add first step", modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = EbbTypography.labelL)
            }
        }
    }
}

@Composable
fun SchedulePickerDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(title = "Schedule", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(EbbSpacing.s1)) {
            ScheduleRow("Today", "3 free slots")
            ScheduleRow("Tomorrow", "Morning looks open")
            ScheduleRow("Pick a day…", "Calendar")
            ScheduleRow("Anytime", "Inbox tray")
        }
    }
}

@Composable
private fun ScheduleRow(
    line1: String,
    line2: String,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(line1, style = EbbTypography.bodyL)
            Text(line2, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        EbbSymbol("chevron_right", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SheetToggleRow(
    line1: String,
    line2: String,
    on: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(line1, style = EbbTypography.bodyL)
            Text(line2, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = on, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SheetChips(
    labels: List<String>,
    selected: Int,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2), verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
        labels.forEachIndexed { index, label ->
            val sel = index == selected
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (sel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (sel) {
                        EbbSymbol("check", fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                    }
                    Text(label, style = EbbTypography.labelL)
                }
            }
        }
    }
}

@Composable
private fun StyleSegment(selectedGentle: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
        SegmentChip("Gentle", selectedGentle)
        SegmentChip("Alarm-style", !selectedGentle)
    }
}

@Composable
private fun SegmentChip(
    label: String,
    selected: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = EbbTypography.labelL)
    }
}

@Composable
fun TaskSheetsAppPreview(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme, content = content)
}
