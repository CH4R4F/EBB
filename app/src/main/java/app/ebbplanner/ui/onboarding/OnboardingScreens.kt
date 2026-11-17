@file:Suppress("LongMethod", "LongParameterList", "MaxLineLength", "UnusedParameter")

package app.ebbplanner.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.R
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

@Composable
private fun OnboardingScreenScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .consumeWindowInsets(WindowInsets.safeDrawing)
                .imePadding(),
    ) {
        topBar?.invoke()
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = EbbSpacing.marginMedium),
        ) {
            content()
        }
        bottomBar()
    }
}

@Composable
fun OnboardingWelcomeScreen(
    onGetStarted: () -> Unit,
    onRestoreBackup: () -> Unit,
) {
    OnboardingScreenScaffold(
        bottomBar = {
            OnboardingBottomActions {
                OnboardingPrimaryButton(
                    text = stringResource(R.string.onboarding_get_started),
                    onClick = onGetStarted,
                )
                OnboardingTextButton(
                    text = stringResource(R.string.onboarding_restore_backup),
                    onClick = onRestoreBackup,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OnboardingBlobIllustration(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                accentColor = MaterialTheme.colorScheme.primary,
                symbolName = "waves",
                size = 220.dp,
            )
            Text(
                text = stringResource(R.string.app_name),
                style = EbbTypography.display,
                modifier = Modifier.padding(top = EbbSpacing.s8),
            )
            Text(
                text = stringResource(R.string.onboarding_welcome_tagline),
                style = EbbTypography.titleL.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W400),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = EbbSpacing.s2),
            )
            Text(
                text = stringResource(R.string.onboarding_welcome_body),
                style = EbbTypography.bodyL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = EbbSpacing.s4).fillMaxWidth(0.9f),
            )
        }
    }
}

