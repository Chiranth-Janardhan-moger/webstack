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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppFeatureTile
import com.example.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun VersionUpdateScreen(
    onDismiss: () -> Unit,
    onViewAllFeatures: () -> Unit = {}
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    var targetDigit by remember { mutableStateOf("1") }

    LaunchedEffect(Unit) {
        delay(450)
        targetDigit = "2"
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
        color = if (appColors.isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
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
                // Version Odometer (1.1.0 -> 1.2.0)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "1.",
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        color = appColors.label,
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
                            color = appColors.label,
                            letterSpacing = (-1.5).sp
                        )
                    }
                    Text(
                        text = ".0",
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        color = appColors.label,
                        letterSpacing = (-1.5).sp
                    )
                }

                // Compact Frosted What's New Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = appColors.surface,
                    border = BorderStroke(0.5.dp, appColors.separator),
                    shadowElevation = if (appColors.isDark) 0.dp else 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Feature 1: Expressive FAB Shapes
                        VersionFeatureTile(
                            title = "Expressive FAB Shapes",
                            description = "Style your action button with dynamic Material 3 shapes."
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = appColors.separator.copy(alpha = 0.5f)
                        )

                        // Feature 2: Bulk Selection & Triage
                        VersionFeatureTile(
                            title = "Bulk Selection & Triage",
                            description = "Select, tag, and clean up multiple bookmarks effortlessly."
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Checklist,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = appColors.separator.copy(alpha = 0.5f)
                        )

                        // Feature 3: Rearrange the Tags
                        VersionFeatureTile(
                            title = "Rearrange the Tags",
                            description = "Reorder tags easily and toggle card details on or off."
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = appColors.separator.copy(alpha = 0.5f)
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
                            VersionFeatureTile(
                                title = "All Features",
                                description = "View full changelog"
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
                color = if (appColors.isDark) Color.White else Color(0xFF0F0F12),
                contentColor = if (appColors.isDark) Color.Black else Color.White,
                shadowElevation = if (appColors.isDark) 0.dp else 4.dp,
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

@Composable
private fun VersionFeatureTile(
    title: String,
    description: String,
    icon: @Composable () -> Unit
) {
    AppFeatureTile(title = title, description = description, icon = icon)
}

