@file:Suppress("LongMethod", "MagicNumber")

package app.ebbplanner.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.ebbplanner.R
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography

private val timelineTrackHeight = 180.dp

@Composable
fun OnboardingUnderlineField(
    value: String,
    placeholder: String,
    focused: Boolean,
    modifier: Modifier = Modifier,
) {
    val lineColor =
        if (focused) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }
    val lineThickness = if (focused) 2.dp else 1.dp
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, EbbShapes.xs)
                .drawBehind {
                    val y = size.height - lineThickness.toPx()
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = lineThickness.toPx(),
                    )
                }
                .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
    ) {
        Text(
            text = stringResource(R.string.onboarding_try_it_field_label),
            style = EbbTypography.labelM,
            color =
                if (focused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
        Box(modifier = Modifier.padding(top = EbbSpacing.s1).height(24.dp)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = EbbTypography.bodyL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(text = value, style = EbbTypography.bodyL, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun OnboardingTryItTimelineSection(
    step: TryItStep,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = EbbShapes.lg,
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                TryItMiniDayTimeline(step = step, dark = dark)
            }
            if (step == TryItStep.Success) {
                EbbSymbol(
                    name = "auto_awesome",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    filled = true,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 78.dp),
                )
            }
        }
        if (step == TryItStep.Success) {
            CelebrationCard(modifier = Modifier.padding(top = EbbSpacing.s6))
            Column(
                modifier = Modifier.padding(top = EbbSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            ) {
                InboxTaskRow(title = "Call bank about card")
                InboxTaskRow(title = "Water plants")
            }
            Text(
                text = stringResource(R.string.onboarding_try_it_inbox_caption),
                style = EbbTypography.labelM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = EbbSpacing.s2),
            )
        }
        if (step == TryItStep.Step2Drag) {
            CoachMark(modifier = Modifier.padding(top = EbbSpacing.s4))
            Column(
                modifier = Modifier.padding(top = EbbSpacing.s2),
                verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            ) {
                InboxTaskRow(title = "Buy stamps", dimmed = true)
                InboxTaskRow(title = "Call bank about card")
                InboxTaskRow(title = "Water plants")
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = EbbSpacing.s3),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.onboarding_try_it_cant_drag),
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = {}) {
                    EbbSymbol("event_available", fontSize = 18.sp, modifier = Modifier.padding(end = EbbSpacing.s1))
                    Text(stringResource(R.string.onboarding_try_it_schedule), style = EbbTypography.labelL)
                }
            }
        }
    }
}

@Composable
private fun TryItMiniDayTimeline(
    step: TryItStep,
    dark: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(timelineTrackHeight)
                .padding(top = EbbSpacing.s3, bottom = EbbSpacing.s2),
    ) {
        PastZone(modifier = Modifier.align(Alignment.TopStart))
        HourRule("14:00", y = 0.dp)
        HourRule("15:00", y = 72.dp)
        HourRule("16:00", y = 144.dp)
        NowLine(y = 24.dp)
        CalendarEventBlock(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 56.dp, end = 12.dp)
                    .offset(y = 91.dp)
                    .fillMaxWidth()
                    .height(52.dp),
        )
        when (step) {
            TryItStep.Step2Drag ->
                GhostTaskBlock(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 56.dp, end = 12.dp)
                            .offset(y = 37.dp)
                            .fillMaxWidth()
                            .height(34.dp),
                )
            TryItStep.Success ->
                PlacedTaskBlock(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 56.dp, end = 12.dp)
                            .offset(y = 37.dp)
                            .fillMaxWidth()
                            .height(26.dp),
                    dark = dark,
                )
            else -> Unit
        }
        if (step == TryItStep.Step2Drag) {
            DraggingTaskOverlay(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 28.dp, y = 56.dp)
                        .zIndex(2f),
            )
        }
    }
}

@Composable
private fun PastZone(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 56.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
    )
}

@Composable
private fun HourRule(
    label: String,
    y: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = y),
    ) {
        Text(
            text = label,
            style = EbbTypography.labelM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .width(56.dp)
                    .padding(end = EbbSpacing.s2)
                    .align(Alignment.TopStart),
            textAlign = TextAlign.End,
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 56.dp, end = 12.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .offset(y = 8.dp)
                    .background(MaterialTheme.colorScheme.outline),
        )
    }
}

