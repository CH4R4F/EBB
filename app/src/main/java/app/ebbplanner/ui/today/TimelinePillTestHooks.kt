package app.ebbplanner.ui.today

import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalTimelinePillChosenLabels =
    staticCompositionLocalOf<MutableMap<String, String>?> { null }

internal val LocalTimelinePillLineEllipsized =
    staticCompositionLocalOf<MutableMap<String, Boolean>?> { null }

internal fun timelinePillEllipsizedKey(blockId: String): String = timelineTimeLeftPillTag(blockId) + "_line_ellipsized"