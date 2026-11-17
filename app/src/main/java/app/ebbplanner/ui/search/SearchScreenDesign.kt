@file:Suppress("LongMethod", "NewLineAtEndOfFile", "MaxLineLength")

package app.ebbplanner.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ebbplanner.ui.components.EbbListLeadingIcon
import app.ebbplanner.ui.theme.EbbShapes
import app.ebbplanner.ui.theme.EbbSpacing
import app.ebbplanner.ui.theme.EbbSymbol
import app.ebbplanner.ui.theme.EbbTypography

@Composable
fun SearchScreenDesign(
    state: SearchPresentationState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SearchQueryBar(state.query)
        when (state.mode) {
            SearchVisualMode.NoResults ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No matches for “${state.query}”", style = EbbTypography.bodyL)
                }
            SearchVisualMode.Recent ->
                LazyColumn(modifier = Modifier.padding(horizontal = EbbSpacing.s4)) {
                    item {
                        Text(
                            "Recent",
                            style = EbbTypography.labelL,
                            modifier = Modifier.padding(vertical = EbbSpacing.s2),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(state.recentQueries) { q ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            EbbSymbol("history", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(q, modifier = Modifier.padding(start = EbbSpacing.s3), style = EbbTypography.bodyL)
                        }
                    }
                }
            SearchVisualMode.Results ->
                LazyColumn {
                    state.sections.forEach { section ->
                        item {
                            Text(
                                section.label,
                                style = EbbTypography.labelL,
                                modifier = Modifier.padding(horizontal = EbbSpacing.s4, vertical = EbbSpacing.s2),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        items(section.hits) { hit -> SearchHitRow(hit) }
                    }
                }
        }
    }
}

@Composable
private fun SearchQueryBar(query: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = EbbSpacing.s1, vertical = EbbSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = {}) { EbbSymbol("arrow_back", fontSize = 22.sp) }
        Surface(
            modifier = Modifier.weight(1f).height(48.dp),
            shape = EbbShapes.full,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(modifier = Modifier.padding(horizontal = EbbSpacing.s3), verticalAlignment = Alignment.CenterVertically) {
                Text(query, style = EbbTypography.bodyL, modifier = Modifier.weight(1f))
                EbbSymbol("close", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun SearchHitRow(hit: SearchHit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = EbbSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EbbListLeadingIcon(hit.icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = highlightTitle(hit), style = EbbTypography.bodyL)
            Text(hit.subtitle, style = EbbTypography.bodyM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun highlightTitle(hit: SearchHit) =
    buildAnnotatedString {
        if (hit.highlightLength > 0) {
            val end = hit.highlightStart + hit.highlightLength
            append(hit.title.substring(0, hit.highlightStart))
            withStyle(SpanStyle(background = MaterialTheme.colorScheme.tertiaryContainer)) {
                append(hit.title.substring(hit.highlightStart, end.coerceAtMost(hit.title.length)))
            }
            if (end < hit.title.length) {
                append(hit.title.substring(end))
            }
        } else {
            append(hit.title)
        }
    }

@Composable
fun SearchAppPreview(
    darkTheme: Boolean,
    state: SearchPresentationState,
) {
    app.ebbplanner.ui.onboarding.OnboardingAppPreview(darkTheme = darkTheme) {
        SearchScreenDesign(state = state)
    }
}
