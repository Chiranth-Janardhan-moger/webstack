package com.example.ui.sheets

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.WebsiteDatabase
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WebsiteViewModel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CustomizeTagsSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var app: Application

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        app.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        resetDatabaseSingleton()
        app.deleteDatabase("webstack_database")
    }

    @After
    fun teardown() {
        resetDatabaseSingleton()
        app.deleteDatabase("webstack_database")
    }

    private fun resetDatabaseSingleton() {
        try {
            val field = WebsiteDatabase::class.java.getDeclaredField("INSTANCE")
            field.isAccessible = true
            field.set(null, null)
        } catch (_: Exception) {}
    }

    @Test
    fun longPressTagFilterTabs_displaysExplainerDialogs() {
        val vm = WebsiteViewModel(app)
        composeTestRule.setContent {
            MyApplicationTheme {
                CustomizeTagsSheetContent(
                    viewModel = vm,
                    onOpenAddTagDialog = {},
                    onDismiss = {}
                )
            }
        }

        // Long-press "All tags"
        composeTestRule.onNodeWithText("All tags", substring = true).performTouchInput { longClick() }
        composeTestRule.onNodeWithText("All Tags").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active tags shown on the Home tag bar", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Got It").performClick()

        // Long-press "Hidden"
        composeTestRule.onNodeWithText("Hidden (", substring = true).performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Hidden Tags").assertIsDisplayed()
        composeTestRule.onNodeWithText("hidden from All and Favorites without requiring a lock", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Got It").performClick()

        // Long-press "Locked"
        composeTestRule.onNodeWithText("Locked (", substring = true).performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Locked Tags").assertIsDisplayed()
        composeTestRule.onNodeWithText("counted in All, but stay hidden", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Got It").performClick()
    }
}
