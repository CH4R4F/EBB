package app.ebbplanner.ui.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.ebbplanner.R
import app.ebbplanner.ui.theme.EbbSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCaptureSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(visible) { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EbbSpacing.marginMedium, vertical = EbbSpacing.s4),
        ) {
            Text(text = stringResource(R.string.quick_capture_title))
            Spacer(modifier = Modifier.height(EbbSpacing.s3))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.quick_capture_hint)) },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(EbbSpacing.s4))
            Button(
                onClick = {
                    onSave(title)
                    title = ""
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank(),
            ) {
                Text(stringResource(R.string.action_save))
            }
            Spacer(modifier = Modifier.height(EbbSpacing.s8))
        }
    }
}
