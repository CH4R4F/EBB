@file:Suppress("LongMethod", "MagicNumber", "MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.garden

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.onboarding.OnboardingAppPreview
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.today.TodayScreenDesign
import app.ebbplanner.ui.today.todayPopulatedDark

@Composable
fun GardenAppPreview(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    OnboardingAppPreview(darkTheme = darkTheme, content = content)
}

@Composable
fun GardenWinsMatureDesign(modifier: Modifier = Modifier) {
    Column(modifier.then(Modifier.fillMaxSize())) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { EbbSymbol("arrow_back", fontSize = 22.sp) }
            Text("Garden & wins", style = EbbTypography.headlineM, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            IconButton(onClick = {}) { EbbSymbol("more_vert", fontSize = 22.sp) }
        }
        Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            GardenPlanterIllustration(modifier = Modifier.size(240.dp))
            Text(
                "Your garden grows when you finish things.",
                style = EbbTypography.bodyL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = 16.dp),
            )
        }
        Surface(
            Modifier.fillMaxWidth().padding(EbbSpacing.s4),
            shape = EbbShapes.lg,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("This week", style = EbbTypography.titleM)
                Text(
                    "12 tasks finished · 3 routines complete",
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
fun CelebrationSmallCalmDesign(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        TodayScreenDesign(state = todayPopulatedDark(), showBottomNav = true, modifier = Modifier.fillMaxSize())
        val rippleColor = MaterialTheme.colorScheme.primary
        Box(Modifier.size(72.dp).align(Alignment.TopEnd).offset(x = (-48).dp, y = 300.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = rippleColor.copy(alpha = 0.5f),
                    radius = 26.dp.toPx(),
                    style = Stroke(width = 2.dp.toPx()),
                )
                drawCircle(
                    color = rippleColor.copy(alpha = 0.25f),
                    radius = 33.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 88.dp).fillMaxWidth(),
            shape = EbbShapes.md,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 6.dp,
        ) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("One less thing.", style = EbbTypography.bodyL, modifier = Modifier.weight(1f))
                Text("Undo", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun GardenPlanterIllustration(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val plants =
        listOf(
            Triple(30f, 220f, scheme.tertiary),
            Triple(67f, 217f, scheme.primary),
            Triple(104f, 214f, scheme.secondary),
            Triple(141f, 220f, EbbTaskColors.strong("amber")),
            Triple(178f, 217f, EbbTaskColors.strong("sky")),
            Triple(215f, 214f, EbbTaskColors.strong("green")),
        )
    Canvas(modifier) {
        val scale = size.width / 260f
        drawRoundRect(
            color = scheme.surfaceVariant,
            topLeft = Offset(20f * scale, 40f * scale),
            size = Size(220f * scale, 200f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f * scale),
            style = Stroke(width = 2f * scale),
        )
        drawRoundRect(
            color = scheme.surfaceVariant,
            topLeft = Offset(20f * scale, 40f * scale),
            size = Size(220f * scale, 200f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f * scale),
        )
        drawRoundRect(
            color = scheme.outline,
            topLeft = Offset(60f * scale, 24f * scale),
            size = Size(140f * scale, 22f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * scale),
        )
        plants.forEach { (x, y, color) ->
            drawOval(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset((x - 14f) * scale, (y - 9f) * scale),
                size = Size(28f * scale, 18f * scale),
            )
        }
    }
}
