package app.ebbplanner.data.local

import android.database.Cursor
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Conservative repair for routine instance keys and skip rows.
 */
internal object RoutineOccurrenceBackfill {
    private const val TAG = "RoutineOccurrenceBackfill"
    private const val WINDOW_PAST = 13L
    private const val WINDOW_FUTURE = 13L

    private const val TASK_SNAPSHOT_COLUMNS =
        """
        id, sourceRoutineId, date, routineOccurrenceDate, title, startLocalTime,
        durationMin, routineInstanceDetached, status, createdAt, updatedAt, deletedAt
        """

    fun run(db: SupportSQLiteDatabase) {
        try {
            runOrThrow(db)
        } catch (t: Throwable) {
            Log.e(TAG, "Routine occurrence backfill failed; opening DB without backfill.", t)
        }
    }

    private fun runOrThrow(db: SupportSQLiteDatabase) {
        val routines = loadRoutines(db)
        if (routines.isEmpty()) return
        val liveByRoutineDate = loadLiveTasksGrouped(db)

        liveByRoutineDate.forEach { (key, tasks) ->
            val routine = routines[key.routineId] ?: return@forEach
            val scheduled = key.date
            if (!appliesOn(routine, scheduled)) {
                tasks.forEach { id -> markUnkeyedDetached(db, id, routines) }
                return@forEach
            }
            if (tasks.size > 1) {
                tasks.forEach { id -> markUnkeyedDetached(db, id, routines) }
                return@forEach
            }
            val task = loadTask(db, tasks.first()) ?: return@forEach
            if (isUnambiguousMatch(task, routine, scheduled)) {
                if (task.occurrenceDate == scheduled.toString() && !task.detached) {
                    return@forEach
                }
                db.execSQL(
                    "UPDATE tasks SET routineOccurrenceDate = ?, routineInstanceDetached = 0 WHERE id = ?",
                    arrayOf(scheduled.toString(), task.id),
                )
            } else {
                if (!hasValidKey(task, routine, scheduled)) {
                    markUnkeyedDetached(db, task.id, routines)
                }
            }
        }

        val allAfterKeying = loadAllRoutineTasks(db)
        removeGeneratorDuplicates(db, routines, allAfterKeying)
        writeProvableSkips(db, routines, loadAllRoutineTasks(db))
    }

    private fun hasValidKey(
        task: TaskSnapshot,
        routine: RoutineRow,
        scheduled: LocalDate,
    ): Boolean {
        val occ = task.occurrenceDate ?: return false
        val occDate = parseDateOrNull(occ) ?: return false
        return occDate == scheduled && isUnambiguousMatch(task.copy(occurrenceDate = occ), routine, scheduled)
    }

    private fun removeGeneratorDuplicates(
        db: SupportSQLiteDatabase,
        routines: Map<String, RoutineRow>,
        tasks: List<TaskSnapshot>,
    ) {
        val live = tasks.filter { it.deletedAt == null }
        val bySchedule =
            live.groupBy { task ->
                val d = task.date?.let { parseDateOrNull(it) } ?: return@groupBy null
                task.routineId to d
            }
        bySchedule.forEach { (key, group) ->
            if (key == null) return@forEach
            val (routineId, _) = key
            val routine = routines[routineId] ?: return@forEach
            val done = group.any { it.status == STATUS_DONE }
            if (!done) return@forEach
            group.filter { it.status != STATUS_DONE }.forEach { open ->
                if (!open.detached && isUntouchedGenerator(open, routine)) {
                    softDeleteTask(db, open.id)
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
            val (routineId, _) = key
            val routine = routines[routineId] ?: return@forEach
            resolveDeletedVsLiveGenerator(db, group, routine, key.second, routines)
            val done = group.any { it.status == STATUS_DONE }
            if (!done) return@forEach
            group.filter { it.status != STATUS_DONE }.forEach { open ->
                if (!open.detached && isUntouchedGenerator(open, routine)) {
                    softDeleteTask(db, open.id)
                }
            }
        }
    }

    private fun resolveDeletedVsLiveGenerator(
        db: SupportSQLiteDatabase,
        liveOnOccurrence: List<TaskSnapshot>,
        routine: RoutineRow,
        occ: LocalDate,
        routines: Map<String, RoutineRow>,
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
            val generatorEcho =
                liveTask.createdAt > deletedAtMax &&
                    isUntouchedGenerator(liveTask, routine)
            if (generatorEcho) {
                softDeleteTask(db, liveTask.id)
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

    private fun isUntouchedGenerator(
        task: TaskSnapshot,
        routine: RoutineRow,
    ): Boolean =
        task.title == routine.title &&
            task.startLocalTime == routine.startLocalTime &&
            task.durationMin == routine.durationMin &&
            !task.detached

    private fun isUnambiguousMatch(
        task: TaskSnapshot,
        routine: RoutineRow,
        scheduled: LocalDate,
    ): Boolean {
        if (task.startLocalTime != routine.startLocalTime) return false
        if (task.durationMin != routine.durationMin) return false
        if (task.title != routine.title) return false
        if (task.detached) return false
        return appliesOn(routine, scheduled)
    }

    private fun markUnkeyedDetached(
        db: SupportSQLiteDatabase,
        taskId: String,
        routines: Map<String, RoutineRow>,
    ) {
        val task = loadTask(db, taskId) ?: return
        val routine = routines[task.routineId] ?: return
        val scheduled = task.date?.let { parseDateOrNull(it) }
        if (scheduled != null && hasValidKey(task, routine, scheduled)) {
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

    private fun softDeleteTask(
        db: SupportSQLiteDatabase,
        taskId: String,
    ) {
        val now = System.currentTimeMillis()
        db.execSQL(
            "UPDATE tasks SET deletedAt = ?, updatedAt = ? WHERE id = ?",
            arrayOf(now, now, taskId),
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

    private const val STATUS_DONE = "done"
}