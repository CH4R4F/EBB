@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.coloricon

fun colorIconSelected(): ColorIconPresentationState = ColorIconPresentationState()

fun colorIconSearchEmpty(): ColorIconPresentationState =
    ColorIconPresentationState(
        searchQuery = "boat",
        mode = ColorIconMode.SearchEmpty,
        iconOptions = emptyList(),
    )
