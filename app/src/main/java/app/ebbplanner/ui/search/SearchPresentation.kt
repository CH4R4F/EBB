@file:Suppress("NewLineAtEndOfFile")

package app.ebbplanner.ui.search

data class SearchHit(
    val title: String,
    val highlightStart: Int,
    val highlightLength: Int,
    val subtitle: String,
    val icon: String,
)

data class SearchSection(
    val label: String,
    val hits: List<SearchHit>,
)

data class SearchPresentationState(
    val query: String = "",
    val sections: List<SearchSection> = emptyList(),
    val recentQueries: List<String> = emptyList(),
    val mode: SearchVisualMode = SearchVisualMode.Results,
)

enum class SearchVisualMode {
    Recent,
    Results,
    NoResults,
}
