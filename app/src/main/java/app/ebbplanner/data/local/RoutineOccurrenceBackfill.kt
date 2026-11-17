package app.ebbplanner.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Conservative repair for routine instance keys and skip rows. Never soft-deletes tasks.
 */
internal object RoutineOccurrenceBackfill {
    private const val WINDOW_PAST = 13L
    private const val WINDOW_FUTURE = 13L

    fun run(db: SupportSQLiteDatabase) {
        val routines = loadRoutines(db)
        if (routines.isEmpty()) return
        val liveByRoutineDate = loadLiveTasksGrouped(db)
        val allRoutineTasks = loadAllRoutineTasks(db)

        liveByRoutineDate.forEach { (key, tasks) ->
            val routine = routines[key.routineId] ?: return@forEach
            val scheduled = key.date
            if (!appliesOn(routine, scheduled)) {
                tasks.forEach { id -> markUnkeyedDetached(db, id) }
                return@forEach
            }
            if (tasks.size > 1) {
                tasks.forEach { id -> markUnkeyedDetached(db, id) }
                return@forEach
            }
            val task = loadTask(db, tasks.first()) ?: return@forEach
            if (isUnambiguousMatch(task, routine, scheduled)) {
                db.execSQL(
                    "UPDATE tasks SET routineOccurrenceDate = ?, routineInstanceDetached = 0 WHERE id = ?",
                    arrayOf(scheduled.toString(), task.id),
                )
            } else {
                markUnkeyedDetached(db, task.id)
            }
        }

        allRoutineTasks.filter { it.deletedAt != null }.forEach { deleted ->
            val routine = routines[deleted.routineId] ?: return@forEach
            val occ =
                deleted.occurrenceDate?.let { parseDateOrNull(it) }
                    ?: deleted.date?.let { parseDateOrNull(it) }
                    ?: return@forEach
            if (!appliesOn(routine, occ)) return@forEach
            insertSkip(db, deleted.routineId, occ.toString())
            resolveDeletedVsLiveGenerator(db, deleted, routine, occ)
        }

        writeConservativeSkips(db, routines, allRoutineTasks)
    }

    private fun resolveDeletedVsLiveGenerator(
        db: SupportSQLiteDatabase,
        deleted: TaskSnapshot,
        routine: RoutineRow,
        occ: LocalDate,
    ) {
        val live =
            db.query(
                """
                SELECT id, title, startLocalTime, durationMin, routineInstanceDetached,
                       routineOccurrenceDate, createdAt, updatedAt, status
                FROM tasks
                WHERE deletedAt IS NULL AND sourceRoutineId = ? AND date = ?
                """.trimIndent(),
                arrayOf(deleted.routineId, occ.toString()),
            ).use { cursor ->
                val rows = mutableListOf<TaskSnapshot>()
                while (cursor.moveToNext()) {
                    rows += rowToSnapshot(cursor, deleted.routineId)
                }
                rows
            }
        if (live.isEmpty()) return
        live.forEach { liveTask ->
            val generatorEcho =
                liveTask.createdAt > (deleted.deletedAt ?: deleted.updatedAt) &&
                    !liveTask.detached &&
                    liveTask.title == routine.title &&
                    liveTask.startLocalTime == routine.startLocalTime &&
                    liveTask.durationMin == routine.durationMin &&
                    liveTask.status != "done"
            if (generatorEcho) {
                markUnkeyedDetached(db, liveTask.id)
                insertSkip(db, deleted.routineId, occ.toString())
            }
        }
    }

    private fun writeConservativeSkips(
        db: SupportSQLiteDatabase,
        routines: Map<String, RoutineRow>,
        allTasks: List<TaskSnapshot>,
    ) {
        val today = LocalDate.now()
        val start = today.minusDays(WINDOW_PAST)
        val end = today.plusDays(WINDOW_FUTURE)
        routines.values.filter { it.enabled }.forEach { routine ->
            var day = start
            while (!day.isAfter(end)) {
                if (!appliesOn(routine, day)) {
                    day = day.plusDays(1)
                    continue
                }
                val occ = day.toString()
                val onDay =
                    allTasks.filter { task ->
                        task.routineId == routine.id && taskMatchesDay(task, day)
                    }
                if (onDay.isNotEmpty()) {
                    insertSkip(db, routine.id, occ)
                } else {
                    val near =
                        allTasks.any { task ->
                            task.routineId == routine.id &&
                                task.deletedAt == null &&
                                (
                                    task.detached ||
                                        task.occurrenceDate.isNullOrBlank() ||
                                        task.status == "done"
                                ) &&
                                task.date?.let { parseDateOrNull(it) }?.let { d ->
                                    kotlin.math.abs(day.toEpochDay() - d.toEpochDay()) <= 1
                                } == true
                        }
                    if (near) insertSkip(db, routine.id, occ)
                }
                day = day.plusDays(1)
            }
        }
    }

    private fun taskMatchesDay(
        task: TaskSnapshot,
        day: LocalDate,
    ): Boolean {
        val d = task.date?.let { parseDateOrNull(it) } ?: return false
        if (d == day) return true
        val occ = task.occurrenceDate?.let { parseDateOrNull(it) }
        return occ == day
    }

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
    ) {
        db.execSQL(
            "UPDATE tasks SET routineOccurrenceDate = NULL, routineInstanceDetached = 1 WHERE id = ?",
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

    private fun loadAllRoutineTasks(db: SupportSQLiteDatabase): List<TaskSnapshot> {
        val list = mutableListOf<TaskSnapshot>()
        db.query(
            """
            SELECT id, sourceRoutineId, date, routineOccurrenceDate, title, startLocalTime,
                   durationMin, routineInstanceDetached, status, createdAt, updatedAt, deletedAt
            FROM tasks WHERE sourceRoutineId IS NOT NULL
            """.trimIndent(),
        ).use { c ->
            while (c.moveToNext()) {
                list += rowToSnapshot(c, c.getString(1))
            }
        }
        return list
    }

    private fun loadTask(
        db: SupportSQLiteDatabase,
        id: String,
    ): TaskSnapshot? =
        db.query(
            """
            SELECT id, sourceRoutineId, date, routineOccurrenceDate, title, startLocalTime,
                   durationMin, routineInstanceDetached, status, createdAt, updatedAt, deletedAt
            FROM tasks WHERE id = ?
            """.trimIndent(),
            arrayOf(id),
        ).use { c ->
            if (!c.moveToFirst()) null else rowToSnapshot(c, c.getString(1))
        }

    private fun rowToSnapshot(
        c: android.database.Cursor,
        routineId: String,
    ): TaskSnapshot =
        TaskSnapshot(
            id = c.getString(0),
            routineId = routineId,
            date = if (c.isNull(2)) null else c.getString(2),
            occurrenceDate = if (c.isNull(3)) null else c.getString(3),
            title = c.getString(4),
            startLocalTime = if (c.isNull(5)) null else c.getString(5),
            durationMin = c.getInt(6),
            detached = c.getInt(7) != 0,
            status = c.getString(8),
            createdAt = c.getLong(9),
            updatedAt = c.getLong(10),
            deletedAt = if (c.isNull(11)) null else c.getLong(11),
        )

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
}