package com.example.ui.sheets

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppFeatureTile
import com.example.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VersionUpdateScreen(
    onDismiss: () -> Unit,
    onViewAllFeatures: () -> Unit = {}
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    // Odometer digits: 1.2.0 -> 1.0.0 -> 2.0.0
    var majorDigit by remember { mutableStateOf("1") }
    var minorDigit by remember { mutableStateOf("2") }

    LaunchedEffect(Unit) {
        // Step 1: Start at 1.2.0
        delay(400)
        // Step 2: Minor digit rolls down 2 -> 0 (becomes 1.0.0)
        minorDigit = "0"
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)

        delay(450)
        // Step 3: Major digit springs up 1 -> 2 (becomes 2.0.0)
        majorDigit = "2"
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    BackHandler {
        if (pagerState.currentPage > 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(0)
            }
        } else {
            onDismiss()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .testTag("version_update_screen"),
        color = if (appColors.isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        // Screen 1: Pure Giant 2.0.0 Odometer (1.2.0 -> 1.0.0 -> 2.0.0)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Major digit: 1 -> 2 (springs upward)
                                AnimatedContent(
                                    targetState = majorDigit,
                                    transitionSpec = {
                                        (slideInVertically(
                                            animationSpec = spring(
                                                dampingRatio = 0.72f,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ) { height -> height } + fadeIn()) togetherWith
                                                (slideOutVertically(
                                                    animationSpec = spring(
                                                        dampingRatio = 0.72f,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                ) { height -> -height } + fadeOut())
                                    },
                                    label = "major_odometer_digit"
                                ) { digit ->
                                    Text(
                                        text = digit,
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Black,
                                        color = appColors.label,
                                        letterSpacing = (-2).sp
                                    )
                                }

                                Text(
                                    text = ".",
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Black,
                                    color = appColors.label,
                                    letterSpacing = (-2).sp
                                )

                                // Minor digit: 2 -> 0 (rolls downward)
                                AnimatedContent(
                                    targetState = minorDigit,
                                    transitionSpec = {
                                        (slideInVertically(
                                            animationSpec = spring(
                                                dampingRatio = 0.72f,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ) { height -> -height } + fadeIn()) togetherWith
                                                (slideOutVertically(
                                                    animationSpec = spring(
                                                        dampingRatio = 0.72f,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                ) { height -> height } + fadeOut())
                                    },
                                    label = "minor_odometer_digit"
                                ) { digit ->
                                    Text(
                                        text = digit,
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Black,
                                        color = appColors.label,
                                        letterSpacing = (-2).sp
                                    )
                                }

                                Text(
                                    text = ".0",
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Black,
                                    color = appColors.label,
                                    letterSpacing = (-2).sp
                                )
                            }
                        }
                    }

                    1 -> {
                        // Screen 2: PC Companion Features Card
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "PC Companion & Wi-Fi Sync",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.label,
                                letterSpacing = (-0.4).sp,
                                modifier = Modifier.padding(bottom = 18.dp)
                            )

                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = appColors.surface,
                                border = BorderStroke(0.5.dp, appColors.separator),
                                shadowElevation = if (appColors.isDark) 0.dp else 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Point 1: Dedicated Desktop App
                                    VersionFeatureTile(
                                        title = "Dedicated Desktop App",
                                        description = "Native Windows & Linux client with shortcuts."
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DesktopWindows,
                                            contentDescription = null,
                                            tint = appColors.label,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Point 2: Zero-Install Browser Mode
                                    VersionFeatureTile(
                                        title = "Zero-Install Browser Mode",
                                        description = "Browse directly in Chrome or Edge via URL."
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Language,
                                            contentDescription = null,
                                            tint = appColors.label,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Point 3: Fast PIN & Permanent Mode
                                    VersionFeatureTile(
                                        title = "Fast PIN & Permanent Mode",
                                        description = "6-digit security; keep the same PIN permanently."
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = appColors.label,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Point 4: Offline Vault Access
                                    VersionFeatureTile(
                                        title = "Offline Vault Access",
                                        description = "Search cached bookmarks on PC without phone."
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.OfflineBolt,
                                            contentDescription = null,
                                            tint = appColors.label,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = appColors.separator.copy(alpha = 0.5f)
                                    )

                                    // Point 5: Explore All Features
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onViewAllFeatures()
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        VersionFeatureTile(
                                            title = "Explore All Features",
                                            description = "View complete changelog & gestures guide"
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Star,
                                                contentDescription = null,
                                                tint = appColors.label,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = appColors.tertiaryLabel,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Overlay: Centered 2-Dots Indicator + Right-Aligned Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                // Centered 2-Dots Indicator
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val page0Active = pagerState.currentPage == 0
                    val dot0Width by animateDpAsState(
                        targetValue = if (page0Active) 18.dp else 6.dp,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                        label = "dot_0_width"
                    )
                    Box(
                        modifier = Modifier
                            .size(width = dot0Width, height = 6.dp)
                            .clip(CircleShape)
                            .background(if (page0Active) appColors.label else appColors.secondaryLabel.copy(alpha = 0.35f))
                    )

                    val page1Active = pagerState.currentPage == 1
                    val dot1Width by animateDpAsState(
                        targetValue = if (page1Active) 18.dp else 6.dp,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                        label = "dot_1_width"
                    )
                    Box(
                        modifier = Modifier
                            .size(width = dot1Width, height = 6.dp)
                            .clip(CircleShape)
                            .background(if (page1Active) appColors.label else appColors.secondaryLabel.copy(alpha = 0.35f))
                    )
                }

                // Bottom-Right Action Pill: -> on Page 1, Checkmark on Page 2
                val isLastPage = pagerState.currentPage == 1
                val pillInteractionSource = remember { MutableInteractionSource() }
                val isPillPressed by pillInteractionSource.collectIsPressedAsState()
                val pillScale by animateFloatAsState(
                    targetValue = if (isPillPressed) 0.90f else 1f,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                    label = "pill_enter_scale"
                )

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (isLastPage) {
                            onDismiss()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    },
                    interactionSource = pillInteractionSource,
                    shape = CircleShape,
                    color = if (appColors.isDark) Color.White else Color(0xFF0F0F12),
                    contentColor = if (appColors.isDark) Color.Black else Color.White,
                    shadowElevation = if (appColors.isDark) 0.dp else 4.dp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(width = 68.dp, height = 48.dp)
                        .scale(pillScale)
                        .testTag("version_update_action_pill")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AnimatedContent(
                            targetState = isLastPage,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "pill_icon_transition"
                        ) { lastPage ->
                            Icon(
                                imageVector = if (lastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = if (lastPage) "Enter WebStack" else "Next Page",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionFeatureTile(
    title: String,
    description: String,
    icon: @Composable () -> Unit
) {
    AppFeatureTile(title = title, description = description, icon = icon)
}
