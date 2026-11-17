package app.ebbplanner.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ebbplanner.R
import app.ebbplanner.data.local.entity.TaskEntity
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography

const val TODAY_DATE_HEADER_TAG = "today_date_header"
const val TODAY_TIMELINE_TAG = "today_timeline"

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCapture by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showCapture = true }) {
                androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = null)
            }
        },
    ) { padding ->
        TodayScreenContent(
            state = state,
            modifier = Modifier.padding(padding),
        )
    }

    QuickCaptureSheet(
        visible = showCapture,
        onDismiss = { showCapture = false },
        onSave = viewModel::captureTask,
    )
}

@Composable
fun TodayScreenContent(
    state: TodayUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = EbbSpacing.marginCompact),
    ) {
        Text(
            text = state.dateLabel,
            style = EbbTypography.headlineM,
            modifier =
                Modifier
                    .padding(vertical = EbbSpacing.s3)
                    .testTag(TODAY_DATE_HEADER_TAG),
        )
        DaySummaryCard(minutesPlanned = state.minutesPlanned)
        Spacer(modifier = Modifier.height(EbbSpacing.s4))
        AnytimeTray(tasks = state.anytimeTasks)
        Spacer(modifier = Modifier.height(EbbSpacing.s4))
        TimelineSection(
            wakeMinutes = state.wakeMinutes,
            bedMinutes = state.bedMinutes,
            tasks = state.scheduledTasks,
        )
    }
}

@Composable
private fun DaySummaryCard(minutesPlanned: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = EbbShapes.md,
    ) {
        Column(modifier = Modifier.padding(EbbSpacing.cardPadComfortable)) {
            Text(
                text = stringResource(R.string.today_summary_title),
                style = EbbTypography.titleM,
            )
            Text(
                text = stringResource(R.string.today_summary_minutes, minutesPlanned),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AnytimeTray(tasks: List<TaskEntity>) {
    Column {
        Text(text = stringResource(R.string.today_anytime_title), style = EbbTypography.titleM)
        Spacer(modifier = Modifier.height(EbbSpacing.s2))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
        ) {
            if (tasks.isEmpty()) {
                Text(
                    text = stringResource(R.string.today_anytime_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                tasks.forEach { task ->
                    AssistChip(
                        onClick = {},
                        label = { Text(task.title) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineSection(
    wakeMinutes: Int,
    bedMinutes: Int,
    tasks: List<TaskEntity>,
) {
    val hours = hourRange(wakeMinutes, bedMinutes)
    val hourHeight = EbbSpacing.hourComfortable
    val totalHeight = hourHeight * hours.size

    Column(modifier = Modifier.testTag(TODAY_TIMELINE_TAG)) {
        Text(text = stringResource(R.string.today_timeline_title), style = EbbTypography.titleM)
        Spacer(modifier = Modifier.height(EbbSpacing.s3))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(totalHeight),
        ) {
            Column {
                hours.forEach { hour ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(hourHeight),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "%02d".format(hour),
                            modifier = Modifier.width(EbbSpacing.hourRail),
                            style = EbbTypography.labelM,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                        )
                    }
                }
            }
            tasks.forEach { task ->
                val topOffset = taskTopOffset(task, wakeMinutes, hourHeight)
                val blockHeight =
                    maxOf(
                        EbbSpacing.blockMin,
                        hourHeight * (task.durationMin / 60f),
                    )
                TimelineBlock(
                    task = task,
                    modifier =
                        Modifier
                            .padding(start = EbbSpacing.hourRail + EbbSpacing.blockInset)
                            .fillMaxWidth()
                            .offset(y = topOffset)
                            .height(blockHeight),
                )
            }
        }
    }
}

@Composable
private fun TimelineBlock(
    task: TaskEntity,
    modifier: Modifier = Modifier,
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val background = EbbTaskColors.tint(task.colorKey, dark)
    Card(
        modifier = modifier,
        shape = EbbShapes.sm,
        colors = CardDefaults.cardColors(containerColor = background),
    ) {
        Column(modifier = Modifier.padding(EbbSpacing.s2)) {
            Text(text = task.title, style = EbbTypography.titleM, maxLines = 2)
            Text(
                text = "${task.startLocalTime} · ${task.durationMin} min",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private fun hourRange(
    wakeMinutes: Int,
    bedMinutes: Int,
): List<Int> {
    val startHour = wakeMinutes / 60
    val endHour =
        if (bedMinutes <= wakeMinutes) {
            24
        } else {
            (bedMinutes / 60).coerceAtMost(24)
        }
    return (startHour until endHour).toList().ifEmpty { (7 until 23).toList() }
}

private fun taskTopOffset(
    task: TaskEntity,
    wakeMinutes: Int,
    hourHeight: androidx.compose.ui.unit.Dp,
): androidx.compose.ui.unit.Dp {
    val start = task.startLocalTime ?: return 0.dp
    val parts = start.split(":")
    if (parts.size != 2) return 0.dp
    val hour = parts[0].toIntOrNull() ?: return 0.dp
    val minute = parts[1].toIntOrNull() ?: 0
    val taskMinutes = hour * 60 + minute
    val deltaHours = (taskMinutes - wakeMinutes).coerceAtLeast(0) / 60f
    val minuteFraction = ((taskMinutes - wakeMinutes) % 60) / 60f
    return hourHeight * (deltaHours + minuteFraction)
}
