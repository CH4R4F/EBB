package app.ebbplanner.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    EbbSymbol(
                        name = tab.symbolName(),
                        fontSize = 22.sp,
                        color =
                            if (tab == selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                },
                label = { Text(mainTabLabel(tab)) },
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
