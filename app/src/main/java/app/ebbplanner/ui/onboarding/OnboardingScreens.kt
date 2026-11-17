package app.ebbplanner.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.R
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun OnboardingWelcomeScreen(
    onGetStarted: () -> Unit,
    onRestoreBackup: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(EbbSpacing.marginMedium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = EbbTypography.display,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(EbbSpacing.s6))
        Text(
            text = stringResource(R.string.onboarding_welcome_tagline),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(EbbSpacing.s12))
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.onboarding_get_started))
        }
        OutlinedButton(
            onClick = onRestoreBackup,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.onboarding_restore_backup))
        }
    }
}

@Composable
fun OnboardingPromiseScreen(onContinue: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(EbbSpacing.marginMedium),
        verticalArrangement = Arrangement.spacedBy(EbbSpacing.s6),
    ) {
        Text(
            text = stringResource(R.string.onboarding_promise_title),
            style = EbbTypography.headlineL,
        )
        PromiseRow(
            title = stringResource(R.string.promise_free_forever),
            subtitle = stringResource(R.string.promise_free_forever_detail),
            icon = Icons.Outlined.VolunteerActivism,
        )
        PromiseRow(
            title = stringResource(R.string.promise_no_account),
            subtitle = stringResource(R.string.promise_no_account_detail),
            icon = Icons.Outlined.PersonOff,
        )
        PromiseRow(
            title = stringResource(R.string.promise_private_offline),
            subtitle = stringResource(R.string.promise_private_offline_detail),
            icon = Icons.Outlined.CloudOff,
        )
        PromiseRow(
            title = stringResource(R.string.promise_open_source),
            subtitle = stringResource(R.string.promise_open_source_detail),
            icon = Icons.Outlined.Code,
        )
        Spacer(modifier = Modifier.height(EbbSpacing.s4))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_continue))
        }
    }
}

@Composable
private fun PromiseRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s4),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(EbbSpacing.s3),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Column {
            Text(text = title, style = EbbTypography.titleM)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun OnboardingPreferencesScreen(
    onContinue: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(EbbSpacing.marginMedium),
        verticalArrangement = Arrangement.spacedBy(EbbSpacing.s4),
    ) {
        Text(
            text = stringResource(R.string.onboarding_preferences_title),
            style = EbbTypography.headlineL,
        )
        Text(
            text = stringResource(R.string.onboarding_day_shape_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = stringResource(R.string.onboarding_wake_label), style = EbbTypography.titleM)
        TimeChipRow(
            selected = state.wakeMinutes,
            options = listOf(6 * 60, 7 * 60, 8 * 60, 9 * 60),
            onSelect = viewModel::setWakeMinutes,
        )
        Text(text = stringResource(R.string.onboarding_bed_label), style = EbbTypography.titleM)
        TimeChipRow(
            selected = state.bedMinutes,
            options = listOf(22 * 60, 23 * 60, 24 * 60 - 1),
            onSelect = viewModel::setBedMinutes,
        )
        Text(
            text = stringResource(R.string.onboarding_change_anytime),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_continue))
        }
    }
}

@Composable
private fun TimeChipRow(
    selected: Int,
    options: List<Int>,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        options.forEach { minutes ->
            FilterChip(
                selected = selected == minutes,
                onClick = { onSelect(minutes) },
                label = { Text(formatMinutes(minutes)) },
            )
        }
    }
}

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "%02d:%02d".format(hours, minutes)
}

@Composable
fun OnboardingDoneScreen(
    onOpenMyDay: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(EbbSpacing.marginMedium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.onboarding_done_title),
            style = EbbTypography.headlineL,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(EbbSpacing.s4))
        Text(
            text =
                stringResource(
                    R.string.onboarding_done_summary,
                    formatMinutes(state.wakeMinutes),
                    formatMinutes(state.bedMinutes),
                ),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(EbbSpacing.s6))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.onboarding_add_sample_tasks))
            Switch(
                checked = state.addSampleTasks,
                onCheckedChange = viewModel::setAddSampleTasks,
            )
        }
        Spacer(modifier = Modifier.height(EbbSpacing.s8))
        Button(
            onClick = { viewModel.completeOnboarding(onOpenMyDay) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.onboarding_open_my_day))
        }
    }
}
