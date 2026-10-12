package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Website::class], version = 3, exportSchema = false)
abstract class WebsiteDatabase : RoomDatabase() {
    abstract fun websiteDao(): WebsiteDao

    companion object {
        @Volatile
        private var INSTANCE: WebsiteDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `websites` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `url` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `dedupeKey` TEXT NOT NULL,
                        `sourcePath` TEXT,
                        `createdAtMs` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_websites_dedupeKey` ON `websites` (`dedupeKey`)")
                db.execSQL("""
                    INSERT OR IGNORE INTO `websites` (`id`, `url`, `title`, `category`, `dedupeKey`, `sourcePath`, `createdAtMs`)
                    SELECT `id`, `url`, `title`, `category`, `url`, NULL, `createdAt` FROM `saved_websites`
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): WebsiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WebsiteDatabase::class.java,
                    "webstack_database"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
