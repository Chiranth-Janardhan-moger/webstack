package com.example.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.model.AppFabShape
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class AppFloatingActionButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNormalMode_displaysFabAndInvokesAddClick() {
        var addClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                AppFloatingActionButton(
                    fabShape = AppFabShape.CIRCLE,
                    isSelectionMode = false,
                    selectedCount = 0,
                    onAddClick = { addClicked = true },
                    onCancelSelection = {},
                    onDeleteSelected = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("add_website_fab").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Website Link").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Website Link").performClick()

        assertTrue("onAddClick should be triggered when Add button is clicked", addClicked)
    }

    @Test
    fun testSelectionMode_displaysBarAndInvokesCancelAndDelete() {
        var cancelClicked = false
        var deleteClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                AppFloatingActionButton(
                    fabShape = AppFabShape.CIRCLE,
                    isSelectionMode = true,
                    selectedCount = 4,
                    onAddClick = {},
                    onCancelSelection = { cancelClicked = true },
                    onDeleteSelected = { deleteClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("bulk_action_bar").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Cancel Selection").assertIsDisplayed()
        composeTestRule.onNodeWithText("4 Selected").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete Selected").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Cancel Selection").performClick()
        assertTrue("onCancelSelection should be triggered", cancelClicked)

        composeTestRule.onNodeWithContentDescription("Delete Selected").performClick()
        assertTrue("onDeleteSelected should be triggered", deleteClicked)
    }

    @Test
    fun testSelectionMode_bulkFavorite_invokesToggleFavorite() {
        var favoriteClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                AppFloatingActionButton(
                    fabShape = AppFabShape.CIRCLE,
                    isSelectionMode = true,
                    selectedCount = 3,
                    onAddClick = {},
                    onCancelSelection = {},
                    onDeleteSelected = {},
                    allSelectedAreFavorites = false,
                    onToggleFavoriteSelected = { favoriteClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("bulk_action_bar").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Mark as Favorite").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Mark as Favorite").performClick()
        assertTrue("onToggleFavoriteSelected should be triggered", favoriteClicked)
    }

    @Test
    fun testAllFabShapes_renderInAppFloatingActionButtonWithoutExceptions() {
        composeTestRule.setContent {
            MyApplicationTheme {
                androidx.compose.foundation.layout.Column {
                    for (shape in AppFabShape.entries) {
                        AppFloatingActionButton(
                            fabShape = shape,
                            isSelectionMode = false,
                            selectedCount = 0,
                            onAddClick = {},
                            onCancelSelection = {},
                            onDeleteSelected = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
