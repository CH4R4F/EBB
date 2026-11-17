@file:Suppress("LongMethod", "NewLineAtEndOfFile", "MaxLineLength")

package app.ebbplanner.ui.donehistory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.components.EbbListLeadingIcon
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun DoneHistoryScreenDesign(
    state: DoneHistoryPresentationState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {}) { EbbSymbol("arrow_back", fontSize = 22.sp) }
            Text("Done", style = EbbTypography.headlineM, modifier = Modifier.weight(1f))
            IconButton(onClick = {}) { EbbSymbol("filter_list", fontSize = 22.sp) }
        }
        DoneFilterRow(state.filters, state.selectedFilter)
        if (state.mode == DoneHistoryMode.Empty) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nothing done yet", style = EbbTypography.bodyL, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = EbbSpacing.s4)) {
                state.sections.forEach { section ->
                    item {
                        Text(
                            section.label,
                            style = EbbTypography.labelL,
                            modifier = Modifier.padding(vertical = EbbSpacing.s2),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(section.items) { item -> DoneRow(item) }
                }
            }
        }
    }
}

@Composable
private fun DoneFilterRow(
    filters: List<String>,
    selected: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s1),
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        filters.forEachIndexed { index, label ->
            val isSel = index == selected
            Surface(
                shape = EbbShapes.full,
                color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
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
private fun DoneRow(item: DoneItem) {
    Row(
        modifier = Modifier.fillMaxWidth().height(if (item.restore) 64.dp else 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.icon != null) {
            EbbListLeadingIcon(item.icon)
        } else {
            Surface(
                modifier = Modifier.size(24.dp).padding(horizontal = 12.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    EbbSymbol("check", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = EbbTypography.bodyL)
            item.subtitle?.let {
                Text(it, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (item.restore) {
            TextButton(onClick = {}) { Text("Restore") }
        } else {
            Text(item.time, style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DoneHistoryAppPreview(
    darkTheme: Boolean,
    state: DoneHistoryPresentationState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        DoneHistoryScreenDesign(state = state)
    }
}