@Composable
fun OnboardingPromiseScreen(onContinue: () -> Unit) {
    OnboardingScreenScaffold(
        topBar = { OnboardingTopBar(activeSteps = 1) },
        bottomBar = {
            OnboardingBottomActions {
                OnboardingPrimaryButton(
                    text = stringResource(R.string.action_continue),
                    onClick = onContinue,
                )
            }
        },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OnboardingBlobIllustration(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                accentColor = MaterialTheme.colorScheme.secondary,
                symbolName = "handshake",
                size = 120.dp,
            )
            Text(
                text = stringResource(R.string.onboarding_promise_title),
                style = EbbTypography.onboardingTitle,
                modifier = Modifier.padding(top = EbbSpacing.s6),
            )
            Column(
                modifier = Modifier.padding(top = EbbSpacing.s6),
                verticalArrangement = Arrangement.spacedBy(EbbSpacing.s4),
            ) {
                OnboardingPromiseRow(
                    symbolName = "volunteer_activism",
                    title = stringResource(R.string.promise_free_forever),
                    subtitle = stringResource(R.string.promise_free_forever_detail),
                )
                OnboardingPromiseRow(
                    symbolName = "person_off",
                    title = stringResource(R.string.promise_no_account),
                    subtitle = stringResource(R.string.promise_no_account_detail),
                )
                OnboardingPromiseRow(
                    symbolName = "lock",
                    title = stringResource(R.string.promise_private_offline),
                    subtitle = stringResource(R.string.promise_private_offline_detail),
                )
                OnboardingPromiseRow(
                    symbolName = "code",
                    title = stringResource(R.string.promise_open_source),
                    subtitle = stringResource(R.string.promise_open_source_detail),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingDayShapeScreen(
    state: OnboardingUiState,
    onWakeChange: (Int) -> Unit,
    onBedChange: (Int) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    var pickingWake by remember { mutableStateOf<Boolean?>(null) }
    if (pickingWake != null) {
        val minutes = if (pickingWake == true) state.wakeMinutes else state.bedMinutes
        val pickerState =
            rememberTimePickerState(
                initialHour = minutes / 60,
                initialMinute = minutes % 60,
                is24Hour = true,
            )
        AlertDialog(
            onDismissRequest = { pickingWake = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val total = pickerState.hour * 60 + pickerState.minute
                        if (pickingWake == true) onWakeChange(total) else onBedChange(total)
                        pickingWake = null
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { pickingWake = null }) {
                    Text(stringResource(R.string.onboarding_not_now))
                }
            },
            text = { TimePicker(state = pickerState) },
        )
    }
    OnboardingScreenScaffold(
        topBar = { OnboardingTopBar(activeSteps = 2, showSkip = true, onSkip = onSkip) },
        bottomBar = {
            OnboardingBottomActions {
                OnboardingPrimaryButton(
                    text = stringResource(R.string.action_continue),
                    onClick = onContinue,
                )
            }
        },
    ) {
        Text(stringResource(R.string.onboarding_day_shape_title), style = EbbTypography.onboardingTitle)
        Text(
            text = stringResource(R.string.onboarding_day_shape_hint),
            style = EbbTypography.bodyL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = EbbSpacing.s3),
        )
        Row(
            modifier = Modifier.padding(top = EbbSpacing.s6).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s3),
        ) {
            DayShapeTimeCard(
                label = stringResource(R.string.onboarding_wake_label),
                symbolName = "wb_sunny",
                timeLabel = formatMinutes(state.wakeMinutes),
                onClick = { pickingWake = true },
                modifier = Modifier.weight(1f),
            )
            DayShapeTimeCard(
                label = stringResource(R.string.onboarding_bed_label),
                symbolName = "bedtime",
                timeLabel = formatMinutes(state.bedMinutes),
                onClick = { pickingWake = false },
                modifier = Modifier.weight(1f),
            )
        }
        DayShapeBar(
            wakeMinutes = state.wakeMinutes,
            bedMinutes = state.bedMinutes,
            modifier = Modifier.padding(top = EbbSpacing.s4),
        )
    }
}

@Composable
fun OnboardingPreferencesScreen(
    state: OnboardingUiState,
    onBreatherSelect: (Int) -> Unit,
    onCelebrationSelect: (CelebrationStyle) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    OnboardingScreenScaffold(
        topBar = { OnboardingTopBar(activeSteps = 3, showSkip = true, onSkip = onSkip) },
        bottomBar = {
            OnboardingBottomActions {
                OnboardingPrimaryButton(
                    text = stringResource(R.string.action_continue),
                    onClick = onContinue,
                )
            }
        },
    ) {
        Text(stringResource(R.string.onboarding_prefs_title), style = EbbTypography.onboardingTitle)
        Text(
            text = stringResource(R.string.onboarding_prefs_subtitle),
            style = EbbTypography.bodyL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = EbbSpacing.s3),
        )
        Text(
            text = stringResource(R.string.onboarding_breather_label),
            style = EbbTypography.titleM,
            modifier = Modifier.padding(top = EbbSpacing.s6),
        )
        Row(
            modifier = Modifier.padding(top = EbbSpacing.s2),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            listOf(0, 5, 10, 15).forEach { minutes ->
                val selected = state.breatherMinutes == minutes
                FilterChip(
                    selected = selected,
                    onClick = { onBreatherSelect(minutes) },
                    label = {
                        Text(if (minutes == 0) "0" else "$minutes min")
                    },
                    leadingIcon =
                        if (selected && minutes != 0) {
                            { EbbSymbol("check", fontSize = 18.sp) }
                        } else {
                            null
                        },
                )
            }
        }
        Text(
            text = stringResource(R.string.onboarding_celebrations_label),
            style = EbbTypography.titleM,
            modifier = Modifier.padding(top = EbbSpacing.s6),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(top = EbbSpacing.s2).fillMaxWidth()) {
            CelebrationStyle.entries.forEachIndexed { index, style ->
                SegmentedButton(
                    selected = state.celebrationStyle == style,
                    onClick = { onCelebrationSelect(style) },
                    shape = SegmentedButtonDefaults.itemShape(index, CelebrationStyle.entries.size),
                    icon = {
                        if (state.celebrationStyle == style) {
                            EbbSymbol("check", fontSize = 16.sp)
                        }
                    },
                    label = {
                        Text(
                            when (style) {
                                CelebrationStyle.Calm -> stringResource(R.string.onboarding_celebration_calm)
                                CelebrationStyle.Lively -> stringResource(R.string.onboarding_celebration_lively)
                                CelebrationStyle.Off -> stringResource(R.string.onboarding_celebration_off)
                            },
                        )
                    },
                )
            }
        }
        Row(
            modifier = Modifier.padding(top = EbbSpacing.s4),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EbbSymbol("tune", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.onboarding_change_anytime),
                style = EbbTypography.bodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun OnboardingTryItScreen(
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    OnboardingScreenScaffold(
        topBar = {
            OnboardingTopBar(
                activeSteps = 4,
                subtitle = stringResource(R.string.onboarding_try_it_step),
                showSkip = true,
                onSkip = onSkip,
            )
        },
        bottomBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
                horizontalArrangement = Arrangement.End,
            ) {
                OnboardingPrimaryButton(
                    text = stringResource(R.string.onboarding_try_it_next),
                    onClick = onNext,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(0.45f),
                )
            }
        },
    ) {
        Text(stringResource(R.string.onboarding_try_it_title), style = EbbTypography.headlineL)
        Text(
            text = stringResource(R.string.onboarding_try_it_hint),
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = EbbSpacing.s2),
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.onboarding_try_it_field_label)) },
            placeholder = { Text(stringResource(R.string.onboarding_try_it_placeholder)) },
            modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s6),
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
fun OnboardingNotificationsScreen(
    onAllow: () -> Unit,
    onSkip: () -> Unit,
) {
    OnboardingPermissionPrimer(
        activeSteps = 5,
        symbolName = "notifications",
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        accentColor = MaterialTheme.colorScheme.primary,
        title = stringResource(R.string.onboarding_notifications_title),
        body = stringResource(R.string.onboarding_notifications_body),
        primaryLabel = stringResource(R.string.onboarding_notifications_allow),
        onPrimary = onAllow,
        secondaryLabel = stringResource(R.string.onboarding_not_now),
        onSecondary = onSkip,
    )
}

