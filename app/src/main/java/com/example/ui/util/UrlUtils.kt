package com.example.ui.util

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.Website

fun extractUrlFromText(rawText: String): String? {
    val trimmed = rawText.trim()
    if (trimmed.isEmpty()) return null

    // 1. Explicit scheme (http:// or https://) or www.
    val schemeOrWwwRegex = Regex("""(?i)\b(?:https?://|www\.)[^\s<>"'{}|\\^`]+""")
    val schemeMatch = schemeOrWwwRegex.find(trimmed)?.value
    if (schemeMatch != null) {
        val cleaned = schemeMatch.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '\'', '"')
        if (cleaned.length >= 4) return cleaned
    }

    // 2. Standalone domain: e.g., github.com, linear.app/login, sub.domain.co.uk
    val domainRegex = Regex("""(?i)\b[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\.[a-zA-Z]{2,10}(?:/[^\s<>"'{}|\\^`]*)?""")
    val domainMatch = domainRegex.find(trimmed)?.value
    if (domainMatch != null) {
        val cleaned = domainMatch.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '\'', '"')
        if (cleaned.length >= 4) return cleaned
    }

    return null
}

fun getClipboardUrl(context: Context): String? {
    return try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
        if (!clipboard.hasPrimaryClip()) return null
        val clip = clipboard.primaryClip ?: return null
        if (clip.itemCount == 0) return null

        for (i in 0 until clip.itemCount) {
            val item = clip.getItemAt(i) ?: continue

            // 1. Direct URI check
            val uri = item.uri
            if (uri != null) {
                val uriStr = uri.toString().trim()
                val extracted = extractUrlFromText(uriStr)
                if (!extracted.isNullOrBlank()) return extracted
            }

            // 2. Coerced text (handles text, HTML, and rich content safely)
            val text = item.coerceToText(context)?.toString()?.trim() ?: ""
            if (text.isNotEmpty()) {
                val extracted = extractUrlFromText(text)
                if (!extracted.isNullOrBlank()) return extracted
            }
        }
        null
    } catch (_: Exception) {
        null
    }
}

fun openWebsiteInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
    }
}

fun shareWebsiteLink(context: Context, website: Website) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, website.title)
            putExtra(Intent.EXTRA_TEXT, "${website.title} - ${website.url}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share link", Toast.LENGTH_SHORT).show()
    }
}

fun formatMiddleTruncatedDomain(domain: String, maxLength: Int = 32): String {
    if (domain.length <= maxLength) return domain
    if (maxLength <= 3) return domain.take(maxLength)
    val available = maxLength - 3
    val half = available / 2
    val prefixLen = available - half
    return "${domain.take(prefixLen)}...${domain.takeLast(half)}"
}

fun extractDomain(url: String): String = try {
    val target = if (url.contains("://")) url else "https://$url"
    Uri.parse(target).host?.removePrefix("www.") ?: url
} catch (_: Exception) {
    url
}

/**
 * Normalizes a URL for comparison to accurately detect duplicate bookmarks.
 * Ensures differentiation based on the full link (path, query, fragment) while standardizing:
 * - Scheme (http vs https equivalence)
 * - Host (lowercase, www. prefix removal)
 * - Path (case preserved, trailing slashes normalized)
 * - Query parameters (sorted key-value pairs)
 * - Fragment (anchors)
 */
fun normalizeUrlForComparison(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ""

    val withScheme = if (!trimmed.startsWith("http://", ignoreCase = true) &&
        !trimmed.startsWith("https://", ignoreCase = true)
    ) {
        "https://$trimmed"
    } else {
        trimmed
    }

    return try {
        val uri = Uri.parse(withScheme)
        val host = (uri.host ?: "").lowercase().removePrefix("www.")
        val port = if (uri.port != -1 && uri.port != 80 && uri.port != 443) ":${uri.port}" else ""
        var path = uri.path ?: ""
        if (path.length > 1 && path.endsWith("/")) {
            path = path.dropLast(1)
        } else if (path == "/") {
            path = ""
        }
        val query = if (!uri.query.isNullOrEmpty()) {
            try {
                val names = uri.queryParameterNames.sorted()
                if (names.isNotEmpty()) {
                    "?" + names.joinToString("&") { key ->
                        val values = uri.getQueryParameters(key)
                        if (values.isEmpty()) key
                        else values.joinToString("&") { v -> "$key=$v" }
                    }
                } else {
                    "?${uri.query}"
                }
            } catch (_: Exception) {
                "?${uri.query}"
            }
        } else {
            ""
        }
        val fragment = if (!uri.fragment.isNullOrEmpty()) "#${uri.fragment}" else ""
        if (host.isNotEmpty()) {
            "$host$port$path$query$fragment"
        } else {
            withScheme.removePrefix("https://").removePrefix("http://").removePrefix("www.").trimEnd('/')
        }
    } catch (_: Exception) {
        withScheme.removePrefix("https://").removePrefix("http://").removePrefix("www.").trimEnd('/')
    }
}
