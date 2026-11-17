@file:Suppress("LongMethod", "NewLineAtEndOfFile", "MaxLineLength")

package app.ebbplanner.ui.routines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.components.EbbBottomNavigation
import app.ebbplanner.ui.components.EbbListLeadingIcon
import app.ebbplanner.ui.navigation.MainTab
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun RoutinesScreenDesign(
    state: RoutinesPresentationState,
    modifier: Modifier = Modifier,
    onFabClick: () -> Unit = {},
    onRoutineClick: (String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { EbbBottomNavigation(selected = MainTab.Routines) },
        floatingActionButton = {
            if (state.mode == RoutinesVisualMode.Empty || state.routines.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onFabClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    icon = { EbbSymbol("add", fontSize = 20.sp) },
                    text = { Text("New routine") },
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            RoutinesTopBar()
            if (state.mode == RoutinesVisualMode.Empty) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No routines yet", style = EbbTypography.bodyL, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = EbbSpacing.s4),
                ) {
                    items(state.routines) { card -> RoutineCardUi(card, onClick = { onRoutineClick(card.id) }) }
                    item { TemplatesCard() }
                    item {
                        Text(
                            "Recurring tasks",
                            style = EbbTypography.labelL,
                            modifier = Modifier.padding(top = EbbSpacing.s4, bottom = EbbSpacing.s2),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    items(state.recurring) { row -> RecurringRow(row) }
                }
            }
        }
    }
}

@Composable
private fun RoutinesTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = EbbSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Routines", style = EbbTypography.headlineM, modifier = Modifier.weight(1f))
        IconButton(onClick = {}) { EbbSymbol("search", fontSize = 22.sp) }
        IconButton(onClick = {}) { EbbSymbol("more_vert", fontSize = 22.sp) }
    }
}

@Composable
private fun RoutineCardUi(
    card: RoutineCard,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        shape = EbbShapes.md,
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Column(modifier = Modifier.padding(EbbSpacing.s4)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), shape = EbbShapes.md, color = MaterialTheme.colorScheme.surface) {
                    Box(contentAlignment = Alignment.Center) {
                        EbbSymbol(card.icon, fontSize = 22.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = EbbSpacing.s3)) {
                    Text(
                        card.title,
                        style = EbbTypography.titleM,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(card.schedule, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(card.meta, style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = EbbShapes.full,
                    color = if (card.running) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EbbSymbol(
                            "play_arrow",
                            fontSize = 16.sp,
                            color =
                                if (card.running) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                },
                        )
                        Text(
                            if (card.running) "Resume" else "Start",
                            style = EbbTypography.labelL,
                            color =
                                if (card.running) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                },
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
            card.stepLabel?.let {
                Row(modifier = Modifier.padding(top = EbbSpacing.s3), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(6.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, EbbShapes.full),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(card.progress)
                                    .background(MaterialTheme.colorScheme.secondary, EbbShapes.full),
                        )
                    }
                    Text(it, modifier = Modifier.padding(start = EbbSpacing.s2), style = EbbTypography.labelM)
                }
            }
        }
    }
}

@Composable
private fun TemplatesCard() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = EbbShapes.md,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(modifier = Modifier.padding(EbbSpacing.s4), verticalAlignment = Alignment.CenterVertically) {
            EbbSymbol("auto_awesome", fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f).padding(horizontal = EbbSpacing.s3)) {
                Text("Browse templates", style = EbbTypography.titleM)
                Text(
                    "Morning, leaving the house, work start-up…",
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            EbbSymbol("chevron_right", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecurringRow(row: RecurringTaskRow) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbListLeadingIcon(icon = row.icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(row.title, style = EbbTypography.bodyL)
            Text(row.schedule, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        EbbSymbol("chevron_right", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun RoutinesAppPreview(
    darkTheme: Boolean,
    state: RoutinesPresentationState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        RoutinesScreenDesign(state = state)
    }
}
