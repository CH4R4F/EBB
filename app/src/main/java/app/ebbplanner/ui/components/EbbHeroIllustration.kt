@file:Suppress("MagicNumber", "MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.theme.EbbSymbol

@Composable
fun EbbHeroIllustration(
    icon: String,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    iconSize: TextUnit = 56.sp,
) {
    val tertiary = MaterialTheme.colorScheme.tertiary
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val scale = this.size.minDimension / 160f
            drawCircle(color = tertiaryContainer, radius = 72f * scale, center = Offset(cx, cy))
            val wave1 =
                Path().apply {
                    moveTo(14f * scale, 104f * scale)
                    quadraticBezierTo(37f * scale, 92f * scale, 60f * scale, 104f * scale)
                    quadraticBezierTo(83f * scale, 116f * scale, 106f * scale, 104f * scale)
                    quadraticBezierTo(129f * scale, 92f * scale, 152f * scale, 104f * scale)
                    lineTo(152f * scale, 120f * scale)
                    quadraticBezierTo(120f * scale, 156f * scale, 80f * scale, 152f * scale)
                    quadraticBezierTo(40f * scale, 156f * scale, 8f * scale, 120f * scale)
                    close()
                }
            drawPath(wave1, color = tertiary.copy(alpha = 0.18f))
            val wave2 =
                Path().apply {
                    moveTo(20f * scale, 118f * scale)
                    quadraticBezierTo(42f * scale, 108f * scale, 64f * scale, 118f * scale)
                    quadraticBezierTo(86f * scale, 128f * scale, 108f * scale, 118f * scale)
                    quadraticBezierTo(130f * scale, 108f * scale, 150f * scale, 118f * scale)
                    lineTo(146f * scale, 128f * scale)
                    quadraticBezierTo(118f * scale, 154f * scale, 80f * scale, 152f * scale)
                    quadraticBezierTo(42f * scale, 154f * scale, 14f * scale, 128f * scale)
                    close()
                }
            drawPath(wave2, color = tertiary.copy(alpha = 0.22f))
            drawCircle(color = tertiary.copy(alpha = 0.25f), radius = 8f * scale, center = Offset(cx + 42f * scale, cy - 40f * scale))
            drawCircle(color = tertiary.copy(alpha = 0.2f), radius = 5f * scale, center = Offset(cx - 44f * scale, cy - 28f * scale))
        }
        EbbSymbol(icon, fontSize = iconSize, color = MaterialTheme.colorScheme.tertiary)
    }
}
