@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@file:Suppress("LongMethod", "LongParameterList", "MaxLineLength", "UnusedParameter")

package app.ebbplanner.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.R
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
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
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
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
                color = MaterialTheme.colorScheme.onSurface,
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OnboardingBlobIllustration(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    symbolName = "handshake",
                    size = 120.dp,
                )
            }
            Text(
                text = stringResource(R.string.onboarding_promise_title),
                style = onboardingTitleStyle(),
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
        Text(stringResource(R.string.onboarding_day_shape_title), style = onboardingTitleStyle())
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
    onWeekStartSelect: (Int) -> Unit = {},
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    var weekPickerOpen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
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
        Text(stringResource(R.string.onboarding_prefs_title), style = onboardingTitleStyle())
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
                    modifier = Modifier.height(EbbSpacing.touchBig),
                    shape = RoundedCornerShape(12.dp),
                    label = {
                        Text(if (minutes == 0) "0" else "$minutes min")
                    },
                    leadingIcon =
                        if (selected && minutes != 0) {
                            { EbbSymbol("check", fontSize = 18.sp) }
                        } else {
                            null
                        },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
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
                    colors =
                        SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    icon = {
                        if (state.celebrationStyle == style) {
                            EbbSymbol("check", fontSize = 16.sp, filled = true)
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
        Column(modifier = Modifier.padding(top = EbbSpacing.s6)) {
            Text(
                text = stringResource(R.string.onboarding_week_starts_label),
                style = EbbTypography.titleM,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(EbbSpacing.touchBig)
                        .padding(top = EbbSpacing.s2)
                        .clickable { weekPickerOpen = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        app.ebbplanner.ui.components.weekStartDayLabel(state.weekStartDay),
                        style = EbbTypography.titleM,
                    )
                    Text(
                        stringResource(R.string.onboarding_week_starts_hint),
                        style = EbbTypography.bodyM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                EbbSymbol("chevron_right", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        app.ebbplanner.ui.components.WeekStartDayPickerSheet(
            visible = weekPickerOpen,
            selected = state.weekStartDay,
            onSelect = onWeekStartSelect,
            onDismiss = { weekPickerOpen = false },
        )
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

enum class TryItStep {
    Step1Empty,
    Step1Typed,
    Step2Drag,
    Success,
}

enum class NotificationPrimerState {
    Default,
    Granted,
    Denied,
}

@Composable
@Suppress("CyclomaticComplexMethod")
fun OnboardingTryItScreen(
    step: TryItStep = TryItStep.Step1Empty,
    inputValue: String = "",
    onInputChange: (String) -> Unit = {},
    celebrationBody: String = "",
    dragTaskTitle: String = "",
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    val stepLabel =
        when (step) {
            TryItStep.Step2Drag, TryItStep.Success -> stringResource(R.string.onboarding_try_it_step2)
            else -> stringResource(R.string.onboarding_try_it_step)
        }
    val title =
        when (step) {
            TryItStep.Step2Drag -> stringResource(R.string.onboarding_try_it_drag_title)
            TryItStep.Success -> stringResource(R.string.onboarding_try_it_success_title)
            else -> stringResource(R.string.onboarding_try_it_title)
        }
    val hint =
        when (step) {
            TryItStep.Step2Drag -> stringResource(R.string.onboarding_try_it_drag_hint)
            TryItStep.Success -> stringResource(R.string.onboarding_try_it_success_hint)
            else -> stringResource(R.string.onboarding_try_it_hint)
        }
    OnboardingScreenScaffold(
        topBar = {
            OnboardingTopBar(
                activeSteps = 4,
                subtitle = stepLabel,
                showSkip = true,
                onSkip = onSkip,
            )
        },
        bottomBar = {
            when (step) {
                TryItStep.Step1Empty, TryItStep.Step1Typed ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = EbbSpacing.marginMedium,
                                    end = EbbSpacing.marginMedium,
                                    top = EbbSpacing.s2,
                                    bottom = EbbSpacing.s4,
                                ),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        OnboardingPrimaryButton(
                            text = stringResource(R.string.onboarding_try_it_next),
                            onClick = onNext,
                            enabled = step != TryItStep.Step1Empty || inputValue.isNotBlank(),
                            block = false,
                        )
                    }
                else ->
                    OnboardingBottomActions {
                        OnboardingPrimaryButton(
                            text =
                                if (step == TryItStep.Success) {
                                    stringResource(R.string.action_continue)
                                } else {
                                    stringResource(R.string.onboarding_try_it_next)
                                },
                            onClick = onNext,
                        )
                    }
            }
        },
    ) {
        Text(title, style = EbbTypography.headlineL, color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = hint,
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = EbbSpacing.s2),
        )
        when (step) {
            TryItStep.Step1Empty, TryItStep.Step1Typed ->
                OnboardingUnderlineField(
                    value = inputValue,
                    placeholder = stringResource(R.string.onboarding_try_it_placeholder),
                    focused = true,
                    onValueChange = onInputChange,
                    modifier = Modifier.padding(top = EbbSpacing.s6),
                )
            TryItStep.Step2Drag, TryItStep.Success ->
                OnboardingTryItTimelineSection(
                    step = step,
                    celebrationBody = celebrationBody,
                    dragTaskTitle = dragTaskTitle,
                    modifier = Modifier.padding(top = EbbSpacing.s4),
                )
        }
    }
}

@Composable
fun OnboardingNotificationsScreen(
    state: NotificationPrimerState = NotificationPrimerState.Default,
    onAllow: () -> Unit,
    onSkip: () -> Unit,
) {
    val title =
        when (state) {
            NotificationPrimerState.Granted ->
                stringResource(R.string.onboarding_notifications_title_granted)
            NotificationPrimerState.Denied ->
                stringResource(R.string.onboarding_notifications_title_denied)
            else -> stringResource(R.string.onboarding_notifications_title)
        }
    val body =
        when (state) {
            NotificationPrimerState.Granted ->
                stringResource(R.string.onboarding_notifications_body_granted)
            NotificationPrimerState.Denied ->
                stringResource(R.string.onboarding_notifications_body_denied)
            else -> stringResource(R.string.onboarding_notifications_body)
        }
    val primary =
        when (state) {
            NotificationPrimerState.Denied ->
                stringResource(R.string.onboarding_notifications_open_settings)
            else -> stringResource(R.string.onboarding_notifications_allow)
        }
    OnboardingScreenScaffold(
        topBar = { OnboardingTopBar(activeSteps = 5) },
        bottomBar = {
            OnboardingBottomActions {
                OnboardingPrimaryButton(text = primary, onClick = onAllow)
                OnboardingTextButton(text = stringResource(R.string.onboarding_not_now), onClick = onSkip)
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (state == NotificationPrimerState.Default) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OnboardingBlobIllustration(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        accentColor = MaterialTheme.colorScheme.primary,
                        symbolName = "notifications",
                        size = 140.dp,
                    )
                    OnboardingNotificationPreview(modifier = Modifier.padding(top = EbbSpacing.s4).fillMaxWidth(0.92f))
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    OnboardingBlobIllustration(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        accentColor = MaterialTheme.colorScheme.primary,
                        symbolName = if (state == NotificationPrimerState.Granted) "check_circle" else "notifications",
                        size = 140.dp,
                    )
                }
            }
            Text(text = title, style = onboardingTitleStyle(), modifier = Modifier.padding(top = EbbSpacing.s6))
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
fun OnboardingExactAlarmsScreen(
    onOpenSettings: () -> Unit,
    onSkip: () -> Unit,
    showExactAlarmStep: Boolean = true,
) {
    if (!showExactAlarmStep) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            onSkip()
        }
        return
    }
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OnboardingBlobIllustration(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    symbolName = "alarm_on",
                )
            }
            Text(
                text = stringResource(R.string.onboarding_alarms_title),
                style = onboardingTitleStyle(),
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
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s3),
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s3),
                    verticalAlignment = Alignment.Top,
                ) {
                    EbbSymbol("touch_app", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, filled = false)
                    Text(
                        text =
                            buildAnnotatedString {
                                append("On the next screen, switch on ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Allow setting alarms and reminders")
                                }
                                append(", then come back.")
                            },
                        style = EbbTypography.bodyM,
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingCalendarChooserScreen(onContinue: () -> Unit) {
    OnboardingScreenScaffold(
        topBar = { OnboardingTopBar(activeSteps = 6) },
        bottomBar = {
            OnboardingBottomActions {
                OnboardingPrimaryButton(
                    text = stringResource(R.string.action_continue),
                    onClick = onContinue,
                )
            }
        },
    ) {
        Text(
            stringResource(R.string.onboarding_calendar_chooser_title),
            style = onboardingTitleStyle(),
        )
        Text(
            stringResource(R.string.onboarding_calendar_chooser_subtitle),
            style = EbbTypography.bodyL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = EbbSpacing.s3),
        )
        CalendarChooserList(modifier = Modifier.padding(top = EbbSpacing.s6))
    }
}

@Composable
private fun CalendarChooserList(modifier: Modifier = Modifier) {
    val rows =
        listOf(
            Triple("Personal", "Google account", "blue"),
            Triple("University", "Shared", "sky"),
            Triple("Work", "Exchange", "green"),
            Triple("Holidays in Morocco", "Subscribed", "amber"),
        )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(vertical = EbbSpacing.s1)) {
            rows.forEachIndexed { index, (name, sub, color) ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = EbbSpacing.touchBig)
                            .padding(start = EbbSpacing.s1, end = EbbSpacing.s4),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s3),
                ) {
                    OnboardingDesignCheckbox(checked = index < 2)
                    Box(
                        modifier =
                            Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(EbbTaskColors.strong(color)),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, style = EbbTypography.titleM, color = MaterialTheme.colorScheme.onSurface)
                        Text(sub, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingDesignCheckbox(checked: Boolean) {
    Box(
        modifier =
            Modifier
                .padding(horizontal = 11.dp, vertical = 15.dp)
                .size(18.dp)
                .then(
                    if (checked) {
                        Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                    } else {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp))
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            EbbSymbol("check", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary, filled = true)
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
    statusBanner: Boolean = false,
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
            Text(text = title, style = onboardingTitleStyle(), modifier = Modifier.padding(top = EbbSpacing.s4))
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OnboardingBlobIllustration(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    symbolName = "check_circle",
                    size = 180.dp,
                )
            }
            Text(
                text = stringResource(R.string.onboarding_done_title),
                style = onboardingTitleStyle(),
                modifier = Modifier.padding(top = EbbSpacing.s4),
            )
            Text(
                text = stringResource(R.string.onboarding_done_subtitle),
                style = EbbTypography.bodyL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = EbbSpacing.s3),
            )
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.padding(top = EbbSpacing.s6).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
                verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            ) {
                SummaryChip(
                    "schedule",
                    stringResource(
                        R.string.onboarding_done_day_chip,
                        formatMinutes(state.wakeMinutes),
                        formatMinutes(state.bedMinutes),
                    ),
                )
                SummaryChip(
                    "notifications",
                    if (state.remindersEnabled) {
                        stringResource(R.string.onboarding_done_reminders_on)
                    } else {
                        stringResource(R.string.onboarding_done_reminders_off)
                    },
                )
                SummaryChip(
                    "air",
                    stringResource(R.string.onboarding_done_breather_chip, state.breatherMinutes),
                )
                SummaryChip(
                    "calendar_month",
                    pluralStringResource(
                        R.plurals.onboarding_calendar_count,
                        state.linkedCalendarCount,
                        state.linkedCalendarCount,
                    ),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s4),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.onboarding_add_sample_tasks),
                        style = EbbTypography.bodyL,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
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
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier =
                Modifier
                    .height(32.dp)
                    .padding(horizontal = EbbSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            EbbSymbol(symbol, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, filled = false)
            Text(label, style = EbbTypography.labelM, color = MaterialTheme.colorScheme.onSurface)
        }
    }
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
        onWeekStartSelect = viewModel::setWeekStartDay,
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
