package app.ebbplanner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.ebbplanner.R
import app.ebbplanner.data.preferences.UserPreferencesRepository
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTypography
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

fun weekStartDayLabel(stored: Int, locale: Locale = Locale.getDefault()): String =
    when (stored) {
        UserPreferencesRepository.WEEK_START_USE_LOCALE -> {
            val regional = java.time.temporal.WeekFields.of(locale).firstDayOfWeek
            regional.getDisplayName(TextStyle.FULL, locale)
        }
        else -> DayOfWeek.of(stored).getDisplayName(TextStyle.FULL, locale)
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekStartDayPickerSheet(
    visible: Boolean,
    selected: Int,
  onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val options =
        listOf(UserPreferencesRepository.WEEK_START_USE_LOCALE) +
            DayOfWeek.entries.map { it.value }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(bottom = EbbSpacing.s6)) {
            Text(
                stringResource(R.string.week_start_picker_title),
                style = EbbTypography.titleL,
                modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
            )
            options.forEach { value ->
                val label =
                    when (value) {
                        UserPreferencesRepository.WEEK_START_USE_LOCALE ->
                            stringResource(R.string.week_start_follow_device, weekStartDayLabel(value))
                        else -> weekStartDayLabel(value)
                    }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(value)
                                onDismiss()
                            }
                            .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == value, onClick = null)
                    Text(label, style = EbbTypography.bodyL, modifier = Modifier.padding(start = EbbSpacing.s2))
                }
            }
        }
    }
}