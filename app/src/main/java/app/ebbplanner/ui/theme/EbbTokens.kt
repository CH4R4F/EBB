package app.ebbplanner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Generated from design/system/tokens.json — re-run tools/generate_ebb_tokens.py */
@Immutable
object EbbPalette {
    val lightBackground = Color(0xFFFAF8F5)
    val lightSurface = Color(0xFFFFFFFF)
    val lightSurfaceVariant = Color(0xFFF0ECE6)
    val lightSurfaceContainer = Color(0xFFF5F2ED)
    val lightSurfaceContainerHigh = Color(0xFFECE8E2)
    val lightOnSurface = Color(0xFF1F2421)
    val lightOnSurfaceVariant = Color(0xFF535C58)
    val lightOnTintVariant = Color(0xFF535C58)
    val lightPrimary = Color(0xFF2F6F6A)
    val lightOnPrimary = Color(0xFFFFFFFF)
    val lightPrimaryContainer = Color(0xFFCDE8E4)
    val lightOnPrimaryContainer = Color(0xFF0E3B37)
    val lightSecondary = Color(0xFF5E5288)
    val lightOnSecondary = Color(0xFFFFFFFF)
    val lightSecondaryContainer = Color(0xFFE6E0F7)
    val lightOnSecondaryContainer = Color(0xFF1E1640)
    val lightTertiary = Color(0xFF9A5B2E)
    val lightOnTertiary = Color(0xFFFFFFFF)
    val lightTertiaryContainer = Color(0xFFFBE3D0)
    val lightOnTertiaryContainer = Color(0xFF3A1E08)
    val lightOutline = Color(0xFFC9C4BC)
    val lightOutlineStrong = Color(0xFF857F76)
    val lightDestructive = Color(0xFFA3402F)
    val lightOnDestructive = Color(0xFFFFFFFF)
    val lightSuccess = Color(0xFF2F6F6A)
    val lightInverseSurface = Color(0xFF2E3330)
    val lightInverseOnSurface = Color(0xFFF0F1EE)
    val lightInversePrimary = Color(0xFF8FD1C9)
    val lightScrim = Color(0x51000000)
    val darkBackground = Color(0xFF121614)
    val darkSurface = Color(0xFF1A1F1D)
    val darkSurfaceVariant = Color(0xFF252B29)
    val darkSurfaceContainer = Color(0xFF1E2321)
    val darkSurfaceContainerHigh = Color(0xFF2A302E)
    val darkOnSurface = Color(0xFFE3E6E3)
    val darkOnSurfaceVariant = Color(0xFFA9B2AE)
    val darkOnTintVariant = Color(0xFFD0D5D2)
    val darkPrimary = Color(0xFF8FD1C9)
    val darkOnPrimary = Color(0xFF00332F)
    val darkPrimaryContainer = Color(0xFF1F4F4A)
    val darkOnPrimaryContainer = Color(0xFFCDE8E4)
    val darkSecondary = Color(0xFFC9BEF0)
    val darkOnSecondary = Color(0xFF2F2757)
    val darkSecondaryContainer = Color(0xFF3F3766)
    val darkOnSecondaryContainer = Color(0xFFE6E0F7)
    val darkTertiary = Color(0xFFF0B98D)
    val darkOnTertiary = Color(0xFF4A2808)
    val darkTertiaryContainer = Color(0xFF5C3A1F)
    val darkOnTertiaryContainer = Color(0xFFFBE3D0)
    val darkOutline = Color(0xFF3A423F)
    val darkOutlineStrong = Color(0xFF7D8783)
    val darkDestructive = Color(0xFFF2A190)
    val darkOnDestructive = Color(0xFF4A120A)
    val darkSuccess = Color(0xFF8FD1C9)
    val darkInverseSurface = Color(0xFFE3E6E3)
    val darkInverseOnSurface = Color(0xFF2E3330)
    val darkInversePrimary = Color(0xFF2F6F6A)
    val darkScrim = Color(0x8C000000)
}

@Immutable
object EbbTaskColors {
    val amberStrong = Color(0xFFE69F00)
    val amberTintLight = Color(0xFFFAEAC7)
    val amberTintDark = Color(0xFF574514)
    val skyStrong = Color(0xFF56B4E9)
    val skyTintLight = Color(0xFFDAEEFA)
    val skyTintDark = Color(0xFF2C4C5A)
    val greenStrong = Color(0xFF009E73)
    val greenTintLight = Color(0xFFC7EAE0)
    val greenTintDark = Color(0xFF124537)
    val lemonStrong = Color(0xFFF0E442)
    val lemonTintLight = Color(0xFFFCF9D5)
    val lemonTintDark = Color(0xFF5A5A28)
    val blueStrong = Color(0xFF0072B2)
    val blueTintLight = Color(0xFFC7E0EE)
    val blueTintDark = Color(0xFF12384A)
    val vermillionStrong = Color(0xFFD55E00)
    val vermillionTintLight = Color(0xFFF6DCC7)
    val vermillionTintDark = Color(0xFF523214)
    val roseStrong = Color(0xFFCC79A7)
    val roseTintLight = Color(0xFFF4E2EC)
    val roseTintDark = Color(0xFF4F3A46)
    val slateStrong = Color(0xFF8C8C8C)
    val slateTintLight = Color(0xFFE6E6E6)
    val slateTintDark = Color(0xFF3C403E)

