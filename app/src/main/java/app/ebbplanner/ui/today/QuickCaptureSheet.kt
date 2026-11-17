package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import app.ebbplanner.ui.theme.EbbSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCaptureSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    defaultDate: LocalDate = LocalDate.now(),
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(visible) { mutableStateOf("") }
    val parsed = remember(title, defaultDate) { QuickCaptureParser.parse(title, defaultDate) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            QuickCaptureSheetContent(
                state =
                    QuickCapturePresentationState(
                        mode = if (title.isBlank()) QuickCaptureMode.Empty else QuickCaptureMode.Typing,
                        text = title,
                        highlights = parsed.highlights,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
                placeholder = { androidx.compose.material3.Text(stringResource(R.string.quick_capture_hint)) },
            )
            androidx.compose.material3.TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title)
                        title = ""
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                androidx.compose.material3.Text(stringResource(R.string.quick_capture_add))
            }
        }
    }
}
