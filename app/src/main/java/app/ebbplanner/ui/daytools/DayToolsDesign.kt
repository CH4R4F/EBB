@file:Suppress("LongMethod", "MaxLineLength", "MagicNumber", "NewLineAtEndOfFile", "TooManyFunctions")

package app.ebbplanner.ui.daytools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.components.EbbRadioDot
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
        Row(
            Modifier.fillMaxWidth().padding(top = EbbSpacing.s3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShiftPreviewColumn("Now", afterShift = false)
            ShiftPreviewColumn("After +15 min", afterShift = true)
        }
        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            EbbSymbol("keep", fontSize = 16.sp)
            Text("Pinned stays put", style = EbbTypography.labelM, modifier = Modifier.padding(start = 4.dp, end = 20.dp))
            Box(
                Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(3.dp))
                    .then(Modifier.padding(0.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
            }
            Text("Moves", style = EbbTypography.labelM, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun OverlapHelperDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "These overlap",
        subtitle = "Call bank and Plan the week share 20 minutes.",
        doneLabel = "Apply",
        sheetTopInset = 96.dp,
        modifier = modifier,
    ) {
        OverlapTimelineCard()
        OverlapChoiceRow("Push later", "Plan the week starts 12:25", icon = "south", selected = true)
        OverlapChoiceRow("Shorten earlier", "Call bank becomes 20 min", icon = "compress", selected = false)
        OverlapChoiceRow("Next free slot", "Move Plan the week to 13:05", icon = "bolt", selected = false)
        OverlapChoiceRow("Keep both", "Fine — I'll juggle", icon = "done_all", selected = false)
    }
}

@Composable
fun MakeItFitDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "Let's make it fit",
        subtitle = "This day is fuller than the time left.",
        doneLabel = "Apply 3 moves",
        sheetTopInset = 96.dp,
        modifier = modifier,
    ) {
        FitMeterRow("Planned", fraction = 1f, barLabel = "7 h", barColor = MaterialTheme.colorScheme.tertiary)
        FitMeterRow("Time left", fraction = 0.71f, barLabel = "5 h")
        Text(
            "Suggested moves",
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = EbbSpacing.s4, bottom = 8.dp),
        )
        FitMoveRow("Design poster", "1 h", "Move to tomorrow — tomorrow is light", checked = true)
        FitMoveRow("Read 10 pages", "20 min", "Move to Inbox — no time set", checked = true)
        FitMoveRow("Clean out the fridge", "30 min", "Low energy task, fits Wednesday", checked = true)
        FitMoveRow("Expense report", "45 min", "Has a reminder — keep today?", checked = false)
        Text(
            "After: about 4 h 50 min planned",
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
fun PlanMyDayDesign(modifier: Modifier = Modifier) {
    Column(modifier.then(Modifier.fillMaxSize())) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { EbbSymbol("close", fontSize = 22.sp) }
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text("Plan my day · step 1 of 3", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { i ->
                        Box(
                            Modifier
                                .height(4.dp)
                                .width(if (i == 0) 36.dp else 36.dp)
                                .background(
                                    if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    EbbShapes.full,
                                ),
                        )
                    }
                }
            }
            Text("Skip", color = MaterialTheme.colorScheme.primary, style = EbbTypography.labelL, modifier = Modifier.padding(end = 12.dp))
        }
        Text("What matters today?", style = EbbTypography.headlineM, modifier = Modifier.padding(horizontal = EbbSpacing.s4))
        Text(
            "Pick a few. Fewer is kinder.",
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = 4.dp),
        )
        PlanFilterChips()
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = EbbSpacing.s4, vertical = 8.dp),
        ) {
            PlanTaskRow("Write project brief", "1 h 15 min", checked = true, energy = 3)
            PlanTaskRow("Call bank", "25 min", checked = true, energy = 2)
            PlanTaskRow("Buy stamps", "15 min", checked = true, energy = 1)
            PlanTaskRow("Design poster", "1 h", checked = false, energy = 3)
            PlanTaskRow("Water plants", "15 min", checked = false, energy = 1)
        }
        Surface(
            shape = EbbShapes.full,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(EbbSpacing.s4),
        ) {
            Text(
                "Next",
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 14.dp),
                style = EbbTypography.labelL,
            )
        }
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
                    if (index == 4) {
                        EbbSymbol("play_arrow", fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                    }
                    if (index == 5) {
                        EbbSymbol("tune", fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                    }
                    Text(label, style = EbbTypography.labelL)
                }
            }
        }
    }
}

