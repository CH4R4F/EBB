@file:Suppress("LongMethod")

package app.ebbplanner.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.components.EbbBottomNavigation
import app.ebbplanner.ui.navigation.MainTab
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun InboxScreenDesign(
    state: InboxPresentationState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        InboxTopBar(title = state.title)
        Text(
            state.subtitle,
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = EbbSpacing.s4),
        )
        InboxFilterRow(
            filters = state.filters,
            selected = state.selectedFilter,
            modifier = Modifier.padding(vertical = EbbSpacing.s3),
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = EbbSpacing.s4),
        ) {
            items(state.tasks, key = { it.id }) { task ->
                InboxTaskCard(task)
            }
        }
        EbbBottomNavigation(selected = MainTab.Inbox)
    }
}

@Composable
private fun InboxTopBar(title: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = EbbSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = EbbTypography.headlineM, modifier = Modifier.weight(1f))
        IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
            EbbSymbol("search", fontSize = 22.sp)
        }
        IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
            EbbSymbol("sort", fontSize = 22.sp)
        }
        IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
            EbbSymbol("more_vert", fontSize = 22.sp)
        }
    }
}

@Composable
private fun InboxFilterRow(
    filters: List<String>,
    selected: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = EbbSpacing.s4),
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        filters.forEachIndexed { index, label ->
            val isSel = index == selected
            Surface(
                shape = EbbShapes.full,
                color =
                    if (isSel) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (isSel && index == 0) {
                        EbbSymbol("check", fontSize = 16.sp)
                    }
                    Text(label, style = EbbTypography.labelL)
                }
            }
        }
    }
}

@Composable
private fun InboxTaskCard(task: InboxTaskRow) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = EbbShapes.md,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.height(72.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(EbbTaskColors.strong(task.colorKey)),
            )
            Surface(
                modifier = Modifier.padding(start = 12.dp).size(40.dp),
                shape = EbbShapes.sm,
                color = EbbTaskColors.tint(task.colorKey, dark),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    EbbSymbol(task.iconKey, fontSize = 20.sp)
                }
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = EbbSpacing.s2),
            ) {
                Text(
                    task.title,
                    style = EbbTypography.titleM,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
                    EbbSymbol("schedule", fontSize = 14.sp)
                    Text(
                        task.estimate,
                        style = EbbTypography.bodyM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    InboxEnergyBars(task.energyLevel)
                    if (task.hasReminder) {
                        EbbSymbol("notifications", fontSize = 14.sp)
                    }
                    task.checklist?.let {
                        EbbSymbol("checklist", fontSize = 14.sp)
                        Text(it, style = EbbTypography.labelM)
                    }
                }
            }
            EbbSymbol(
                "drag_indicator",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

@Composable
private fun InboxEnergyBars(level: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        listOf(4.dp, 6.dp, 8.dp).forEachIndexed { index, h ->
            Box(
                modifier =
                    Modifier
                        .width(3.dp)
                        .height(h)
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
fun InboxAppPreview(
    darkTheme: Boolean,
    state: InboxPresentationState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        InboxScreenDesign(state = state)
    }
}
