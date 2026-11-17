@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.coloricon

data class ColorIconPresentationState(
    val searchQuery: String = "",
    val selectedColor: String = "amber",
    val selectedIcon: String = "edit_note",
    val colorOptions: List<String> =
        listOf("amber", "sky", "green", "lemon", "blue", "vermillion", "rose", "slate"),
    val iconOptions: List<String> =
        listOf(
            "home", "work", "call", "mail", "shopping_cart", "medication", "fitness_center",
            "menu_book", "school", "directions_car", "cleaning_services", "local_laundry_service",
            "restaurant", "water_drop", "bedtime", "self_improvement", "music_note", "code", "brush",
            "pets", "child_care", "payments", "calendar_month", "redeem", "potted_plant", "edit_note",
            "local_post_office", "medical_services",
        ),
    val mode: ColorIconMode = ColorIconMode.Selected,
)

enum class ColorIconMode {
    Selected,
    SearchEmpty,
}
