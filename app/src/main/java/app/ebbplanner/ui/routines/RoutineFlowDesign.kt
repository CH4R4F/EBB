@file:Suppress("LongMethod", "MagicNumber", "MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.onboarding.OnboardingAppPreview
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun RoutineFlowAppPreview(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    OnboardingAppPreview(darkTheme = darkTheme, content = content)
}

@Composable
fun RoutineEditorEditingDesign(modifier: Modifier = Modifier) {
    Column(modifier.then(Modifier.fillMaxSize())) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { EbbSymbol("close", fontSize = 22.sp) }
            Text("Edit routine", style = EbbTypography.headlineM, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary) {
                Text(
                    "Save",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = EbbTypography.labelL,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                )
            }
            Box(Modifier.width(8.dp))
        }
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = EbbSpacing.s4, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(56.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        EbbSymbol("work", fontSize = 32.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text("Name", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Work start-up", style = EbbTypography.titleL)
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Steps", style = EbbTypography.titleL, modifier = Modifier.weight(1f))
                Text("Total 21 min", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4),
                shape = EbbShapes.md,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                listOf(
                    Triple(1, "Fill water bottle", "2 min"),
                    Triple(2, "Open calendar + plan", "5 min"),
                    Triple(3, "Check messages (10 min max)", "10 min"),
                    Triple(4, "Review priorities", "4 min"),
                ).forEachIndexed { index, (num, title, dur) ->
                    Column {
                        Row(
                            Modifier.fillMaxWidth().height(52.dp).padding(start = 16.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                num.toString(),
                                style = EbbTypography.labelM,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(18.dp),
                            )
                            Text(title, style = EbbTypography.bodyL, modifier = Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text(dur, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = EbbTypography.labelM)
                            }
                            IconButton(onClick = {}) { EbbSymbol("drag_indicator", fontSize = 20.sp) }
                        }
                        if (index < 3) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineRunFirstDesign(modifier: Modifier = Modifier) {
    Column(modifier.then(Modifier.fillMaxSize())) {
        Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { EbbSymbol("close", fontSize = 22.sp) }
            Text(
                "Work start-up · 1 of 5",
                style = EbbTypography.labelL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = {}) { EbbSymbol("pause", fontSize = 22.sp) }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s4),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Box(Modifier.height(10.dp).width(24.dp).background(MaterialTheme.colorScheme.secondary, EbbShapes.full))
            repeat(4) {
                Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.surfaceVariant, EbbShapes.full))
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    EbbSymbol("water_drop", fontSize = 32.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Text("Fill water bottle", style = EbbTypography.display, modifier = Modifier.padding(top = 24.dp), textAlign = TextAlign.Center)
            Text("1:42", style = EbbTypography.timerL, modifier = Modifier.padding(top = 24.dp))
            Text("of 2 min", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
            Modifier.fillMaxWidth().padding(16.dp),
            shape = EbbShapes.md,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Next",
                    style = EbbTypography.labelM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(48.dp),
                )
                Text("Open calendar + plan", style = EbbTypography.bodyL, modifier = Modifier.weight(1f))
                Text("5 min", style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TemplatesGalleryDesign(modifier: Modifier = Modifier) {
    Column(modifier.then(Modifier.fillMaxSize())) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { EbbSymbol("arrow_back", fontSize = 22.sp) }
            Text("Templates", style = EbbTypography.headlineM, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Box(Modifier.width(48.dp))
        }
        Text(
            "Editable copies, not rules. Change anything.",
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = EbbSpacing.s4),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(EbbSpacing.s4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TemplateCard("Morning", "wb_sunny", "6 steps · 45 min", Modifier.weight(1f))
            TemplateCard("Evening wind-down", "bedtime", "5 steps · 30 min", Modifier.weight(1f))
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = EbbSpacing.s4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TemplateCard("Work start-up", "work", "4 steps · 21 min", Modifier.weight(1f))
            TemplateCard("Exercise", "fitness_center", "3 steps · 20 min", Modifier.weight(1f))
        }
    }
}

@Composable
private fun TemplateCard(
    title: String,
    icon: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier.height(132.dp).then(modifier),
        shape = EbbShapes.lg,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(Modifier.padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    EbbSymbol(icon, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Text(title, style = EbbTypography.titleM, modifier = Modifier.padding(top = 12.dp))
            Text(subtitle, style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
