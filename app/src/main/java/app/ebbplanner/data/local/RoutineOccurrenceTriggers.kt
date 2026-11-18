package app.ebbplanner.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

internal object RoutineOccurrenceTriggers {
    private const val INDEX_NAME = "index_tasks_routine_occurrence_live"
    private const val TRIGGER_INSERT = "routine_occ_unique_ins"
    private const val TRIGGER_UPDATE = "routine_occ_unique_upd"

    fun dropPartialUniqueIndexIfExists(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX IF EXISTS $INDEX_NAME")
    }

    fun ensureInstalled(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS $TRIGGER_INSERT
            BEFORE INSERT ON tasks
            WHEN NEW.deletedAt IS NULL
                AND NEW.routineOccurrenceDate IS NOT NULL
                AND NEW.sourceRoutineId IS NOT NULL
                AND EXISTS (
                    SELECT 1 FROM tasks
                    WHERE sourceRoutineId = NEW.sourceRoutineId
                      AND routineOccurrenceDate = NEW.routineOccurrenceDate
                      AND deletedAt IS NULL
                )
            BEGIN
                SELECT RAISE(IGNORE);
            END
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS $TRIGGER_UPDATE
            BEFORE UPDATE OF routineOccurrenceDate, deletedAt, sourceRoutineId ON tasks
            WHEN NEW.deletedAt IS NULL
                AND NEW.routineOccurrenceDate IS NOT NULL
                AND NEW.sourceRoutineId IS NOT NULL
                AND EXISTS (
                    SELECT 1 FROM tasks
                    WHERE sourceRoutineId = NEW.sourceRoutineId
                      AND routineOccurrenceDate = NEW.routineOccurrenceDate
                      AND deletedAt IS NULL
                      AND id != NEW.id
                )
            BEGIN
                SELECT RAISE(IGNORE);
            END
            """.trimIndent(),
        )
    }
}