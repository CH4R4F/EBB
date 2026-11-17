package app.ebbplanner.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun EbbSymbol(
    name: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    color: Color = LocalContentColor.current,
    filled: Boolean = false,
) {
    val family = if (filled) EbbFonts.symbolsFilled else EbbFonts.symbolsOutlined
    Text(
        text = name,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = family,
        textAlign = TextAlign.Center,
        style =
            TextStyle(
                fontFamily = family,
                fontSize = fontSize,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
    )
}
