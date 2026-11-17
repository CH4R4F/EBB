package app.ebbplanner.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import app.ebbplanner.R

@Immutable
object EbbFonts {
    val lexend =
        FontFamily(
            Font(R.font.lexend, weight = FontWeight.W500),
        )

    val atkinsonNext =
        FontFamily(
            Font(R.font.atkinson_next_400, weight = FontWeight.W400),
            Font(R.font.atkinson_next_700, weight = FontWeight.W700),
        )

    val atkinsonMono =
        FontFamily(
            Font(R.font.atkinson_mono_500, weight = FontWeight.W500),
        )

    val symbols =
        FontFamily(
            Font(R.font.material_symbols_rounded, weight = FontWeight.Normal, style = FontStyle.Normal),
        )
}
