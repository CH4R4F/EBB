package app.ebbplanner.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.R
import app.ebbplanner.ui.navigation.MainTab
import app.ebbplanner.ui.theme.EbbSymbol

@Composable
fun EbbBottomNavigation(
    selected: MainTab,
    modifier: Modifier = Modifier,
    onTabSelected: (MainTab) -> Unit = {},
) {
    val colors =
        NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        MainTab.entries.forEach { tab ->
            val isSelected = tab == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    val iconColor =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier =
                                Modifier
                                    .height(32.dp)
                                    .widthIn(min = 56.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            ) {
                                EbbSymbol(name = tab.symbolName(), fontSize = 22.sp, color = iconColor)
                            }
                        }
                    } else {
                        EbbSymbol(name = tab.symbolName(), fontSize = 22.sp, color = iconColor)
                    }
                },
                label = {
                    Text(
                        mainTabLabel(tab),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = colors,
            )
        }
    }
}

private fun MainTab.symbolName(): String =
    when (this) {
        MainTab.Today -> "today"
        MainTab.Inbox -> "inbox"
        MainTab.Week -> "view_week"
        MainTab.Routines -> "event_repeat"
    }

@Composable
private fun mainTabLabel(tab: MainTab): String =
    when (tab) {
        MainTab.Today -> stringResource(R.string.tab_today)
        MainTab.Inbox -> stringResource(R.string.tab_inbox)
        MainTab.Week -> stringResource(R.string.tab_week)
        MainTab.Routines -> stringResource(R.string.tab_routines)
    }
