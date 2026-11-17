package app.ebbplanner.ui.navigation

object EbbDestinations {
    const val ONBOARDING_WELCOME = "onboarding/o1-welcome"
    const val ONBOARDING_PROMISE = "onboarding/o2-promise"
    const val ONBOARDING_DAY_SHAPE = "onboarding/o3-day-shape"
    const val ONBOARDING_PREFERENCES = "onboarding/o4-preferences"
    const val ONBOARDING_TRY_IT = "onboarding/o8-try-it"
    const val ONBOARDING_NOTIFICATIONS = "onboarding/o5-notifications"
    const val ONBOARDING_EXACT_ALARMS = "onboarding/o6-exact-alarms"
    const val ONBOARDING_CALENDAR = "onboarding/o7-calendar"
    const val ONBOARDING_DONE = "onboarding/o9-done"
    const val MAIN = "main"
    const val ONBOARDING_GRAPH = "onboarding"
}

object AppRoutes {
    const val TASK_DETAIL = "flow/task/{taskId}"
    const val SHIFT_DAY = "flow/shift"
    const val PLAN_DAY = "flow/plan"
    const val FOCUS_RUN = "flow/focus"

    fun taskDetail(taskId: String): String = "flow/task/$taskId"
}

enum class MainTab(val route: String, val label: String) {
    Today("main/today", "Today"),
    Inbox("main/inbox", "Inbox"),
    Week("main/week", "Week"),
    Routines("main/routines", "Routines"),
}
