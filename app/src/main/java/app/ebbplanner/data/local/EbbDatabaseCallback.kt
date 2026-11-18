package app.ebbplanner.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class EbbDatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        RoutineOccurrenceRepair.ensureMetaTable(db)
        RoutineOccurrenceTriggers.reinstall(db)
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        RoutineOccurrenceTriggers.reinstall(db)
    }
}