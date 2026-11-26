package com.example.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.Website
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class AppCategoryCapsuleBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleWebsites = listOf(
        Website(id = 1L, url = "https://example.com", title = "Example", category = "Tech", dedupeKey = "example.com"),
        Website(id = 2L, url = "https://google.com", title = "Google", category = "Tools", dedupeKey = "google.com")
    )

    @Test
    fun testAppCategoryCapsuleBar_withZeroFavorites_doesNotDisplayHeartCapsule() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AppCategoryCapsuleBar(
                    allWebsites = sampleWebsites,
                    categories = listOf("Tech", "Tools"),
                    selectedCategory = "All",
                    favoriteCount = 0,
                    onSelectCategory = {},
                    onTagLongPress = {},
                    onAddNewTag = {}
                )
            }
        }

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_item_favorites").assertDoesNotExist()
    }

    @Test
    fun testAppCategoryCapsuleBar_withFavorites_displaysHeartCapsuleAndInvokesSelect() {
        var selectedCat: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                AppCategoryCapsuleBar(
                    allWebsites = sampleWebsites,
                    categories = listOf("Tech", "Tools"),
                    selectedCategory = "All",
                    favoriteCount = 2,
                    onSelectCategory = { selectedCat = it },
                    onTagLongPress = {},
                    onAddNewTag = {}
                )
            }
        }

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_item_favorites").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Favorites").assertIsDisplayed()

        composeTestRule.onNodeWithTag("category_item_favorites").performClick()
        assertEquals("Favorites", selectedCat)
    }

    @Test
    fun testAppCategoryCapsuleBar_displaysLockedAndUnlockedCapsulesAndCountsInAll() {
        var clickedCategory: String? = null
        val mixedWebsites = sampleWebsites + Website(
            id = 3L,
            url = "https://vault.example.com",
            title = "Vault Link",
            category = "Secret",
            dedupeKey = "vault.example.com"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                AppCategoryCapsuleBar(
                    allWebsites = mixedWebsites,
                    categories = listOf("Tech", "Tools"),
                    selectedCategory = "All",
                    favoriteCount = 0,
                    onSelectCategory = { clickedCategory = it },
                    onTagLongPress = {},
                    onAddNewTag = {},
                    lockedCategories = listOf("Secret"),
                    unlockedCategories = emptySet()
                )
            }
        }

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Secret").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Locked Secret", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Count 3", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("Secret").performClick()
        assertEquals("Secret", clickedCategory)
    }

    @Test
    fun testAppCategoryCapsuleBar_unlockedCategoryShowsLockOpenIcon() {
        val mixedWebsites = sampleWebsites + Website(
            id = 3L,
            url = "https://vault.example.com",
            title = "Vault Link",
            category = "Secret",
            dedupeKey = "vault.example.com"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                AppCategoryCapsuleBar(
                    allWebsites = mixedWebsites,
                    categories = listOf("Tech", "Tools"),
                    selectedCategory = "Secret",
                    favoriteCount = 0,
                    onSelectCategory = {},
                    onTagLongPress = {},
                    onAddNewTag = {},
                    lockedCategories = listOf("Secret"),
                    unlockedCategories = setOf("secret")
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Unlocked Secret", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testAppCategoryCapsuleBar_excludesParkedCategoriesFromAllCount() {
        val mixedWebsites = sampleWebsites + listOf(
            Website(id = 3L, url = "https://vault.example.com", title = "Vault Link", category = "Secret", dedupeKey = "vault.example.com"),
            Website(id = 4L, url = "https://hidden.example.com", title = "Hidden Link", category = "Archived", dedupeKey = "hidden.example.com")
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                AppCategoryCapsuleBar(
                    allWebsites = mixedWebsites,
                    categories = listOf("Tech", "Tools"),
                    selectedCategory = "All",
                    favoriteCount = 0,
                    onSelectCategory = {},
                    onTagLongPress = {},
                    onAddNewTag = {},
                    lockedCategories = listOf("Secret"),
                    parkedCategories = listOf("Archived")
                )
            }
        }

        // Tech (1) + Tools (1) + Locked Secret (1) = 3; Archived (1) is excluded from All count
        composeTestRule.onNodeWithContentDescription("Count 3", useUnmergedTree = true).assertIsDisplayed()
    }
}
