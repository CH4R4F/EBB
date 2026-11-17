package app.ebbplanner.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Data repair for upgrades from DB v6 where [routineOccurrenceDate] was added but not backfilled.
 */
internal object RoutineOccurrenceBackfill {
    fun run(db: SupportSQLiteDatabase) {
        val routines = loadRoutines(db)
        val tasks = loadRoutineTasks(db)
        tasks.forEach { task ->
            val routine = routines[task.routineId] ?: return@forEach
            val scheduled = task.date?.let { parseDate(it) } ?: return@forEach
            val occurrence =
                when {
                    task.occurrence != null -> {
                        val occ = parseDate(task.occurrence)
                        when {
                            appliesOn(routine, occ) -> occ
                            appliesOn(routine, scheduled) -> scheduled
                            else -> previousApplicableDay(routine, scheduled)
                        }
                    }
                    appliesOn(routine, scheduled) -> scheduled
                    else -> previousApplicableDay(routine, scheduled)
                }
            db.execSQL(
                "UPDATE tasks SET routineOccurrenceDate = ? WHERE id = ?",
                arrayOf(occurrence.toString(), task.id),
            )
        }
        dedupeLiveOccurrences(db)
        insertSkipsForDeleted(db)
        insertSkipsForMissingLiveSlots(db, routines)
    }

    private fun dedupeLiveOccurrences(db: SupportSQLiteDatabase) {
        db.query(
            """
            SELECT sourceRoutineId, routineOccurrenceDate, GROUP_CONCAT(id) AS ids
            FROM tasks
            WHERE deletedAt IS NULL AND sourceRoutineId IS NOT NULL AND routineOccurrenceDate IS NOT NULL
            GROUP BY sourceRoutineId, routineOccurrenceDate
            HAVING COUNT(*) > 1
            """.trimIndent(),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val ids = cursor.getString(2).split(',')
                val keep = ids.first()
                ids.drop(1).forEach { dup ->
                    val now = System.currentTimeMillis()
                    db.execSQL(
                        "UPDATE tasks SET deletedAt = ?, updatedAt = ? WHERE id = ?",
                        arrayOf(now, now, dup),
                    )
                }
            }
        }
    }

    private fun insertSkipsForDeleted(db: SupportSQLiteDatabase) {
        db.query(
            """
            SELECT DISTINCT sourceRoutineId, COALESCE(routineOccurrenceDate, date) AS occ
            FROM tasks
            WHERE deletedAt IS NOT NULL AND sourceRoutineId IS NOT NULL
            AND COALESCE(routineOccurrenceDate, date) IS NOT NULL
            """.trimIndent(),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val routineId = cursor.getString(0)
                val occ = cursor.getString(1)
                db.execSQL(
                    "INSERT OR IGNORE INTO routine_skips (routineId, occurrenceDate) VALUES (?, ?)",
                    arrayOf(routineId, occ),
                )
            }
        }
    }

    private fun insertSkipsForMissingLiveSlots(
        db: SupportSQLiteDatabase,
        routines: Map<String, RoutineRow>,
    ) {
        val today = LocalDate.now()
        val start = today.minusDays(13)
        val end = today.plusDays(13)
        routines.values.filter { it.enabled }.forEach { routine ->
            var day = start
            while (!day.isAfter(end)) {
                if (appliesOn(routine, day)) {
                    val occ = day.toString()
                    val live =
                        db.query(
                            """
                            SELECT id FROM tasks
                            WHERE deletedAt IS NULL AND sourceRoutineId = ? AND routineOccurrenceDate = ?
                            LIMIT 1
                            """.trimIndent(),
                            arrayOf(routine.id, occ),
                        ).use { it.moveToFirst() }
                    if (!live) {
                        val hadDeleted =
                            db.query(
                                """
                                SELECT id FROM tasks
                                WHERE deletedAt IS NOT NULL AND sourceRoutineId = ?
                                AND COALESCE(routineOccurrenceDate, date) = ?
                                LIMIT 1
                                """.trimIndent(),
                                arrayOf(routine.id, occ),
                            ).use { it.moveToFirst() }
                        if (hadDeleted) {
                            db.execSQL(
                                "INSERT OR IGNORE INTO routine_skips (routineId, occurrenceDate) VALUES (?, ?)",
                                arrayOf(routine.id, occ),
                            )
                        }
                    }
                }
                day = day.plusDays(1)
            }
        }
    }

    private data class RoutineRow(
        val id: String,
        val repeatRule: String,
        val weeklyDays: String,
        val enabled: Boolean,
    )

    private data class TaskRow(
        val id: String,
        val routineId: String,
        val date: String?,
        val occurrence: String?,
    )

    private fun loadRoutines(db: SupportSQLiteDatabase): Map<String, RoutineRow> {
        val map = mutableMapOf<String, RoutineRow>()
        db.query("SELECT id, repeatRule, weeklyDays, enabled FROM routines").use { c ->
            while (c.moveToNext()) {
                map[c.getString(0)] =
                    RoutineRow(
                        id = c.getString(0),
                        repeatRule = c.getString(1),
                        weeklyDays = c.getString(2),
                        enabled = c.getInt(3) != 0,
                    )
            }
        }
        return map
    }

    private fun loadRoutineTasks(db: SupportSQLiteDatabase): List<TaskRow> {
        val list = mutableListOf<TaskRow>()
        db.query(
            """
            SELECT id, sourceRoutineId, date, routineOccurrenceDate
            FROM tasks
            WHERE sourceRoutineId IS NOT NULL AND deletedAt IS NULL
            """.trimIndent(),
        ).use { c ->
            while (c.moveToNext()) {
                list +=
                    TaskRow(
                        id = c.getString(0),
                        routineId = c.getString(1),
                        date = if (c.isNull(2)) null else c.getString(2),
                        occurrence = if (c.isNull(3)) null else c.getString(3),
                    )
            }
        }
        return list
    }

    private fun parseDate(raw: String): LocalDate = LocalDate.parse(raw)

    private fun previousApplicableDay(
        routine: RoutineRow,
        from: LocalDate,
    ): LocalDate {
        var day = from
        repeat(366) {
            if (appliesOn(routine, day)) return day
            day = day.minusDays(1)
        }
        return from
    }

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