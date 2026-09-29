package com.example.data

import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val version: Int,
    val exportedAt: Long,
    val categories: List<String>,
    val websites: List<Website>
)

object BackupManager {
    const val CURRENT_VERSION = 1
    const val APP_NAME = "WebStack"

    /**
     * Serializes saved bookmarks and categories into formatted JSON string.
     */
    fun exportToJson(websites: List<Website>, categories: List<String>): String {
        val root = JSONObject()
        root.put("version", CURRENT_VERSION)
        root.put("appName", APP_NAME)
        root.put("exportedAt", System.currentTimeMillis())

        val categoriesArray = JSONArray()
        categories.forEach { categoriesArray.put(it) }
        root.put("categories", categoriesArray)

        val websitesArray = JSONArray()
        websites.forEach { site ->
            val siteObj = JSONObject()
            siteObj.put("url", site.url)
            siteObj.put("title", site.title)
            siteObj.put("domain", site.domain)
            siteObj.put("category", site.category)
            siteObj.put("createdAt", site.createdAt)
            websitesArray.put(siteObj)
        }
        root.put("websites", websitesArray)

        return root.toString(2)
    }

    /**
     * Parses a WebStack backup JSON string.
     * Returns Result.success(BackupData) or Result.failure on corruption / invalid schema.
     */
    fun parseBackup(jsonString: String): Result<BackupData> {
        return try {
            val root = JSONObject(jsonString)
            val version = root.optInt("version", 1)
            val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())

            val categoriesList = mutableListOf<String>()
            val categoriesArray = root.optJSONArray("categories")
            if (categoriesArray != null) {
                for (i in 0 until categoriesArray.length()) {
                    val cat = categoriesArray.optString(i, "").trim()
                    if (cat.isNotBlank() && !cat.equals("All", ignoreCase = true) && !categoriesList.contains(cat)) {
                        categoriesList.add(cat)
                    }
                }
            }

            val websitesList = mutableListOf<Website>()
            val websitesArray = root.optJSONArray("websites")
            if (websitesArray != null) {
                for (i in 0 until websitesArray.length()) {
                    val siteObj = websitesArray.optJSONObject(i) ?: continue
                    val url = siteObj.optString("url", "").trim()
                    if (url.isBlank()) continue

                    val title = siteObj.optString("title", "").ifBlank { url }
                    val domain = siteObj.optString("domain", "").ifBlank { com.example.ui.util.extractDomain(url) }
                    val category = siteObj.optString("category", "Personal").ifBlank { "Personal" }
                    val createdAt = siteObj.optLong("createdAt", System.currentTimeMillis())

                    websitesList.add(
                        Website(
                            id = 0,
                            url = url,
                            title = title,
                            domain = domain,
                            category = category,
                            createdAt = createdAt
                        )
                    )
                }
            }

            Result.success(
                BackupData(
                    version = version,
                    exportedAt = exportedAt,
                    categories = categoriesList,
                    websites = websitesList
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
