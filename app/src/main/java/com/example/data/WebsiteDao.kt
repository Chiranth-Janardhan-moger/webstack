package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WebsiteDao {
    @Query("SELECT * FROM saved_websites ORDER BY createdAt DESC")
    fun getAllWebsites(): Flow<List<Website>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebsite(website: Website): Long

    @Query("DELETE FROM saved_websites WHERE id = :id")
    suspend fun deleteWebsite(id: Long)

    @Query("UPDATE saved_websites SET category = :newCategory WHERE category = :oldCategory")
    suspend fun renameCategory(oldCategory: String, newCategory: String)

    @Query("UPDATE saved_websites SET category = 'Personal' WHERE category = :category")
    suspend fun resetCategoryToDefault(category: String)
}
