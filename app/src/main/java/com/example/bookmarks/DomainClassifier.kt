package com.example.bookmarks

import android.content.Context
import com.google.common.net.InternetDomainName
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Locale

enum class CategorySource {
    FOLDER,
    DOMAIN_RULE,
    CLUSTER,
    UNCATEGORIZED
}

data class ClassifiedBookmark(
    val url: String,
    val title: String,
    val dedupeKey: String,
    val folderPath: List<String>,
    val createdAtMs: Long,
    val category: String,
    val source: CategorySource,
    val sourcePath: String?,
    val host: String,
    val registrableDomain: String
)

class DomainClassifier(
    val taxonomyMap: Map<String, String> = emptyMap()
) {
    companion object {
        fun registrableDomain(host: String): String = try {
            val d = InternetDomainName.from(host)
            if (d.isUnderPublicSuffix) d.topPrivateDomain().toString() else host
        } catch (_: IllegalArgumentException) {
            host
        }

        fun loadTaxonomyFromJson(jsonString: String): Map<String, String> {
            val map = HashMap<String, String>()
            val json = JSONObject(jsonString)
            for (key in json.keys()) {
                map[key.lowercase(Locale.ROOT)] = json.getString(key)
            }
            return map
        }

        fun loadFromAssets(context: Context): DomainClassifier {
            val inputStream = try {
                context.assets.open("domain_taxonomy.json")
            } catch (_: Exception) {
                DomainClassifier::class.java.classLoader?.getResourceAsStream("domain_taxonomy.json")
                    ?: DomainClassifier::class.java.getResourceAsStream("/domain_taxonomy.json")
            }
            val jsonStr = inputStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: ""
            val taxonomy = if (jsonStr.isNotBlank()) loadTaxonomyFromJson(jsonStr) else emptyMap()
            return DomainClassifier(taxonomy)
        }
    }

    /**
     * Classifies a list of parsed bookmarks according to the 4-step precedence:
     * 1) Clean user folder (deepest non-noise)
     * 2) Domain taxonomy (host suffix walk or amazon/ebay registrable label)
     * 3) Domain-frequency cluster (>= 3 unclassified links on same registrable domain)
     * 4) "Uncategorized"
     */
    fun classifyAll(parsed: List<ParsedBookmark>): List<ClassifiedBookmark> {
        // Collect all folders to resolve canonical display names
        val allFolders = parsed.flatMap { it.folderPath }
        val canonicalFolderMap = FolderCleaner.canonicalDisplayNames(allFolders)

        data class IntermediateBookmark(
            val parsed: ParsedBookmark,
            val host: String,
            val regDomain: String,
            val category: String?,
            val source: CategorySource?,
            val sourcePath: String?
        )

        val intermediates = parsed.map { b ->
            val host = b.url.toHttpUrlOrNull()?.host?.lowercase(Locale.ROOT) ?: ""
            val regDomain = if (host.isNotEmpty()) registrableDomain(host) else ""

            // 1. Walk folderPath from leaf upward; first folder where cleanFolderName != null wins
            var folderWinner: String? = null
            for (f in b.folderPath.asReversed()) {
                val cleaned = FolderCleaner.cleanFolderName(f)
                if (cleaned != null) {
                    val key = cleaned.lowercase(Locale.ROOT)
                    folderWinner = canonicalFolderMap[key] ?: cleaned
                    break
                }
            }

            if (folderWinner != null) {
                IntermediateBookmark(
                    parsed = b,
                    host = host,
                    regDomain = regDomain,
                    category = folderWinner,
                    source = CategorySource.FOLDER,
                    sourcePath = b.folderPath.joinToString("/")
                )
            } else {
                // 2. Domain taxonomy: match by host suffix, walking labels from most specific to least
                val domainWinner = matchDomainTaxonomy(host, regDomain)
                if (domainWinner != null) {
                    IntermediateBookmark(
                        parsed = b,
                        host = host,
                        regDomain = regDomain,
                        category = domainWinner,
                        source = CategorySource.DOMAIN_RULE,
                        sourcePath = if (b.folderPath.isNotEmpty()) b.folderPath.joinToString("/") else null
                    )
                } else {
                    // Unclassified so far
                    IntermediateBookmark(
                        parsed = b,
                        host = host,
                        regDomain = regDomain,
                        category = null,
                        source = null,
                        sourcePath = if (b.folderPath.isNotEmpty()) b.folderPath.joinToString("/") else null
                    )
                }
            }
        }

        // 3. Cluster unclassified links by REGISTRABLE domain using Public Suffix List
        val unclassifiedGroups = intermediates
            .filter { it.category == null }
            .groupBy { it.regDomain }

        val clusterAssignments = HashMap<String, Pair<String, CategorySource>>()
        for ((regDomain, group) in unclassifiedGroups) {
            if (regDomain.isNotEmpty() && group.size >= 3) {
                // Name from the first label of registrable domain, title-cased
                val firstLabel = regDomain.substringBefore('.')
                val clusterName = firstLabel.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                }
                clusterAssignments[regDomain] = Pair(clusterName, CategorySource.CLUSTER)
            } else {
                clusterAssignments[regDomain] = Pair("Uncategorized", CategorySource.UNCATEGORIZED)
            }
        }

        return intermediates.map { item ->
            val finalCategory: String
            val finalSource: CategorySource
            if (item.category != null && item.source != null) {
                finalCategory = item.category
                finalSource = item.source
            } else {
                val assignment = clusterAssignments[item.regDomain]
                    ?: Pair("Uncategorized", CategorySource.UNCATEGORIZED)
                finalCategory = assignment.first
                finalSource = assignment.second
            }

            val dedupe = BookmarkNormalizer.dedupeKey(item.parsed.url) ?: item.parsed.url

            ClassifiedBookmark(
                url = item.parsed.url,
                title = item.parsed.title,
                dedupeKey = dedupe,
                folderPath = item.parsed.folderPath,
                createdAtMs = item.parsed.addDateMs,
                category = finalCategory,
                source = finalSource,
                sourcePath = item.sourcePath,
                host = item.host,
                registrableDomain = item.regDomain
            )
        }
    }

    private fun matchDomainTaxonomy(host: String, registrableDomain: String): String? {
        if (host.isEmpty()) return null

        // Walk labels from most specific to least
        var current = host
        while (current.isNotEmpty()) {
            val match = taxonomyMap[current]
            if (match != null) return match
            val nextDot = current.indexOf('.')
            if (nextDot == -1) break
            current = current.substring(nextDot + 1)
        }

        // Match on second-level label of registrable domain for amazon.* / ebay.*
        if (registrableDomain.isNotEmpty()) {
            val firstLabel = registrableDomain.substringBefore('.')
            if (firstLabel.equals("amazon", ignoreCase = true) || firstLabel.equals("ebay", ignoreCase = true)) {
                return taxonomyMap[firstLabel] ?: "Shopping"
            }
        }

        return null
    }
}
