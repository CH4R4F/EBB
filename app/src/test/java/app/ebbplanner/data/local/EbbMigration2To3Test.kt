package app.ebbplanner.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EbbMigration2To3Test {
    @Test
    fun migrate2To3_preservesTasksAndAddsReminderEnabled() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val dbName = "ebb-migration-2-3-${System.nanoTime()}"
        val callback =
            object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE tasks (
                            id TEXT NOT NULL PRIMARY KEY,
                            title TEXT NOT NULL,
                            notes TEXT NOT NULL,
                            status TEXT NOT NULL,
                            placement TEXT NOT NULL,
                            date TEXT,
                            startLocalTime TEXT,
                            zoneId TEXT NOT NULL,
                            durationMin INTEGER NOT NULL,
                            colorKey TEXT NOT NULL,
                            iconKey TEXT,
                            energy TEXT,
                            pinned INTEGER NOT NULL,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL,
                            deletedAt INTEGER
                        )
                        """.trimIndent(),
                    )
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int,
                ) {
                    MIGRATION_2_3.migrate(db)
                }
            }
        val config =
            SupportSQLiteOpenHelper.Configuration
                .builder(context)
                .name(dbName)
                .callback(callback)
                .build()
        val openHelper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = openHelper.writableDatabase
        db.execSQL(
            """
            INSERT INTO tasks (
                id, title, notes, status, placement, date, startLocalTime, zoneId,
                durationMin, colorKey, iconKey, energy, pinned, createdAt, updatedAt, deletedAt
            ) VALUES (
                'task-1', 'Keep me', '', 'open', 'scheduled', '2025-11-17', '09:00', 'UTC',
                30, 'slate', NULL, NULL, 0, 1, 1, NULL
            )
            """.trimIndent(),
        )
        db.close()

        val migrated =
            openHelper.writableDatabase.also { it.version = 2 }
        MIGRATION_2_3.migrate(migrated)
        migrated.query("SELECT title, reminderEnabled FROM tasks WHERE id = 'task-1'").use { cursor ->
            check(cursor.moveToFirst())
            assertEquals("Keep me", cursor.getString(0))
            assertEquals(1, cursor.getInt(1))
        }
        migrated.close()
    }
}
