package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_websites")
data class Website(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val domain: String,
    val faviconUrl: String,
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis()
)
