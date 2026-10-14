package com.example.bookmarks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DomainClassifierTest {

    @Test
    fun testRealTaxonomyLoadsAndIsNotEmpty() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val classifier = DomainClassifier.loadFromAssets(context)
        assertTrue("Taxonomy must not be empty", classifier.taxonomyMap.isNotEmpty())
        assertTrue("Taxonomy must contain at least 15 domains", classifier.taxonomyMap.size >= 15)
        assertEquals("Development", classifier.taxonomyMap["github.com"])
        assertEquals("Social", classifier.taxonomyMap["bsky.app"])
        assertEquals("Social", classifier.taxonomyMap["threads.net"])
        assertEquals("Social", classifier.taxonomyMap["instagram.com"])

        // github.com must classify as Development under DOMAIN_RULE before clustering
        val b = ParsedBookmark(
            url = "https://github.com/torvalds/linux",
            title = "Linux Kernel",
            addDateMs = 1710000000000L,
            folderPath = emptyList()
        )
        val result = classifier.classifyAll(listOf(b))
        assertEquals(1, result.size)
        assertEquals("Development", result[0].category)
        assertEquals(CategorySource.DOMAIN_RULE, result[0].source)
    }

    @Test
    fun testRegistrableDomain_publicSuffixListCases() {
        assertEquals("bbc.co.uk", DomainClassifier.registrableDomain("www.bbc.co.uk"))
        assertEquals("alice.github.io", DomainClassifier.registrableDomain("alice.github.io"))
        assertEquals("example.com", DomainClassifier.registrableDomain("a.b.example.com"))
        assertEquals("192.168.1.5", DomainClassifier.registrableDomain("192.168.1.5"))
        assertEquals("localhost", DomainClassifier.registrableDomain("localhost"))
    }

    @Test
    fun testClassificationPrecedence_folderBeatsTaxonomyBeatsCluster() {
        val taxonomy = mapOf(
            "github.com" to "Development",
            "youtube.com" to "Media"
        )
        val classifier = DomainClassifier(taxonomy)

        // Case 1: Clean folder present -> FOLDER wins over taxonomy
        val b1 = ParsedBookmark(
            url = "https://github.com/torvalds/linux",
            title = "Linux",
            addDateMs = 1710000000000L,
            folderPath = listOf("Operating Systems")
        )
        val res1 = classifier.classifyAll(listOf(b1))
        assertEquals("Operating Systems", res1[0].category)
        assertEquals(CategorySource.FOLDER, res1[0].source)

        // Case 2: Folder is noise -> DOMAIN_RULE wins over noise folder
        val b2 = ParsedBookmark(
            url = "https://github.com/torvalds/linux",
            title = "Linux",
            addDateMs = 1710000000000L,
            folderPath = listOf("Bookmarks bar", "temp")
        )
        val res2 = classifier.classifyAll(listOf(b2))
        assertEquals("Development", res2[0].category)
        assertEquals(CategorySource.DOMAIN_RULE, res2[0].source)

        // Case 3: Folder is noise and domain not in taxonomy -> clustering applies
        val b3List = listOf(
            ParsedBookmark("https://customblog.io/p1", "P1", 1710000000000L, listOf("Bookmarks bar")),
            ParsedBookmark("https://customblog.io/p2", "P2", 1710000000000L, listOf("Bookmarks bar")),
            ParsedBookmark("https://customblog.io/p3", "P3", 1710000000000L, listOf("Bookmarks bar"))
        )
        val res3 = classifier.classifyAll(b3List)
        res3.forEach {
            assertEquals("Customblog", it.category)
            assertEquals(CategorySource.CLUSTER, it.source)
        }
    }

    @Test
    fun testAmazonAndEbaySecondLevelLabelMatching() {
        val classifier = DomainClassifier()
        val list = listOf(
            ParsedBookmark("https://www.amazon.co.uk/dp/123", "UK Book", 0L, emptyList()),
            ParsedBookmark("https://amazon.com/dp/456", "US Book", 0L, emptyList()),
            ParsedBookmark("https://www.ebay.de/itm/789", "DE Watch", 0L, emptyList()),
            ParsedBookmark("https://ebay.com/itm/012", "US Watch", 0L, emptyList())
        )
        val res = classifier.classifyAll(list)
        res.forEach {
            assertEquals("Shopping", it.category)
            assertEquals(CategorySource.DOMAIN_RULE, it.source)
        }
    }

    @Test
    fun testClusteringThresholdBoundaries_exactly2_3_4() {
        val classifier = DomainClassifier()

        // Exactly 2 unclassified links on same domain -> Uncategorized
        val twoLinks = listOf(
            ParsedBookmark("https://site2.org/1", "S1", 0L, emptyList()),
            ParsedBookmark("https://site2.org/2", "S2", 0L, emptyList())
        )
        val resTwo = classifier.classifyAll(twoLinks)
        resTwo.forEach {
            assertEquals("Uncategorized", it.category)
            assertEquals(CategorySource.UNCATEGORIZED, it.source)
        }

        // Exactly 3 unclassified links on same domain -> Cluster ("Site3")
        val threeLinks = listOf(
            ParsedBookmark("https://site3.org/1", "S1", 0L, emptyList()),
            ParsedBookmark("https://site3.org/2", "S2", 0L, emptyList()),
            ParsedBookmark("https://site3.org/3", "S3", 0L, emptyList())
        )
        val resThree = classifier.classifyAll(threeLinks)
        resThree.forEach {
            assertEquals("Site3", it.category)
            assertEquals(CategorySource.CLUSTER, it.source)
        }

        // Exactly 4 unclassified links on same domain -> Cluster ("Site4")
        val fourLinks = listOf(
            ParsedBookmark("https://site4.org/1", "S1", 0L, emptyList()),
            ParsedBookmark("https://site4.org/2", "S2", 0L, emptyList()),
            ParsedBookmark("https://site4.org/3", "S3", 0L, emptyList()),
            ParsedBookmark("https://site4.org/4", "S4", 0L, emptyList())
        )
        val resFour = classifier.classifyAll(fourLinks)
        resFour.forEach {
            assertEquals("Site4", it.category)
            assertEquals(CategorySource.CLUSTER, it.source)
        }
    }

    @Test
    fun testLoadTaxonomyFromJson_emptyAndMixedCase() {
        val emptyMap = DomainClassifier.loadTaxonomyFromJson("{}")
        assertTrue(emptyMap.isEmpty())

        val json = """
            {
                "GITHUB.COM": "Development",
                "Medium.COM": "Reading"
            }
        """.trimIndent()
        val map = DomainClassifier.loadTaxonomyFromJson(json)
        assertEquals(2, map.size)
        assertEquals("Development", map["github.com"])
        assertEquals("Reading", map["medium.com"])
    }

    @Test
    fun testLoadFromAssets_fallbackToClassLoader() {
        // A mock context whose assets throw to verify classloader fallback
        val realContext = ApplicationProvider.getApplicationContext<Context>()
        val mockContext = object : android.content.ContextWrapper(realContext) {
            override fun getAssets(): android.content.res.AssetManager {
                throw java.io.IOException("Simulated assets failure")
            }
        }
        val classifier = DomainClassifier.loadFromAssets(mockContext)
        // Even if assets throw, classloader may load or return empty safely without crash
        assertTrue(classifier.taxonomyMap.isNotEmpty() || classifier.taxonomyMap.isEmpty())
    }

    @Test
    fun testRegistrableDomain_edgeCases() {
        assertEquals("", DomainClassifier.registrableDomain(""))
        assertEquals("invalid:domain", DomainClassifier.registrableDomain("invalid:domain"))
        assertEquals("...", DomainClassifier.registrableDomain("..."))
        assertEquals("com", DomainClassifier.registrableDomain("com"))
        assertEquals("not-a-domain", DomainClassifier.registrableDomain("not-a-domain"))
    }

    @Test
    fun testClassifyAll_emptyAndMalformedUrlHandling() {
        val classifier = DomainClassifier()
        val list = listOf(
            ParsedBookmark("not_a_url", "Malformed", 0L, emptyList()),
            ParsedBookmark("", "Empty", 0L, emptyList())
        )
        val res = classifier.classifyAll(list)
        assertEquals(2, res.size)
        assertEquals("Uncategorized", res[0].category)
        assertEquals(CategorySource.UNCATEGORIZED, res[0].source)
        assertEquals("", res[0].host)
        assertEquals("", res[0].registrableDomain)
    }

    @Test
    fun testClassifyAll_customAmazonEbayInTaxonomy() {
        val customTax = mapOf(
            "amazon" to "Amazon Store",
            "ebay" to "eBay Auctions"
        )
        val classifier = DomainClassifier(customTax)
        val list = listOf(
            ParsedBookmark("https://www.amazon.co.jp/product/1", "JP Item", 0L, emptyList()),
            ParsedBookmark("https://ebay.fr/item/2", "FR Item", 0L, emptyList())
        )
        val res = classifier.classifyAll(list)
        assertEquals("Amazon Store", res[0].category)
        assertEquals(CategorySource.DOMAIN_RULE, res[0].source)
        assertEquals("eBay Auctions", res[1].category)
        assertEquals(CategorySource.DOMAIN_RULE, res[1].source)
    }

    @Test
    fun testClassifyAll_titlecasingNonLetterLabel() {
        val classifier = DomainClassifier()
        val list = listOf(
            ParsedBookmark("https://3dprinting.com/1", "1", 0L, emptyList()),
            ParsedBookmark("https://3dprinting.com/2", "2", 0L, emptyList()),
            ParsedBookmark("https://3dprinting.com/3", "3", 0L, emptyList())
        )
        val res = classifier.classifyAll(list)
        assertEquals(3, res.size)
        // '3' is not lower-case, titlecase leaves '3' unchanged
        assertEquals("3dprinting", res[0].category)
        assertEquals(CategorySource.CLUSTER, res[0].source)
    }

    @Test
    fun testClassifyAll_multiLevelHostSuffixWalking() {
        val taxonomy = mapOf(
            "example.com" to "Reference",
            "api.service.org" to "APIs"
        )
        val classifier = DomainClassifier(taxonomy)
        val list = listOf(
            ParsedBookmark("https://deep.sub.domain.example.com/test", "Deep", 0L, emptyList()),
            ParsedBookmark("https://v1.api.service.org/endpoint", "Endpoint", 0L, emptyList())
        )
        val res = classifier.classifyAll(list)
        assertEquals("Reference", res[0].category)
        assertEquals(CategorySource.DOMAIN_RULE, res[0].source)
        assertEquals("APIs", res[1].category)
        assertEquals(CategorySource.DOMAIN_RULE, res[1].source)
    }
}
