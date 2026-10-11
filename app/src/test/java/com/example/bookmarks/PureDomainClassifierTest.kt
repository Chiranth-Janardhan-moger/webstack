package com.example.bookmarks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PureDomainClassifierTest {

    @Test
    fun testRegistrableDomain() {
        assertEquals("bbc.co.uk", DomainClassifier.registrableDomain("www.bbc.co.uk"))
        assertEquals("alice.github.io", DomainClassifier.registrableDomain("alice.github.io"))
        assertEquals("example.com", DomainClassifier.registrableDomain("a.b.example.com"))
        assertEquals("192.168.1.5", DomainClassifier.registrableDomain("192.168.1.5"))
        assertEquals("localhost", DomainClassifier.registrableDomain("localhost"))
        assertEquals("invalid:domain", DomainClassifier.registrableDomain("invalid:domain"))
        assertEquals("", DomainClassifier.registrableDomain(""))
        assertEquals("...", DomainClassifier.registrableDomain("..."))
        assertEquals("com", DomainClassifier.registrableDomain("com"))
    }

    @Test
    fun testClassificationPrecedence_allBranches() {
        val taxonomy = mapOf(
            "github.com" to "Development",
            "youtube.com" to "Media",
            "example.com" to "Reference",
            "amazon" to "Shopping"
        )
        val classifier = DomainClassifier(taxonomy)

        // 1. Folder beats taxonomy
        val bFolder = ParsedBookmark("https://github.com/repo", "Repo", 0L, listOf("Custom Folder"))
        val resFolder = classifier.classifyAll(listOf(bFolder))
        assertEquals("Custom Folder", resFolder[0].category)
        assertEquals(CategorySource.FOLDER, resFolder[0].source)
        assertEquals("Custom Folder", resFolder[0].sourcePath)

        // 2. Noise folder -> taxonomy wins
        val bTax = ParsedBookmark("https://github.com/repo", "Repo", 0L, listOf("Bookmarks bar", "temp"))
        val resTax = classifier.classifyAll(listOf(bTax))
        assertEquals("Development", resTax[0].category)
        assertEquals(CategorySource.DOMAIN_RULE, resTax[0].source)

        // 3. Subdomain walking
        val bSub = ParsedBookmark("https://deep.sub.example.com/page", "Deep", 0L, emptyList())
        val resSub = classifier.classifyAll(listOf(bSub))
        assertEquals("Reference", resSub[0].category)
        assertEquals(CategorySource.DOMAIN_RULE, resSub[0].source)

        // 4. Amazon second level label matches Shopping
        val bAmazon = ParsedBookmark("https://www.amazon.co.uk/dp/1", "Item", 0L, emptyList())
        val resAmazon = classifier.classifyAll(listOf(bAmazon))
        assertEquals("Shopping", resAmazon[0].category)
        assertEquals(CategorySource.DOMAIN_RULE, resAmazon[0].source)

        // 5. Ebay fallback "Shopping"
        val bEbay = ParsedBookmark("https://www.ebay.de/itm/1", "Item", 0L, emptyList())
        val resEbay = classifier.classifyAll(listOf(bEbay))
        assertEquals("Shopping", resEbay[0].category)
        assertEquals(CategorySource.DOMAIN_RULE, resEbay[0].source)
    }

    @Test
    fun testClusteringAndTitlecasing() {
        val classifier = DomainClassifier()

        // Boundary: 2 links -> Uncategorized
        val twoLinks = listOf(
            ParsedBookmark("https://site2.org/1", "1", 0L, emptyList()),
            ParsedBookmark("https://site2.org/2", "2", 0L, emptyList())
        )
        val resTwo = classifier.classifyAll(twoLinks)
        assertEquals(2, resTwo.size)
        assertEquals("Uncategorized", resTwo[0].category)
        assertEquals(CategorySource.UNCATEGORIZED, resTwo[0].source)

        // Boundary: 3 links -> Cluster
        val threeLinks = listOf(
            ParsedBookmark("https://site3.org/1", "1", 0L, emptyList()),
            ParsedBookmark("https://site3.org/2", "2", 0L, emptyList()),
            ParsedBookmark("https://site3.org/3", "3", 0L, emptyList())
        )
        val resThree = classifier.classifyAll(threeLinks)
        assertEquals(3, resThree.size)
        assertEquals("Site3", resThree[0].category)
        assertEquals(CategorySource.CLUSTER, resThree[0].source)

        // Non-lowercase starting label (e.g. 3dprinting)
        val numLinks = listOf(
            ParsedBookmark("https://3dprint.net/1", "1", 0L, emptyList()),
            ParsedBookmark("https://3dprint.net/2", "2", 0L, emptyList()),
            ParsedBookmark("https://3dprint.net/3", "3", 0L, emptyList())
        )
        val resNum = classifier.classifyAll(numLinks)
        assertEquals("3dprint", resNum[0].category)
        assertEquals(CategorySource.CLUSTER, resNum[0].source)
    }

    @Test
    fun testEmptyAndMalformedUrls() {
        val classifier = DomainClassifier()
        val list = listOf(
            ParsedBookmark("bad_url", "Bad", 0L, emptyList()),
            ParsedBookmark("", "Empty", 0L, emptyList())
        )
        val res = classifier.classifyAll(list)
        assertEquals(2, res.size)
        assertEquals("Uncategorized", res[0].category)
        assertEquals(CategorySource.UNCATEGORIZED, res[0].source)
        assertEquals("", res[0].host)
        assertEquals("", res[0].registrableDomain)
    }
}
