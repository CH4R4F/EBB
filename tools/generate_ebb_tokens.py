#!/usr/bin/env python3
"""Generate EbbTokens.kt from design/system/tokens.json."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TOKENS_JSON = ROOT / "design/system/tokens.json"
OUT = ROOT / "app/src/main/java/app/ebbplanner/ui/theme/EbbTokens.kt"


def parse_color(value: str) -> str:
    value = value.strip()
    if value.startswith("rgba"):
        inner = value[value.index("(") + 1 : value.index(")")]
        parts = [p.strip() for p in inner.split(",")]
        r = int(float(parts[0]) * 255) if "." in parts[0] else int(parts[0])
        g = int(float(parts[1]) * 255) if "." in parts[1] else int(parts[1])
        b = int(float(parts[2]) * 255) if "." in parts[2] else int(parts[2])
        a = int(float(parts[3]) * 255) if float(parts[3]) <= 1 else int(float(parts[3]))
        return f"Color(0x{a:02X}{r:02X}{g:02X}{b:02X})"
    hexv = value.lstrip("#")
    return f"Color(0xFF{hexv.upper()})"


def camel(name: str) -> str:
    return name[0].upper() + name[1:] if name else name


def main() -> None:
    tokens = json.loads(TOKENS_JSON.read_text())
    out: list[str] = []
    append = out.append

    append("package app.ebbplanner.ui.theme\n")
    append("\nimport androidx.compose.foundation.shape.RoundedCornerShape\n")
    append("import androidx.compose.material3.ColorScheme\n")
    append("import androidx.compose.material3.Shapes\n")
    append("import androidx.compose.material3.Typography\n")
    append("import androidx.compose.material3.darkColorScheme\n")
    append("import androidx.compose.material3.lightColorScheme\n")
    append("import androidx.compose.runtime.Immutable\n")
    append("import androidx.compose.ui.graphics.Color\n")
    append("import androidx.compose.ui.text.TextStyle\n")
    append("import androidx.compose.ui.text.font.FontFamily\n")
    append("import androidx.compose.ui.text.font.FontWeight\n")
    append("import androidx.compose.ui.unit.dp\n")
    append("import androidx.compose.ui.unit.sp\n")
    append("\n/** Generated from design/system/tokens.json — re-run tools/generate_ebb_tokens.py */\n")
    append("@Immutable\nobject EbbPalette {\n")

    for scheme in ("light", "dark"):
        for key, obj in tokens["color"][scheme].items():
            append(f"    val {scheme}{camel(key)} = {parse_color(obj['$value'])}\n")

    append("}\n\n")
    append("@Immutable\nobject EbbTaskColors {\n")
    for name, obj in tokens["taskColor"].items():
        append(f"    val {name}Strong = {parse_color(obj['strong']['$value'])}\n")
        append(f"    val {name}TintLight = {parse_color(obj['tintLight']['$value'])}\n")
        append(f"    val {name}TintDark = {parse_color(obj['tintDark']['$value'])}\n")

    append("\n    fun tint(forKey: String, dark: Boolean): Color = when (forKey) {\n")
    for name in tokens["taskColor"]:
        append(f'        "{name}" -> if (dark) {name}TintDark else {name}TintLight\n')
    append('        else -> if (dark) slateTintDark else slateTintLight\n')
    append("    }\n\n    fun strong(forKey: String): Color = when (forKey) {\n")
    for name in tokens["taskColor"]:
        append(f'        "{name}" -> {name}Strong\n')
    append('        else -> slateStrong\n    }\n}\n\n')

    append("@Immutable\nobject EbbSpacing {\n")
    for k, v in tokens["spacing"].items():
        append(f"    val s{k} = {int(v['$value'].replace('dp', ''))}.dp\n")
    for k, v in tokens["layout"].items():
        prop = k[0].lower() + k[1:]
        append(f"    val {prop} = {int(v['$value'].replace('dp', ''))}.dp\n")
    append("}\n\n")

    append("@Immutable\nobject EbbShapes {\n")
    for k, v in tokens["shape"].items():
        if v["$value"] == "50%":
            append(f"    val {k} = RoundedCornerShape(percent = 50)\n")
        else:
            dp = int(v["$value"].replace("dp", ""))
            append(f"    val {k} = RoundedCornerShape({dp}.dp)\n")
    append(
        """
    val material: Shapes = Shapes(
        extraSmall = xs,
        small = sm,
        medium = md,
        large = lg,
        extraLarge = xl,
    )
}
"""
    )

    append("@Immutable\nobject EbbMotion {\n")
    for k, v in tokens["motion"].items():
        ms = int(v["$value"]["duration"].replace("ms", ""))
        append(f"    const val {k}Ms: Int = {ms}\n")
    append("}\n\n")

    typo_defs = []
    for key, obj in tokens["typography"].items():
        v = obj["$value"]
        size = v["fontSize"].replace("sp", "")
        lh = v["lineHeight"].replace("sp", "")
        fw = v["fontWeight"]
        typo_defs.append(
            f'        val {key} = TextStyle(fontFamily = sans, fontWeight = FontWeight.W{fw}, '
            f"fontSize = {size}.sp, lineHeight = {lh}.sp),\n"
        )

    append(
        """@Immutable
