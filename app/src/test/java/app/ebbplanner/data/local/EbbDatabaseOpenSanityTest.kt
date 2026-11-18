package app.ebbplanner.data.local

import android.content.Context
import androidx.room.Room
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
class EbbDatabaseOpenSanityTest {
    private fun migrationBuilder(
        context: Context,
        name: String,
    ) =
        Room.databaseBuilder(context, EbbDatabase::class.java, name)
            .addMigrations(
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
            )
            .addCallback(EbbDatabaseCallback())

    @Test
    fun freshInstallOpensVersion11() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db =
            migrationBuilder(context, "ebb-fresh-11.db")
                .allowMainThreadQueries()
                .build()
        db.openHelper.writableDatabase.use { sqlite ->
            check(sqlite.version == 11)
            sqlite.query(
                "SELECT name FROM sqlite_master WHERE type = 'trigger' AND name = 'routine_occ_unique_ins'",
            ).use { check(it.moveToFirst()) }
        }
        db.taskDao()
        db.close()
    }

    @Test
    fun migration10To11DropsLegacyIndexAndOpensRoom() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val callback =
            object : SupportSQLiteOpenHelper.Callback(10) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    bootstrapVersion10(db)
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int,
                ) = Unit
            }
        val config =
            SupportSQLiteOpenHelper.Configuration
                .builder(context)
                .name("ebb-v10-legacy.db")
                .callback(callback)
                .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        helper.setWriteAheadLoggingEnabled(false)
        val sqlite = helper.writableDatabase
        sqlite.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_tasks_routine_occurrence_live
            ON tasks(sourceRoutineId, routineOccurrenceDate)
            WHERE deletedAt IS NULL AND routineOccurrenceDate IS NOT NULL
            """.trimIndent(),
        )
        MIGRATION_10_11.migrate(sqlite)
        sqlite.query(
            "SELECT name FROM sqlite_master WHERE type = 'index' AND name = 'index_tasks_routine_occurrence_live'",
        ).use { check(!it.moveToFirst()) }
        sqlite.close()
        val room =
            migrationBuilder(context, "ebb-v10-legacy.db")
                .allowMainThreadQueries()
                .build()
        room.taskDao()
        room.close()
    }

    private fun bootstrapVersion10(db: SupportSQLiteDatabase) {
        db.execSQL(openV10TasksDdl())
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS routines (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL DEFAULT '', repeatRule TEXT NOT NULL DEFAULT '', weeklyDays TEXT NOT NULL DEFAULT '', startLocalTime TEXT NOT NULL DEFAULT '', durationMin INTEGER NOT NULL DEFAULT 0, colorKey TEXT NOT NULL DEFAULT '', iconKey TEXT, enabled INTEGER NOT NULL DEFAULT 1, createdAt INTEGER NOT NULL DEFAULT 0, updatedAt INTEGER NOT NULL DEFAULT 0)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS routine_skips (routineId TEXT NOT NULL, occurrenceDate TEXT NOT NULL, PRIMARY KEY(routineId, occurrenceDate))",
        )
    }

    private fun openV10TasksDdl(): String =
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
            sourceRoutineId TEXT,
            routineOccurrenceDate TEXT,
            routineInstanceDetached INTEGER NOT NULL,
            breatherBufferMin INTEGER NOT NULL,
            createdAt INTEGER NOT NULL,
            updatedAt INTEGER NOT NULL,
            deletedAt INTEGER
        )
        """.trimIndent()
}