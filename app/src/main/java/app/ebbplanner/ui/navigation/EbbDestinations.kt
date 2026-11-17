package app.ebbplanner.ui.navigation

object EbbDestinations {
    const val ONBOARDING_WELCOME = "onboarding/welcome"
    const val ONBOARDING_PROMISE = "onboarding/promise"
    const val ONBOARDING_PREFERENCES = "onboarding/preferences"
    const val ONBOARDING_DONE = "onboarding/done"
    const val MAIN = "main"
}

enum class MainTab(val route: String, val label: String) {
    Today("main/today", "Today"),
    Inbox("main/inbox", "Inbox"),
    Week("main/week", "Week"),
    Routines("main/routines", "Routines"),
}
