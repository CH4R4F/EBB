@file:Suppress("LongMethod", "MaxLineLength", "MagicNumber", "NewLineAtEndOfFile", "TooManyFunctions")

package app.ebbplanner.ui.daytools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.components.EbbRadioDot
import app.ebbplanner.ui.onboarding.OnboardingAppPreview
import app.ebbplanner.ui.sheets.EbbSheetScaffold
import app.ebbplanner.ui.theme.EbbPalette
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.today.TodayScreenDesign
import app.ebbplanner.ui.today.todayPopulatedDark

private val DAY_TOOL_SHEET_TOP_INSET = 72.dp
private const val SHIFT_TIMELINE_DESIGN_PX = 230.0
private val SHIFT_TIMELINE_HEIGHT = 280.dp
private const val SHIFT_TIMELINE_MINUTES = 360.0
private const val SHIFT_PREVIEW_MINUTES = 15.0

private data class ShiftTimelineBlock(
    val title: String,
    val color: String,
    val topPx: Double,
    val heightPx: Double,
    val pinned: Boolean,
    val isEvent: Boolean,
)

private val shiftPreviewBlocks =
    listOf(
        ShiftTimelineBlock("Call bank", "green", 13.777777777777777, 16.0, pinned = false, isEvent = false),
        ShiftTimelineBlock("Lunch with Ines", "blue", 39.33333333333333, 36.33333333333333, pinned = true, isEvent = true),
        ShiftTimelineBlock("Buy stamps", "lemon", 96.83333333333334, 16.0, pinned = false, isEvent = false),
        ShiftTimelineBlock("Design poster", "blue", 116.0, 36.333333333333314, pinned = false, isEvent = false),
        ShiftTimelineBlock("Dentist", "vermillion", 173.5, 26.75, pinned = true, isEvent = false),
    )

@Composable
fun DayToolsAppPreview(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    OnboardingAppPreview(darkTheme = darkTheme, content = content)
}

