package com.example.ui.sheets

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bookmarks.BookmarkCluster
import com.example.bookmarks.CategorySource
import com.example.bookmarks.ClassifiedBookmark
import com.example.bookmarks.ImportPreprocessResult
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class ClusterApprovalSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMissingNetscapeHeader_displaysWarningBanner() {
        val resultWithWarning = ImportPreprocessResult(
            classifiedBookmarks = listOf(
                ClassifiedBookmark(
                    url = "https://example.com",
                    title = "Example",
                    dedupeKey = "https://example.com",
                    folderPath = emptyList(),
                    createdAtMs = 0L,
                    category = "General",
                    source = CategorySource.UNCATEGORIZED,
                    sourcePath = "General",
                    host = "example.com",
                    registrableDomain = "example.com"
                )
            ),
            clusters = listOf(
                BookmarkCluster(
                    id = "c1",
                    displayName = "General",
                    linkCount = 1,
                    source = CategorySource.UNCATEGORIZED,
                    sampleDomains = listOf("example.com"),
                    assignedCategory = "General",
                    isIncluded = true
                )
            ),
            internalDuplicatesCount = 0,
            unsupportedSchemeCount = 0,
            warnings = listOf("Missing Netscape bookmark header"),
            hasNetscapeHeader = false
        )

        composeTestRule.setContent {
            ClusterApprovalSheet(
                preprocessResult = resultWithWarning,
                existingCategories = listOf("General"),
                onConfirmImport = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithTag("netscape_header_warning_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Warning: Missing standard Netscape bookmark header. File may not be an official browser export.").assertIsDisplayed()
    }

    @Test
    fun testValidNetscapeHeader_doesNotDisplayWarningBanner() {
        val resultValid = ImportPreprocessResult(
            classifiedBookmarks = listOf(
                ClassifiedBookmark(
                    url = "https://example.com",
                    title = "Example",
                    dedupeKey = "https://example.com",
                    folderPath = emptyList(),
                    createdAtMs = 0L,
                    category = "General",
                    source = CategorySource.UNCATEGORIZED,
                    sourcePath = "General",
                    host = "example.com",
                    registrableDomain = "example.com"
                )
            ),
            clusters = listOf(
                BookmarkCluster(
                    id = "c1",
                    displayName = "General",
                    linkCount = 1,
                    source = CategorySource.UNCATEGORIZED,
                    sampleDomains = listOf("example.com"),
                    assignedCategory = "General",
                    isIncluded = true
                )
            ),
            internalDuplicatesCount = 0,
            unsupportedSchemeCount = 0,
            warnings = emptyList(),
            hasNetscapeHeader = true
        )

        composeTestRule.setContent {
            ClusterApprovalSheet(
                preprocessResult = resultValid,
                existingCategories = listOf("General"),
                onConfirmImport = {},
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithTag("netscape_header_warning_banner").assertDoesNotExist()
    }
}