    fun tint(
        forKey: String,
        dark: Boolean,
    ): Color =
        when (forKey) {
            "amber" -> if (dark) amberTintDark else amberTintLight
            "sky" -> if (dark) skyTintDark else skyTintLight
            "green" -> if (dark) greenTintDark else greenTintLight
            "lemon" -> if (dark) lemonTintDark else lemonTintLight
            "blue" -> if (dark) blueTintDark else blueTintLight
            "vermillion" -> if (dark) vermillionTintDark else vermillionTintLight
            "rose" -> if (dark) roseTintDark else roseTintLight
            "slate" -> if (dark) slateTintDark else slateTintLight
            else -> if (dark) slateTintDark else slateTintLight
        }

    fun strong(forKey: String): Color =
        when (forKey) {
            "amber" -> amberStrong
            "sky" -> skyStrong
            "green" -> greenStrong
            "lemon" -> lemonStrong
            "blue" -> blueStrong
            "vermillion" -> vermillionStrong
            "rose" -> roseStrong
            "slate" -> slateStrong
            else -> slateStrong
        }
}

@Immutable
object EbbSpacing {
    val s1 = 4.dp
    val s2 = 8.dp
    val s3 = 12.dp
    val s4 = 16.dp
    val s5 = 20.dp
    val s6 = 24.dp
    val s8 = 32.dp
    val s10 = 40.dp
    val s12 = 48.dp
    val s16 = 64.dp
    val marginCompact = 16.dp
    val marginMedium = 24.dp
    val marginExpanded = 32.dp
    val cardPadCompact = 12.dp
    val cardPadComfortable = 16.dp
    val listGap = 8.dp
    val hourRail = 56.dp
    val hourCompact = 48.dp
    val hourComfortable = 72.dp
    val hourSpacious = 112.dp
    val blockMin = 28.dp
    val blockInset = 8.dp
    val touchMin = 48.dp
    val touchBig = 56.dp
}

@Immutable
object EbbShapes {
    val xs = RoundedCornerShape(4.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(28.dp)
    val full = RoundedCornerShape(percent = 50)

    val material: Shapes =
        Shapes(
            extraSmall = xs,
            small = sm,
            medium = md,
            large = lg,
            extraLarge = xl,
        )
}

@Immutable
object EbbMotion {
    const val shortMs: Int = 125
    const val mediumMs: Int = 300
    const val longMs: Int = 450
    const val reducedMs: Int = 150
}

@Immutable
object EbbTypography {
    private val heading = EbbFonts.lexend
    private val body = EbbFonts.atkinsonNext
    private val mono = EbbFonts.atkinsonMono

    val display =
        TextStyle(
            fontFamily = heading,
            fontWeight = FontWeight.W500,
            fontSize = 40.sp,
            lineHeight = 48.sp,
        )
    val onboardingTitle =
        TextStyle(
            fontFamily = heading,
            fontWeight = FontWeight.W500,
            fontSize = 32.sp,
            lineHeight = 40.sp,
        )
    val timerXL =
        TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.W500,
            fontSize = 64.sp,
            lineHeight = 72.sp,
        )
    val timerL =
        TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.W500,
            fontSize = 40.sp,
            lineHeight = 48.sp,
        )
    val headlineL =
        TextStyle(
            fontFamily = heading,
            fontWeight = FontWeight.W500,
            fontSize = 28.sp,
            lineHeight = 36.sp,
        )
    val headlineM =
        TextStyle(
            fontFamily = heading,
            fontWeight = FontWeight.W500,
            fontSize = 24.sp,
            lineHeight = 32.sp,
        )
    val titleL =
        TextStyle(
            fontFamily = heading,
            fontWeight = FontWeight.W500,
            fontSize = 20.sp,
            lineHeight = 28.sp,
        )
    val titleM =
        TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.W700,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
    val bodyL =
        TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
    val bodyM =
        TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.W400,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    val labelL =
        TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.W700,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        )
    val labelM =
        TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.W700,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.sp,
        )
    val labelS =
        TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.W700,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.sp,
        )

    fun material(): Typography =
        Typography(
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

fun ebbLightColorScheme(): ColorScheme =
    lightColorScheme(
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

fun ebbDarkColorScheme(): ColorScheme =
    darkColorScheme(
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
