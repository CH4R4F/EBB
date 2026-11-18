@file:Suppress("LongMethod", "MaxLineLength", "MagicNumber", "NewLineAtEndOfFile", "TooManyFunctions")

package app.ebbplanner.ui.daytools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import app.ebbplanner.R
import app.ebbplanner.domain.day.DayGapScheduler
import app.ebbplanner.domain.day.MakeItFitPlan
import app.ebbplanner.domain.day.OverlapResolutionType
import app.ebbplanner.domain.day.OverlapSituation
import app.ebbplanner.domain.day.ShiftPreviewBlock
import app.ebbplanner.ui.components.EbbHeroIllustration
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

private val DAY_TOOL_SHEET_TOP_INSET = 66.dp
private val SHIFT_TIMELINE_HEIGHT = 250.dp
private const val SHIFT_TIMELINE_MINUTES = 360.0

@Composable
fun DayToolsAppPreview(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    OnboardingAppPreview(darkTheme = darkTheme, content = content)
}

@Suppress("LongParameterList")
@Composable
fun ShiftTheRestDesign(
    modifier: Modifier = Modifier,
    shiftMinutes: Int = 15,
    chipIndex: Int = 1,
    customMinutes: Int = 15,
    previewBefore: List<ShiftPreviewBlock> = emptyList(),
    previewAfter: List<ShiftPreviewBlock> = emptyList(),
    canShift: Boolean = true,
    onChipSelected: (Int) -> Unit = {},
    onCustomMinutesChanged: (Int) -> Unit = {},
    onApplyShift: () -> Unit = {},
    onCancel: () -> Unit = {},
    backgroundState: app.ebbplanner.ui.today.TodayPresentationState? = null,
) {
    EbbSheetScaffold(
        title = "Running behind? No problem.",
        subtitle = "Move everything after now, together.",
        doneLabel = "Shift +$shiftMinutes min",
        doneLeadingIcon = "more_time",
        sheetTopInset = DAY_TOOL_SHEET_TOP_INSET,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        sheetWrapContent = true,
        modifier = modifier,
        onDoneClick = onApplyShift,
        onCancelClick = onCancel,
        doneEnabled = canShift,
    ) {
        if (!canShift) {
            Text(
                stringResource(R.string.shift_empty_message),
                style = EbbTypography.bodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = EbbSpacing.s3),
            )
        }
        ShiftAmountChips(selected = chipIndex, onSelected = onChipSelected)
        if (chipIndex == 5) {
            androidx.compose.material3.OutlinedTextField(
                value = customMinutes.toString(),
                onValueChange = { raw -> raw.toIntOrNull()?.let(onCustomMinutesChanged) },
                label = { Text("Minutes to add") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true,
            )
        }
        if (canShift) {
            Row(
                Modifier.fillMaxWidth().padding(top = EbbSpacing.s3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShiftPreviewColumn("Now", afterShift = false, blocks = previewBefore)
                ShiftPreviewColumn("After +$shiftMinutes min", afterShift = true, blocks = previewAfter)
            }
        }
        Row(
            Modifier.padding(top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
fun OverlapHelperDesign(
    modifier: Modifier = Modifier,
    situation: OverlapSituation? = null,
    selected: OverlapResolutionType = OverlapResolutionType.PUSH_LATER,
    backgroundState: app.ebbplanner.ui.today.TodayPresentationState? = null,
    onSelectOption: (OverlapResolutionType) -> Unit = {},
    onApply: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    val headline = situation?.headline ?: "These overlap"
    val subtitle = situation?.detail ?: "No overlapping tasks right now."
    EbbSheetScaffold(
        title = headline,
        subtitle = subtitle,
        doneLabel = "Apply",
        sheetTopInset = DAY_TOOL_SHEET_TOP_INSET,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier,
        onDoneClick = onApply,
        onCancelClick = onCancel,
    ) {
        if (situation != null) {
            OverlapTimelineCard(
                firstTitle = situation.firstTitle,
                secondTitle = situation.secondTitle,
                firstRange = DayGapScheduler.formatRange(situation.firstStartMinutes, situation.firstDurationMin),
                secondRange = DayGapScheduler.formatRange(situation.secondStartMinutes, situation.secondDurationMin),
                firstStartMinutes = situation.firstStartMinutes,
                firstDurationMin = situation.firstDurationMin,
                secondStartMinutes = situation.secondStartMinutes,
                secondDurationMin = situation.secondDurationMin,
            )
            situation.options.forEach { option ->
                val icon =
                    when (option.type) {
                        OverlapResolutionType.PUSH_LATER -> "south"
                        OverlapResolutionType.SHORTEN_EARLIER -> "compress"
                        OverlapResolutionType.NEXT_FREE_SLOT -> "bolt"
                        OverlapResolutionType.KEEP_BOTH -> "done_all"
                    }
                OverlapChoiceRow(
                    option.title,
                    option.subtitle,
                    icon = icon,
                    selected = option.type == selected,
                    onClick = { onSelectOption(option.type) },
                )
            }
        }
    }
}

@Composable
fun MakeItFitDesign(
    modifier: Modifier = Modifier,
    plan: MakeItFitPlan? = null,
    selectedMoveIds: Set<String> = emptySet(),
    afterPlannedMinutes: Int? = null,
    backgroundState: app.ebbplanner.ui.today.TodayPresentationState? = null,
    onToggleMove: (String) -> Unit = {},
    onApply: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    val planned = plan?.plannedMinutes ?: 0
    val available = plan?.availableMinutes ?: 1
    val plannedFraction = if (available > 0) (planned.toFloat() / available).coerceAtMost(1f) else 1f
    val leftFraction = if (available > 0) 1f else 0f
    val afterTotal = afterPlannedMinutes ?: plan?.afterPlannedMinutes
    EbbSheetScaffold(
        title = "Let's make it fit",
        subtitle = if (plan != null && planned > available) "This day is fuller than the time left." else "You're within today's time.",
        doneLabel = "Apply moves",
        sheetTopInset = DAY_TOOL_SHEET_TOP_INSET,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier,
        onDoneClick = onApply,
        onCancelClick = onCancel,
    ) {
        FitMeterRow(
            "Planned",
            fraction = plannedFraction,
            barLabel = formatMinutesShort(planned),
            barColor = MaterialTheme.colorScheme.tertiary,
        )
        FitMeterRow("Time left", fraction = leftFraction, barLabel = formatMinutesShort(available))
        if (plan != null && plan.moves.isNotEmpty()) {
            Text(
                "Suggested moves",
                style = EbbTypography.labelL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = EbbSpacing.s4, bottom = 8.dp),
            )
            plan.moves.forEach { move ->
                FitMoveRow(
                    move.title,
                    "${move.durationMin} min",
                    move.reason,
                    checked = selectedMoveIds.contains(move.taskId),
                    onToggle = { onToggleMove(move.taskId) },
                )
            }
            afterTotal?.let { after ->
                Text(
                    "After: about ${formatMinutesShort(after)} planned",
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

private fun formatMinutesShort(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "$h h" + if (m > 0) " $m m" else "" else "$m m"
}

@Composable
fun PlanMyDayDesign(
    modifier: Modifier = Modifier,
    tasks: List<PlanDayTaskRow> = emptyList(),
    emptyCandidates: Boolean = false,
    unplacedTitles: List<String> = emptyList(),
    onClose: () -> Unit = {},
    onToggleTask: (String) -> Unit = {},
    onApply: () -> Unit = {},
    onDismissUnplaced: () -> Unit = {},
) {
    Column(modifier.then(Modifier.fillMaxSize())) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { EbbSymbol("close", fontSize = 22.sp) }
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
            if (emptyCandidates || tasks.isEmpty()) {
                Text(
                    "Nothing to place — inbox and Anytime are clear.",
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                tasks.forEach { row ->
                    PlanTaskRow(
                        row.title,
                        row.estimate,
                        checked = row.selected,
                        energy = 2,
                        onToggle = { onToggleTask(row.id) },
                    )
                }
            }
        }
        if (unplacedTitles.isNotEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EbbSpacing.s4, vertical = 8.dp),
            ) {
                Text(
                    "Couldn't fit today",
                    style = EbbTypography.titleM,
                    color = MaterialTheme.colorScheme.error,
                )
                unplacedTitles.forEach { title ->
                    Text("· $title", style = EbbTypography.bodyM, modifier = Modifier.padding(top = 4.dp))
                }
                Text(
                    "Got it",
                    color = MaterialTheme.colorScheme.primary,
                    style = EbbTypography.labelL,
                    modifier = Modifier.padding(top = 12.dp).clickable(onClick = onDismissUnplaced),
                )
            }
        }
        Surface(
            shape = EbbShapes.full,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(EbbSpacing.s4)
                    .clickable(onClick = onApply),
        ) {
            Text(
                "Place on my day",
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
            EbbHeroIllustration(icon = "celebration", size = 140.dp)
            Text("You did 6 things today", style = EbbTypography.headlineL, modifier = Modifier.padding(top = 16.dp))
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
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(Modifier.padding(vertical = 4.dp)) {
                listOf("Work start-up", "Emails", "Write project brief", "Call bank", "Lunch with Ines", "Buy stamps").forEach { item ->
                    Row(Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        EbbDoneCheckmark()
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
            WelcomeBackCard(
                "Fresh start",
                "Waiting things go quietly to your Inbox",
                icon = "wb_twilight",
                selected = true,
            )
            WelcomeBackCard("Review what’s waiting", "Decide one by one", icon = "checklist", selected = false)
            WelcomeBackCard("Just show today", "Deal with it later", icon = "today", selected = false)
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
        cancelLabel = "Close",
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
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp),
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
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text("November 2026", style = EbbTypography.titleM)
                        EbbSymbol("arrow_drop_down", fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                    IconButton(onClick = {}) { EbbSymbol("chevron_left", fontSize = 22.sp) }
                    IconButton(onClick = {}) { EbbSymbol("chevron_right", fontSize = 22.sp) }
                }
                JumpToDateMonthGrid(selectedDay = 19, todayDay = 16)
                Text(
                    "Dots show how full a day is — no counts.",
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Today", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Cancel",
                        style = EbbTypography.labelL,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                    Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary) {
                        Text(
                            "Go",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = EbbTypography.labelL,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftAmountChips(
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    val labels = listOf("+5 min", "+15 min", "+30 min", "+60 min", "Start from now", "Custom")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            val sel = index == selected
            Surface(
                onClick = { onSelected(index) },
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
    blocks: List<ShiftPreviewBlock>,
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
            val timelineStart = blocks.minOfOrNull { it.startMinutes } ?: (9 * 60)
            val hourStep = SHIFT_TIMELINE_HEIGHT / 6
            val hourLabels =
                (0..6).map { index ->
                    app.ebbplanner.ui.time.ClockFormat.minutesToClock(
                        timelineStart + (SHIFT_TIMELINE_MINUTES * index / 6).toInt(),
                    )
                }
            Box(Modifier.width(34.dp).height(SHIFT_TIMELINE_HEIGHT)) {
                hourLabels.forEachIndexed { index, hour ->
                    Text(
                        hour,
                        style = EbbTypography.labelM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.TopEnd).offset(y = hourStep * index - 10.dp),
                        maxLines = 1,
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
                    val timelineStart = blocks.minOfOrNull { it.startMinutes } ?: (11 * 60)
                    blocks.forEach { block ->
                        val movable = !block.pinned
                        val top = shiftBlockTopMinutes(block.startMinutes, timelineStart)
                        val height =
                            (block.durationMin.toDouble() / SHIFT_TIMELINE_MINUTES * SHIFT_TIMELINE_HEIGHT.value).dp
                        ShiftBlock(
                            title = block.title,
                            color = block.colorKey,
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

private fun shiftBlockTopMinutes(
    startMinutes: Int,
    timelineStartMin: Int,
): Dp {
    val offset = (startMinutes - timelineStartMin).coerceAtLeast(0)
    return (offset.toDouble() / SHIFT_TIMELINE_MINUTES * SHIFT_TIMELINE_HEIGHT.value).dp
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
            Text(
                title,
                style = EbbTypography.labelM,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            if (isPinned) {
                Spacer(Modifier.weight(1f))
                EbbSymbol("keep", fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
            }
        }
    }
}

@Composable
private fun OverlapTimelineCard(
    firstTitle: String,
    secondTitle: String,
    firstRange: String,
    secondRange: String,
    firstStartMinutes: Int = 11 * 60 + 30,
    firstDurationMin: Int = 30,
    secondStartMinutes: Int = 11 * 60 + 30,
    secondDurationMin: Int = 60,
) {
    val firstEnd = firstStartMinutes + firstDurationMin
    val secondEnd = secondStartMinutes + secondDurationMin
    val windowStart = minOf(firstStartMinutes, secondStartMinutes).let { (it / 30) * 30 }
    val windowEnd = maxOf(firstEnd, secondEnd) + 15
    val windowSpan = (windowEnd - windowStart).coerceAtLeast(45)
    val railHeight = 126.dp
    fun minutesToY(min: Int): Dp {
        val fraction = (min - windowStart).toFloat() / windowSpan
        return (fraction * railHeight.value).dp
    }
    fun minutesToHeight(duration: Int): Dp {
        val fraction = duration.toFloat() / windowSpan
        return (fraction * railHeight.value).coerceAtLeast(24f).dp
    }
  val railLabels =
        listOf(0, 1, 2).map { index ->
            app.ebbplanner.ui.time.ClockFormat.minutesToClock(windowStart + (windowSpan * index / 2))
        }
    val overlapTop = minutesToY(maxOf(firstStartMinutes, secondStartMinutes))
    val overlapBottom = minutesToY(minOf(firstEnd, secondEnd))
    val overlapHeight =
        if (overlapBottom > overlapTop) {
            (overlapBottom - overlapTop).coerceAtLeast(8.dp)
        } else {
            0.dp
        }
    Surface(Modifier.fillMaxWidth().height(150.dp), shape = EbbShapes.md, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(12.dp)) {
            val largeText = androidx.compose.ui.platform.LocalConfiguration.current.fontScale >= 1.75f
            val railWidth = if (largeText) 52.dp else 44.dp
            Column(Modifier.width(railWidth).height(railHeight), verticalArrangement = Arrangement.SpaceBetween) {
                railLabels.forEach { label ->
                    Text(
                        label,
                        style = if (largeText) EbbTypography.labelS else EbbTypography.labelM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
            Box(Modifier.weight(1f).height(railHeight)) {
                Surface(
                    Modifier
                        .fillMaxWidth(0.48f)
                        .height(minutesToHeight(firstDurationMin))
                        .offset(y = minutesToY(firstStartMinutes)),
                    shape = EbbShapes.sm,
                    color = EbbTaskColors.tint("green", dark = true),
                    border = BorderStroke(0.dp, Color.Transparent),
                ) {
                    Row(Modifier.fillMaxSize()) {
                        Box(Modifier.width(3.dp).fillMaxHeight().background(EbbTaskColors.strong("green")))
                        Column(Modifier.padding(8.dp)) {
                            Text(firstTitle, style = EbbTypography.labelL, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Text(firstRange, style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                }
                Surface(
                    Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxWidth(0.48f)
                        .height(minutesToHeight(secondDurationMin))
                        .offset(y = minutesToY(secondStartMinutes)),
                    shape = EbbShapes.sm,
                    color = EbbTaskColors.tint("sky", dark = true),
                ) {
                    Row(Modifier.fillMaxSize()) {
                        Box(Modifier.width(3.dp).fillMaxHeight().background(EbbTaskColors.strong("sky")))
                        Column(Modifier.padding(8.dp)) {
                            Text(secondTitle, style = EbbTypography.labelL, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Text(secondRange, style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                }
                val hatchBase = MaterialTheme.colorScheme.secondary
                if (overlapHeight > 0.dp) {
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(overlapHeight)
                        .offset(y = overlapTop)
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
}

@Composable
private fun OverlapChoiceRow(
    title: String,
    subtitle: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
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
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = EbbTypography.labelM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Text(
                barLabel,
                style = EbbTypography.labelL,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(16.dp),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun FitMoveRow(
    title: String,
    duration: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
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
    onToggle: () -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbSymbol(if (checked) "check_box" else "check_box_outline_blank", fontSize = 22.sp)
        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(title, style = EbbTypography.bodyL)
            Text(estimate, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("●".repeat(energy), style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EbbDoneCheckmark() {
    Box(
        Modifier
            .size(22.dp)
            .background(MaterialTheme.colorScheme.primary, EbbShapes.full),
        contentAlignment = Alignment.Center,
    ) {
        EbbSymbol("check", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
private fun WelcomeBackCard(
    title: String,
    body: String,
    icon: String,
    selected: Boolean,
) {
    val containerColor =
        if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    Surface(shape = EbbShapes.lg, color = containerColor) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    EbbSymbol(icon, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(
                    title,
                    style = EbbTypography.titleM,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    body,
                    style = EbbTypography.bodyM,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
            EbbSymbol(
                "chevron_right",
                fontSize = 22.sp,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            )
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
        EbbSymbol(icon, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp).width(40.dp))
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
private fun JumpToDateMonthGrid(
    selectedDay: Int,
    todayDay: Int,
) {
    val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
    val dotsByDay =
        mapOf(
            1 to 1,
            2 to 2,
            3 to 1,
            5 to 2,
            6 to 1,
            8 to 1,
            9 to 2,
            10 to 1,
            12 to 2,
            13 to 1,
            15 to 2,
            16 to 3,
            17 to 1,
            19 to 3,
            20 to 1,
            22 to 2,
            24 to 1,
            25 to 1,
            26 to 2,
            27 to 1,
            28 to 2,
            29 to 1,
        )
    Column(Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            weekdays.forEach {
                Text(
                    it,
                    style = EbbTypography.labelM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).height(32.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        val leadingBlanks = 6
        val monthDays = (1..29).toList()
        val cells: List<Int?> = List(leadingBlanks) { null } + monthDays.map { it as Int? }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
                        if (day != null) {
                            val selected = day == selectedDay
                            val isToday = day == todayDay
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val dayModifier =
                                    Modifier
                                        .size(40.dp)
                                        .then(
                                            if (isToday && !selected) {
                                                Modifier.border(1.dp, MaterialTheme.colorScheme.primary, EbbShapes.full)
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .then(
                                            if (selected) {
                                                Modifier.background(MaterialTheme.colorScheme.primary, EbbShapes.full)
                                            } else {
                                                Modifier
                                            },
                                        )
                                Box(dayModifier, contentAlignment = Alignment.Center) {
                                    Text(
                                        day.toString(),
                                        style = EbbTypography.bodyM,
                                        color =
                                            when {
                                                selected -> MaterialTheme.colorScheme.onPrimary
                                                isToday -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
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
                    Box(Modifier.weight(1f).height(48.dp))
                }
            }
        }
    }
}
