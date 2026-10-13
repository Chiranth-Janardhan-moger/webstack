package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@Database(entities = [Website::class], version = 3, exportSchema = false)
abstract class WebsiteDatabase : RoomDatabase() {
    abstract fun websiteDao(): WebsiteDao

    companion object {
        @Volatile
        private var INSTANCE: WebsiteDatabase? = null

        private val FROZEN_TRACKING = Regex("^(utm_.*|fbclid|gclid|mc_eid|igshid)$", RegexOption.IGNORE_CASE)

        private fun frozenDedupeKey(raw: String): String {
            val candidate = raw.trim()
            val httpUrl = candidate.toHttpUrlOrNull()
                ?: if (!candidate.contains("://")) ("https://$candidate").toHttpUrlOrNull() else null
            if (httpUrl == null) return candidate
            val b = httpUrl.newBuilder().fragment(null)
            httpUrl.queryParameterNames.filter { FROZEN_TRACKING.matches(it) }.forEach { b.removeAllQueryParameters(it) }
            val u = b.build()
            val host = u.host.removePrefix("www.")
            val path = if (u.encodedPath.length > 1) u.encodedPath.trimEnd('/').ifEmpty { "/" } else u.encodedPath
            val port = if (u.port != okhttp3.HttpUrl.defaultPort(u.scheme)) ":${u.port}" else ""
            val query = u.encodedQuery?.let { "?$it" } ?: ""
            return "${u.scheme}://$host$port$path$query"
        }

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

                val checkCursor = db.query("SELECT 1 FROM sqlite_master WHERE type='table' AND name='saved_websites'")
                val hasSaved = checkCursor.moveToNext()
                checkCursor.close()
                if (!hasSaved) return

                val savedCursor = db.query("SELECT `id`, `url`, `title`, `category`, `createdAt` FROM `saved_websites` ORDER BY `id` ASC")
                var droppedCount = 0L
                val seenKeys = HashSet<String>()
                val insertStmt = db.compileStatement("""
                    INSERT INTO `websites` (`id`, `url`, `title`, `category`, `dedupeKey`, `sourcePath`, `createdAtMs`)
                    VALUES (?, ?, ?, ?, ?, NULL, ?)
                """.trimIndent())

                try {
                    while (savedCursor.moveToNext()) {
                        val id = savedCursor.getLong(0)
                        val url = savedCursor.getString(1)
                        val title = savedCursor.getString(2)
                        val category = savedCursor.getString(3)
                        val createdAt = savedCursor.getLong(4)

                        val key = frozenDedupeKey(url)
                        if (!seenKeys.add(key)) {
                            // Collision: keep lowest id, drop duplicate
                            droppedCount++
                            continue
                        }

                        insertStmt.bindLong(1, id)
                        insertStmt.bindString(2, url)
                        insertStmt.bindString(3, title)
                        insertStmt.bindString(4, category)
                        insertStmt.bindString(5, key)
                        insertStmt.bindLong(6, createdAt)
                        insertStmt.executeInsert()
                        insertStmt.clearBindings()
                    }
                } finally {
                    savedCursor.close()
                    insertStmt.close()
                }

                // Invariant assertion: websites count + dropped = saved_websites count
                val savedCountCursor = db.query("SELECT COUNT(*) FROM `saved_websites`")
                var savedCount = 0L
                if (savedCountCursor.moveToNext()) {
                    savedCount = savedCountCursor.getLong(0)
                }
                savedCountCursor.close()

                val websitesCountCursor = db.query("SELECT COUNT(*) FROM `websites`")
                var websitesCount = 0L
                if (websitesCountCursor.moveToNext()) {
                    websitesCount = websitesCountCursor.getLong(0)
                }
                websitesCountCursor.close()

                if (websitesCount + droppedCount != savedCount) {
                    throw IllegalStateException("Migration invariant failed: websites ($websitesCount) + dropped ($droppedCount) != saved_websites ($savedCount)")
                }
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
