package app.ebbplanner.ui.text

import androidx.compose.runtime.staticCompositionLocalOf

val LocalTextOverflowReports =
    staticCompositionLocalOf<MutableMap<String, Boolean>?> { null }