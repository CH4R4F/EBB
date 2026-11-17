package app.ebbplanner.ui.today

data class QuickCaptureHighlight(
    val start: Int,
    val end: Int,
    val kind: QuickCaptureHighlightKind,
)

enum class QuickCaptureHighlightKind {
    Time,
    Duration,
    Tomorrow,
}

data class QuickCapturePresentationState(
    val mode: QuickCaptureMode = QuickCaptureMode.Empty,
    val text: String = "",
    val draftRestored: Boolean = false,
    val pastePromptVisible: Boolean = false,
    val highlights: List<QuickCaptureHighlight> = emptyList(),
    val counterText: String? = null,
    val footerHint: String = "Saves to your Inbox · Enter adds another",
    val showKeyboardStub: Boolean = false,
)

enum class QuickCaptureMode {
    Empty,
    Typing,
    Counter,
    DraftRestored,
    PastePrompt,
    NaturalLanguage,
    OverHome,
}