object EbbTypography {
    private val sans = FontFamily.SansSerif

"""
    )
    out.extend(typo_defs)
    append(
        """
    fun material(): Typography = Typography(
        displayLarge = display,
        displayMedium = headlineL,
        headlineLarge = headlineL,
        headlineMedium = headlineM,
        titleLarge = titleL,
        titleMedium = titleM,
        bodyLarge = bodyL,
        bodyMedium = bodyM,
        labelLarge = labelL,
        labelMedium = labelM,
        labelSmall = labelS,
    )
}
"""
    )

    append(
        """
fun ebbLightColorScheme(): ColorScheme = lightColorScheme(
    primary = EbbPalette.lightPrimary,
    onPrimary = EbbPalette.lightOnPrimary,
    primaryContainer = EbbPalette.lightPrimaryContainer,
    onPrimaryContainer = EbbPalette.lightOnPrimaryContainer,
    secondary = EbbPalette.lightSecondary,
    onSecondary = EbbPalette.lightOnSecondary,
    secondaryContainer = EbbPalette.lightSecondaryContainer,
    onSecondaryContainer = EbbPalette.lightOnSecondaryContainer,
    tertiary = EbbPalette.lightTertiary,
    onTertiary = EbbPalette.lightOnTertiary,
    tertiaryContainer = EbbPalette.lightTertiaryContainer,
    onTertiaryContainer = EbbPalette.lightOnTertiaryContainer,
    background = EbbPalette.lightBackground,
    onBackground = EbbPalette.lightOnSurface,
    surface = EbbPalette.lightSurface,
    onSurface = EbbPalette.lightOnSurface,
    surfaceVariant = EbbPalette.lightSurfaceVariant,
    onSurfaceVariant = EbbPalette.lightOnSurfaceVariant,
    outline = EbbPalette.lightOutlineStrong,
    outlineVariant = EbbPalette.lightOutline,
    error = EbbPalette.lightDestructive,
    onError = EbbPalette.lightOnDestructive,
    inverseSurface = EbbPalette.lightInverseSurface,
    inverseOnSurface = EbbPalette.lightInverseOnSurface,
    inversePrimary = EbbPalette.lightInversePrimary,
    surfaceContainer = EbbPalette.lightSurfaceContainer,
    surfaceContainerHigh = EbbPalette.lightSurfaceContainerHigh,
)

fun ebbDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = EbbPalette.darkPrimary,
    onPrimary = EbbPalette.darkOnPrimary,
    primaryContainer = EbbPalette.darkPrimaryContainer,
    onPrimaryContainer = EbbPalette.darkOnPrimaryContainer,
    secondary = EbbPalette.darkSecondary,
    onSecondary = EbbPalette.darkOnSecondary,
    secondaryContainer = EbbPalette.darkSecondaryContainer,
    onSecondaryContainer = EbbPalette.darkOnSecondaryContainer,
    tertiary = EbbPalette.darkTertiary,
    onTertiary = EbbPalette.darkOnTertiary,
    tertiaryContainer = EbbPalette.darkTertiaryContainer,
    onTertiaryContainer = EbbPalette.darkOnTertiaryContainer,
    background = EbbPalette.darkBackground,
    onBackground = EbbPalette.darkOnSurface,
    surface = EbbPalette.darkSurface,
    onSurface = EbbPalette.darkOnSurface,
    surfaceVariant = EbbPalette.darkSurfaceVariant,
    onSurfaceVariant = EbbPalette.darkOnSurfaceVariant,
    outline = EbbPalette.darkOutlineStrong,
    outlineVariant = EbbPalette.darkOutline,
    error = EbbPalette.darkDestructive,
    onError = EbbPalette.darkOnDestructive,
    inverseSurface = EbbPalette.darkInverseSurface,
    inverseOnSurface = EbbPalette.darkInverseOnSurface,
    inversePrimary = EbbPalette.darkInversePrimary,
    surfaceContainer = EbbPalette.darkSurfaceContainer,
    surfaceContainerHigh = EbbPalette.darkSurfaceContainerHigh,
)
"""
    )

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("".join(out))
    print(f"Wrote {OUT}")


if __name__ == "__main__":
    main()