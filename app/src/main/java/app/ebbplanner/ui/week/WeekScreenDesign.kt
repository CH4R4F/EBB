@file:Suppress("LongMethod", "MagicNumber", "MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.week

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.components.EbbBottomNavigation
import app.ebbplanner.ui.navigation.MainTab
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun WeekScreenDesign(
    state: WeekPresentationState,
    modifier: Modifier = Modifier,
    onPreviousWeek: () -> Unit = {},
    onNextWeek: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { EbbBottomNavigation(selected = MainTab.Week) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                EbbSymbol("add", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            WeekTopBar(
                range = state.rangeLabel,
                onPreviousWeek = onPreviousWeek,
                onNextWeek = onNextWeek,
            )
            state.bannerMessage?.let { WeekBanner(it) }
            state.dragLabel?.let { WeekDragChip(it) }
            if (state.mode == WeekVisualMode.Landscape) {
                WeekLandscapeGrid(state.days)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
                    contentPadding =
                        androidx.compose.foundation.layout.PaddingValues(
                            horizontal = EbbSpacing.s4,
                            vertical = EbbSpacing.s1,
                        ),
                ) {
                    items(state.days) { day -> WeekDayCard(day) }
                }
            }
        }
    }
}

@Composable
private fun WeekTopBar(
    range: String,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = EbbSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousWeek) { EbbSymbol("chevron_left", fontSize = 22.sp) }
        Text(range, style = EbbTypography.headlineM, modifier = Modifier.weight(1f), maxLines = 1)
        IconButton(onClick = onNextWeek) { EbbSymbol("chevron_right", fontSize = 22.sp) }
        TextButton(onClick = {}) { Text("This week", style = EbbTypography.labelL) }
        IconButton(onClick = {}) { EbbSymbol("more_vert", fontSize = 22.sp) }
    }
}

@Composable
private fun WeekBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s1),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = EbbShapes.md,
    ) {
        Row(modifier = Modifier.padding(EbbSpacing.s3), verticalAlignment = Alignment.CenterVertically) {
            EbbSymbol("wb_twilight", fontSize = 20.sp)
            Text(message, modifier = Modifier.padding(start = EbbSpacing.s2), style = EbbTypography.bodyM)
        }
    }
}

@Composable
private fun WeekDragChip(label: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4),
        shape = EbbShapes.full,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun WeekDayCard(day: WeekDayRow) {
    val border =
        if (day.today) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(72.dp),
        shape = EbbShapes.md,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = border,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(day.weekday, style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    day.dayNumber,
                    style = EbbTypography.titleM,
                    color = if (day.today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = EbbSpacing.s3)) {
                Text(
                    if (day.open) "Open" else day.summary,
                    style = EbbTypography.bodyM,
                    color = if (day.open) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                WeekLoadRow(day)
            }
        }
    }
}

@Composable
private fun WeekLoadRow(day: WeekDayRow) {
    val barColor =
        if (day.loadFull) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.primary
        }
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .width(64.dp)
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, EbbShapes.full),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(day.loadProgress.coerceIn(0f, 1f))
                        .background(barColor, EbbShapes.full),
            )
        }
        Text(
            day.loadLabel,
            style = EbbTypography.labelM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp),
        )
        if (day.eventCount > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(day.eventCount.coerceAtMost(3)) {
                    Box(
                        modifier =
                            Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                }
                Text(
                    "${day.eventCount} ${if (day.eventCount == 1) "event" else "events"}",
                    style = EbbTypography.labelM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WeekLandscapeGrid(days: List<WeekDayRow>) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        days.forEach { day ->
            Surface(
                modifier = Modifier.width(120.dp),
                shape = EbbShapes.md,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = if (day.today) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            ) {
                Column(modifier = Modifier.padding(EbbSpacing.s2)) {
                    Text(day.weekday, style = EbbTypography.labelM)
                    Text(day.dayNumber, style = EbbTypography.titleM)
                    Text(day.summary, style = EbbTypography.bodyM, maxLines = 4, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun WeekAppPreview(
    darkTheme: Boolean,
    state: WeekPresentationState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        WeekScreenDesign(state = state)
    }
}
