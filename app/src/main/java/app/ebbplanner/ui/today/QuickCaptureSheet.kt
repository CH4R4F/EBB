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
import app.ebbplanner.domain.capture.QuickCaptureParser
import java.time.LocalDate

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
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(visible, defaultText) { mutableStateOf(defaultText) }
    val parsed = remember(title, defaultDate) { QuickCaptureParser.parse(title, defaultDate) }

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
                title = if (trimmed.isEmpty()) suffix else "$trimmed $suffix"
            },
        )
    }
}