@Composable
fun ShiftTheRestDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "Running behind? No problem.",
        subtitle = "Move everything after now, together.",
        doneLabel = "Shift +15 min",
        doneLeadingIcon = "more_time",
        sheetTopInset = DAY_TOOL_SHEET_TOP_INSET,
        sheetContainerColor = EbbPalette.darkSurfaceContainerHigh,
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
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)),
            )
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
        sheetTopInset = DAY_TOOL_SHEET_TOP_INSET,
        sheetContainerColor = EbbPalette.darkSurfaceContainerHigh,
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
        sheetTopInset = DAY_TOOL_SHEET_TOP_INSET,
        sheetContainerColor = EbbPalette.darkSurfaceContainerHigh,
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
        Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { EbbSymbol("close", fontSize = 22.sp) }
            Row(Modifier.weight(1f).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(4) { step ->
                    Box(
                        Modifier
                            .height(4.dp)
                            .width(36.dp)
                            .background(
                                if (step == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                EbbShapes.full,
                            ),
                    )
                }
            }
            Text("Skip", color = MaterialTheme.colorScheme.primary, style = EbbTypography.labelL, modifier = Modifier.padding(end = 12.dp))
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EbbHeroIllustration(icon = "celebration")
            Text("You did 6 things today", style = EbbTypography.headlineM, modifier = Modifier.padding(top = 16.dp))
            Text(
                "Rest well.",
                style = EbbTypography.bodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Surface(
            Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = 8.dp),
            shape = EbbShapes.lg,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(Modifier.padding(vertical = 4.dp)) {
                listOf("Work start-up", "Emails", "Write project brief", "Call bank", "Lunch with Ines", "Buy stamps").forEach { item ->
                    Row(Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        EbbSymbol("check_circle", fontSize = 22.sp, color = MaterialTheme.colorScheme.tertiary)
                        Text(item, style = EbbTypography.bodyL, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        }
        Box(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth().padding(EbbSpacing.s4), horizontalArrangement = Arrangement.End) {
            Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary) {
                Text(
                    "Next",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = EbbTypography.labelL,
                )
            }
        }
    }
}

@Composable
fun WelcomeBackDesign(modifier: Modifier = Modifier) {
    Column(modifier.then(Modifier.fillMaxSize())) {
        Column(
            Modifier.fillMaxWidth().padding(top = 40.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EbbHeroIllustration(icon = "waving_hand", size = 170.dp)
            Text(
                "Welcome back!",
                style = EbbTypography.display,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                "Let’s make today simple.",
                style = EbbTypography.bodyL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Column(Modifier.padding(horizontal = EbbSpacing.s4, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            WelcomeBackCard("Fresh start", "Waiting things go quietly to your Inbox", icon = "wb_twilight", filledIcon = true)
            WelcomeBackCard("Review what’s waiting", "Decide one by one", icon = "checklist", filledIcon = false)
            WelcomeBackCard("Just show today", "Deal with it later", icon = "today", filledIcon = false)
        }
        Box(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EbbSymbol("expand_more", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "A few things are waiting",
                style = EbbTypography.bodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventDetailDesign(modifier: Modifier = Modifier) {
    EbbSheetScaffold(
        title = "Lunch with Ines",
        doneLabel = "Open in calendar",
        doneLeadingIcon = "open_in_new",
        sheetTopInset = DAY_TOOL_SHEET_TOP_INSET,
        sheetContainerColor = EbbPalette.darkSurfaceContainerHigh,
        modifier = modifier,
    ) {
        EventDetailRow(icon = "schedule", title = "12:00–13:00 · Mon 16 Nov", subtitle = "Repeats every Monday", tall = true)
        EventDetailRow(
            icon = "calendar_month",
            title = "Personal",
            trailing = {
                Box(Modifier.size(14.dp).background(EbbTaskColors.strong("blue"), RoundedCornerShape(7.dp)))
            },
        )
        EventDetailRow(icon = "location_on", title = "Café Hafa, Tangier")
        EventDetailRow(
            icon = "notes",
            title = "Catch up + plan the trip",
            subtitle = "From your calendar · read-only",
            tall = true,
        )
        FlowRow(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                "add_task" to "Add prep task",
                "notifications" to "Add reminder in Ebb",
                "visibility_off" to "Hide this calendar",
            ).forEach {
                    (icon, label) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        EbbSymbol(icon, fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                        Text(label, style = EbbTypography.labelL)
                    }
                }
            }
        }
    }
}

@Composable
fun JumpToDateDesign(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        TodayScreenDesign(state = todayPopulatedDark(), showBottomNav = true, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)))
        Surface(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 12.dp),
            shape = EbbShapes.lg,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 12.dp)) {
                Text(
                    "Jump to",
                    style = EbbTypography.labelL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Text(
                    "Thu 19 Nov",
                    style = EbbTypography.headlineM,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp),
                )
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text("November 2026", style = EbbTypography.titleM)
                        EbbSymbol("arrow_drop_down", fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                    IconButton(onClick = {}) { EbbSymbol("chevron_left", fontSize = 22.sp) }
                    IconButton(onClick = {}) { EbbSymbol("chevron_right", fontSize = 22.sp) }
                }
                JumpToDateMonthGrid(selectedDay = 19)
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
        Text(
            label,
            style = EbbTypography.labelL,
            color = if (afterShift) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Row(Modifier.padding(top = 8.dp)) {
            val hourStep = SHIFT_TIMELINE_HEIGHT / 6
            Box(Modifier.width(34.dp).height(SHIFT_TIMELINE_HEIGHT)) {
                listOf("11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00").forEachIndexed { index, hour ->
                    Text(
                        hour,
                        style = EbbTypography.labelM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.TopEnd).offset(y = hourStep * index - 10.dp),
                    )
                }
            }
            Surface(
                Modifier
                    .weight(1f)
                    .height(SHIFT_TIMELINE_HEIGHT),
                shape = EbbShapes.md,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = if (afterShift) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            ) {
                Box(Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                    val lineColor = MaterialTheme.colorScheme.outline
                    Canvas(Modifier.fillMaxSize()) {
                        repeat(7) { index ->
                            val y = index * (size.height / 6f)
                            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        }
                    }
                    shiftPreviewBlocks.forEach { block ->
                        val movable = !block.pinned
                        val top = shiftBlockTop(block.topPx, afterShift = afterShift, movable = movable)
                        val height = (block.heightPx * SHIFT_TIMELINE_HEIGHT.value / SHIFT_TIMELINE_DESIGN_PX).dp
                        ShiftBlock(
                            title = block.title,
                            color = block.color,
                            top = top,
                            height = height,
                            moved = afterShift && movable,
                            isEvent = block.isEvent,
                            isPinned = block.pinned,
                        )
                    }
                }
            }
        }
    }
}

private fun shiftBlockTop(
    topPx: Double,
    afterShift: Boolean,
    movable: Boolean,
): Dp {
    val shiftPx =
        if (afterShift && movable) {
            SHIFT_PREVIEW_MINUTES / SHIFT_TIMELINE_MINUTES * SHIFT_TIMELINE_DESIGN_PX
        } else {
            0.0
        }
    return ((topPx + shiftPx) * SHIFT_TIMELINE_HEIGHT.value / SHIFT_TIMELINE_DESIGN_PX).dp
}

@Composable
private fun ShiftBlock(
    title: String,
    color: String,
    top: Dp,
    height: Dp,
    moved: Boolean = false,
    isEvent: Boolean = false,
    isPinned: Boolean = false,
) {
    Surface(
        modifier =
            Modifier
                .offset(y = top)
                .fillMaxWidth()
                .height(height),
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
                Spacer(Modifier.weight(1f))
                EbbSymbol("keep", fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
            }
        }
    }
}

@Composable
private fun OverlapTimelineCard() {
    Surface(Modifier.fillMaxWidth().height(150.dp), shape = EbbShapes.md, color = MaterialTheme.colorScheme.surface) {
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
                val hatchBase = MaterialTheme.colorScheme.secondary
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(35.dp)
                        .offset(y = 45.dp)
                        .background(hatchBase.copy(alpha = 0.12f), RoundedCornerShape(4.dp)),
                ) {
                    val hatchLine = hatchBase.copy(alpha = 0.35f)
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 3f
                        var x = -size.height
                        while (x < size.width + size.height) {
                            drawLine(
                                hatchLine,
                                Offset(x, size.height),
                                Offset(x + size.height, 0f),
                                strokeWidth = stroke,
                            )
                            x += 7f
                        }
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

@Composable
private fun EbbHeroIllustration(
    icon: String,
    size: androidx.compose.ui.unit.Dp = 140.dp,
) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(color = Color(0xFF4D4433), radius = size.toPx() * 0.4f)
        }
        EbbSymbol(icon, fontSize = 56.sp, color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun WelcomeBackCard(
    title: String,
    body: String,
    icon: String,
    filledIcon: Boolean,
) {
    Surface(shape = EbbShapes.lg, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (filledIcon) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    EbbSymbol(icon, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(title, style = EbbTypography.titleM)
                Text(body, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            EbbSymbol("chevron_right", fontSize = 22.sp)
        }
    }
}

@Composable
private fun EventDetailRow(
    icon: String,
    title: String,
    subtitle: String? = null,
    tall: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = if (tall) 8.dp else 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                EbbSymbol(icon, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, style = EbbTypography.bodyL)
            subtitle?.let {
                Text(it, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun JumpToDateMonthGrid(selectedDay: Int) {
    val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
    val dotsByDay =
        mapOf(
            1 to 1,
            2 to 2,
            3 to 1,
            5 to 2,
            19 to 3,
        )
    Column(Modifier.padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            weekdays.forEach {
                Text(
                    it,
                    style = EbbTypography.labelM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        val leadingBlanks = 6
        val days = (1..30).toList()
        val cells = List(leadingBlanks) { null } + days
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                week.forEach { day ->
                    Box(Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
                        if (day != null) {
                            val selected = day == selectedDay
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = EbbShapes.full,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                ) {
                                    Text(
                                        day.toString(),
                                        style = EbbTypography.bodyM,
                                        color =
                                            if (selected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    )
                                }
                                Row(Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(dotsByDay.getOrDefault(day, 0)) {
                                        Box(
                                            Modifier
                                                .size(4.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                                    RoundedCornerShape(2.dp),
                                                ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                repeat(7 - week.size) {
                    Box(Modifier.weight(1f))
                }
            }
        }
    }
}
