package com.example.bookmarks

import java.util.Locale

object FolderCleaner {
    private val NOISE = Regex(
        """^(bookmarks?( ?(bar|toolbar|menu))?|other bookmarks?|mobile bookmarks?|favou?rites( bar)?|unfiled bookmarks?|imported( from .+)?|new folder( ?\(?\d+\)?)?|untitled( folder)?( ?\d+)?|temp(orary)?|tmp|stuff|misc(ellaneous)?|links?|asdf+)$""",
        RegexOption.IGNORE_CASE
    )

    // "01_Tech", "1. Tech", "01 - Tech", "1) Tech" -> strip. "3D Printing", "5-minute meals", "2024 Taxes", "1.5 GHz CPUs" -> keep.
    private val ORDER_PREFIX = Regex("""^\d{1,3}(?:\s*[_.)]\s*|\s+-\s+)(?=\p{L})""")

    fun cleanFolderName(raw: String): String? {
        val n = raw.replace(Regex("\\s+"), " ").trim().replace(ORDER_PREFIX, "").trim()
        return if (n.isEmpty() || NOISE.matches(n)) null else n
    }

    /**
     * Resolves the canonical display names for cleaned folder names:
     * Group folder names case-insensitively (lowercase(Locale.ROOT) as key).
     * Display name = most frequent original casing in the file, ties go to first seen.
     */
    fun canonicalDisplayNames(rawFolders: List<String>): Map<String, String> {
        val groups = LinkedHashMap<String, MutableList<String>>()
        for (raw in rawFolders) {
            val cleaned = cleanFolderName(raw) ?: continue
            val key = cleaned.lowercase(Locale.ROOT)
            groups.getOrPut(key) { mutableListOf() }.add(cleaned)
        }

        val canonical = HashMap<String, String>()
        for ((key, instances) in groups) {
            val frequencies = LinkedHashMap<String, Int>()
            for (inst in instances) {
                frequencies[inst] = (frequencies[inst] ?: 0) + 1
            }
            var bestCasing = instances.first()
            var maxFreq = 0
            for ((casing, freq) in frequencies) {
                if (freq > maxFreq) {
                    maxFreq = freq
                    bestCasing = casing
                }
            }
            canonical[key] = bestCasing
        }
        return canonical
    }
}
