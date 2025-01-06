package com.equationl.wxsteplog.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.equationl.wxsteplog.db.DbUtil.DATABASE_FILE_NAME

@Database(
    entities = [
        WxStepTable::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
//@TypeConverters(DBConverters::class)
abstract class WxStepDB : RoomDatabase() {
    companion object {
        fun create(context: Context, useInMemory: Boolean = false): WxStepDB {
            val databaseBuilder = if (useInMemory) {
                Room.inMemoryDatabaseBuilder(context, WxStepDB::class.java)
            } else {
                Room.databaseBuilder(context, WxStepDB::class.java, DATABASE_FILE_NAME)
            }
            return databaseBuilder
                //.fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun manHoursDB(): WxStepDao
}