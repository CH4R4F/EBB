@file:Suppress("MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.today.TodayScreenDesign
import app.ebbplanner.ui.today.todayPopulatedDark

@Suppress("LongParameterList", "LongMethod")
@Composable
fun EbbSheetScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    doneLabel: String = "Done",
    doneLeadingIcon: String? = null,
    sheetMinHeight: androidx.compose.ui.unit.Dp = 560.dp,
    sheetTopInset: androidx.compose.ui.unit.Dp = 0.dp,
    sheetContainerColor: Color = Color.Unspecified,
    showBottomActions: Boolean = true,
    sheetWrapContent: Boolean = false,
    cancelLabel: String = "Cancel",
    onDoneClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    backgroundState: app.ebbplanner.ui.today.TodayPresentationState? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        TodayScreenDesign(
            state = backgroundState ?: todayPopulatedDark(),
            showBottomNav = true,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)))
        val sheetModifier =
            if (sheetTopInset > 0.dp) {
                val base = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = sheetTopInset)
                if (sheetWrapContent) {
                    base.wrapContentHeight()
                } else {
                    base.fillMaxHeight()
                }
            } else {
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(sheetMinHeight)
            }
        val containerColor =
            when {
                sheetContainerColor != Color.Unspecified -> sheetContainerColor
                sheetTopInset > 0.dp -> MaterialTheme.colorScheme.surfaceContainerHigh
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        Surface(
            modifier = sheetModifier,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = containerColor,
        ) {
            Column(if (sheetWrapContent) Modifier.wrapContentHeight() else Modifier.fillMaxSize()) {
                SheetGrabber()
                Text(title, style = EbbTypography.titleL, modifier = Modifier.padding(horizontal = EbbSpacing.s4))
                subtitle?.let {
                    Text(
                        it,
                        style = EbbTypography.bodyM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = 4.dp),
                    )
                }
                if (sheetWrapContent) {
                    Column(
                        modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
                    ) {
                        content()
                    }
                } else {
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
                    ) {
                        content()
                    }
                }
                if (showBottomActions) {
                    SheetActions(
                        doneLabel = doneLabel,
                        doneLeadingIcon = doneLeadingIcon,
                        cancelLabel = cancelLabel,
                        onDoneClick = onDoneClick,
                        onCancelClick = onCancelClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetGrabber() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.width(32.dp).height(4.dp).background(MaterialTheme.colorScheme.outline, EbbShapes.full))
    }
}

@Composable
fun SheetActions(
    doneLabel: String = "Done",
    doneLeadingIcon: String? = null,
    cancelLabel: String = "Cancel",
    onDoneClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
) {
    HorizontalDivider()
    Row(
        Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s2, vertical = EbbSpacing.s2),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            cancelLabel,
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .clickable(onClick = onCancelClick)
                    .padding(horizontal = EbbSpacing.s3, vertical = 10.dp),
        )
        Surface(
            shape = EbbShapes.full,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp).clickable(onClick = onDoneClick),
        ) {
            Row(
                Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                doneLeadingIcon?.let {
                    EbbSymbol(it, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(end = 8.dp))
                }
                Text(
                    doneLabel,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = EbbTypography.labelL,
                )
            }
        }
    }
}
