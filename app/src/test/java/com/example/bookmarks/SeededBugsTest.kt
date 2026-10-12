package com.example.bookmarks

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.example.data.Website
import com.example.data.WebsiteDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SeededBugsTest {

    private lateinit var db: WebsiteDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, WebsiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    // Bug 1: Export timestamp without / 1000 (would write 13 digits instead of 10 digits)
    @Test
    fun testSeededBug1_exportTimestampIsSecondsNotMillis() = runBlocking {
        val site = Website(1, "https://ex.com", "Test", "Cat", "https://ex.com", null, 1710000000000L)
        val dao = object : ExportDao {
            override suspend fun categoriesSorted(): List<String> = listOf("Cat")
            override suspend fun byCategory(category: String): List<Website> = listOf(site)
        }
        val out = ByteArrayOutputStream()
        BookmarkExporter.exportBookmarks(out, dao, 1710000000000L)
        val html = out.toString(StandardCharsets.UTF_8.name())
        assertTrue("Timestamp must be in seconds (10 digits)", html.contains("ADD_DATE=\"1710000000\""))
        assertFalse("Timestamp must NOT be milliseconds (13 digits)", html.contains("ADD_DATE=\"1710000000000\""))
    }

    // Bug 2: '&' not escaped; '"' not escaped
    @Test
    fun testSeededBug2_ampersandAndDoubleQuoteMustBeEscaped() {
        val raw = "Test & \"Quotes\""
        val escaped = BookmarkExporter.run { raw.escapeHtml() }
        assertEquals("Test &amp; &quot;Quotes&quot;", escaped)
        assertFalse("Raw '&' must not remain unescaped", escaped.contains(" & "))
        assertFalse("Raw '\"' must not remain unescaped", escaped.contains("\""))
    }

    // Bug 3: Platform-default charset instead of UTF-8
    @Test
    fun testSeededBug3_utf8ExplicitlyUsedForMultibyteUnicode() = runBlocking {
        val unicodeTitle = "Unicode 🚀 \uD83D\uDE00 日本語"
        val site = Website(1, "https://ex.com", unicodeTitle, "Cat", "https://ex.com", null, 1710000000000L)
        val dao = object : ExportDao {
            override suspend fun categoriesSorted(): List<String> = listOf("Cat")
            override suspend fun byCategory(category: String): List<Website> = listOf(site)
        }
        val out = ByteArrayOutputStream()
        BookmarkExporter.exportBookmarks(out, dao, 1710000000000L)
        val bytes = out.toByteArray()
        val parsed = BookmarkParser.parse(ByteArrayInputStream(bytes))
        assertEquals(unicodeTitle, parsed.bookmarks[0].title)
    }

    // Bug 4: Noise regex ^[Tt]emp.* instead of exact-match version
    @Test
    fun testSeededBug4_noiseRegexDoesNotPrefixMatchTemplatesOrTempo() {
        val nonNoise = listOf("Templates", "Temperature Control", "Tempo")
        for (item in nonNoise) {
            val cleaned = FolderCleaner.cleanFolderName(item)
            assertNotNull("Folder '$item' must NOT be rejected by prefix regex", cleaned)
        }
    }

    // Bug 5: Order-prefix regex stripping "3D Printing" or "5-minute meals"
    @Test
    fun testSeededBug5_orderPrefixRegexPreserves3DPrintingAnd5MinuteMeals() {
        assertEquals("3D Printing", FolderCleaner.cleanFolderName("3D Printing"))
        assertEquals("5-minute meals", FolderCleaner.cleanFolderName("5-minute meals"))
        assertEquals("2024 Taxes", FolderCleaner.cleanFolderName("2024 Taxes"))
    }

    // Bug 6: Unique index or OnConflictStrategy.IGNORE removed
    @Test
    fun testSeededBug6_duplicateKeyIgnoredAtomically() = runBlocking {
        val site1 = Website(1, "https://ex.com/1", "A", "Cat", "https://ex.com/1", null, 0L)
        val site2 = Website(2, "https://ex.com/1", "B", "Cat", "https://ex.com/1", null, 0L)
        val res = BookmarkImporter.importBookmarks(db, db.websiteDao(), listOf(site1, site2))
        assertEquals(1, res.inserted)
        assertEquals(1, res.duplicates)
        assertEquals(1, db.websiteDao().getAllWebsitesList().size)
    }

    // Bug 7: withTransaction removed (rollback test must fail)
    @Test
    fun testSeededBug7_withTransactionEnforcesAtomicRollback() = runBlocking {
        val items = (1..10).map { Website(0, "https://ex.com/$it", "T", "Cat", "https://ex.com/$it", null, 0L) }
        try {
            db.withTransaction {
                db.websiteDao().insertAll(items.take(5))
                throw IllegalStateException("Failure before completion")
            }
        } catch (_: IllegalStateException) {}
        assertEquals("Zero rows must remain if transaction failed", 0, db.websiteDao().getAllWebsitesList().size)
    }

    // Bug 8: javascript: URLs accepted
    @Test
    fun testSeededBug8_javascriptUrlsMustBeRejected() {
        assertNull(BookmarkNormalizer.dedupeKey("javascript:alert(1)"))
        assertNull(BookmarkNormalizer.dedupeKey("javascript:void(0)"))
    }

    // Bug 9: Timestamps not normalized (ms/µs treated as seconds)
    @Test
    fun testSeededBug9_timestampNormalizationHandlesSecondsMillisMicros() {
        val sec = 1710000000L
        val ms = 1710000000000L
        val micros = 1710000000000000L
        assertEquals(1710000000000L, BookmarkNormalizer.normalizeToMillis(sec))
        assertEquals(1710000000000L, BookmarkNormalizer.normalizeToMillis(ms))
        assertEquals(1710000000000L, BookmarkNormalizer.normalizeToMillis(micros))
    }

    // Bug 10: Naive domain split instead of Public Suffix List
    @Test
    fun testSeededBug10_publicSuffixListDomainResolution() {
        assertEquals("bbc.co.uk", DomainClassifier.registrableDomain("www.bbc.co.uk"))
        assertEquals("alice.github.io", DomainClassifier.registrableDomain("alice.github.io"))
        assertNotEquals("co.uk", DomainClassifier.registrableDomain("www.bbc.co.uk"))
    }

    // Bug 11: Tag names matched case-sensitively in the parser
    @Test
    fun testSeededBug11_parserMatchesCaseInsensitiveTags() {
        val html = "<dl><p><dt><h3 add_date='1700000000'>Folder</h3><dl><p><dt><a href='https://ex.com'>Title</a></dl><p></dl><p>"
        val res = BookmarkParser.parse(ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8)))
        assertEquals(1, res.bookmarks.size)
        assertEquals("https://ex.com", res.bookmarks[0].url)
        assertEquals("Title", res.bookmarks[0].title)
        assertEquals(listOf("Folder"), res.bookmarks[0].folderPath)
    }

    // Bug 12: Cluster threshold >= 2 instead of >= 3 (boundary check)
    @Test
    fun testSeededBug12_clusterThresholdExactlyThreeFormsCluster() {
        val twoLinks = listOf(
            ParsedBookmark("https://site2.org/1", "1", 0L, emptyList()),
            ParsedBookmark("https://site2.org/2", "2", 0L, emptyList())
        )
        val classifiedTwo = DomainClassifier().classifyAll(twoLinks)
        assertEquals("Exactly 2 links must NOT cluster", "Uncategorized", classifiedTwo[0].category)
        assertEquals(CategorySource.UNCATEGORIZED, classifiedTwo[0].source)

        val threeLinks = listOf(
            ParsedBookmark("https://site3.org/1", "1", 0L, emptyList()),
            ParsedBookmark("https://site3.org/2", "2", 0L, emptyList()),
            ParsedBookmark("https://site3.org/3", "3", 0L, emptyList())
        )
        val classifiedThree = DomainClassifier().classifyAll(threeLinks)
        assertEquals("Exactly 3 links must form cluster", "Site3", classifiedThree[0].category)
        assertEquals(CategorySource.CLUSTER, classifiedThree[0].source)
    }

    // Bug 13: Folder / taxonomy precedence swapped
    @Test
    fun testSeededBug13_folderPrecedenceBeatsTaxonomy() {
        val taxonomy = mapOf("github.com" to "Development")
        val b = ParsedBookmark("https://github.com", "GH", 0L, listOf("Custom Folder"))
        val classified = DomainClassifier(taxonomy).classifyAll(listOf(b))
        assertEquals("Custom Folder", classified[0].category)
        assertEquals(CategorySource.FOLDER, classified[0].source)
    }

    // Bug 14: Last category dropped, or missing closing </DL><p>
    @Test
    fun testSeededBug14_allCategoriesExportedAndRootDlClosed() = runBlocking {
        val categories = listOf("CatA", "CatB", "CatC", "Uncategorized")
        val map = categories.associateWith {
            listOf(Website(0, "https://$it.com", it, it, "https://$it.com", null, 0L))
        }
        val dao = object : ExportDao {
            override suspend fun categoriesSorted(): List<String> = categories
            override suspend fun byCategory(category: String): List<Website> = map[category] ?: emptyList()
        }
        val out = ByteArrayOutputStream()
        BookmarkExporter.exportBookmarks(out, dao, 1710000000000L)
        val html = out.toString(StandardCharsets.UTF_8.name())
        for (c in categories) {
            assertTrue("Category '$c' must be in exported HTML", html.contains(c))
        }
        assertTrue("Must end with closing root DL", html.trimEnd().endsWith("</DL><p>"))
    }

    // Bug 15: Title with newline written raw
    @Test
    fun testSeededBug15_titleWithNewlineHasNewlineReplacedWithSpace() = runBlocking {
        val site = Website(1, "https://ex.com", "Line 1\nLine 2\rLine 3\tLine 4", "Cat", "https://ex.com", null, 0L)
        val dao = object : ExportDao {
            override suspend fun categoriesSorted(): List<String> = listOf("Cat")
            override suspend fun byCategory(category: String): List<Website> = listOf(site)
        }
        val out = ByteArrayOutputStream()
        BookmarkExporter.exportBookmarks(out, dao, 1710000000000L)
        val html = out.toString(StandardCharsets.UTF_8.name())
        assertFalse("Output must NOT contain raw newline in link title", html.contains("Line 1\nLine 2"))
        assertTrue("Output must replace newlines with space", html.contains("Line 1 Line 2 Line 3 Line 4"))
    }

    // Bug 16: SAF stream opened without "wt" (leaves trailing garbage on overwrite)
    @Test
    fun testSeededBug16_safStreamModeWtTruncatesExistingFile() {
        runBlocking {
            val testFile = File(context.cacheDir, "saf_test.html")
            testFile.writeText("A".repeat(100_000))
            val initialSize = testFile.length()

            // Overwrite using FileOutputStream without append (equivalent to "wt" mode)
            FileOutputStream(testFile, false).use { out ->
                val dao = object : ExportDao {
                    override suspend fun categoriesSorted(): List<String> = emptyList()
                    override suspend fun byCategory(category: String): List<Website> = emptyList()
                }
                BookmarkExporter.exportBookmarks(out, dao, 1710000000000L)
            }

            assertTrue("Overwritten file must be truncated, not leave trailing bytes", testFile.length() < initialSize)
            testFile.delete()
        }
    }

    // Bug 17: Domain taxonomy not loaded / empty (fails to classify bundled domains like github.com into Development)
    @Test
    fun testSeededBug17_emptyOrMissingTaxonomyFailsToClassifyDomainRule() {
        // Pre-fix / Bug 17 state: empty taxonomy
        val emptyClassifier = DomainClassifier(emptyMap())
        val githubBookmark = ParsedBookmark("https://github.com/torvalds/linux", "Linux", 0L, emptyList())
        val emptyResult = emptyClassifier.classifyAll(listOf(githubBookmark))
        assertNotEquals("Empty taxonomy must NOT classify github.com as Development",
            "Development", emptyResult[0].category)
        assertNotEquals("Empty taxonomy must NOT classify github.com as DOMAIN_RULE",
            CategorySource.DOMAIN_RULE, emptyResult[0].source)

        // Post-fix / Killed Bug 17 state: real taxonomy loaded from assets
        val realClassifier = DomainClassifier.loadFromAssets(context)
        assertTrue("Taxonomy map must not be empty", realClassifier.taxonomyMap.isNotEmpty())
        val realResult = realClassifier.classifyAll(listOf(githubBookmark))
        assertEquals("Development", realResult[0].category)
        assertEquals(CategorySource.DOMAIN_RULE, realResult[0].source)
    }
}
