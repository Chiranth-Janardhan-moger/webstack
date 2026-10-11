package com.example.bookmarks

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.Website
import com.example.data.WebsiteDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RealBookmarksTest {

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

    private fun findProjectRoot(): File {
        val userDir = File(System.getProperty("user.dir") ?: ".")
        return if (File(userDir, "testdata").exists()) userDir else userDir.parentFile ?: userDir
    }

    private fun findRealHtmlFile(): File {
        val root = findProjectRoot()
        val inProject = File(root, "testdata/real/bookmarks_10_10_2026.html")
        if (inProject.exists()) return inProject
        val chromeReal = File(root, "testdata/real/chrome_real.html")
        if (chromeReal.exists()) return chromeReal
        val userFile = File("C:/Users/Chiranth/Downloads/bookmarks_10_10_2026.html")
        if (userFile.exists()) return userFile

        throw IllegalStateException("Real bookmarks HTML file not found")
    }

    @Test
    fun testRealUserBookmarkImportExportPipeline() {
        runBlocking {
            val file = findRealHtmlFile()
            assertTrue("Real bookmarks file must exist: ${file.absolutePath}", file.exists())

            // 1. PARSE
            val rawBytes = file.readBytes()
            val parseResult = BookmarkParser.parse(ByteArrayInputStream(rawBytes))

            println("=== REAL BOOKMARK PARSE METRICS ===")
            println("File path: ${file.absolutePath}")
            println("File size bytes: ${file.length()}")
            println("Total parsed <a> tags: ${parseResult.bookmarks.size}")
            println("Parser warnings: ${parseResult.warnings}")

            assertTrue("Must parse non-empty list of bookmarks", parseResult.bookmarks.isNotEmpty())

            // 2. PREPROCESS & CLASSIFY (with real bundled taxonomy)
            val classifier = DomainClassifier.loadFromAssets(context)
            assertTrue("Real taxonomy must be loaded and non-empty", classifier.taxonomyMap.isNotEmpty())

            val preprocessed = BookmarkImporter.preprocess(ByteArrayInputStream(rawBytes), classifier)

            val totalFound = preprocessed.classifiedBookmarks.size + preprocessed.internalDuplicatesCount + preprocessed.unsupportedSchemeCount
            println("\n=== PREPROCESS METRICS ===")
            println("Total links found: $totalFound")
            println("Unsupported schemes skipped: ${preprocessed.unsupportedSchemeCount}")
            println("Internal duplicates skipped: ${preprocessed.internalDuplicatesCount}")
            println("Unique classified links: ${preprocessed.classifiedBookmarks.size}")

            // Assertions on filtering
            assertTrue("Unsupported scheme count must be >= 1 (e.g. chrome://newtab)", preprocessed.unsupportedSchemeCount >= 1)
            assertEquals("Clean user export has 0 internal duplicates", 0, preprocessed.internalDuplicatesCount)

            // Assert no noise folder leaked as category
            val noiseRegex = Regex(
                """^(bookmarks?( ?(bar|toolbar|menu))?|other bookmarks?|mobile bookmarks?|favou?rites( bar)?|unfiled bookmarks?|imported( from .+)?|new folder( ?\(?\d+\)?)?|untitled( folder)?( ?\d+)?|temp(orary)?|tmp|stuff|misc(ellaneous)?|links?|asdf+)$""",
                RegexOption.IGNORE_CASE
            )
            val categories = preprocessed.classifiedBookmarks.map { it.category }.distinct()
            println("\n=== DERIVED CATEGORIES ===")
            categories.forEach { cat ->
                println("Category: '$cat'")
                assertFalse("Category '$cat' must NOT match noise regex", noiseRegex.matches(cat))
            }

            // Classification breakdown
            val bySource = preprocessed.classifiedBookmarks.groupBy { it.source }
            val totalClassified = preprocessed.classifiedBookmarks.size
            println("\n=== CLASSIFICATION BREAKDOWN ===")
            CategorySource.entries.forEach { src ->
                val count = bySource[src]?.size ?: 0
                val pct = if (totalClassified > 0) (count * 100.0 / totalClassified) else 0.0
                println("Source $src: $count ($pct%)")
            }

            println("\n=== UNCATEGORIZED LINKS (${bySource[CategorySource.UNCATEGORIZED]?.size}) ===")
            bySource[CategorySource.UNCATEGORIZED]?.forEach {
                println("- Host: '${it.host}', Title: '${it.title}', URL: '${it.url}'")
            }

            // Verify github.com links outside clean folders classify as Development under DOMAIN_RULE
            val githubUnfiled = preprocessed.classifiedBookmarks.filter {
                it.url.contains("github.com") && it.sourcePath == null
            }
            println("Unfiled github.com links: ${githubUnfiled.size}")
            assertTrue("Unfiled github.com links must exist", githubUnfiled.isNotEmpty())
            githubUnfiled.forEach {
                assertEquals("Unfiled github.com must classify as Development", "Development", it.category)
                assertEquals("Unfiled github.com must have source DOMAIN_RULE", CategorySource.DOMAIN_RULE, it.source)
            }
            assertTrue("DOMAIN_RULE count must be >= 3", (bySource[CategorySource.DOMAIN_RULE]?.size ?: 0) >= 3)

            // Spot-checks on user folders
            val expectedFolders = listOf("coding_realted", "paper", "hackaton", "placement", "learing for better")
            for (f in expectedFolders) {
                val hasFolder = preprocessed.classifiedBookmarks.any { it.category.equals(f, ignoreCase = true) }
                assertTrue("Expected user folder category '$f' must be present", hasFolder)
            }

            // 3. PERSISTENCE IN ROOM
            val websites = preprocessed.classifiedBookmarks.map { b ->
                Website(
                    url = b.url,
                    title = b.title,
                    category = b.category,
                    dedupeKey = b.dedupeKey,
                    sourcePath = b.sourcePath,
                    createdAtMs = b.createdAtMs
                )
            }

            val dao = db.websiteDao()
            val importResult = BookmarkImporter.importBookmarks(db, dao, websites)

            println("\n=== DATABASE IMPORT RESULT ===")
            println("Inserted: ${importResult.inserted}")
            println("Duplicates: ${importResult.duplicates}")
            assertEquals("Inserted count must match unique websites", websites.size, importResult.inserted)
            assertEquals("Duplicates inside websites list should be 0", 0, importResult.duplicates)
            assertEquals("Database total rows must equal inserted", websites.size, dao.getAllWebsitesList().size)

            // 4. RE-IMPORT IDEMPOTENCY
            val reimportResult = BookmarkImporter.importBookmarks(db, dao, websites)
            println("\n=== RE-IMPORT IDEMPOTENCY ===")
            println("Re-imported inserted: ${reimportResult.inserted}")
            println("Re-imported duplicates: ${reimportResult.duplicates}")
            assertEquals("Re-importing identical bookmarks must insert 0 rows", 0, reimportResult.inserted)
            assertEquals("Re-importing identical bookmarks must mark all as duplicates", websites.size, reimportResult.duplicates)
            assertEquals("Database size must remain unchanged", websites.size, dao.getAllWebsitesList().size)

            // 5. EXPORT STREAMING
            val exportFile = File(context.cacheDir, "real_export_out.html")
            FileOutputStream(exportFile).use { out ->
                val exportDao = object : ExportDao {
                    override suspend fun categoriesSorted(): List<String> = dao.categoriesSorted()
                    override suspend fun byCategory(category: String): List<Website> = dao.byCategory(category)
                }
                BookmarkExporter.exportBookmarks(out, exportDao, 1710000000000L)
            }

            println("\n=== EXPORT METRICS ===")
            println("Exported HTML size bytes: ${exportFile.length()}")
            assertTrue("Exported file must not be empty", exportFile.length() > 0)

            // 6. VALIDATE NETSCAPE STRUCTURE USING PYTHON VALIDATOR
            val root = findProjectRoot()
            val validatorScript = File(root, "tools/validate_netscape.py")
            if (validatorScript.exists()) {
                val proc = ProcessBuilder("python", validatorScript.absolutePath, exportFile.absolutePath)
                    .redirectErrorStream(true)
                    .start()
                val validatorOutput = proc.inputStream.bufferedReader().readText()
                val exitCode = proc.waitFor()
                println("\n=== PYTHON VALIDATOR OUTPUT ===")
                println(validatorOutput)
                assertEquals("Python validate_netscape.py must exit with code 0", 0, exitCode)
            }

            // 7. ROUND-TRIP PARSE
            val reParsed = FileInputStream(exportFile).use { BookmarkParser.parse(it) }
            assertEquals("Re-parsed bookmark count must match exported database count",
                websites.size, reParsed.bookmarks.size)

            exportFile.delete()
        }
    }
}
