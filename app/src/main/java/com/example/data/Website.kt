package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "websites",
    indices = [Index(value = ["dedupeKey"], unique = true)]
)
data class Website(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val category: String,
    val dedupeKey: String,
    val sourcePath: String? = null,
    val createdAtMs: Long = System.currentTimeMillis()
) {
    val domain: String get() = com.example.ui.util.extractDomain(url)
    val createdAt: Long get() = createdAtMs
    val faviconUrl: String get() = ""
}
