package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DownloadEntity::class], version = 1, exportSchema = false)
abstract class TubeForgeDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: TubeForgeDatabase? = null

        fun getDatabase(context: Context): TubeForgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TubeForgeDatabase::class.java,
                    "tubeforge_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
