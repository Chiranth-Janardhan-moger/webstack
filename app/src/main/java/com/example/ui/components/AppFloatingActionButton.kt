package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.AppFabShape
import com.example.ui.theme.LocalAppColors

/**
 * Modular Action Button component supporting both standard and expressive Material 3 shapes,
 * smooth expansion into the bulk selection triage bar, physical wheel rotation, and uniform
 * unclipped elevation shadow across all shapes.
 */
@Composable
fun AppFloatingActionButton(
    fabShape: AppFabShape,
    isSelectionMode: Boolean,
    selectedCount: Int,
    onAddClick: () -> Unit,
    onCancelSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth animated width for continuous expansion from FAB into floating action bar
    val barWidth by animateDpAsState(
        targetValue = if (isSelectionMode) 220.dp else 58.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bar_width_anim"
    )

    // Physical wheel rotation: rolls -360 degrees counterclockwise to the left, and +360 back
    val wheelRotation by animateFloatAsState(
        targetValue = if (isSelectionMode) -360f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "wheel_rotation_anim"
    )

    val fabScale by animateFloatAsState(
        targetValue = if (isPressed && !isSelectionMode) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_scale_anim"
    )

    val currentFabShape = fabShape.toComposeShape()
    val activeShape = if (isSelectionMode) RoundedCornerShape(29.dp) else currentFabShape

    Box(
        modifier = modifier
            .padding(bottom = 16.dp, end = 8.dp)
            .height(58.dp)
            .width(barWidth)
            .graphicsLayer {
                scaleX = fabScale
                scaleY = fabScale
            }
    ) {
        Surface(
            color = appColors.label,
            contentColor = appColors.systemBackground,
            shape = activeShape,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            border = if (appColors.isDark) BorderStroke(0.5.dp, Color(0x33FFFFFF)) else null,
            modifier = Modifier
                .fillMaxSize()
                .testTag(if (isSelectionMode) "bulk_action_bar" else "add_website_fab")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Rolling Wheel Button (+ in normal mode, ✕ in selection mode)
                IconButton(
                    onClick = {
                        if (isSelectionMode) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCancelSelection()
                        } else {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddClick()
                        }
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .size(50.dp)
                        .graphicsLayer {
                            rotationZ = wheelRotation
                        }
                ) {
                    if (isSelectionMode) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Selection",
                            tint = appColors.systemBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Website Link",
                            tint = appColors.systemBackground,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Center Selection Count (Apple typography)
                AnimatedVisibility(
                    visible = isSelectionMode && barWidth > 130.dp,
                    enter = fadeIn(tween(140, delayMillis = 60)) + scaleIn(initialScale = 0.85f),
                    exit = fadeOut(tween(80)) + scaleOut(targetScale = 0.85f)
                ) {
                    Text(
                        text = "$selectedCount Selected",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = appColors.systemBackground,
                        letterSpacing = (-0.2).sp
                    )
                }

                // Right Action: Delete Button (Apple red destructive circle)
                AnimatedVisibility(
                    visible = isSelectionMode && barWidth > 170.dp,
                    enter = fadeIn(tween(140, delayMillis = 90)) + slideInHorizontally { it / 2 },
                    exit = fadeOut(tween(80))
                ) {
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDeleteSelected()
                        },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF3B30).copy(alpha = 0.18f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = Color(0xFFFF453A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
