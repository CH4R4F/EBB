package app.ebbplanner.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE tasks ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 1",
            )
        }
    }

val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS subtasks (
                    id TEXT NOT NULL PRIMARY KEY,
                    taskId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    durationMin INTEGER,
                    sortOrder INTEGER NOT NULL,
                    done INTEGER NOT NULL,
                    FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_taskId ON subtasks(taskId)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS routines (
                    id TEXT NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    repeatRule TEXT NOT NULL,
                    weeklyDays TEXT NOT NULL,
                    startLocalTime TEXT NOT NULL,
                    durationMin INTEGER NOT NULL,
                    colorKey TEXT NOT NULL,
                    iconKey TEXT,
                    enabled INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN sourceRoutineId TEXT")
        }
    }

val MIGRATION_5_6 =
    object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN routineOccurrenceDate TEXT")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS routine_skips (
                    routineId TEXT NOT NULL,
                    occurrenceDate TEXT NOT NULL,
                    PRIMARY KEY(routineId, occurrenceDate)
                )
                """.trimIndent(),
            )
        }
    }

val MIGRATION_6_7 =
    object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE tasks ADD COLUMN routineInstanceDetached INTEGER NOT NULL DEFAULT 0",
            )
            db.execSQL(
                "ALTER TABLE tasks ADD COLUMN breatherBufferMin INTEGER NOT NULL DEFAULT 0",
            )
            RoutineOccurrenceRepair.runDuringMigration(db, recordPendingOnFailure = false)
        }
    }

val MIGRATION_7_8 =
    object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            RoutineOccurrenceRepair.runDuringMigration(db, recordPendingOnFailure = false)
        }
    }

val MIGRATION_8_9 =
    object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            RoutineOccurrenceRepair.ensureMetaTable(db)
            RoutineOccurrenceRepair.runDuringMigration(db, recordPendingOnFailure = true)
        }
    }

val MIGRATION_9_10 =
    object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            RoutineOccurrenceRepair.deduplicateLiveOccurrenceKeysForIndex(db)
        }
    }

val MIGRATION_10_11 =
    object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            RoutineOccurrenceTriggers.dropPartialUniqueIndexIfExists(db)
            RoutineOccurrenceRepair.deduplicateLiveOccurrenceKeysForIndex(db)
            RoutineOccurrenceRepair.ensureMetaTable(db)
            RoutineOccurrenceTriggers.ensureInstalled(db)
        }
    }
