package app.ebbplanner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.routineDeferredDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "routine_deferred_work",
)

@Singleton
class RoutineDeferredWorkPreferences
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.routineDeferredDataStore

        suspend fun enqueuePendingLink(
            routineId: String,
            taskId: String,
        ) {
            dataStore.edit { prefs ->
                val current = parseLinks(prefs[KEY_PENDING_LINKS]).toMutableList()
                if (current.none { it.first == routineId && it.second == taskId }) {
                    current += routineId to taskId
                    prefs[KEY_PENDING_LINKS] = encodeLinks(current)
                }
            }
        }

        suspend fun enqueuePendingSync(routineId: String) {
            dataStore.edit { prefs ->
                val ids = parseIds(prefs[KEY_PENDING_SYNC_IDS]).toMutableSet()
                ids += routineId
                prefs[KEY_PENDING_SYNC_IDS] = ids.joinToString(",")
            }
        }

        suspend fun takePendingLinks(): List<Pair<String, String>> {
            val snapshot =
                dataStore.data.map { prefs ->
                    parseLinks(prefs[KEY_PENDING_LINKS])
                }.first()
            if (snapshot.isEmpty()) return emptyList()
            dataStore.edit { prefs ->
                prefs.remove(KEY_PENDING_LINKS)
            }
            return snapshot
        }

        suspend fun takePendingSyncRoutineIds(): List<String> {
            val snapshot =
                dataStore.data.map { prefs ->
                    parseIds(prefs[KEY_PENDING_SYNC_IDS])
                }.first()
            if (snapshot.isEmpty()) return emptyList()
            dataStore.edit { prefs ->
                prefs.remove(KEY_PENDING_SYNC_IDS)
            }
            return snapshot
        }

        companion object {
            private val KEY_PENDING_LINKS = stringPreferencesKey("pending_routine_links")
            private val KEY_PENDING_SYNC_IDS = stringPreferencesKey("pending_routine_sync_ids")
            private const val LINK_SEP = "\n"
            private const val FIELD_SEP = "|"

            private fun parseLinks(raw: String?): List<Pair<String, String>> {
                if (raw.isNullOrBlank()) return emptyList()
                return raw.split(LINK_SEP).mapNotNull { line ->
                    val parts = line.split(FIELD_SEP, limit = 2)
                    if (parts.size != 2) return@mapNotNull null
                    parts[0] to parts[1]
                }
            }

            private fun encodeLinks(links: List<Pair<String, String>>): String =
                links.joinToString(LINK_SEP) { "${it.first}$FIELD_SEP${it.second}" }

            private fun parseIds(raw: String?): List<String> =
                raw
                    ?.split(',')
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
        }
    }