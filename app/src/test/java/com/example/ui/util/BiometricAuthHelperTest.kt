package com.example.ui.util

import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.view.ContextThemeWrapper
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.AppLockOverlay
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowBiometricManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BiometricAuthHelperTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun findFragmentActivity_unwrapsNestedContextThemeWrappers() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val sheetWrapped = ContextThemeWrapper(ContextThemeWrapper(activity, 0), 0)

        assertSame(activity, activity.findFragmentActivity())
        assertSame(activity, sheetWrapped.findFragmentActivity())
    }

    @Test
    fun authenticateContext_nonActivityContextInvokesErrorWithoutThrowing() {
        val appContext = ApplicationProvider.getApplicationContext<Application>()
        assertNull(appContext.findFragmentActivity())

        var errorMsg: String? = null
        BiometricAuthHelper.authenticateContext(
            context = appContext,
            title = "Locked Tags",
            subtitle = "Verify identity",
            onSuccess = {},
            onUnavailable = {},
            onError = { errorMsg = it }
        )

        assertEquals("Unable to open device authentication from this window", errorMsg)
    }

    @Test
    fun canAuthenticate_respectsKeyguardDeviceSecureFallback() {
        val appContext = ApplicationProvider.getApplicationContext<Application>()
        val km = appContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val bm = appContext.getSystemService(BiometricManager::class.java)
        val shadowBm = Shadow.extract<ShadowBiometricManager>(bm)

        shadowBm.setCanAuthenticate(false)
        Shadows.shadowOf(km).setIsDeviceSecure(false)
        assertFalse(BiometricAuthHelper.canAuthenticate(appContext))

        // Device PIN/pattern/password configured should pass even without enrolled biometrics
        Shadows.shadowOf(km).setIsDeviceSecure(true)
        assertTrue(BiometricAuthHelper.canAuthenticate(appContext))

        // Enrolled biometrics should also pass even if KeyguardManager fallback reports false
        Shadows.shadowOf(km).setIsDeviceSecure(false)
        shadowBm.setCanAuthenticate(true)
        assertTrue(BiometricAuthHelper.canAuthenticate(appContext))
    }

    @Test
    fun appLockOverlay_displaysMessageAndInvokesUnlock() {
        var clicked = false
        composeTestRule.setContent {
            MyApplicationTheme {
                AppLockOverlay(onUnlockClick = { clicked = true })
            }
        }

        composeTestRule.onNodeWithTag("app_lock_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithText("WebStack Locked").assertIsDisplayed()
        composeTestRule.onNodeWithTag("app_lock_unlock_button").performClick()
        assertTrue(clicked)
    }
}
