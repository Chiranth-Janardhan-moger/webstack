package com.example.bookmarks

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.example.data.Website
import com.example.data.WebsiteDao
import java.io.InputStream

data class ImportResult(
    val inserted: Int,
    val duplicates: Int
)

data class ImportPreprocessResult(
    val classifiedBookmarks: List<ClassifiedBookmark>,
    val internalDuplicatesCount: Int,
    val unsupportedSchemeCount: Int,
    val warnings: List<String>,
    val clusters: List<BookmarkCluster>,
    val hasNetscapeHeader: Boolean = true
)

object BookmarkImporter {

    /**
     * Preprocesses a bookmark input stream: parses, filters unsupported schemes,
     * dedupes within file in-memory, classifies, and creates clusters for user review.
     */
    fun preprocess(
        input: InputStream,
        classifier: DomainClassifier
    ): ImportPreprocessResult {
        val parseResult = BookmarkParser.parse(input)

        var unsupportedCount = 0
        var internalDupesCount = 0

        val seenDedupeKeys = HashSet<String>()
        val validBookmarks = mutableListOf<ParsedBookmark>()

        for (parsed in parseResult.bookmarks) {
            val key = BookmarkNormalizer.dedupeKey(parsed.url)
            if (key == null) {
                unsupportedCount++
                continue
            }
            if (!seenDedupeKeys.add(key)) {
                internalDupesCount++
                continue
            }
            validBookmarks.add(parsed)
        }

        val classified = classifier.classifyAll(validBookmarks)
        val clusters = buildClusters(classified)

        return ImportPreprocessResult(
            classifiedBookmarks = classified,
            internalDuplicatesCount = internalDupesCount,
            unsupportedSchemeCount = unsupportedCount,
            warnings = parseResult.warnings,
            clusters = clusters,
            hasNetscapeHeader = parseResult.hasNetscapeHeader
        )
    }

    private fun buildClusters(classified: List<ClassifiedBookmark>): List<BookmarkCluster> {
        val grouped = classified.groupBy { it.category }
        val clusterList = ArrayList<BookmarkCluster>(grouped.size)
        for ((catName, bookmarks) in grouped) {
            val sampleDomains = mutableListOf<String>()
            for (b in bookmarks) {
                val host = b.host
                if (host.isNotEmpty()) {
                    val cleanedHost = if (host.startsWith("www.")) host.substring(4) else host
                    if (!sampleDomains.contains(cleanedHost)) {
                        sampleDomains.add(cleanedHost)
                        if (sampleDomains.size == 3) break
                    }
                }
            }

            var dominantSource = CategorySource.UNCATEGORIZED
            var maxCount = -1
            val counts = HashMap<CategorySource, Int>()
            for (b in bookmarks) {
                val count = (counts[b.source] ?: 0) + 1
                counts[b.source] = count
                if (count > maxCount) {
                    maxCount = count
                    dominantSource = b.source
                }
            }

            clusterList.add(
                BookmarkCluster(
                    id = catName,
                    displayName = catName,
                    linkCount = bookmarks.size,
                    source = dominantSource,
                    sampleDomains = sampleDomains,
                    assignedCategory = catName,
                    isIncluded = true
                )
            )
        }
        clusterList.sortByDescending { it.linkCount }
        return clusterList
    }

    /**
     * Executes batch insertion and progress accounting in chunks of 500.
     */
    suspend fun executeBatchInsert(
        items: List<Website>,
        insertChunk: suspend (List<Website>) -> List<Long>,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): ImportResult {
        var inserted = 0
        items.chunked(500).forEachIndexed { i, chunk ->
            inserted += insertChunk(chunk).count { it != -1L }
            onProgress(minOf((i + 1) * 500, items.size), items.size)
        }
        return ImportResult(inserted = inserted, duplicates = items.size - inserted)
    }

    /**
     * Executes atomic Room import as specified in Section 2.6.
     * ZERO rows are committed if an exception occurs mid-import.
     */
    suspend fun importBookmarks(
        db: RoomDatabase,
        dao: WebsiteDao,
        items: List<Website>,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): ImportResult = db.withTransaction {
        executeBatchInsert(items, { chunk -> dao.insertAll(chunk) }, onProgress)
    }
}
