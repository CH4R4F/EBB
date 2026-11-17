package app.ebbplanner.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
