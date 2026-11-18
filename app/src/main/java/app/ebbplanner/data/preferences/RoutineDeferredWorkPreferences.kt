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

data class PendingRoutineLink(
    val routineId: String,
    val taskId: String,
    val failureAttempts: Int = 0,
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
                val idx = current.indexOfFirst { it.routineId == routineId && it.taskId == taskId }
                if (idx >= 0) {
                    current[idx] = PendingRoutineLink(routineId, taskId, failureAttempts = 0)
                } else {
                    current += PendingRoutineLink(routineId, taskId, failureAttempts = 0)
                }
                prefs[KEY_PENDING_LINKS] = encodeLinks(current)
            }
        }

        suspend fun enqueuePendingSync(routineId: String) {
            dataStore.edit { prefs ->
                val ids = parseIds(prefs[KEY_PENDING_SYNC_IDS]).toMutableSet()
                ids += routineId
                prefs[KEY_PENDING_SYNC_IDS] = ids.joinToString(",")
                val attempts = parseSyncAttempts(prefs[KEY_SYNC_ATTEMPTS]).toMutableMap()
                attempts.remove(routineId)
                prefs[KEY_SYNC_ATTEMPTS] = encodeSyncAttempts(attempts)
            }
        }

        suspend fun snapshotPendingLinks(): List<PendingRoutineLink> =
            dataStore.data.map { prefs ->
                parseLinks(prefs[KEY_PENDING_LINKS])
            }.first()

        suspend fun snapshotPendingSyncRoutineIds(): List<String> =
            dataStore.data.map { prefs ->
                parseIds(prefs[KEY_PENDING_SYNC_IDS])
            }.first()

        suspend fun incrementLinkFailureAttempts(
            routineId: String,
            taskId: String,
        ): Int {
            var next = 0
            dataStore.edit { prefs ->
                val current = parseLinks(prefs[KEY_PENDING_LINKS]).toMutableList()
                val idx = current.indexOfFirst { it.routineId == routineId && it.taskId == taskId }
                if (idx < 0) return@edit
                next = current[idx].failureAttempts + 1
                current[idx] = current[idx].copy(failureAttempts = next)
                prefs[KEY_PENDING_LINKS] = encodeLinks(current)
            }
            return next
        }

        suspend fun syncFailureAttempts(routineId: String): Int =
            dataStore.data.map { prefs ->
                parseSyncAttempts(prefs[KEY_SYNC_ATTEMPTS])[routineId] ?: 0
            }.first()

        suspend fun incrementSyncFailureAttempts(routineId: String): Int {
            var next = 0
            dataStore.edit { prefs ->
                val attempts = parseSyncAttempts(prefs[KEY_SYNC_ATTEMPTS]).toMutableMap()
                next = (attempts[routineId] ?: 0) + 1
                attempts[routineId] = next
                prefs[KEY_SYNC_ATTEMPTS] = encodeSyncAttempts(attempts)
            }
            return next
        }

        suspend fun removePendingLink(
            routineId: String,
            taskId: String,
        ) {
            dataStore.edit { prefs ->
                val current = parseLinks(prefs[KEY_PENDING_LINKS]).toMutableList()
                val removed = current.removeAll { it.routineId == routineId && it.taskId == taskId }
                if (!removed) return@edit
                if (current.isEmpty()) {
                    prefs.remove(KEY_PENDING_LINKS)
                } else {
                    prefs[KEY_PENDING_LINKS] = encodeLinks(current)
                }
            }
        }

        suspend fun removePendingSync(routineId: String) {
            dataStore.edit { prefs ->
                val current = parseIds(prefs[KEY_PENDING_SYNC_IDS]).toMutableList()
                if (!current.remove(routineId)) return@edit
                if (current.isEmpty()) {
                    prefs.remove(KEY_PENDING_SYNC_IDS)
                } else {
                    prefs[KEY_PENDING_SYNC_IDS] = current.joinToString(",")
                }
                val attempts = parseSyncAttempts(prefs[KEY_SYNC_ATTEMPTS]).toMutableMap()
                attempts.remove(routineId)
                if (attempts.isEmpty()) {
                    prefs.remove(KEY_SYNC_ATTEMPTS)
                } else {
                    prefs[KEY_SYNC_ATTEMPTS] = encodeSyncAttempts(attempts)
                }
            }
        }

        companion object {
            const val MAX_DEFERRED_FAILURE_ATTEMPTS = 5
            private val KEY_PENDING_LINKS = stringPreferencesKey("pending_routine_links")
            private val KEY_PENDING_SYNC_IDS = stringPreferencesKey("pending_routine_sync_ids")
            private val KEY_SYNC_ATTEMPTS = stringPreferencesKey("pending_routine_sync_attempts")
            private const val LINK_SEP = "\n"
            private const val FIELD_SEP = "|"

            private fun parseLinks(raw: String?): List<PendingRoutineLink> {
                if (raw.isNullOrBlank()) return emptyList()
                return raw.split(LINK_SEP).mapNotNull { line ->
                    val parts = line.split(FIELD_SEP)
                    if (parts.size < 2) return@mapNotNull null
                    PendingRoutineLink(
                        routineId = parts[0],
                        taskId = parts[1],
                        failureAttempts = parts.getOrNull(2)?.toIntOrNull() ?: 0,
                    )
                }
            }

            private fun encodeLinks(links: List<PendingRoutineLink>): String =
                links.joinToString(LINK_SEP) {
                    "${it.routineId}$FIELD_SEP${it.taskId}$FIELD_SEP${it.failureAttempts}"
                }

            private fun parseIds(raw: String?): List<String> =
                raw
                    ?.split(',')
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()

            private fun parseSyncAttempts(raw: String?): Map<String, Int> {
                if (raw.isNullOrBlank()) return emptyMap()
                return raw.split(LINK_SEP).mapNotNull { line ->
                    val parts = line.split(FIELD_SEP, limit = 2)
                    if (parts.size != 2) return@mapNotNull null
                    val count = parts[1].toIntOrNull() ?: return@mapNotNull null
                    parts[0] to count
                }.toMap()
            }

            private fun encodeSyncAttempts(attempts: Map<String, Int>): String =
                attempts.entries.joinToString(LINK_SEP) { "${it.key}$FIELD_SEP${it.value}" }
        }
    }