@Composable
fun OnboardingExactAlarmsScreen(
    onOpenSettings: () -> Unit,
    onSkip: () -> Unit,
) {
    OnboardingScreenScaffold(
        topBar = { OnboardingTopBar(activeSteps = 5) },
        bottomBar = {
            OnboardingBottomActions {
                OnboardingPrimaryButton(
                    text = stringResource(R.string.onboarding_alarms_open_setting),
                    onClick = onOpenSettings,
                    leadingSymbol = "open_in_new",
                )
                OnboardingTextButton(
                    text = stringResource(R.string.onboarding_not_now),
                    onClick = onSkip,
                )
            }
        },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OnboardingBlobIllustration(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                accentColor = MaterialTheme.colorScheme.secondary,
                symbolName = "alarm_on",
            )
            Text(
                text = stringResource(R.string.onboarding_alarms_title),
                style = EbbTypography.onboardingTitle,
                modifier = Modifier.padding(top = EbbSpacing.s4),
            )
            Text(
                text = stringResource(R.string.onboarding_alarms_body),
                style = EbbTypography.bodyL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = EbbSpacing.s3),
            )
            Surface(
                modifier = Modifier.padding(top = EbbSpacing.s6).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Row(
                    modifier = Modifier.padding(EbbSpacing.s3),
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s3),
                ) {
                    EbbSymbol("touch_app", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.onboarding_alarms_tip), style = EbbTypography.bodyM)
                }
            }
        }
    }
}

@Composable
fun OnboardingCalendarScreen(
    onAllow: () -> Unit,
    onSkip: () -> Unit,
) {
    OnboardingPermissionPrimer(
        activeSteps = 6,
        symbolName = "calendar_month",
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        accentColor = MaterialTheme.colorScheme.primary,
        title = stringResource(R.string.onboarding_calendar_title),
        body = stringResource(R.string.onboarding_calendar_body),
        primaryLabel = stringResource(R.string.onboarding_calendar_allow),
        onPrimary = onAllow,
        secondaryLabel = stringResource(R.string.onboarding_skip),
        onSecondary = onSkip,
    )
}

