package app.ebbplanner.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.R
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun OnboardingStepDots(
    activeCount: Int,
    total: Int = 6,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.width(120.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { index ->
            val active = index < activeCount
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
            )
        }
    }
}

@Composable
fun OnboardingTopBar(
    activeSteps: Int,
    subtitle: String? = null,
    showSkip: Boolean = false,
    onSkip: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = EbbSpacing.marginMedium, end = EbbSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OnboardingStepDots(activeCount = activeSteps)
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = EbbTypography.labelM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = EbbSpacing.s3),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (showSkip && onSkip != null) {
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.onboarding_skip))
            }
        }
    }
}

@Composable
fun OnboardingBlobIllustration(
    containerColor: Color,
    accentColor: Color,
    symbolName: String,
    size: Dp = 160.dp,
    symbolSize: Dp = 56.dp,
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val r = this.size.minDimension * 0.45f
            drawCircle(color = containerColor, radius = r, center = Offset(cx, cy))
            val scale = this.size.minDimension / 160f
            with(drawContext.canvas) {
                save()
                translate(cx - 80f * scale, cy - 80f * scale)
                scale(scale, scale)
                val wave1 =
                    Path().apply {
                        moveTo(14f, 104f)
                        cubicTo(37f, 92f, 60f, 104f, 106f, 104f)
                        cubicTo(130f, 104f, 140f, 104f, 152f, 104f)
                        lineTo(152f, 120f)
                        cubicTo(120f, 156f, 80f, 152f, 40f, 156f)
                        cubicTo(8f, 120f, 8f, 120f, 8f, 120f)
                        close()
                    }
                drawPath(wave1, accentColor.copy(alpha = 0.18f))
                val wave2 =
                    Path().apply {
                        moveTo(20f, 118f)
                        cubicTo(42f, 108f, 64f, 118f, 108f, 118f)
                        cubicTo(132f, 118f, 142f, 118f, 150f, 118f)
                        lineTo(146f, 128f)
                        cubicTo(118f, 154f, 80f, 152f, 42f, 154f)
                        cubicTo(14f, 128f, 14f, 128f, 14f, 128f)
                        close()
                    }
                drawPath(wave2, accentColor.copy(alpha = 0.22f))
                drawCircle(color = accentColor.copy(alpha = 0.25f), radius = 8f, center = Offset(122f, 40f))
                drawCircle(color = accentColor.copy(alpha = 0.2f), radius = 5f, center = Offset(36f, 52f))
                restore()
            }
        }
        EbbSymbol(
            name = symbolName,
            fontSize = symbolSize.value.sp,
            color = accentColor,
            filled = true,
        )
    }
}

@Composable
fun OnboardingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingSymbol: String? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(EbbSpacing.touchBig),
        shape = EbbShapes.full,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
            ),
    ) {
        if (leadingSymbol != null) {
            EbbSymbol(
                name = leadingSymbol,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(end = EbbSpacing.s2),
            )
        }
        Text(
            text = text,
            style = EbbTypography.titleM,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
fun OnboardingTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingSymbol: String? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(EbbSpacing.touchBig),
    ) {
        if (leadingSymbol != null) {
            EbbSymbol(
                name = leadingSymbol,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = EbbSpacing.s2),
            )
        }
        Text(
            text = text,
            style = EbbTypography.titleM,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun OnboardingNotificationPreview(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s3)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    EbbSymbol("waves", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary, filled = true)
                }
                Text(
                    stringResource(R.string.onboarding_notifications_preview_meta),
                    style = EbbTypography.labelM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                stringResource(R.string.onboarding_notifications_preview_title),
                style = EbbTypography.titleM,
                modifier = Modifier.padding(top = EbbSpacing.s2),
            )
            Row(
                modifier = Modifier.padding(top = EbbSpacing.s2),
                horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s4),
            ) {
                Text(
                    stringResource(R.string.onboarding_notifications_preview_action_focus),
                    style = EbbTypography.labelL,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(R.string.onboarding_notifications_preview_action_snooze),
                    style = EbbTypography.labelL,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(R.string.onboarding_notifications_preview_action_done),
                    style = EbbTypography.labelL,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun OnboardingBottomActions(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = EbbSpacing.marginMedium,
                    end = EbbSpacing.marginMedium,
                    top = EbbSpacing.s4,
                    bottom = EbbSpacing.s6,
                ),
        verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        content()
    }
}

@Composable
fun OnboardingPromiseRow(
    symbolName: String,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s4),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            EbbSymbol(
                name = symbolName,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Column {
            Text(
                text = title,
                style = EbbTypography.titleM,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = EbbTypography.bodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun DayShapeTimeCard(
    label: String,
    symbolName: String,
    timeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline,
            ),
    ) {
        Column(modifier = Modifier.padding(EbbSpacing.s4)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EbbSymbol(
                    name = symbolName,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = label,
                    style = EbbTypography.labelL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = timeLabel,
                style = EbbTypography.display,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = EbbSpacing.s2),
            )
        }
    }
}

@Composable
@Suppress("LongMethod")
fun DayShapeBar(
    wakeMinutes: Int,
    bedMinutes: Int,
    modifier: Modifier = Modifier,
) {
    val wrapsMidnight = bedMinutes <= wakeMinutes
    val daySpan = daySpanMinutes(wakeMinutes, bedMinutes)
    val startFrac = wakeMinutes / (24 * 60f)
    val widthFrac = daySpan / (24 * 60f)
    val bedFrac = bedMinutes / (24 * 60f)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(EbbSpacing.s4),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("00:00", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("12:00", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("24:00", style = EbbTypography.labelL, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
        ) {
            if (!wrapsMidnight) {
                val barWidth = maxWidth * widthFrac
                val offsetX = maxWidth * startFrac
                Box(
                    modifier =
                        Modifier
                            .offset(x = offsetX)
                            .width(barWidth)
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                )
            } else {
                val eveningWidth = maxWidth * (1f - startFrac)
                Box(
                    modifier =
                        Modifier
                            .offset(x = maxWidth * startFrac)
                            .width(eveningWidth)
                            .height(28.dp)
                            .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                )
                val morningWidth = maxWidth * bedFrac
                Box(
                    modifier =
                        Modifier
                            .width(morningWidth)
                            .height(28.dp)
                            .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                )
            }
        }
        if (wrapsMidnight) {
            Row(
                modifier = Modifier.padding(top = EbbSpacing.s2),
                horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EbbSymbol(
                    name = "nights_stay",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    filled = true,
                )
                Text(
                    text =
                        stringResource(
                            R.string.onboarding_day_span_after_midnight,
                            daySpan / 60,
                        ),
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Text(
                text =
                    stringResource(
                        R.string.onboarding_day_span_summary,
                        formatMinutes(wakeMinutes),
                        formatMinutes(bedMinutes),
                        daySpan / 60,
                    ),
                style = EbbTypography.bodyM,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = EbbSpacing.s2),
            )
        }
    }
}

fun daySpanMinutes(
    wake: Int,
    bed: Int,
): Int = if (bed > wake) bed - wake else (24 * 60 - wake) + bed

fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "%02d:%02d".format(hours, minutes)
}
