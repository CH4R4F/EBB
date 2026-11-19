@file:Suppress("LongParameterList")

package app.ebbplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ebbplanner.R
import app.ebbplanner.ui.text.LocalTextOverflowReports
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTypography

const val EBB_SHEET_CONFIRM_TAG = "ebb_sheet_confirm"
const val EBB_SHEET_CANCEL_TAG = "ebb_sheet_cancel"
const val EBB_SHEET_CONFIRM_LABEL_TAG = "ebb_sheet_confirm_label"
const val EBB_SHEET_CANCEL_LABEL_TAG = "ebb_sheet_cancel_label"

private val minTouch = 48.dp

@Composable
fun EbbConfirmButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = EBB_SHEET_CONFIRM_TAG,
) {
    val overflow = LocalTextOverflowReports.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .defaultMinSize(minHeight = minTouch)
                .heightIn(min = minTouch)
                .testTag(testTag)
                .semantics { contentDescription = label },
    ) {
        Text(
            label,
            style = EbbTypography.labelL,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.testTag(EBB_SHEET_CONFIRM_LABEL_TAG),
            onTextLayout = {
                overflow?.put(EBB_SHEET_CONFIRM_LABEL_TAG, it.hasVisualOverflow || it.isLineEllipsized(0))
            },
        )
    }
}

@Composable
fun EbbCancelButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = EBB_SHEET_CANCEL_TAG,
) {
    val overflow = LocalTextOverflowReports.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .defaultMinSize(minHeight = minTouch)
                .heightIn(min = minTouch)
                .testTag(testTag)
                .semantics { contentDescription = label },
    ) {
        Text(
            label,
            style = EbbTypography.labelL,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.testTag(EBB_SHEET_CANCEL_LABEL_TAG),
            onTextLayout = {
                overflow?.put(EBB_SHEET_CANCEL_LABEL_TAG, it.hasVisualOverflow || it.isLineEllipsized(0))
            },
        )
    }
}

@Composable
fun EbbSheetActions(
    confirmLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    cancelLabel: String = stringResource(R.string.action_cancel),
    confirmEnabled: Boolean = true,
    cancelEnabled: Boolean = true,
    confirmTestTag: String = EBB_SHEET_CONFIRM_TAG,
    cancelTestTag: String = EBB_SHEET_CANCEL_TAG,
    stacked: Boolean? = null,
) {
    val fontScale = LocalConfiguration.current.fontScale
    val useStack = stacked ?: (fontScale >= 1.3f)
    if (useStack) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            EbbConfirmButton(
                label = confirmLabel,
                onClick = onConfirm,
                enabled = confirmEnabled,
                testTag = confirmTestTag,
                modifier = Modifier.fillMaxWidth(),
            )
            EbbCancelButton(
                label = cancelLabel,
                onClick = onCancel,
                enabled = cancelEnabled,
                testTag = cancelTestTag,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EbbCancelButton(
                label = cancelLabel,
                onClick = onCancel,
                enabled = cancelEnabled,
                testTag = cancelTestTag,
            )
            Spacer(Modifier.width(EbbSpacing.s2))
            EbbConfirmButton(
                label = confirmLabel,
                onClick = onConfirm,
                enabled = confirmEnabled,
                testTag = confirmTestTag,
            )
        }
    }
}
