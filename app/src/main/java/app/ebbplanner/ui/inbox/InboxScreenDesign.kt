@file:Suppress("LongMethod", "LongParameterList", "MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.inbox

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.R
import app.ebbplanner.ui.components.EbbBottomNavigation
import app.ebbplanner.ui.components.EbbCancelButton
import app.ebbplanner.ui.navigation.MainTab
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTaskColors
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.today.QUICK_CAPTURE_FAB_TAG
import app.ebbplanner.ui.today.QUICK_CAPTURE_SNACKBAR_HOST_TAG

const val INBOX_TASK_TAG_PREFIX = "inbox_task_"
const val INBOX_MULTISELECT_DONE_TAG = "inbox_multiselect_done"
const val INBOX_MULTISELECT_SELECT_ALL_TAG = "inbox_multiselect_select_all"
const val INBOX_MULTISELECT_TODAY_TAG = "inbox_multiselect_today"
const val INBOX_MULTISELECT_TOMORROW_TAG = "inbox_multiselect_tomorrow"
const val INBOX_MULTISELECT_ESTIMATE_TAG = "inbox_multiselect_estimate"
const val INBOX_MULTISELECT_ENERGY_TAG = "inbox_multiselect_energy"
const val INBOX_MULTISELECT_LET_GO_TAG = "inbox_multiselect_let_go"

fun inboxTaskTestTag(taskId: String): String = INBOX_TASK_TAG_PREFIX + taskId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InboxScreenDesign(
    state: InboxPresentationState,
    modifier: Modifier = Modifier,
    showBottomNav: Boolean = false,
    onFabClick: () -> Unit = {},
    onTaskClick: (String) -> Unit = {},
    onTaskLongPress: (String) -> Unit = {},
    onSearchToggle: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onFilterSelected: (Int) -> Unit = {},
    onMultiselectDone: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onMoveToToday: () -> Unit = {},
    onMoveToTomorrow: () -> Unit = {},
    onLetGo: () -> Unit = {},
    onSetDuration: (Int) -> Unit = {},
    onSetEnergy: (Int) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
) {
    val multiselect = state.visualMode == InboxVisualMode.Multiselect
    var showEstimateDialog by remember { mutableStateOf(false) }
    var showEnergyDialog by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            snackbarHostState?.let { host ->
                SnackbarHost(
                    hostState = host,
                    modifier = Modifier.testTag(QUICK_CAPTURE_SNACKBAR_HOST_TAG),
                )
            }
        },
        bottomBar = {
            if (multiselect) {
                InboxMultiselectActionBar(
                    onToday = onMoveToToday,
                    onTomorrow = onMoveToTomorrow,
                    onEstimate = { showEstimateDialog = true },
                    onEnergy = { showEnergyDialog = true },
                    onLetGo = onLetGo,
                )
            } else if (showBottomNav) {
                EbbBottomNavigation(selected = MainTab.Inbox)
            }
        },
        floatingActionButton = {
            if (state.showFab) {
                FloatingActionButton(
                    onClick = onFabClick,
                    modifier = Modifier.testTag(QUICK_CAPTURE_FAB_TAG),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                ) {
                    EbbSymbol("add", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            if (multiselect) {
                InboxMultiselectTopBar(
                    title = state.title,
                    onDone = onMultiselectDone,
                    onSelectAll = onSelectAll,
                )
            } else {
                InboxTopBar(title = state.title, onSearchToggle = onSearchToggle)
            }
            if (state.searchVisible) {
                androidx.compose.material3.OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s1),
                    placeholder = { Text(stringResource(R.string.inbox_search_hint)) },
                    singleLine = true,
                )
            }
            if (!multiselect) {
                Text(
                    state.subtitle,
                    style = EbbTypography.bodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .padding(horizontal = EbbSpacing.s4)
                            .offset(y = (-8).dp),
                )
            }
            state.dragToTodayHint?.let {
                InboxHintBanner(it)
            }
            if (!multiselect) {
                InboxFilterRow(
                    filters = state.filters,
                    selected = state.selectedFilter,
                    onFilterSelected = onFilterSelected,
                    modifier = Modifier.padding(top = EbbSpacing.s2, bottom = EbbSpacing.s2),
                )
            }
            when (state.visualMode) {
                InboxVisualMode.Loading -> InboxLoadingSkeleton()
                InboxVisualMode.Empty, InboxVisualMode.FilteredEmpty -> InboxEmptyBody(state.emptyMessage)
                else ->
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
                        contentPadding =
                            androidx.compose.foundation.layout.PaddingValues(
                                horizontal = EbbSpacing.s4,
                                vertical = EbbSpacing.s1,
                            ),
                    ) {
                        items(state.tasks, key = { it.id }) { task ->
                            val swipe = state.swipeReveal?.takeIf { it.taskId == task.id }
                            InboxTaskCard(
                                task = task,
                                swipeReveal = swipe,
                                onClick = { onTaskClick(task.id) },
                                onLongClick = { onTaskLongPress(task.id) },
                            )
                        }
                    }
            }
        }
    }
    if (showEstimateDialog) {
        InboxEstimateDialog(
            onDismiss = { showEstimateDialog = false },
            onSelect = { minutes ->
                onSetDuration(minutes)
                showEstimateDialog = false
            },
        )
    }
    if (showEnergyDialog) {
        InboxEnergyDialog(
            onDismiss = { showEnergyDialog = false },
            onSelect = { level ->
                onSetEnergy(level)
                showEnergyDialog = false
            },
        )
    }
}