@Composable
private fun NowLine(y: androidx.compose.ui.unit.Dp) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = y),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 56.dp, end = 12.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(primary),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 52.dp)
                    .offset(y = (-3).dp)
                    .size(8.dp)
                    .background(primary, CircleShape),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 4.dp)
                    .offset(y = (-10).dp)
                    .height(20.dp)
                    .background(primary, EbbShapes.full)
                    .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("14:20", style = EbbTypography.labelM, color = onPrimary)
        }
    }
}

@Composable
private fun PlacedTaskBlock(
    modifier: Modifier,
    dark: Boolean,
) {
    val lemonFill = EbbTaskColors.tint("lemon", dark)
    val lemonAccent = EbbTaskColors.strong("lemon")
    Row(
        modifier =
            modifier
                .clip(EbbShapes.sm)
                .background(lemonFill)
                .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .height(26.dp)
                    .background(lemonAccent),
        )
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            EbbSymbol("local_post_office", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Buy stamps",
                style = EbbTypography.titleM,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text(
                "14:30 · 20 min",
                style = EbbTypography.labelM,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TaskCompletionRing()
        }
    }
}

@Composable
private fun GhostTaskBlock(modifier: Modifier) {
    val dark = isSystemInDarkTheme()
    Row(
        modifier =
            modifier
                .clip(EbbShapes.sm)
                .background(EbbTaskColors.tint("slate", dark))
                .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        EbbSymbol("radio_button_unchecked", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "Buy stamps",
            style = EbbTypography.titleM,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            "14:30 · 30 min",
            style = EbbTypography.labelM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DraggingTaskOverlay(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    Surface(
        modifier = modifier.width(250.dp),
        shape = EbbShapes.md,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.height(44.dp).padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .background(EbbTaskColors.strong("slate")),
            )
            TaskCompletionRing(modifier = Modifier.padding(start = 8.dp))
            Text(
                "Buy stamps",
                style = EbbTypography.titleM,
                modifier = Modifier.weight(1f).padding(horizontal = EbbSpacing.s2),
            )
            EbbSymbol(
                "drag_indicator",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = EbbSpacing.s2),
            )
        }
    }
}

@Composable
private fun CalendarEventBlock(modifier: Modifier) {
    val accent = EbbTaskColors.strong("blue")
    val hatch =
        Brush.linearGradient(
            colors =
                listOf(
                    Color.Transparent,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                ),
        )
    Row(
        modifier =
            modifier
                .clip(EbbShapes.md)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, accent, EbbShapes.md)
                .drawBehind {
                    val step = 8f
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            brush = hatch,
                            start = Offset(x, 0f),
                            end = Offset(x + step, size.height),
                            strokeWidth = 2f,
                        )
                        x += step
                    }
                }
                .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        EbbSymbol("event", fontSize = 18.sp, color = accent)
        Column(modifier = Modifier.weight(1f)) {
            Text("Team call", style = EbbTypography.titleM, color = MaterialTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s3)) {
                Text("15:15–16:00", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("45 min", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    EbbSymbol(
                        "calendar_month",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("Work", style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CelebrationCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = EbbShapes.lg,
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(EbbSpacing.s4),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s3),
            verticalAlignment = Alignment.Top,
        ) {
            EbbSymbol(
                name = "celebration",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                filled = true,
            )
            Column {
                Text(
                    stringResource(R.string.onboarding_try_it_success_celebration_title),
                    style = EbbTypography.titleM,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    stringResource(R.string.onboarding_try_it_success_celebration_body),
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun InboxTaskRow(
    title: String,
    dimmed: Boolean = false,
) {
    val alpha = if (dimmed) 0.35f else 1f
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = EbbShapes.md,
        color = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.height(52.dp).padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .background(EbbTaskColors.strong("slate").copy(alpha = alpha)),
            )
            TaskCompletionRing(modifier = Modifier.padding(start = 8.dp))
            Text(
                title,
                style = EbbTypography.titleM,
                modifier = Modifier.weight(1f).padding(horizontal = EbbSpacing.s2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            )
            EbbSymbol(
                "drag_indicator",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                modifier = Modifier.padding(end = EbbSpacing.s2),
            )
        }
    }
}

@Composable
private fun TaskCompletionRing(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(24.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
    )
}

@Composable
private fun CoachMark(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = EbbShapes.md,
        color = MaterialTheme.colorScheme.inverseSurface,
    ) {
        Column(modifier = Modifier.padding(EbbSpacing.s4)) {
            Text(
                stringResource(R.string.onboarding_try_it_coach),
                style = EbbTypography.bodyM,
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
            Text(
                text = stringResource(R.string.onboarding_try_it_got_it),
                style = EbbTypography.labelL,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.padding(top = EbbSpacing.s2),
            )
        }
    }
}