@Composable
private fun OnboardingPermissionPrimer(
    activeSteps: Int,
    symbolName: String,
    containerColor: androidx.compose.ui.graphics.Color,
    accentColor: androidx.compose.ui.graphics.Color,
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
) {
    OnboardingScreenScaffold(
        topBar = { OnboardingTopBar(activeSteps = activeSteps) },
        bottomBar = {
            OnboardingBottomActions {
                OnboardingPrimaryButton(text = primaryLabel, onClick = onPrimary)
                OnboardingTextButton(text = secondaryLabel, onClick = onSecondary)
            }
        },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OnboardingBlobIllustration(
                containerColor = containerColor,
                accentColor = accentColor,
                symbolName = symbolName,
            )
            Text(text = title, style = EbbTypography.onboardingTitle, modifier = Modifier.padding(top = EbbSpacing.s4))
            Text(
                text = body,
                style = EbbTypography.bodyL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = EbbSpacing.s3),
            )
        }
    }
}

@Composable
fun OnboardingDoneScreen(
    state: OnboardingUiState,
    onOpenMyDay: () -> Unit,
    onSampleTasksChange: (Boolean) -> Unit,
    onComplete: () -> Unit,
) {
    OnboardingScreenScaffold(
        topBar = { OnboardingTopBar(activeSteps = 6) },
        bottomBar = {
            OnboardingBottomActions {
                OnboardingPrimaryButton(
                    text = stringResource(R.string.onboarding_open_my_day),
                    onClick = onComplete,
                    leadingSymbol = "arrow_forward",
                )
            }
        },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OnboardingBlobIllustration(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                accentColor = MaterialTheme.colorScheme.tertiary,
                symbolName = "check_circle",
                size = 180.dp,
            )
            Text(
                text = stringResource(R.string.onboarding_done_title),
                style = EbbTypography.onboardingTitle,
                modifier = Modifier.padding(top = EbbSpacing.s4),
            )
            Text(
                text = stringResource(R.string.onboarding_done_subtitle),
                style = EbbTypography.bodyL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = EbbSpacing.s3),
            )
            Column(
                modifier = Modifier.padding(top = EbbSpacing.s6).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
                    SummaryChip(
                        "schedule",
                        stringResource(
                            R.string.onboarding_done_day_chip,
                            formatMinutes(state.wakeMinutes),
                            formatMinutes(state.bedMinutes),
                        ),
                    )
                    SummaryChip("notifications", stringResource(R.string.onboarding_done_reminders_chip))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
                    SummaryChip("air", stringResource(R.string.onboarding_done_breather_chip, state.breatherMinutes))
                    SummaryChip("calendar_month", stringResource(R.string.onboarding_done_calendar_chip))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s4),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.onboarding_add_sample_tasks), style = EbbTypography.titleM)
                    Text(
                        text = stringResource(R.string.onboarding_add_sample_tasks_hint),
                        style = EbbTypography.bodyM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = state.addSampleTasks, onCheckedChange = onSampleTasksChange)
            }
        }
    }
}

@Composable
private fun SummaryChip(
    symbol: String,
    label: String,
) {
    FilterChip(
        selected = false,
        onClick = {},
        enabled = false,
        label = { Text(label, style = EbbTypography.labelM) },
        leadingIcon = { EbbSymbol(symbol, fontSize = 18.sp) },
        colors = FilterChipDefaults.filterChipColors(disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer),
    )
}

// Hilt entry screens
@Composable
fun OnboardingDayShapeRoute(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingDayShapeScreen(
        state = state,
        onWakeChange = viewModel::setWakeMinutes,
        onBedChange = viewModel::setBedMinutes,
        onContinue = onContinue,
        onSkip = onSkip,
    )
}

@Composable
fun OnboardingPreferencesRoute(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingPreferencesScreen(
        state = state,
        onBreatherSelect = viewModel::setBreatherMinutes,
        onCelebrationSelect = viewModel::setCelebrationStyle,
        onContinue = onContinue,
        onSkip = onSkip,
    )
}

@Composable
fun OnboardingDoneRoute(
    onOpenMyDay: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingDoneScreen(
        state = state,
        onOpenMyDay = onOpenMyDay,
        onSampleTasksChange = viewModel::setAddSampleTasks,
        onComplete = { viewModel.completeOnboarding(onOpenMyDay) },
    )
}
