package com.example.ui.sheets

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.Speed
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
import com.example.ui.theme.LocalAppleColors
import kotlinx.coroutines.delay

@Composable
fun AppleVersionUpdateScreen(
    onDismiss: () -> Unit,
    onViewAllFeatures: () -> Unit = {}
) {
    val appleColors = LocalAppleColors.current
    val haptics = LocalHapticFeedback.current
    var targetDigit by remember { mutableStateOf("0") }

    LaunchedEffect(Unit) {
        delay(450)
        targetDigit = "1"
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    BackHandler {
        onDismiss()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .testTag("version_101_screen"),
        color = if (appleColors.isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Main Centered Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Version Odometer (1.0.0 -> 1.0.1)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "1.0.",
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        color = appleColors.label,
                        letterSpacing = (-1.5).sp
                    )
                    AnimatedContent(
                        targetState = targetDigit,
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
                        label = "odometer_digit"
                    ) { digit ->
                        Text(
                            text = digit,
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Black,
                            color = appleColors.label,
                            letterSpacing = (-1.5).sp
                        )
                    }
                }

                // Compact Frosted What's New Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = appleColors.surface,
                    border = BorderStroke(0.5.dp, appleColors.separator),
                    shadowElevation = if (appleColors.isDark) 0.dp else 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Feature 1: 2-Card Grid
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = appleColors.fill,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = null,
                                        tint = appleColors.label,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "2-Card Grid",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = appleColors.label,
                                    letterSpacing = (-0.2).sp
                                )
                                Text(
                                    text = "New 2-column layout mode",
                                    fontSize = 13.sp,
                                    color = appleColors.secondaryLabel
                                )
                            }
                        }

                        // Feature 2: Enhanced UI
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = appleColors.fill,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Speed,
                                        contentDescription = null,
                                        tint = appleColors.label,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Enhanced UI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = appleColors.label,
                                    letterSpacing = (-0.2).sp
                                )
                                Text(
                                    text = "Softer feel & refined animations",
                                    fontSize = 13.sp,
                                    color = appleColors.secondaryLabel
                                )
                            }
                        }

                        // Feature 3: Bug Fixes
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = appleColors.fill,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = appleColors.label,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Bug Fixes",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = appleColors.label,
                                    letterSpacing = (-0.2).sp
                                )
                                Text(
                                    text = "Stability & visual improvements",
                                    fontSize = 13.sp,
                                    color = appleColors.secondaryLabel
                                )
                            }
                        }

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = appleColors.separator.copy(alpha = 0.5f)
                        )

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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = appleColors.fill,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Star,
                                            contentDescription = null,
                                            tint = appleColors.label,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "WebStack Features",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = appleColors.label,
                                        letterSpacing = (-0.2).sp
                                    )
                                    Text(
                                        text = "See all capabilities & tools",
                                        fontSize = 13.sp,
                                        color = appleColors.secondaryLabel
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = appleColors.tertiaryLabel,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Bottom-Right Black Pill Action Button
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
                    onDismiss()
                },
                interactionSource = pillInteractionSource,
                shape = CircleShape,
                color = if (appleColors.isDark) Color.White else Color(0xFF0F0F12),
                contentColor = if (appleColors.isDark) Color.Black else Color.White,
                shadowElevation = if (appleColors.isDark) 0.dp else 4.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 28.dp, end = 24.dp)
                    .size(width = 68.dp, height = 48.dp)
                    .scale(pillScale)
                    .testTag("version_101_enter_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Enter WebStack",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
