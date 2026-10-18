package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WebsiteDatabase
import com.example.ui.util.DEFAULT_CATEGORIES
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagCustomizationViewModelTest {

    private lateinit var app: Application

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        val prefs = app.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
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
    fun testInitialCategories_matchesDefaults_parkedIsEmpty() {
        val vm = WebsiteViewModel(app)
        assertEquals(DEFAULT_CATEGORIES, vm.categories.value)
        assertTrue(vm.parkedCategories.value.isEmpty())
    }

    @Test
    fun testParkCategory_movesFromActiveToParked_andPersists() {
        val vm = WebsiteViewModel(app)
        assertTrue(vm.categories.value.contains("Tools"))

        vm.parkCategory("Tools")

        assertFalse(vm.categories.value.contains("Tools"))
        assertTrue(vm.parkedCategories.value.contains("Tools"))

        // Verify persistence across new ViewModel instance
        val newVm = WebsiteViewModel(app)
        assertFalse(newVm.categories.value.contains("Tools"))
        assertTrue(newVm.parkedCategories.value.contains("Tools"))
    }

    @Test
    fun testUnparkCategory_movesFromParkedToActive_andPersists() {
        val vm = WebsiteViewModel(app)
        vm.parkCategory("Reading")
        assertTrue(vm.parkedCategories.value.contains("Reading"))

        vm.unparkCategory("Reading", toIndex = 0)

        assertFalse(vm.parkedCategories.value.contains("Reading"))
        assertEquals("Reading", vm.categories.value.first())

        // Verify persistence
        val newVm = WebsiteViewModel(app)
        assertEquals("Reading", newVm.categories.value.first())
        assertFalse(newVm.parkedCategories.value.contains("Reading"))
    }

    @Test
    fun testReorderActiveCategories_updatesOrder() {
        val vm = WebsiteViewModel(app)
        val initialFirst = vm.categories.value[0]
        val initialSecond = vm.categories.value[1]

        vm.reorderActiveCategories(fromIndex = 0, toIndex = 1)

        assertEquals(initialSecond, vm.categories.value[0])
        assertEquals(initialFirst, vm.categories.value[1])
    }

    @Test
    fun testResetCategoriesToDefaults_restoresDefaultAndClearsParked() {
        val vm = WebsiteViewModel(app)
        vm.parkCategory("Work")
        vm.parkCategory("Design")
        assertEquals(2, vm.parkedCategories.value.size)

        vm.resetCategoriesToDefaults()

        assertEquals(DEFAULT_CATEGORIES, vm.categories.value)
        assertTrue(vm.parkedCategories.value.isEmpty())
    }

    @Test
    fun testPersonal_cannotBeDeletedOrParkedOrRenamed() {
        val vm = WebsiteViewModel(app)
        assertTrue(vm.categories.value.contains("Personal"))

        // Attempt to park Personal
        vm.parkCategory("Personal")
        assertTrue("Personal must remain in active categories", vm.categories.value.contains("Personal"))
        assertFalse("Personal must not be in parked categories", vm.parkedCategories.value.contains("Personal"))

        // Attempt to delete Personal
        val deleteSuccess = vm.deleteCategory("Personal")
        assertFalse("deleteCategory on Personal must return false", deleteSuccess)
        assertTrue("Personal must still exist in categories", vm.categories.value.contains("Personal"))

        // Attempt to rename Personal
        val renameSuccess = vm.renameCategory("Personal", "CustomTag")
        assertFalse("renameCategory on Personal must return false", renameSuccess)
        assertTrue("Personal must remain with original name", vm.categories.value.contains("Personal"))
    }

    @Test
    fun testDeleteCategory_removesFromParkedAndActive() {
        val vm = WebsiteViewModel(app)
        vm.parkCategory("Design")
        assertTrue(vm.parkedCategories.value.contains("Design"))

        val deletedParked = vm.deleteCategory("Design")
        assertTrue(deletedParked)
        assertFalse(vm.parkedCategories.value.contains("Design"))

        val deletedActive = vm.deleteCategory("Tools")
        assertTrue(deletedActive)
        assertFalse(vm.categories.value.contains("Tools"))
    }

    @Test
    fun testAddCategory_rejectsIfAlreadyInParked() {
        val vm = WebsiteViewModel(app)
        vm.parkCategory("Work")
        assertTrue(vm.parkedCategories.value.contains("Work"))

        val added = vm.addCategory("Work")
        assertFalse("Should not allow adding a tag that exists in parked list", added)
    }

    @Test
    fun testFallbackCategory_defaultIsPersonal() {
        val vm = WebsiteViewModel(app)
        assertEquals("Personal", vm.fallbackCategory.value)
    }

    @Test
    fun testSetFallbackCategory_persistsAcrossInstances() {
        val vm = WebsiteViewModel(app)
        vm.setFallbackCategory("Tools")
        assertEquals("Tools", vm.fallbackCategory.value)

        val newVm = WebsiteViewModel(app)
        assertEquals("Tools", newVm.fallbackCategory.value)
    }

    @Test
    fun testDeleteCategory_reassignsBookmarksToConfiguredFallback() = kotlinx.coroutines.runBlocking {
        val vm = WebsiteViewModel(app)
        vm.setFallbackCategory("Tools")

        val dao = vm.websiteDao
        val siteId = dao.insertWebsite(
            com.example.data.Website(
                url = "https://example.com/work-doc",
                title = "Work Doc",
                category = "Work",
                dedupeKey = "https://example.com/work-doc"
            )
        )
        val beforeList = dao.byCategory("Work")
        assertEquals(1, beforeList.size)

        vm.deleteCategory("Work")
        kotlinx.coroutines.delay(50)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        val workList = dao.byCategory("Work")
        val toolsList = dao.byCategory("Tools")
        assertEquals(0, workList.size)
        assertEquals(1, toolsList.size)
        assertEquals(siteId, toolsList[0].id)
    }

    @Test
    fun testDeleteFallbackCategory_reassignsToAlternativeActiveTag() = kotlinx.coroutines.runBlocking {
        val vm = WebsiteViewModel(app)
        vm.setFallbackCategory("Work")
        assertEquals("Work", vm.fallbackCategory.value)

        vm.deleteCategory("Work")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        assertFalse(vm.categories.value.contains("Work"))
        assertFalse(vm.fallbackCategory.value.equals("Work", ignoreCase = true))
        assertTrue(vm.categories.value.contains(vm.fallbackCategory.value))
    }
}

