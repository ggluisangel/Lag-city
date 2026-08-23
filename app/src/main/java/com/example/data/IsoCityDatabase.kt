package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CityEntity::class], version = 1, exportSchema = false)
abstract class IsoCityDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    companion object {
        @Volatile
        private var INSTANCE: IsoCityDatabase? = null

        fun getDatabase(context: Context): IsoCityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IsoCityDatabase::class.java,
                    "isocity_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