@Composable
private fun RowScope.ShiftPreviewColumn(
    label: String,
    afterShift: Boolean,
) {
    Column(Modifier.weight(1f)) {
        Text(label, style = EbbTypography.labelL, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Row(Modifier.padding(top = 8.dp)) {
            Column(Modifier.width(34.dp), horizontalAlignment = Alignment.End) {
                listOf("11:00", "12:00", "13:00", "14:00", "15:00", "16:00").forEach {
                    Text(
                        it,
                        style = EbbTypography.labelM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                }
            }
            Surface(
                Modifier
                    .weight(1f)
                    .height(230.dp),
                shape = EbbShapes.md,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = if (afterShift) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            ) {
                Box(Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                    val lineColor = MaterialTheme.colorScheme.outline
                    Canvas(Modifier.fillMaxSize()) {
                        repeat(7) { i ->
                            val y = i * (size.height / 7f)
                            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        }
                    }
                    ShiftBlock("Call bank", "green", top = 18.dp, moved = afterShift)
                    if (!afterShift) {
                        ShiftBlock("Lunch with Ines", "blue", top = 42.dp, isEvent = true)
                    } else {
                        ShiftBlock("Lunch with Ines", "blue", top = 42.dp, isEvent = true)
                    }
                    ShiftBlock("Buy stamps", "lemon", top = if (afterShift) 98.dp else 82.dp, moved = afterShift)
                    ShiftBlock("Design poster", "blue", top = if (afterShift) 118.dp else 102.dp, moved = afterShift)
                    ShiftBlock("Dentist", "vermillion", top = 168.dp, isPinned = true)
                }
            }
        }
    }
}

@Composable
private fun ShiftBlock(
    title: String,
    color: String,
    top: androidx.compose.ui.unit.Dp,
    moved: Boolean = false,
    isEvent: Boolean = false,
    isPinned: Boolean = false,
) {
    Surface(
        modifier =
            Modifier
                .offset(y = top)
                .fillMaxWidth()
                .height(if (isEvent) 38.dp else 18.dp),
        shape = EbbShapes.sm,
        color = if (isEvent) MaterialTheme.colorScheme.surface else EbbTaskColors.tint(color, dark = true),
        border =
            when {
                moved -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                isEvent -> BorderStroke(1.5.dp, EbbTaskColors.strong("blue"))
                else -> null
            },
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (!isEvent) {
                Box(Modifier.width(3.dp).fillMaxHeight().background(EbbTaskColors.strong(color)))
            }
            Text(title, style = EbbTypography.labelM, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
            if (isPinned) {
                EbbSymbol("keep", fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
            }
        }
    }
}

@Composable
private fun OverlapTimelineCard() {
    Surface(Modifier.fillMaxWidth().height(150.dp), shape = EbbShapes.md, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(12.dp)) {
            Column(Modifier.width(44.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Text("11:30", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("12:00", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("12:30", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(Modifier.weight(1f)) {
                Surface(
                    Modifier
                        .fillMaxWidth(0.48f)
                        .height(70.dp)
                        .offset(y = 10.dp),
                    shape = EbbShapes.sm,
                    color = EbbTaskColors.tint("green", dark = true),
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text("Call bank", style = EbbTypography.labelL)
                        Text("11:40–12:20", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(
                    Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxWidth(0.48f)
                        .height(95.dp)
                        .offset(y = 45.dp),
                    shape = EbbShapes.sm,
                    color = EbbTaskColors.tint("sky", dark = true),
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text("Plan the week", style = EbbTypography.labelL)
                        Text("12:00–13:00", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlapChoiceRow(
    title: String,
    subtitle: String,
    icon: String,
    selected: Boolean,
) {
    Surface(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            EbbRadioDot(selected = selected)
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(title, style = EbbTypography.bodyL)
                Text(subtitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            EbbSymbol(icon, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FitMeterRow(
    label: String,
    fraction: Float,
    barLabel: String,
    barColor: Color = MaterialTheme.colorScheme.primary,
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.weight(1f).height(16.dp),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Text(barLabel, style = EbbTypography.labelL, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun FitMoveRow(
    title: String,
    duration: String,
    subtitle: String,
    checked: Boolean,
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        EbbSymbol(if (checked) "check_box" else "check_box_outline_blank", fontSize = 22.sp, modifier = Modifier.padding(top = 8.dp))
        Column(Modifier.padding(start = 8.dp)) {
            Row {
                Text(title, style = EbbTypography.bodyL)
                Text(" $duration", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(subtitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanFilterChips() {
    val labels = listOf("All", "From yesterday", "Inbox", "Recurring")
    FlowRow(
        modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val sel = index == 0
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
private fun PlanTaskRow(
    title: String,
    estimate: String,
    checked: Boolean,
    energy: Int,
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        EbbSymbol(if (checked) "check_box" else "check_box_outline_blank", fontSize = 22.sp)
        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(title, style = EbbTypography.bodyL)
            Text(estimate, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("●".repeat(energy), style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
