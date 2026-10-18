package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.bookmarks.ExportDao
import kotlinx.coroutines.flow.Flow

@Dao
interface WebsiteDao : ExportDao {
    @Query("SELECT * FROM websites ORDER BY createdAtMs DESC")
    fun getAllWebsites(): Flow<List<Website>>

    @Query("SELECT * FROM websites ORDER BY createdAtMs DESC")
    suspend fun getAllWebsitesList(): List<Website>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebsite(website: Website): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<Website>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebsites(websites: List<Website>): List<Long>

    @Query("DELETE FROM websites WHERE id = :id")
    suspend fun deleteWebsite(id: Long)

    @Query("DELETE FROM websites WHERE id IN (:ids)")
    suspend fun deleteWebsitesChunk(ids: List<Long>): Int

    @Transaction
    suspend fun deleteWebsites(ids: List<Long>) {
        if (ids.isEmpty()) return
        ids.chunked(500).forEach { chunk ->
            deleteWebsitesChunk(chunk)
        }
    }

    @Query("UPDATE websites SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String)

    @Query("UPDATE websites SET category = :newCategory WHERE category = :oldCategory")
    suspend fun renameCategory(oldCategory: String, newCategory: String)

    @Query("UPDATE websites SET category = 'Personal' WHERE category = :category")
    suspend fun resetCategoryToDefault(category: String)

    @Query("SELECT DISTINCT category FROM websites ORDER BY category ASC")
    override suspend fun categoriesSorted(): List<String>

    @Query("SELECT * FROM websites WHERE category = :category ORDER BY createdAtMs DESC")
    override suspend fun byCategory(category: String): List<Website>
}
