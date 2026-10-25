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
                    val category = siteObj.optString("category", "Personal").ifBlank { "Personal" }
                    val createdAt = siteObj.optLong("createdAt", System.currentTimeMillis())

                    websitesList.add(
                        Website(
                            id = 0,
                            url = url,
                            title = title,
                            category = category,
                            dedupeKey = com.example.bookmarks.BookmarkNormalizer.dedupeKey(url) ?: url,
                            createdAtMs = createdAt
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

    const val MAX_PLAIN_TEXT_IMPORT_LIMIT = 50_000

    /**
     * Serializes saved bookmarks into a plain text URL list, one URL per line.
     */
    fun exportToPlainText(websites: List<Website>): String {
        return websites.joinToString("\n") { it.url }
    }

    /**
     * Parses plain text URLs from an input stream.
     * Capped at MAX_PLAIN_TEXT_IMPORT_LIMIT to prevent mobile OOM/ANR.
     */
    fun parsePlainTextUrls(inputStream: java.io.InputStream): List<Website> {
        val websites = mutableListOf<Website>()
        val seenDedupeKeys = HashSet<String>()
        val now = System.currentTimeMillis()

        inputStream.bufferedReader().useLines { lines ->
            for (rawLine in lines) {
                if (websites.size >= MAX_PLAIN_TEXT_IMPORT_LIMIT) break
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue

                val urlCandidate = extractUrlFromLine(line) ?: continue
                val dedupe = com.example.bookmarks.BookmarkNormalizer.dedupeKey(urlCandidate) ?: continue
                if (!seenDedupeKeys.add(dedupe)) continue

                val domain = com.example.ui.util.extractDomain(urlCandidate)
                val title = if (domain.isNotBlank()) domain else urlCandidate

                websites.add(
                    Website(
                        id = 0,
                        url = urlCandidate,
                        title = title,
                        category = "Personal",
                        dedupeKey = dedupe,
                        createdAtMs = now
                    )
                )
            }
        }
        return websites
    }

    private fun extractUrlFromLine(line: String): String? {
        val tokens = line.split("\\s+".toRegex())
        for (token in tokens) {
            val trimmed = token.trim().trim('(', ')', '[', ']', '<', '>', ',', ';')
            if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                return trimmed
            }
        }
        if (tokens.size == 1 && line.contains(".") && !line.contains(" ") && !line.contains("@")) {
            return "https://$line"
        }
        return null
    }
}