@Composable
private fun InboxHintBanner(message: String) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s1),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = EbbShapes.md,
    ) {
        Text(
            message,
            modifier = Modifier.padding(EbbSpacing.s3),
            style = EbbTypography.bodyM,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun InboxMultiselectTopBar(
    title: String,
    onDone: () -> Unit,
    onSelectAll: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = EbbSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onDone,
            modifier = Modifier.size(48.dp).testTag(INBOX_MULTISELECT_DONE_TAG),
        ) {
            EbbSymbol("close", fontSize = 22.sp)
        }
        Text(title, style = EbbTypography.headlineM, modifier = Modifier.weight(1f), maxLines = 1)
        IconButton(
            onClick = onSelectAll,
            modifier = Modifier.size(48.dp).testTag(INBOX_MULTISELECT_SELECT_ALL_TAG),
        ) {
            EbbSymbol("select_all", fontSize = 22.sp)
        }
    }
}

@Composable
private fun InboxMultiselectActionBar(
    onToday: () -> Unit,
    onTomorrow: () -> Unit,
    onEstimate: () -> Unit,
    onEnergy: () -> Unit,
    onLetGo: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = EbbSpacing.s2),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InboxActionButton(INBOX_MULTISELECT_TODAY_TAG, "today", stringResource(R.string.inbox_action_today), onToday)
            InboxActionButton(INBOX_MULTISELECT_TOMORROW_TAG, "event", stringResource(R.string.inbox_action_tomorrow), onTomorrow)
            InboxActionButton(INBOX_MULTISELECT_ESTIMATE_TAG, "schedule", stringResource(R.string.inbox_action_estimate), onEstimate)
            InboxActionButton(INBOX_MULTISELECT_ENERGY_TAG, "battery_horiz_050", stringResource(R.string.inbox_action_energy), onEnergy)
            InboxActionButton(INBOX_MULTISELECT_LET_GO_TAG, "spa", stringResource(R.string.inbox_action_let_go), onLetGo)
        }
    }
}

@Composable
private fun InboxActionButton(
    tag: String,
    icon: String,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .testTag(tag)
                .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        EbbSymbol(icon, fontSize = 22.sp)
        Text(label, style = EbbTypography.labelM, maxLines = 1)
    }
}

@Composable
private fun InboxEstimateDialog(
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            EbbCancelButton(label = stringResource(R.string.action_cancel), onClick = onDismiss)
        },
        title = { Text(stringResource(R.string.inbox_action_estimate)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            ) {
                listOf(15, 30, 45, 60, 90).forEach { minutes ->
                    FilterChip(
                        selected = false,
                        onClick = { onSelect(minutes) },
                        label = { Text("$minutes min") },
                    )
                }
            }
        },
    )
}

@Composable
private fun InboxEnergyDialog(
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            EbbCancelButton(label = stringResource(R.string.action_cancel), onClick = onDismiss)
        },
        title = { Text(stringResource(R.string.inbox_action_energy)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
            ) {
                FilterChip(
                    selected = false,
                    onClick = { onSelect(1) },
                    label = { Text(stringResource(R.string.inbox_energy_low)) },
                )
                FilterChip(
                    selected = false,
                    onClick = { onSelect(2) },
                    label = { Text(stringResource(R.string.inbox_energy_med)) },
                )
                FilterChip(
                    selected = false,
                    onClick = { onSelect(3) },
                    label = { Text(stringResource(R.string.inbox_energy_high)) },
                )
            }
        },
    )
}

