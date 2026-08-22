package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Website::class], version = 2, exportSchema = false)
abstract class WebsiteDatabase : RoomDatabase() {
    abstract fun websiteDao(): WebsiteDao

    companion object {
        @Volatile
        private var INSTANCE: WebsiteDatabase? = null

        fun getDatabase(context: Context): WebsiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WebsiteDatabase::class.java,
                    "webstack_database"
                )
                .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
