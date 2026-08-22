package com.example.data

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

    suspend fun insert(website: Website) {
        websiteDao.insertWebsite(website)
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

        val domain = extractDomain(url)
        var title = ""

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
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
            title = domain.split(".")[0].replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        val faviconUrl = "https://www.google.com/s2/favicons?sz=128&domain=$domain"
        val category = if (!customCategory.isNullOrBlank() && customCategory != "All") {
            customCategory
        } else {
            inferCategory(domain, title, url)
        }

        val website = Website(
            url = url,
            title = title,
            domain = domain,
            faviconUrl = faviconUrl,
            category = category
        )

        val id = websiteDao.insertWebsite(website)
        Result.success(website.copy(id = id))
    }

    private fun inferCategory(domain: String, title: String, url: String): String {
        val combined = "$domain $title $url".lowercase()
        return when {
            combined.contains("figma") || combined.contains("framer") || combined.contains("dribbble") ||
            combined.contains("behance") || combined.contains("unsplash") || combined.contains("spline") ||
            combined.contains("design") || combined.contains("font") || combined.contains("color") ||
            combined.contains("icon") || combined.contains("awwwards") -> "Design"

            combined.contains("github") || combined.contains("gitlab") || combined.contains("dev") ||
            combined.contains("vercel") || combined.contains("replit") || combined.contains("stack") ||
            combined.contains("linear") || combined.contains("notion") || combined.contains("chatgpt") ||
            combined.contains("openai") || combined.contains("claude") || combined.contains("tool") ||
            combined.contains("api") || combined.contains("studio") -> "Tools"

            combined.contains("medium") || combined.contains("substack") || combined.contains("news") ||
            combined.contains("blog") || combined.contains("wiki") || combined.contains("article") ||
            combined.contains("read") || combined.contains("book") || combined.contains("paper") -> "Reading"

            combined.contains("work") || combined.contains("slack") || combined.contains("jira") ||
            combined.contains("asana") || combined.contains("monday") || combined.contains("trello") ||
            combined.contains("zoom") || combined.contains("meet") || combined.contains("calendar") ||
            combined.contains("mail") || combined.contains("office") -> "Work"

            combined.contains("youtube") || combined.contains("twitter") || combined.contains("x.com") ||
            combined.contains("reddit") || combined.contains("instagram") || combined.contains("linkedin") ||
            combined.contains("spotify") || combined.contains("cubestar") || combined.contains("music") ||
            combined.contains("personal") || combined.contains("portfolio") -> "Personal"

            else -> "Personal"
        }
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val host = uri.host
            if (host != null) {
                if (host.startsWith("www.")) host.substring(4) else host
            } else {
                var cleaned = url.replace("https://", "").replace("http://", "")
                val slashIdx = cleaned.indexOf('/')
                if (slashIdx != -1) {
                    cleaned = cleaned.substring(0, slashIdx)
                }
                if (cleaned.startsWith("www.")) cleaned.substring(4) else cleaned
            }
        } catch (e: Exception) {
            var cleaned = url.replace("https://", "").replace("http://", "")
            val slashIdx = cleaned.indexOf('/')
            if (slashIdx != -1) {
                cleaned = cleaned.substring(0, slashIdx)
            }
            if (cleaned.startsWith("www.")) cleaned.substring(4) else cleaned
        }
    }

    private fun extractTitleFromHtml(html: String): String? {
        return try {
            val titlePattern = "<title[^>]*>(.*?)</title>".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            val matchResult = titlePattern.find(html)
            val rawTitle = matchResult?.groups?.get(1)?.value?.trim()
            rawTitle?.let { decodeHtmlEntities(it) }
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeHtmlEntities(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&#x27;", "'")
            .replace("&#x2F;", "/")
            .replace("&nbsp;", " ")
            .trim()
    }
}
