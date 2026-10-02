package com.example.data

import com.example.ui.util.extractDomain
import com.example.ui.util.normalizeUrlForComparison
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WebsiteRepository(private val websiteDao: WebsiteDao) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    val allWebsites = websiteDao.getAllWebsites()

    suspend fun getAllWebsitesList(): List<Website> = websiteDao.getAllWebsitesList()

    suspend fun insertWebsites(websites: List<Website>): List<Long> = websiteDao.insertWebsites(websites)

    suspend fun findDuplicate(inputUrl: String, excludeId: Long? = null): Website? {
        val target = normalizeUrlForComparison(inputUrl)
        if (target.isBlank()) return null
        return getAllWebsitesList().firstOrNull {
            it.id != excludeId && normalizeUrlForComparison(it.url) == target
        }
    }

    suspend fun update(website: Website) {
        websiteDao.insertWebsite(website)
    }

    suspend fun delete(id: Long) {
        websiteDao.deleteWebsite(id)
    }

    suspend fun renameCategory(oldCategory: String, newCategory: String) {
        websiteDao.renameCategory(oldCategory, newCategory)
    }

    suspend fun resetCategory(category: String) {
        websiteDao.resetCategoryToDefault(category)
    }

    suspend fun fetchAndSave(inputUrl: String, customCategory: String? = null): Result<Website> = withContext(Dispatchers.IO) {
        var url = inputUrl.trim()
        if (url.isBlank()) {
            return@withContext Result.failure(Exception("URL cannot be empty"))
        }

        // Validate basic structure, if not starting with protocol, default to https
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }

        val duplicate = findDuplicate(url)
        if (duplicate != null) {
            val titleDisplay = duplicate.title.ifBlank { duplicate.domain }
            return@withContext Result.failure(Exception("Link already in your stack: \"$titleDisplay\""))
        }

        val domain = extractDomain(url)
        var title = ""

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: ""
                    title = extractTitleFromHtml(html) ?: ""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback title generation if fetch failed or returned empty
        if (title.isBlank()) {
            title = domain.substringBefore(".").replaceFirstChar { it.uppercaseChar() }
        }

        val category = if (!customCategory.isNullOrBlank() && customCategory != "All") {
            customCategory
        } else {
            inferCategory(domain, title, url)
        }

        val website = Website(
            url = url,
            title = title,
            domain = domain,
            category = category
        )

        val id = websiteDao.insertWebsite(website)
        Result.success(website.copy(id = id))
    }

    private fun inferCategory(domain: String, title: String, url: String): String {
        val combined = "$domain $title $url".lowercase()
        return CATEGORY_KEYWORDS.entries.firstOrNull { (_, keywords) ->
            keywords.any { combined.contains(it) }
        }?.key ?: "Personal"
    }

    private fun extractTitleFromHtml(html: String): String? = runCatching {
        Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .find(html)?.groups?.get(1)?.value?.trim()
            ?.let { android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim() }
    }.getOrNull()

    companion object {
        private val CATEGORY_KEYWORDS = mapOf(
            "Design" to listOf("figma", "framer", "dribbble", "behance", "unsplash", "spline", "design", "font", "color", "icon", "awwwards"),
            "Tools" to listOf("github", "gitlab", "dev", "vercel", "replit", "stack", "linear", "notion", "chatgpt", "openai", "claude", "tool", "api", "studio"),
            "Reading" to listOf("medium", "substack", "news", "blog", "wiki", "article", "read", "book", "paper"),
            "Work" to listOf("work", "slack", "jira", "asana", "monday", "trello", "zoom", "meet", "calendar", "mail", "office"),
            "Personal" to listOf("youtube", "twitter", "x.com", "reddit", "instagram", "linkedin", "spotify", "cubestar", "music", "personal", "portfolio")
        )
    }
}
