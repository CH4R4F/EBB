@file:Suppress("LongMethod", "MaxLineLength", "MagicNumber", "NewLineAtEndOfFile")

package app.ebbplanner.ui.daytools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.sheets.EbbSheetScaffold
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun DayToolsAppPreview(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme, content = content)
}

@Composable
fun ShiftTheRestDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "Running behind? No problem.",
        subtitle = "Move everything after now, together.",
        doneLabel = "Shift +15 min",
        sheetTopInset = 96.dp,
        modifier = modifier,
    ) {
        ShiftAmountChips(selected = 1)
        Row(Modifier.fillMaxWidth().padding(top = EbbSpacing.s3), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniShiftColumn("Now", highlighted = false)
            MiniShiftColumn("After +15 min", highlighted = true)
        }
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            EbbSymbol("keep", fontSize = 16.sp)
            Text("Pinned stays put", style = EbbTypography.labelM, modifier = Modifier.padding(start = 4.dp, end = 16.dp))
            Box(Modifier.size(12.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(3.dp)).then(Modifier))
            Text("Moves", style = EbbTypography.labelM, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun OverlapHelperDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "Overlap helper",
        subtitle = "Design poster overlaps Lunch with Ines",
        sheetTopInset = 96.dp,
        modifier = modifier,
    ) {
        OverlapPreviewRow("Move Design poster", selected = true)
        OverlapPreviewRow("Shorten both", selected = false)
        OverlapPreviewRow("Keep overlap (it's fine)", selected = false)
        Text(
            "Pinned events stay put unless you choose otherwise.",
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = EbbSpacing.s2),
        )
    }
}

@Composable
fun MakeItFitDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "Make it fit",
        subtitle = "3 tasks won't fit before bedtime",
        sheetTopInset = 96.dp,
        modifier = modifier,
    ) {
        SheetChipRow(listOf("Shorten", "Move to tomorrow", "Inbox", "Drop lowest energy"), selected = 0)
        TaskFitRow("Design poster", "1 h → 45 min")
        TaskFitRow("Expense report", "30 min → tomorrow")
        TaskFitRow("Call gran", "20 min → Anytime")
    }
}

@Composable
fun PlanMyDayDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "Plan my day",
        subtitle = "Pick a few tasks for the empty slots",
        sheetTopInset = 96.dp,
        modifier = modifier,
    ) {
        Text("Suggested", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PlanPickRow("Water plants", "15 min", picked = true)
        PlanPickRow("Reply to Sam", "10 min", picked = true)
        PlanPickRow("Tidy desk", "20 min", picked = false)
        Text(
            "Free slots",
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text("11:45 · 25 min · 14:15 · 30 min", style = EbbTypography.bodyM)
    }
}

@Composable
fun EndOfDayReviewDesign(modifier: Modifier = Modifier) {
    Column(modifier.then(Modifier.fillMaxSize())) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = EbbSpacing.s1), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { EbbSymbol("close", fontSize = 22.sp) }
            Text("End of day", style = EbbTypography.headlineM, modifier = Modifier.weight(1f))
            Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary) {
                Text(
                    "Next",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = EbbTypography.labelL,
                )
            }
        }
        Column(Modifier.padding(EbbSpacing.s4)) {
            Text("What went well?", style = EbbTypography.titleL)
            Surface(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = EbbShapes.md,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    "Finished the brief draft…",
                    modifier = Modifier.padding(16.dp),
                    style = EbbTypography.bodyL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "2 done · 1 carried · bedtime in 45 min",
                style = EbbTypography.bodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
fun WelcomeBackDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(title = "Welcome back", subtitle = "While you were away", sheetTopInset = 120.dp, modifier = modifier) {
        ReviewRow("3 tasks finished", "Nice momentum")
        ReviewRow("Dentist moved to tomorrow", "Shift applied")
        ReviewRow("2 reminders waiting", "Tap to review")
    }
}

@Composable
fun EventDetailDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "Lunch with Ines",
        subtitle = "12:00–13:00 · Personal calendar",
        sheetTopInset = 96.dp,
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EbbSymbol("event", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            Column(Modifier.padding(start = 12.dp)) {
                Text("Calendar event", style = EbbTypography.bodyL)
                Text(
                    "Keeps its time when you shift the day",
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Surface(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            shape = EbbShapes.md,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text("Location: Café Verde", modifier = Modifier.padding(16.dp), style = EbbTypography.bodyL)
        }
    }
}

@Composable
fun JumpToDateDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(title = "Jump to date", sheetTopInset = 160.dp, modifier = modifier) {
        SheetChipRow(listOf("Today", "Tomorrow", "Next Monday", "Pick on calendar"), selected = 2)
        Surface(Modifier.fillMaxWidth(), shape = EbbShapes.md, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Column(Modifier.padding(16.dp)) {
                Text("Mon 23 Nov", style = EbbTypography.titleL)
                Text("Light day · 3 tasks scheduled", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftAmountChips(selected: Int) {
    val labels = listOf("+5 min", "+15 min", "+30 min", "+60 min", "Start from now", "Custom")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            val sel = index == selected
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (sel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
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
private fun RowScope.MiniShiftColumn(
    label: String,
    highlighted: Boolean,
) {
    Column(Modifier.weight(1f)) {
        Text(
            label,
            style = EbbTypography.labelL,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Surface(
            Modifier.fillMaxWidth().padding(top = 8.dp).height(120.dp),
            shape = EbbShapes.md,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = if (highlighted) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        ) {
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniTaskBlock("Call bank", "green")
                MiniTaskBlock("Buy stamps", "lemon", moved = highlighted)
            }
        }
    }
}

@Composable
private fun MiniTaskBlock(
    title: String,
    color: String,
    moved: Boolean = false,
) {
    Surface(
        Modifier.fillMaxWidth().height(22.dp),
        shape = EbbShapes.sm,
        color = EbbTaskColors.tint(color, dark = true),
        border = if (moved) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(3.dp).fillMaxHeight().background(EbbTaskColors.strong(color)))
            Text(title, style = EbbTypography.labelM, modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
private fun OverlapPreviewRow(
    label: String,
    selected: Boolean,
) {
    Surface(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
    ) {
        Text(label, modifier = Modifier.padding(12.dp), style = EbbTypography.bodyL)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SheetChipRow(
    labels: List<String>,
    selected: Int,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            val sel = index == selected
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (sel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = EbbTypography.labelL)
            }
        }
    }
}

@Composable
private fun TaskFitRow(
    title: String,
    change: String,
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = EbbTypography.bodyL, modifier = Modifier.weight(1f))
        Text(change, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PlanPickRow(
    title: String,
    estimate: String,
    picked: Boolean,
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        EbbSymbol(if (picked) "check_box" else "check_box_outline_blank", fontSize = 22.sp)
        Column(Modifier.padding(start = 8.dp)) {
            Text(title, style = EbbTypography.bodyL)
            Text(estimate, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReviewRow(
    title: String,
    subtitle: String,
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column {
            Text(title, style = EbbTypography.bodyL)
            Text(subtitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
