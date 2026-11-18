@file:Suppress("LongMethod")

package app.ebbplanner.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol

@Suppress("LongParameterList")
@Composable
fun TodayScreenDesign(
    state: TodayPresentationState,
    modifier: Modifier = Modifier,
    showBottomNav: Boolean = true,
    onFabClick: () -> Unit = {},
    onPlanDayClick: () -> Unit = {},
    onAddTaskClick: () -> Unit = {},
    onShiftClick: () -> Unit = {},
    onOpenOverlap: () -> Unit = {},
    onOpenMakeItFit: () -> Unit = {},
    onPrevDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    onSettings: () -> Unit = {},
    onAnytimeClick: (String) -> Unit = {},
    onAnytimeDone: (String) -> Unit = {},
    onAddAnytimeCapture: () -> Unit = {},
    onTaskClick: (String) -> Unit = {},
    onTaskLongPress: (String) -> Unit = {},
    onMarkTaskDone: (String) -> Unit = {},
) {
    val dimmed = state.visualMode == TodayVisualMode.Drag
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomNav) {
                TodayBottomNavPreview()
            }
        },
        floatingActionButton = {
            if (state.showFab && state.quickCapture == null) {
                FloatingActionButton(
                    onClick = onFabClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                ) {
                    EbbSymbol("add", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .then(if (dimmed) Modifier.alpha(0.65f) else Modifier),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TodayTopBar(
                    dateLabel = state.dateLabel,
                    multiselect = state.visualMode == TodayVisualMode.Multiselect,
                    multiselectCount = state.multiselectCount,
                    onPrevDay = onPrevDay,
                    onNextDay = onNextDay,
                    onSearch = null,
                    onSettings = onSettings,
                    onMore = {},
                )
                when {
                    state.bannerMessage != null ->
                        TodayBanner(state.bannerMessage, onClick = onOpenMakeItFit)
                }
                state.overlapHint?.let { TodayOverlapHint(it, onClick = onOpenOverlap) }
                if (state.visualMode != TodayVisualMode.Multiselect) {
                    TodayDayHead(state = state, onShiftClick = onShiftClick)
                    TodayAnytimeTray(
                        chips = state.anytimeChips,
                        onChipClick = onAnytimeClick,
                        onChipDone = onAnytimeDone,
                        onAddAnytime = onAddAnytimeCapture,
                    )
                }
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = EbbSpacing.s1),
                ) {
                    when (state.visualMode) {
                        TodayVisualMode.Loading -> TodayLoadingSkeleton()
                        TodayVisualMode.Empty ->
                            TodayEmptyState(
                                onPlanDayClick = onPlanDayClick,
                                onAddTaskClick = onAddTaskClick,
                            )
                        else -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            TodayTimeline(
                                state = state,
                                onTaskClick = onTaskClick,
                                onTaskLongPress = onTaskLongPress,
                                onMarkTaskDone = onMarkTaskDone,
                            )
                            Spacer(modifier = Modifier.height(timelineFabScrollClearance))
                        }
                    }
                }
            }
            state.quickCapture?.let { capture ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
                )
                QuickCaptureOverlay(
                    capture = capture,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
        if (dimmed && state.dragOverlay != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .alpha(state.dragOverlay.scrimAlpha)
                        .padding(padding),
            ) {
                // scrim handled by parent alpha; drag overlay draws on timeline
            }
        }
    }
}

@Composable
fun TodayAppPreview(
    darkTheme: Boolean,
    state: TodayPresentationState,
    modifier: Modifier = Modifier,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        TodayScreenDesign(state = state, modifier = modifier)
    }
}
