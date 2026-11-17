@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.coloricon

data class ColorIconPresentationState(
    val searchQuery: String = "",
    val selectedColor: String = "amber",
    val selectedIcon: String = "edit_note",
    val colorOptions: List<String> = listOf("amber", "rose", "sky", "green", "blue", "lemon", "slate"),
    val iconOptions: List<String> = listOf("edit_note", "call", "mail", "brush", "local_post_office"),
    val mode: ColorIconMode = ColorIconMode.Selected,
)

enum class ColorIconMode {
    Selected,
    SearchEmpty,
}
