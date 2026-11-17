@file:Suppress("LongMethod")

package app.ebbplanner.ui.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.R
import app.ebbplanner.ui.components.EbbBottomNavigation
import app.ebbplanner.ui.navigation.MainTab
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun TodayTopBar(
    dateLabel: String,
    multiselect: Boolean,
    multiselectCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = EbbSpacing.s1, vertical = EbbSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        if (multiselect) {
            Text(
                text = stringResource(R.string.today_multiselect_title, multiselectCount),
                style = EbbTypography.titleM,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = {}) {
                Text(stringResource(R.string.action_done))
            }
        } else {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                    EbbSymbol("chevron_left", fontSize = 24.sp)
                }
                Text(
                    text = dateLabel,
                    style = EbbTypography.headlineM,
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag(TODAY_DATE_HEADER_TAG),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                    EbbSymbol("chevron_right", fontSize = 24.sp)
                }
            }
            IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                EbbSymbol("search", fontSize = 22.sp)
            }
            IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                EbbSymbol("settings", fontSize = 22.sp)
            }
            IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                EbbSymbol("more_vert", fontSize = 22.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayDayHead(
    state: TodayPresentationState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = EbbSpacing.s3, vertical = EbbSpacing.s2),
        shape = EbbShapes.lg,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EbbSpacing.s3, vertical = EbbSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s3),
        ) {
            DayProgressRing(percent = state.progressPercent)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = state.timeLeftLabel, style = EbbTypography.titleL)
                if (!state.titleOnlyBlocks) {
                    Text(
                        text = state.dayMeta,
                        style = EbbTypography.bodyM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                onClick = {},
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = EbbSpacing.s3, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
                ) {
                    EbbSymbol(
                        "more_time",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        stringResource(R.string.today_shift),
                        style = EbbTypography.labelL,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayProgressRing(percent: Int) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val arc = MaterialTheme.colorScheme.primary
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
        Canvas(modifier = Modifier.size(44.dp)) {
            val stroke = 4.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = arc,
                startAngle = -90f,
                sweepAngle = 360f * (percent / 100f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Text("$percent%", style = EbbTypography.labelM)
    }
}

@Composable
fun TodayAnytimeTray(
    chips: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = EbbSpacing.s3, vertical = EbbSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        Text(
            text = stringResource(R.string.today_anytime_title),
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            chips.forEach { title ->
                Surface(
                    shape = EbbShapes.full,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = title,
                        style = EbbTypography.labelL,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
            Surface(
                shape = EbbShapes.full,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    EbbSymbol("add", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        stringResource(R.string.today_anytime_add),
                        style = EbbTypography.labelL,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
fun TodayBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(message, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onTertiaryContainer)
            EbbSymbol("chevron_right", fontSize = 20.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

@Composable
fun TodayOverlapHint(
    hint: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(horizontal = EbbSpacing.s3),
        shape = EbbShapes.full,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s1),
        ) {
            EbbSymbol("warning", fontSize = 16.sp)
            Text(hint, style = EbbTypography.labelM)
        }
    }
}

@Composable
fun TodayLoadingSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = EbbSpacing.s3),
        verticalArrangement = Arrangement.spacedBy(EbbSpacing.s3),
    ) {
        repeat(4) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, EbbShapes.md),
            )
        }
    }
}

@Composable
fun TodayEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(EbbSpacing.s6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(EbbSpacing.s3),
    ) {
        Text(stringResource(R.string.today_empty_title), style = EbbTypography.titleL)
        Text(
            stringResource(R.string.today_empty_body),
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2)) {
            TextButton(onClick = {}) { Text(stringResource(R.string.today_plan_day)) }
            TextButton(onClick = {}) { Text(stringResource(R.string.today_add_task)) }
        }
    }
}

@Composable
fun TodayBottomNavPreview(
    selectedToday: Boolean = true,
    modifier: Modifier = Modifier,
) {
    EbbBottomNavigation(
        selected = if (selectedToday) MainTab.Today else MainTab.Inbox,
        modifier = modifier,
    )
}
