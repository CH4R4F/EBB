@file:Suppress("MaxLineLength", "NewLineAtEndOfFile")

package app.ebbplanner.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbTypography
import app.ebbplanner.ui.today.TodayScreenDesign
import app.ebbplanner.ui.today.todayPopulatedDark

@Composable
fun EbbSheetScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    doneLabel: String = "Done",
    sheetMinHeight: androidx.compose.ui.unit.Dp = 560.dp,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        TodayScreenDesign(state = todayPopulatedDark(), showBottomNav = true, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)))
        Surface(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(sheetMinHeight),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(Modifier.fillMaxSize()) {
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
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
                ) {
                    content()
                }
                SheetActions(doneLabel = doneLabel)
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
fun SheetActions(doneLabel: String = "Done") {
    HorizontalDivider()
    Row(
        Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s2, vertical = EbbSpacing.s2),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Cancel",
            style = EbbTypography.labelL,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = EbbSpacing.s3, vertical = 10.dp),
        )
        Surface(shape = EbbShapes.full, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp)) {
            Text(
                doneLabel,
                color = MaterialTheme.colorScheme.onPrimary,
                style = EbbTypography.labelL,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}
