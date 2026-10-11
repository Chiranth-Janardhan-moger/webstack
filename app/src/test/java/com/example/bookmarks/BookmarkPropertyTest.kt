package com.example.bookmarks

import com.example.data.Website
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Random

class BookmarkPropertyTest {

    private class PropertyFakeDao(
        private val categories: List<String>,
        private val sitesByCategory: Map<String, List<Website>>
    ) : ExportDao {
        override suspend fun categoriesSorted(): List<String> = categories
        override suspend fun byCategory(category: String): List<Website> =
            sitesByCategory[category] ?: emptyList()
    }

    private fun normalizeTitleForExport(title: String, url: String): String {
        val t = title.ifBlank { url }
        return buildString(t.length) {
            for (c in t) when {
                c == '\n' || c == '\r' || c == '\t' -> append(' ')
                c < ' ' -> {}
                else -> append(c)
            }
        }
    }

    @Test
    fun testRoundTripExportParse_preservesMultisetAcross10000GeneratedCases() {
        runBlocking {
        val random = Random(42L)
        val specialChars = arrayOf("&", "<", ">", "\"", "'", "\n", "\t", " ", "a", "Z", "0", "🚀", "한", "글", "ع", "ر", "ب", "ي")

        val categories = listOf("Development", "Design", "Reading", "Uncategorized", "Special & <Cat> '\"")
        val totalCases = 10_000

        var currentId = 1L
        val websites = ArrayList<Website>(totalCases)

        for (i in 0 until totalCases) {
            val titleLen = random.nextInt(20) + 1
            val titleBuilder = StringBuilder()
            for (j in 0 until titleLen) {
                titleBuilder.append(specialChars[random.nextInt(specialChars.size)])
            }
            val title = titleBuilder.toString()
            val cat = categories[random.nextInt(categories.size)]
            val scheme = if (random.nextBoolean()) "https" else "http"
            val url = if (cat == "Uncategorized") {
                "$scheme://uncat$i.org/path/${i}?param=val_${random.nextInt(100)}"
            } else {
                "$scheme://example${i % 100}.org/path/${i}?param=val_${random.nextInt(100)}"
            }

            websites.add(
                Website(
                    id = currentId++,
                    url = url,
                    title = title,
                    category = cat,
                    dedupeKey = BookmarkNormalizer.dedupeKey(url) ?: url,
                    sourcePath = null,
                    createdAtMs = 1710000000000L
                )
            )
        }

        val sitesByCategory = websites.groupBy { it.category }
        val sortedCats = categories.filter { it != "Uncategorized" }.sorted() + "Uncategorized"

        val dao = PropertyFakeDao(sortedCats, sitesByCategory)
        val out = ByteArrayOutputStream()
        val nowMs = 1710000000000L
        BookmarkExporter.exportBookmarks(out, dao, nowMs)

        val exportedBytes = out.toByteArray()
        val parseResult = BookmarkParser.parse(ByteArrayInputStream(exportedBytes))

        assertEquals("Total parsed bookmarks must equal total exported", totalCases, parseResult.bookmarks.size)

        // Classify all parsed bookmarks to restore category from folder
        val classifier = DomainClassifier()
        val classified = classifier.classifyAll(parseResult.bookmarks)

        // Verify multiset match
        val expectedMultiset = websites.map {
            Triple(it.url, normalizeTitleForExport(it.title, it.url), it.category)
        }.sortedBy { it.first }

        val actualMultiset = classified.map {
            Triple(it.url, it.title, it.category)
        }.sortedBy { it.first }

        for (i in 0 until totalCases) {
            assertEquals("URL mismatch at $i", expectedMultiset[i].first, actualMultiset[i].first)
            assertEquals("Title mismatch at $i", expectedMultiset[i].second, actualMultiset[i].second)
            assertEquals("Category mismatch at $i", expectedMultiset[i].third, actualMultiset[i].third)
        }
    }
}

    @Test
    fun testParserNeverThrowsOnMutatedOrTruncatedInput_10000Cases() {
        val random = Random(1337L)
        val validTemplate = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
            <DT><H3 ADD_DATE="1710000000">Folder</H3>
            <DL><p>
            <DT><A HREF="https://example.com/test" ADD_DATE="1710000000">Test Link</A>
            </DL><p>
            </DL><p>
        """.trimIndent()
        val templateBytes = validTemplate.toByteArray(StandardCharsets.UTF_8)

        for (i in 0 until 10_000) {
            val mutationType = random.nextInt(4)
            val mutatedBytes = when (mutationType) {
                0 -> {
                    // Truncation at arbitrary length
                    val len = random.nextInt(templateBytes.size + 1)
                    templateBytes.copyOf(len)
                }
                1 -> {
                    // Random byte mutation
                    val copy = templateBytes.clone()
                    val numCorruptions = random.nextInt(10) + 1
                    for (c in 0 until numCorruptions) {
                        val idx = random.nextInt(copy.size)
                        copy[idx] = random.nextInt(256).toByte()
                    }
                    copy
                }
                2 -> {
                    // Random tag injection (unclosed tags, random brackets)
                    val junk = "<${"a".repeat(random.nextInt(50))}> <\"\"\"'> <<<<>>>>> <!--"
                    junk.toByteArray(StandardCharsets.UTF_8)
                }
                else -> {
                    // Completely random pseudo-random bytes
                    val len = random.nextInt(500)
                    val bytes = ByteArray(len)
                    random.nextBytes(bytes)
                    bytes
                }
            }

            // Must never throw an exception
            try {
                val res = BookmarkParser.parse(ByteArrayInputStream(mutatedBytes))
                assertTrue(res.bookmarks.size >= 0)
            } catch (e: Throwable) {
                throw AssertionError("BookmarkParser threw exception on iteration $i: ${e.message}", e)
            }
        }
    }
}
