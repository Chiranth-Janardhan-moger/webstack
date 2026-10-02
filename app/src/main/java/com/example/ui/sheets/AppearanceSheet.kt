package com.example.ui.sheets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.model.AppThemeMode
import com.example.ui.model.WebStackLayoutMode
import com.example.ui.theme.LocalAppColors

@Composable
fun AppearanceSubSheetContent(
    themeMode: AppThemeMode,
    onSetThemeMode: (AppThemeMode) -> Unit,
    layoutMode: WebStackLayoutMode,
    onSetLayoutMode: (WebStackLayoutMode) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppSubSheetHeader(
            title = "Appearance",
            onBack = onBack,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section: THEME
        Text(
            text = "THEME",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Segmented Slider Pill (Black thumb on white theme, White thumb on black theme)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(3.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val segmentWidth = maxWidth / 3
                val selectedIndex = when (themeMode) {
                    AppThemeMode.SYSTEM -> 0
                    AppThemeMode.LIGHT -> 1
                    AppThemeMode.DARK -> 2
                }
                val targetOffset = segmentWidth * selectedIndex
                val animatedOffset by animateDpAsState(
                    targetValue = targetOffset,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "theme_segment_slider"
                )

                val thumbColor by animateColorAsState(
                    targetValue = appColors.label,
                    animationSpec = tween(durationMillis = 200),
                    label = "theme_thumb_color"
                )

                // Animated sliding thumb: Black in light mode, White in dark mode
                Box(
                    modifier = Modifier
                        .offset(x = animatedOffset)
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .background(
                            color = thumbColor,
                            shape = RoundedCornerShape(9.dp)
                        )
                )

                // Interactive touch segments
                Row(modifier = Modifier.fillMaxSize()) {
                    val modes = listOf(
                        AppThemeMode.SYSTEM to "System",
                        AppThemeMode.LIGHT to "Light",
                        AppThemeMode.DARK to "Dark"
                    )
                    modes.forEach { (mode, title) ->
                        val isSelected = themeMode == mode
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) appColors.systemBackground else appColors.secondaryLabel,
                            animationSpec = tween(durationMillis = 200),
                            label = "theme_text_${mode.name}"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (themeMode != mode) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSetThemeMode(mode)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: LAYOUT
        Text(
            text = "LAYOUT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val layoutOptions = listOf(
                Triple(WebStackLayoutMode.LARGE_CARDS, "Card", R.drawable.ic_layout_card),
                Triple(WebStackLayoutMode.GRID_CARDS, "Grid", R.drawable.ic_layout_grid),
                Triple(WebStackLayoutMode.COMPACT_LIST, "List", R.drawable.ic_layout_list)
            )

            layoutOptions.forEach { (mode, title, iconRes) ->
                val isSelected = layoutMode == mode
                val interactionSource = remember(mode) { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "layout_bouncy_${mode.name}"
                )

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSetLayoutMode(mode)
                    },
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) appColors.surface else appColors.secondaryGroupedBackground,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 0.75.dp,
                        color = if (isSelected) appColors.label else appColors.separator
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(78.dp)
                                .background(
                                    color = if (appColors.isDark) Color(0xFF141416) else Color(0xFFF7FAFE),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = "$title layout preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = appColors.label,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) appColors.label else appColors.secondaryLabel
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        val doneInteractionSource = remember { MutableInteractionSource() }
        val isDonePressed by doneInteractionSource.collectIsPressedAsState()
        val doneScale by animateFloatAsState(
            targetValue = if (isDonePressed) 0.94f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "done_bouncy"
        )

        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onBack()
            },
            interactionSource = doneInteractionSource,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.label,
                contentColor = appColors.systemBackground
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .graphicsLayer {
                    scaleX = doneScale
                    scaleY = doneScale
                }
        ) {
            Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
