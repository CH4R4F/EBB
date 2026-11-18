package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.ebbplanner.R
import app.ebbplanner.domain.capture.QuickCaptureParser
import java.time.LocalDate

enum class QuickCaptureSaveTarget {
    Today,
    Inbox,
    Week,
}

const val QUICK_CAPTURE_TEXT_FIELD_TAG = "quick_capture_text_field"
const val QUICK_CAPTURE_ADD_BUTTON_TAG = "quick_capture_add_button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCaptureSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    defaultDate: LocalDate = LocalDate.now(),
    defaultText: String = "",
    saveTarget: QuickCaptureSaveTarget = QuickCaptureSaveTarget.Today,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(visible, defaultText) { mutableStateOf(defaultText) }
    val parsed = remember(title, defaultDate) { QuickCaptureParser.parse(title, defaultDate) }
    val footerHint =
        when (saveTarget) {
            QuickCaptureSaveTarget.Inbox -> stringResource(R.string.quick_capture_footer_inbox)
            QuickCaptureSaveTarget.Today, QuickCaptureSaveTarget.Week -> stringResource(R.string.quick_capture_footer_today)
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        QuickCaptureSheetContent(
            state =
                QuickCapturePresentationState(
                    mode = if (title.isBlank()) QuickCaptureMode.Empty else QuickCaptureMode.Typing,
                    text = title,
                    highlights = parsed.highlights,
                    footerHint = footerHint,
                ),
            modifier = Modifier.fillMaxWidth(),
            showDragHandle = false,
            interactive = true,
            text = title,
            onTextChange = { title = it },
            onAdd = {
                if (title.isNotBlank()) {
                    onSave(title)
                    title = ""
                    onDismiss()
                }
            },
            onAppendEstimate = { suffix ->
                val trimmed = title.trimEnd()
                val hasDuration = trimmed.contains(Regex("""\b\d+\s*min\b""", RegexOption.IGNORE_CASE))
                if (!hasDuration) {
                    title = if (trimmed.isEmpty()) suffix else "$trimmed $suffix"
                }
            },
        )
    }
}