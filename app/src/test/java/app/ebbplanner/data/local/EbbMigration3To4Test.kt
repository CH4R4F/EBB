package app.ebbplanner.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EbbMigration3To4Test {
    @Test
    fun migrate3To4_addsRoutinesAndSubtasks() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val callback =
            object : SupportSQLiteOpenHelper.Callback(3) {
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
                            reminderEnabled INTEGER NOT NULL,
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
                    MIGRATION_3_4.migrate(db)
                }
            }
        val config =
            SupportSQLiteOpenHelper.Configuration
                .builder(context)
                .name("ebb-migration-3-4.db")
                .callback(callback)
                .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase
        MIGRATION_3_4.migrate(db)
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='routines'").use { cursor ->
            check(cursor.moveToFirst())
        }
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='subtasks'").use { cursor ->
            check(cursor.moveToFirst())
        }
        db.close()
    }
}
