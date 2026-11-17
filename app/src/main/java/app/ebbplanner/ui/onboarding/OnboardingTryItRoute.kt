package app.ebbplanner.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.ebbplanner.R
import app.ebbplanner.domain.capture.QuickCaptureParser
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun OnboardingTryItRoute(
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(TryItStep.Step1Empty) }
    var input by rememberSaveable { mutableStateOf("") }
    var celebrationBody by rememberSaveable { mutableStateOf("") }
    var dragTaskTitle by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val anytimeLabel = stringResource(R.string.onboarding_try_it_anytime_label)

    val resolvedStep =
        when (step) {
            TryItStep.Step1Empty ->
                if (input.isNotBlank()) {
                    TryItStep.Step1Typed
                } else {
                    TryItStep.Step1Empty
                }
            else -> step
        }

    OnboardingTryItScreen(
        step = resolvedStep,
        inputValue = input,
        onInputChange = { input = it },
        celebrationBody = celebrationBody,
        dragTaskTitle = dragTaskTitle,
        onSkip = onSkip,
        onNext = {
            when (step) {
                TryItStep.Step2Drag -> step = TryItStep.Success
                TryItStep.Success -> onNext()
                else -> {
                    if (input.isBlank()) {
                        onSkip()
                        return@OnboardingTryItScreen
                    }
                    val today = LocalDate.now()
                    val parsed = QuickCaptureParser.parse(input, today)
                    dragTaskTitle = parsed.title.ifBlank { input.trim() }
                    val target = parsed.targetDate ?: today
                    val timed = parsed.startTime != null
                    val timeLabel = parsed.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")).orEmpty()
                    celebrationBody =
                        when {
                            target.isAfter(today) && timed ->
                                context.getString(
                                    R.string.onboarding_try_it_success_celebration_body_tomorrow_timed,
                                    dragTaskTitle,
                                    timeLabel,
                                )
                            target.isAfter(today) ->
                                context.getString(
                                    R.string.onboarding_try_it_success_celebration_body_tomorrow_anytime,
                                    dragTaskTitle,
                                )
                            timed ->
                                context.getString(
                                    R.string.onboarding_try_it_success_celebration_body_today_timed,
                                    dragTaskTitle,
                                    timeLabel,
                                )
                            else ->
                                context.getString(
                                    R.string.onboarding_try_it_success_celebration_body_today_anytime,
                                    dragTaskTitle,
                                )
                        }
                    step = TryItStep.Step2Drag
                }
            }
        },
    )
}
