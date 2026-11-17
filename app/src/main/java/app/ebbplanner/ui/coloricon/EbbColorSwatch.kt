@file:Suppress("MagicNumber", "NewLineAtEndOfFile", "LongMethod")

package app.ebbplanner.ui.coloricon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors

@Composable
fun EbbColorSwatch(
    colorKey: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val strong = EbbTaskColors.strong(colorKey)
    Surface(
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = strong,
        border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val patternColor = Color.Black.copy(alpha = 0.28f)
            when (colorKey) {
                "sky" ->
                    repeat(((size.width / 7f).toInt() + 2)) { col ->
                        repeat(((size.height / 7f).toInt() + 2)) { row ->
                            drawCircle(patternColor, radius = 1.5f, center = Offset(col * 7f, row * 7f))
                        }
                    }
                "green" -> {
                    var y = -size.height
                    while (y < size.height * 2) {
                        drawLine(patternColor, Offset(0f, y), Offset(size.width, y + size.width), strokeWidth = 2f)
                        y += 6f
                    }
                }
                "lemon" -> {
                    var y = 0f
                    while (y < size.height) {
                        drawLine(patternColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
                        y += 6f
                    }
                }
                "blue" -> {
                    var x = 0f
                    while (x < size.width) {
                        drawLine(patternColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2f)
                        x += 6f
                    }
                }
                "vermillion" -> {
                    var y = -size.height
                    while (y < size.height * 2) {
                        drawLine(patternColor, Offset(0f, y), Offset(size.width, y + size.width), strokeWidth = 1.5f)
                        drawLine(patternColor, Offset(0f, y + size.width), Offset(size.width, y), strokeWidth = 1.5f)
                        y += 6f
                    }
                }
                "rose" -> {
                    repeat(((size.width / 8f).toInt() + 2)) { col ->
                        drawArc(
                            color = patternColor,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(col * 8f, 0f),
                            size = androidx.compose.ui.geometry.Size(8f, 6f),
                            style = Stroke(width = 1.5f),
                        )
                    }
                }
                else -> Unit
            }
        }
        if (selected) {
            Box(contentAlignment = Alignment.Center) {
                Surface(Modifier.size(26.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                    Box(contentAlignment = Alignment.Center) {
                        EbbSymbol("check", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