@Composable
private fun InboxTopBar(
    title: String,
    onSearchToggle: () -> Unit = {},
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = EbbSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = EbbTypography.headlineM, modifier = Modifier.weight(1f), maxLines = 1)
        IconButton(onClick = onSearchToggle, modifier = Modifier.size(48.dp)) {
            EbbSymbol("search", fontSize = 22.sp)
        }
    }
}

@Composable
private fun InboxFilterRow(
    filters: List<String>,
    selected: Int,
    modifier: Modifier = Modifier,
    onFilterSelected: (Int) -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = EbbSpacing.s4),
        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        filters.forEachIndexed { index, label ->
            val isSel = index == selected
            Surface(
                onClick = { onFilterSelected(index) },
                shape = EbbShapes.full,
                color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (isSel && index == 0) {
                        EbbSymbol("check", fontSize = 16.sp)
                    }
                    Text(label, style = EbbTypography.labelL)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun InboxTaskCard(
    task: InboxTaskRow,
    swipeReveal: InboxSwipeReveal?,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(modifier = Modifier.fillMaxWidth()) {
        if (swipeReveal != null) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                shape = EbbShapes.md,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = EbbSpacing.s4),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        swipeReveal.label,
                        style = EbbTypography.labelL,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    EbbSymbol(
                        swipeReveal.icon,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(start = EbbSpacing.s2),
                    )
                }
            }
        }
        val shiftX =
            when (swipeReveal?.side) {
                InboxSwipeSide.Left -> (-120).dp
                InboxSwipeSide.Right -> 120.dp
                null -> 0.dp
            }
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset(x = shiftX)
                    .testTag(inboxTaskTestTag(task.id))
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            shape = EbbShapes.md,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Row(
                modifier = Modifier.height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(EbbTaskColors.strong(task.colorKey)),
                )
                InboxCheckRing(
                    done = task.done,
                    selected = task.selected,
                    modifier = Modifier.padding(start = 8.dp),
                )
                Surface(
                    modifier = Modifier.padding(start = 8.dp).size(40.dp),
                    shape = EbbShapes.sm,
                    color = EbbTaskColors.tint(task.colorKey, dark),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        EbbSymbol(task.iconKey, fontSize = 20.sp)
                    }
                }
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = EbbSpacing.s2),
                ) {
                    Text(
                        task.title,
                        style = EbbTypography.titleM,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EbbSymbol("schedule", fontSize = 14.sp)
                        Text(
                            task.estimate,
                            style = EbbTypography.bodyM,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        InboxEnergyBars(task.energyLevel)
                        if (task.hasReminder) {
                            EbbSymbol("notifications", fontSize = 14.sp)
                        }
                        task.checklist?.let {
                            EbbSymbol("checklist", fontSize = 14.sp)
                            Text(it, style = EbbTypography.labelM)
                        }
                    }
                }
                EbbSymbol(
                    "drag_indicator",
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun InboxCheckRing(
    done: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val ringColor =
        when {
            selected -> MaterialTheme.colorScheme.primary
            done -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        }
    Box(
        modifier =
            modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .then(
                    Modifier.background(
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        shape = CircleShape,
                    ),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(22.dp),
            shape = CircleShape,
            border = BorderStroke(2.dp, ringColor),
            color = androidx.compose.ui.graphics.Color.Transparent,
        ) {
            if (done || selected) {
                Box(contentAlignment = Alignment.Center) {
                    EbbSymbol("check", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun InboxEnergyBars(level: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        listOf(4.dp, 6.dp, 8.dp).forEachIndexed { index, h ->
            Box(
                modifier =
                    Modifier
                        .width(3.dp)
                        .height(h)
                        .background(
                            if (index < level) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            EbbShapes.xs,
                        ),
            )
        }
    }
}

@Composable
private fun InboxLoadingSkeleton() {
    Column(
        modifier = Modifier.padding(horizontal = EbbSpacing.s4),
        verticalArrangement = Arrangement.spacedBy(EbbSpacing.s2),
    ) {
        repeat(5) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, EbbShapes.md),
            )
        }
    }
}

@Composable
private fun InboxEmptyBody(message: String?) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(EbbSpacing.s6),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            message.orEmpty(),
            style = EbbTypography.bodyL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun InboxAppPreview(
    darkTheme: Boolean,
    state: InboxPresentationState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        InboxScreenDesign(state = state, showBottomNav = true)
    }
}
