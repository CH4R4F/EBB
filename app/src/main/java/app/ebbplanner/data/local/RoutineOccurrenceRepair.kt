package app.ebbplanner.data.local

import android.database.Cursor
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Repairs routine occurrence keys and skip rows (no deletes / soft-deletes).
 */
internal object RoutineOccurrenceRepair {
    private const val TAG = "RoutineOccurrenceRepair"
    private const val META_TABLE = "ebb_meta"
    private const val META_KEY_PENDING = "routine_repair_pending"
    private const val SAVEPOINT = "routine_occurrence_repair"

    private const val TASK_SNAPSHOT_COLUMNS =
        """
        id, sourceRoutineId, date, routineOccurrenceDate, title, startLocalTime,
        durationMin, routineInstanceDetached, status, createdAt, updatedAt, deletedAt,
        notes, reminderEnabled, energy
        """

    fun runDuringMigration(
        db: SupportSQLiteDatabase,
        recordPendingOnFailure: Boolean,
    ) {
        db.execSQL("SAVEPOINT $SAVEPOINT")
        try {
            executeRepair(db)
            db.execSQL("RELEASE SAVEPOINT $SAVEPOINT")
            if (recordPendingOnFailure) {
                clearPendingInMeta(db)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Routine occurrence repair failed during migration; rolling back savepoint.", t)
            rollbackSavepoint(db)
            if (recordPendingOnFailure) {
                setPendingInMeta(db)
            }
        }
    }

    fun executeRepair(db: SupportSQLiteDatabase) {
        ensureMetaTable(db)
        val routines = loadRoutines(db)
        if (routines.isEmpty()) return
        purgeFutureSkipsWithoutEvidence(db, routines)
        keyLiveRoutineTasks(db, routines)
        detachStrictUntouchedDuplicates(db, routines)
        writeProvableSkips(db, routines, loadAllRoutineTasks(db))
    }

    fun ensureMetaTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $META_TABLE (
                key TEXT NOT NULL PRIMARY KEY,
                value TEXT NOT NULL
            )
            """.trimIndent(),
        )
    }

    fun isPendingInMeta(db: SupportSQLiteDatabase): Boolean {
        ensureMetaTable(db)
        db.query(
            "SELECT value FROM $META_TABLE WHERE key = ? LIMIT 1",
            arrayOf(META_KEY_PENDING),
        ).use { c ->
            if (!c.moveToFirst()) return false
            return c.getString(0) == "1"
        }
    }

    fun setPendingInMeta(db: SupportSQLiteDatabase) {
        ensureMetaTable(db)
        db.execSQL(
            "INSERT OR REPLACE INTO $META_TABLE (key, value) VALUES (?, ?)",
            arrayOf(META_KEY_PENDING, "1"),
        )
    }

    fun clearPendingInMeta(db: SupportSQLiteDatabase) {
        ensureMetaTable(db)
        db.execSQL("DELETE FROM $META_TABLE WHERE key = ?", arrayOf(META_KEY_PENDING))
    }

    fun deduplicateLiveOccurrenceKeysForIndex(db: SupportSQLiteDatabase) {
        db.query(
            """
            SELECT sourceRoutineId, routineOccurrenceDate
            FROM tasks
            WHERE deletedAt IS NULL
            AND sourceRoutineId IS NOT NULL
            AND routineOccurrenceDate IS NOT NULL
            GROUP BY sourceRoutineId, routineOccurrenceDate
            HAVING COUNT(*) > 1
            """.trimIndent(),
        ).use { groups ->
            while (groups.moveToNext()) {
                val routineId = groups.getString(0)
                val occurrence = groups.getString(1)
                val taskIds = mutableListOf<Pair<String, Long>>()
                db.query(
                    """
                    SELECT id, createdAt FROM tasks
                    WHERE deletedAt IS NULL
                    AND sourceRoutineId = ?
                    AND routineOccurrenceDate = ?
                    ORDER BY createdAt ASC, id ASC
                    """.trimIndent(),
                    arrayOf(routineId, occurrence),
                ).use { c ->
                    while (c.moveToNext()) {
                        taskIds += c.getString(0) to c.getLong(1)
                    }
                }
                taskIds.drop(1).forEach { (id, _) ->
                    db.execSQL(
                        """
                        UPDATE tasks
                        SET routineOccurrenceDate = NULL, routineInstanceDetached = 1
                        WHERE id = ?
                        """.trimIndent(),
                        arrayOf(id),
                    )
                }
            }
        }
    }

    private fun rollbackSavepoint(db: SupportSQLiteDatabase) {
        db.execSQL(";ROLLBACK TO $SAVEPOINT")
        db.execSQL("RELEASE SAVEPOINT $SAVEPOINT")
    }

    private fun purgeFutureSkipsWithoutEvidence(
        db: SupportSQLiteDatabase,
        routines: Map<String, RoutineRow>,
    ) {
        val today = LocalDate.now()
        val toDelete = mutableListOf<Pair<String, String>>()
        db.query("SELECT routineId, occurrenceDate FROM routine_skips").use { c ->
            while (c.moveToNext()) {
                val routineId = c.getString(0)
                if (routines[routineId] == null) continue
                val occ = parseDateOrNull(c.getString(1)) ?: continue
                if (occ.isBefore(today)) continue
                if (!skipHasEvidence(db, routineId, occ)) {
                    toDelete += routineId to occ.toString()
                }
            }
        }
        toDelete.forEach { (routineId, occ) ->
            db.execSQL(
                "DELETE FROM routine_skips WHERE routineId = ? AND occurrenceDate = ?",
                arrayOf(routineId, occ),
            )
        }
    }

    private fun skipHasEvidence(
        db: SupportSQLiteDatabase,
        routineId: String,
        occurrence: LocalDate,
    ): Boolean {
        val occ = occurrence.toString()
        db.query(
            """
            SELECT 1 FROM tasks
            WHERE sourceRoutineId = ? AND deletedAt IS NOT NULL
            AND (date = ? OR routineOccurrenceDate = ?)
            LIMIT 1
            """.trimIndent(),
            arrayOf(routineId, occ, occ),
        ).use { if (it.moveToFirst()) return true }
        db.query(
            """
            SELECT 1 FROM tasks
            WHERE sourceRoutineId = ? AND deletedAt IS NULL
            AND routineOccurrenceDate = ? AND date IS NOT NULL AND date != ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(routineId, occ, occ),
        ).use { return it.moveToFirst() }
    }

    private fun keyLiveRoutineTasks(
        db: SupportSQLiteDatabase,
        routines: Map<String, RoutineRow>,
    ) {
        val liveByRoutineDate = loadLiveTasksGrouped(db)
        liveByRoutineDate.forEach { (key, taskIds) ->
            val routine = routines[key.routineId] ?: return@forEach
            val scheduled = key.date
            if (!appliesOn(routine, scheduled)) {
                taskIds.forEach { markDetachedPreservingValidKey(db, it, routines) }
                return@forEach
            }
            if (taskIds.size > 1) {
                insertSkip(db, routine.id, scheduled.toString())
                taskIds.forEach { markDetachedPreservingValidKey(db, it, routines) }
                return@forEach
            }
            val task = loadTask(db, taskIds.first()) ?: return@forEach
            if (hasValidOccurrenceKey(task, routine, scheduled)) {
                return@forEach
            }
            db.execSQL(
                """
                UPDATE tasks
                SET routineOccurrenceDate = ?, routineInstanceDetached = ?
                WHERE id = ?
                """.trimIndent(),
                arrayOf(
                    scheduled.toString(),
                    if (task.detached) 1 else 0,
                    task.id,
                ),
            )
        }
    }

    private fun detachStrictUntouchedDuplicates(
        db: SupportSQLiteDatabase,
        routines: Map<String, RoutineRow>,
    ) {
        val live = loadAllRoutineTasks(db).filter { it.deletedAt == null }
        val bySchedule =
            live.groupBy { task ->
                val d = task.date?.let { parseDateOrNull(it) } ?: return@groupBy null
                task.routineId to d
            }
        bySchedule.forEach { (key, group) ->
            if (key == null || group.size < 2) return@forEach
            val routine = routines[key.first] ?: return@forEach
            val hasDone = group.any { it.status == STATUS_DONE }
            if (!hasDone) return@forEach
            group.filter { it.status != STATUS_DONE }.forEach { open ->
                if (isStrictUntouchedGenerator(db, open, routine)) {
                    detachTask(db, open.id)
                }
            }
        }
        val byOccurrence =
            live.groupBy { task ->
                val occ =
                    task.occurrenceDate?.let { parseDateOrNull(it) }
                        ?: task.date?.let { parseDateOrNull(it) }
                        ?: return@groupBy null
                task.routineId to occ
            }
        byOccurrence.forEach { (key, group) ->
            if (key == null) return@forEach
            val routine = routines[key.first] ?: return@forEach
            detachGeneratorEchoes(db, group, routine, key.second)
            val hasDone = group.any { it.status == STATUS_DONE }
            if (!hasDone) return@forEach
            group.filter { it.status != STATUS_DONE }.forEach { open ->
                if (isStrictUntouchedGenerator(db, open, routine)) {
                    detachTask(db, open.id)
                }
            }
        }
    }

    private fun detachGeneratorEchoes(
        db: SupportSQLiteDatabase,
        liveOnOccurrence: List<TaskSnapshot>,
        routine: RoutineRow,
        occ: LocalDate,
    ) {
        val deleted =
            queryTaskSnapshots(
                db,
                """
                SELECT $TASK_SNAPSHOT_COLUMNS FROM tasks
                WHERE sourceRoutineId = ? AND deletedAt IS NOT NULL
                AND (routineOccurrenceDate = ? OR date = ?)
                """.trimIndent(),
                arrayOf(routine.id, occ.toString(), occ.toString()),
            )
        if (deleted.isEmpty()) return
        val deletedAtMax = deleted.maxOf { it.deletedAt ?: it.updatedAt }
        liveOnOccurrence.forEach { liveTask ->
            if (liveTask.detached) return@forEach
            val echo =
                liveTask.createdAt > deletedAtMax &&
                    isStrictUntouchedGenerator(db, liveTask, routine)
            if (echo) {
                detachTask(db, liveTask.id)
                insertSkip(db, routine.id, occ.toString())
            }
        }
    }

    private fun writeProvableSkips(
        db: SupportSQLiteDatabase,
        routines: Map<String, RoutineRow>,
        tasks: List<TaskSnapshot>,
    ) {
        tasks.filter { it.deletedAt != null }.forEach { deleted ->
            val routine = routines[deleted.routineId] ?: return@forEach
            val occ =
                deleted.occurrenceDate?.let { parseDateOrNull(it) }
                    ?: deleted.date?.let { parseDateOrNull(it) }
                    ?: return@forEach
            if (!appliesOn(routine, occ)) return@forEach
            insertSkip(db, deleted.routineId, occ.toString())
        }
        tasks.filter { it.deletedAt == null }.forEach { live ->
            val occ = live.occurrenceDate ?: return@forEach
            val sched = live.date ?: return@forEach
            if (occ != sched) {
                insertSkip(db, live.routineId, occ)
            }
        }
    }

    private fun isStrictUntouchedGenerator(
        db: SupportSQLiteDatabase,
        task: TaskSnapshot,
        routine: RoutineRow,
    ): Boolean {
        if (task.status != STATUS_OPEN) return false
        if (task.detached) return false
        if (task.updatedAt != task.createdAt) return false
        if (task.notes.isNotBlank()) return false
        if (task.energy != null) return false
        if (task.reminderEnabled != DEFAULT_REMINDER_ENABLED) return false
        if (task.title != routine.title) return false
        if (task.startLocalTime != routine.startLocalTime) return false
        if (task.durationMin != routine.durationMin) return false
        return !hasSubtasks(db, task.id)
    }

    private fun hasSubtasks(
        db: SupportSQLiteDatabase,
        taskId: String,
    ): Boolean {
        db.query(
            "SELECT 1 FROM subtasks WHERE taskId = ? LIMIT 1",
            arrayOf(taskId),
        ).use { return it.moveToFirst() }
    }

    private fun hasValidOccurrenceKey(
        task: TaskSnapshot,
        routine: RoutineRow,
        scheduled: LocalDate,
    ): Boolean {
        val occ = task.occurrenceDate ?: return false
        val occDate = parseDateOrNull(occ) ?: return false
        if (!appliesOn(routine, occDate)) return false
        if (occDate == scheduled) return true
        val liveDate = task.date?.let { parseDateOrNull(it) } ?: return false
        return occDate != liveDate
    }

    private fun markDetachedPreservingValidKey(
        db: SupportSQLiteDatabase,
        taskId: String,
        routines: Map<String, RoutineRow>,
    ) {
        val task = loadTask(db, taskId) ?: return
        val routine = routines[task.routineId] ?: return
        val scheduled = task.date?.let { parseDateOrNull(it) }
        if (scheduled != null && hasValidOccurrenceKey(task, routine, scheduled)) {
            db.execSQL(
                "UPDATE tasks SET routineInstanceDetached = 1 WHERE id = ?",
                arrayOf(taskId),
            )
            return
        }
        db.execSQL(
            "UPDATE tasks SET routineOccurrenceDate = NULL, routineInstanceDetached = 1 WHERE id = ?",
            arrayOf(taskId),
        )
    }

    private fun detachTask(
        db: SupportSQLiteDatabase,
        taskId: String,
    ) {
        db.execSQL(
            "UPDATE tasks SET routineInstanceDetached = 1 WHERE id = ?",
            arrayOf(taskId),
        )
    }

    private fun insertSkip(
        db: SupportSQLiteDatabase,
        routineId: String,
        occurrence: String,
    ) {
        db.execSQL(
            "INSERT OR IGNORE INTO routine_skips (routineId, occurrenceDate) VALUES (?, ?)",
            arrayOf(routineId, occurrence),
        )
    }

    private data class RoutineRow(
        val id: String,
        val title: String,
        val repeatRule: String,
        val weeklyDays: String,
        val startLocalTime: String,
        val durationMin: Int,
        val enabled: Boolean,
    )

    private data class RoutineDateKey(
        val routineId: String,
        val date: LocalDate,
    )

    private data class TaskSnapshot(
        val id: String,
        val routineId: String,
        val date: String?,
        val occurrenceDate: String?,
        val title: String,
        val startLocalTime: String?,
        val durationMin: Int,
        val detached: Boolean,
        val status: String,
        val createdAt: Long,
        val updatedAt: Long,
        val deletedAt: Long?,
        val notes: String,
        val reminderEnabled: Boolean,
        val energy: String?,
    )

    private fun loadRoutines(db: SupportSQLiteDatabase): Map<String, RoutineRow> {
        val map = mutableMapOf<String, RoutineRow>()
        db.query(
            "SELECT id, title, repeatRule, weeklyDays, startLocalTime, durationMin, enabled FROM routines",
        ).use { c ->
            while (c.moveToNext()) {
                map[c.getString(0)] =
                    RoutineRow(
                        id = c.getString(0),
                        title = c.getString(1),
                        repeatRule = c.getString(2),
                        weeklyDays = c.getString(3),
                        startLocalTime = c.getString(4),
                        durationMin = c.getInt(5),
                        enabled = c.getInt(6) != 0,
                    )
            }
        }
        return map
    }

    private fun loadLiveTasksGrouped(db: SupportSQLiteDatabase): Map<RoutineDateKey, List<String>> {
        val map = mutableMapOf<RoutineDateKey, MutableList<String>>()
        db.query(
            """
            SELECT id, sourceRoutineId, date
            FROM tasks
            WHERE deletedAt IS NULL AND sourceRoutineId IS NOT NULL AND date IS NOT NULL
            """.trimIndent(),
        ).use { c ->
            while (c.moveToNext()) {
                val id = c.getString(0)
                val routineId = c.getString(1)
                val date = parseDateOrNull(c.getString(2)) ?: continue
                val key = RoutineDateKey(routineId, date)
                map.getOrPut(key) { mutableListOf() }.add(id)
            }
        }
        return map
    }

    private fun loadAllRoutineTasks(db: SupportSQLiteDatabase): List<TaskSnapshot> =
        queryTaskSnapshots(
            db,
            "SELECT $TASK_SNAPSHOT_COLUMNS FROM tasks WHERE sourceRoutineId IS NOT NULL",
        )

    private fun loadTask(
        db: SupportSQLiteDatabase,
        id: String,
    ): TaskSnapshot? =
        queryTaskSnapshots(
            db,
            "SELECT $TASK_SNAPSHOT_COLUMNS FROM tasks WHERE id = ?",
            arrayOf(id),
        ).firstOrNull()

    private fun queryTaskSnapshots(
        db: SupportSQLiteDatabase,
        sql: String,
        args: Array<String>? = null,
    ): List<TaskSnapshot> {
        val list = mutableListOf<TaskSnapshot>()
        db.query(sql, args ?: emptyArray()).use { c ->
            while (c.moveToNext()) {
                list += rowToSnapshot(c)
            }
        }
        return list
    }

    private fun rowToSnapshot(c: Cursor): TaskSnapshot {
        fun col(name: String): Int = c.getColumnIndexOrThrow(name)
        fun stringOrNull(index: Int): String? = if (c.isNull(index)) null else c.getString(index)
        fun longOrNull(index: Int): Long? = if (c.isNull(index)) null else c.getLong(index)
        return TaskSnapshot(
            id = c.getString(col("id")),
            routineId = c.getString(col("sourceRoutineId")),
            date = stringOrNull(col("date")),
            occurrenceDate = stringOrNull(col("routineOccurrenceDate")),
            title = c.getString(col("title")),
            startLocalTime = stringOrNull(col("startLocalTime")),
            durationMin = c.getInt(col("durationMin")),
            detached = c.getInt(col("routineInstanceDetached")) != 0,
            status = c.getString(col("status")),
            createdAt = c.getLong(col("createdAt")),
            updatedAt = c.getLong(col("updatedAt")),
            deletedAt = longOrNull(col("deletedAt")),
            notes = c.getString(col("notes")),
            reminderEnabled = c.getInt(col("reminderEnabled")) != 0,
            energy = stringOrNull(col("energy")),
        )
    }

    private fun parseDateOrNull(raw: String): LocalDate? = runCatching { LocalDate.parse(raw) }.getOrNull()

    private fun appliesOn(
        routine: RoutineRow,
        date: LocalDate,
    ): Boolean =
        when (routine.repeatRule) {
            "daily" -> true
            "weekdays" -> date.dayOfWeek.value in DayOfWeek.MONDAY.value..DayOfWeek.FRIDAY.value
            "weekly" -> {
                val allowed =
                    routine.weeklyDays.split(',')
                        .mapNotNull { it.trim().toIntOrNull() }
                        .toSet()
                allowed.isNotEmpty() && allowed.contains(date.dayOfWeek.value)
            }
            else -> false
        }

    private const val STATUS_OPEN = "open"
    private const val STATUS_DONE = "done"
    private const val DEFAULT_REMINDER_ENABLED = true
}