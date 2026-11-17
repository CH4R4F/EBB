@file:Suppress("LongMethod", "MagicNumber", "MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.components.EbbJoinedSegmentedControl
import app.ebbplanner.ui.components.EbbRadioDot
import app.ebbplanner.ui.components.EbbSegmentedChips
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun ReminderPickerDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(title = "Reminders", modifier = modifier, sheetMinHeight = 580.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
            SheetToggleRow("Remind at start", "10:00", on = true)
            Text("Heads-up before", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SheetChips(listOf("5 min", "10 min", "15 min", "30 min", "60 min"), selected = 0)
            SheetToggleRow("At end", "Time to wrap up", on = false)
            Text("Style", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
            EbbJoinedSegmentedControl(listOf("Gentle", "Alarm-style"), icons = listOf(null, "alarm"), selectedIndex = 0)
            Text(
                "Alarm-style rings over the lock screen until you respond. Good for meds and appointments.",
                style = EbbTypography.bodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            SheetToggleRow("Keep reminding until I respond", "Every 10 min, up to 3 times", on = true)
        }
    }
}

@Composable
fun RepeatEditorDesign(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = EbbSpacing.s1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {}) { EbbSymbol("close", fontSize = 22.sp) }
            Text("Repeat", style = EbbTypography.headlineM, modifier = Modifier.weight(1f))
            Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary) {
                Text(
                    "Save",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = EbbTypography.labelL,
                )
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4),
            shape = EbbShapes.md,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(modifier = Modifier.padding(EbbSpacing.s3), verticalAlignment = Alignment.CenterVertically) {
                EbbSymbol("repeat", fontSize = 20.sp)
                Text("Every weekday at 10:00", style = EbbTypography.titleM, modifier = Modifier.padding(start = 8.dp))
            }
        }
        SheetChips(
            listOf("None", "Daily", "Weekdays", "Weekly", "Monthly", "Yearly", "Custom"),
            selected = 2,
            modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s3),
        )
        Text(
            "Mode",
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = EbbSpacing.s4),
        )
        EbbSegmentedChips(
            listOf("On a schedule", "After I finish it"),
            selectedIndex = 0,
            modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
        )
        Text(
            "If not done",
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
        )
        RadioChoiceRow("Let it go", "Default · skipped ones quietly disappear", selected = true)
        RadioChoiceRow("Carry over", "For things that must happen, like rent", selected = false)
    }
}

@Composable
fun BreakItDownDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "Break it down",
        subtitle = "Write project brief",
        modifier = modifier,
        sheetMinHeight = 400.dp,
    ) {
        Text("What's the very first tiny step?", style = EbbTypography.titleM)
        Text(
            "Smaller than you think. \"Open the doc\" counts.",
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EbbSymbol("drag_indicator", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                shape = EbbShapes.md,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    "Tiny step…",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    style = EbbTypography.bodyL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    EbbSymbol("timer", fontSize = 16.sp)
                    Text("—", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s4),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").forEach { key ->
                    Text(key, style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SchedulePickerDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "Schedule",
        subtitle = "Fix the squeaky door hinge · 10 min",
        doneLabel = "Schedule",
        sheetTopInset = 96.dp,
        modifier = modifier,
    ) {
        val dayChips = listOf("Today", "Tomorrow", "This weekend", "Next week", "Pick date", "Inbox", "Someday")
        SheetChips(dayChips, selected = 0)
        Column(modifier = Modifier.padding(top = EbbSpacing.s2), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ScheduleOptionRow("schedule", "Anytime", "In the Anytime tray", selected = false)
            ScheduleOptionRow("bolt", "Next free slot · 14:30", "After Buy stamps", selected = true, showCheck = true)
            ScheduleOptionRow("skip_next", "After current task · 11:20", null, selected = false)
            ScheduleOptionRow("wb_sunny", "This afternoon", "13:00–18:00", selected = false)
            ScheduleOptionRow("nights_stay", "This evening", "18:00–22:00", selected = false)
            ScheduleOptionRow("more_time", "Pick a time…", null, selected = false)
        }
        ScheduleMiniPreview(modifier = Modifier.padding(top = EbbSpacing.s3))
    }
}

@Composable
private fun ScheduleMiniPreview(modifier: Modifier = Modifier) {
    val railWidth = 40.dp
    Surface(
        modifier = modifier.fillMaxWidth().height(118.dp),
        shape = EbbShapes.md,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.width(railWidth).padding(top = 10.dp)) {
                Text("14:00", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(42.dp))
                Text("15:00", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(Modifier.weight(1f).fillMaxHeight().padding(end = 8.dp, top = 8.dp, bottom = 8.dp)) {
                val hourLine = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                Canvas(Modifier.matchParentSize()) {
                    val y1 = size.height * 0.35f
                    val y2 = size.height * 0.72f
                    drawLine(hourLine, Offset(0f, y1), Offset(size.width, y1), strokeWidth = 1.dp.toPx())
                    drawLine(hourLine, Offset(0f, y2), Offset(size.width, y2), strokeWidth = 1.dp.toPx())
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(26.dp),
                    shape = EbbShapes.sm,
                    color = EbbTaskColors.tint("lemon", dark = true),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(4.dp).fillMaxHeight().background(EbbTaskColors.strong("lemon")))
                        EbbSymbol("local_post_office", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        Text("Buy stamps", style = EbbTypography.labelL, modifier = Modifier.weight(1f))
                        Text("13:30 · 15 min", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp)
                            .height(26.dp)
                            .drawBehind {
                                drawRoundRect(
                                    color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.5f),
                                    style =
                                        Stroke(
                                            width = 2.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                                        ),
                                    cornerRadius = CornerRadius(8.dp.toPx()),
                                )
                            },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                        EbbSymbol("radio_button_unchecked", fontSize = 16.sp)
                        Text("Fix squeaky hinge", style = EbbTypography.labelL, modifier = Modifier.weight(1f))
                        Text("14:30 · 10 min", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleOptionRow(
    icon: String,
    line1: String,
    line2: String?,
    selected: Boolean,
    showCheck: Boolean = false,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            EbbSymbol(icon, fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(line1, style = EbbTypography.bodyL)
                line2?.let { Text(it, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (showCheck) {
                EbbSymbol("check", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun RadioChoiceRow(
    title: String,
    subtitle: String,
    selected: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        EbbRadioDot(selected = selected, modifier = Modifier.padding(top = 2.dp))
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, style = EbbTypography.bodyL)
            Text(subtitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
        Switch(
            checked = on,
            onCheckedChange = {},
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SheetChips(
    labels: List<String>,
    selected: Int,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
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
fun TaskSheetsAppPreview(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme, content = content)
}
