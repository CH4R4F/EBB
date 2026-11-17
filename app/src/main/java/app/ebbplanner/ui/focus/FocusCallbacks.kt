package app.ebbplanner.ui.focus

data class FocusCallbacks(
    val onClose: () -> Unit = {},
    val onStartFocus: () -> Unit = {},
    val onJustFiveMinutes: () -> Unit = {},
    val onPause: () -> Unit = {},
    val onResume: () -> Unit = {},
    val onAddFive: () -> Unit = {},
    val onDone: () -> Unit = {},
    val onDistracted: () -> Unit = {},
    val onTimesUpDone: () -> Unit = {},
    val onTimesUpAddFive: () -> Unit = {},
    val onTimesUpAddFifteen: () -> Unit = {},
    val onTimesUpShiftDay: () -> Unit = {},
    val onTimesUpBreak: () -> Unit = {},
    val onMenu: () -> Unit = {},
    val onTimerModeSelected: (Int) -> Unit = {},
    val onRingBarSelected: (Int) -> Unit = {},
    val onHideNumbersChanged: (Boolean) -> Unit = {},
    val onFixedMinutesSelected: (Int) -> Unit = {},
)