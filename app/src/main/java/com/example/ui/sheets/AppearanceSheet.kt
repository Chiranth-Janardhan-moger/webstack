package com.example.ui.sheets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppExplainerDialog
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.model.AppFabShape
import com.example.ui.model.AppThemeMode
import com.example.ui.model.FavoriteBadgeShape
import com.example.ui.model.FavoriteColorPreset
import com.example.ui.model.FavoriteIconType
import com.example.ui.model.WebStackLayoutMode
import com.example.ui.theme.LocalAppColors

private data class CustomizationExplainer(
    val title: String,
    val description: String,
    val icon: ImageVector
)

/**
 * Main Customization Hub: single unified interface with grouped navigation cards
 * leading to dedicated sub-sheets: Appearance, Favorites, and Interface.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomizationSubSheetContent(
    themeMode: AppThemeMode,
    layoutMode: WebStackLayoutMode,
    onOpenAppearance: () -> Unit,
    onOpenFavorites: () -> Unit,
    fabShape: AppFabShape = AppFabShape.CIRCLE,
    onOpenFabShapePicker: () -> Unit = {},
    onOpenCardDetails: () -> Unit = {},
    onOpenCustomizeTags: () -> Unit = {},
    favoriteIconType: FavoriteIconType = FavoriteIconType.HEART,
    favoriteBadgeShape: FavoriteBadgeShape = FavoriteBadgeShape.PURE_FLOAT,
    favoriteColorPreset: FavoriteColorPreset = FavoriteColorPreset.CRIMSON_PINK,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()
    var activeExplainer by remember { mutableStateOf<CustomizationExplainer?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Sub-sheet Header (Empty title as requested)
        AppSubSheetHeader(
            title = "",
            onBack = onBack,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(14.dp))

        // -------------------------------------------------------------
        // CARD 1: APPEARANCE (Navigates to dedicated Theme & Layout page)
        // -------------------------------------------------------------
        Text(
            text = "APPEARANCE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.secondaryGroupedBackground,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenAppearance()
                        },
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            activeExplainer = CustomizationExplainer(
                                title = "Appearance",
                                description = "Personalize your theme (Light, Dark, System) and switch between Large Cards, 2-Card Grid, or Compact List layouts.",
                                icon = Icons.Outlined.Palette
                            )
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = appColors.label,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Palette,
                                    contentDescription = null,
                                    tint = appColors.systemBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Appearance",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = appColors.label
                            )
                            val layoutLabel = when (layoutMode) {
                                WebStackLayoutMode.LARGE_CARDS -> "Card"
                                WebStackLayoutMode.GRID_CARDS -> "Grid"
                                WebStackLayoutMode.COMPACT_LIST -> "List"
                            }
                            Text(
                                text = "${themeMode.title} Theme • $layoutLabel Layout",
                                fontSize = 12.sp,
                                color = appColors.secondaryLabel
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open appearance options",
                        tint = appColors.tertiaryLabel,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -------------------------------------------------------------
        // CARD 2: FAVORITES (Navigates to dedicated Favorites page)
        // -------------------------------------------------------------
        Text(
            text = "FAVORITES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.secondaryGroupedBackground,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenFavorites()
                        },
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            activeExplainer = CustomizationExplainer(
                                title = "Favorites",
                                description = "Personalize your favorite indicator with custom symbols and vibrant color palettes.",
                                icon = favoriteIconType.getFilledIcon()
                            )
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = favoriteColorPreset.color.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = favoriteIconType.getFilledIcon(),
                                    contentDescription = null,
                                    tint = favoriteColorPreset.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Favorites",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = appColors.label
                            )
                            Text(
                                text = "${favoriteIconType.displayName} • ${favoriteColorPreset.displayName}",
                                fontSize = 12.sp,
                                color = appColors.secondaryLabel
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open favorites options",
                        tint = appColors.tertiaryLabel,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -------------------------------------------------------------
        // CARD 3: INTERFACE (Button Shape, Card Details, Tags & Categories)
        // -------------------------------------------------------------
        Text(
            text = "INTERFACE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.secondaryGroupedBackground,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Button Shape
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenFabShapePicker()
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                activeExplainer = CustomizationExplainer(
                                    title = "Button Shape",
                                    description = "Choose from 12 expressive shapes for your quick-add floating action button on the home screen.",
                                    icon = Icons.Default.Add
                                )
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val currentShape = fabShape.toComposeShape()
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(color = appColors.label, shape = currentShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = appColors.systemBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Button Shape",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = appColors.label
                                )
                                Text(
                                    text = fabShape.displayName,
                                    fontSize = 12.sp,
                                    color = appColors.secondaryLabel
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Change button shape",
                            tint = appColors.tertiaryLabel,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = appColors.separator
                )

                // 2. Card Details
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenCardDetails()
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                activeExplainer = CustomizationExplainer(
                                    title = "Card Details",
                                    description = "Configure bookmark card display options, including hiding domain URLs and category tags.",
                                    icon = Icons.Outlined.DashboardCustomize
                                )
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = appColors.label,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.DashboardCustomize,
                                        contentDescription = null,
                                        tint = appColors.systemBackground,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Card Details",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = appColors.label
                                )
                                Text(
                                    text = "Domain, tags, and display options",
                                    fontSize = 12.sp,
                                    color = appColors.secondaryLabel
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Customize card details",
                            tint = appColors.tertiaryLabel,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = appColors.separator
                )

                // 3. Tags & Categories
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenCustomizeTags()
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                activeExplainer = CustomizationExplainer(
                                    title = "Tags & Categories",
                                    description = "Customize your category bar with drag-and-drop reordering, hide tags to keep your screen tidy, and set fallback tags.",
                                    icon = Icons.AutoMirrored.Outlined.Label
                                )
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = appColors.label,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Label,
                                        contentDescription = null,
                                        tint = appColors.systemBackground,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Tags & Categories",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = appColors.label
                                )
                                Text(
                                    text = "Reorder, hide, and organize category tags",
                                    fontSize = 12.sp,
                                    color = appColors.secondaryLabel
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Customize tags",
                            tint = appColors.tertiaryLabel,
                            modifier = Modifier.size(16.dp)
                        )
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
            label = "customization_done_bouncy"
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

    activeExplainer?.let { exp ->
        AppExplainerDialog(
            title = exp.title,
            description = exp.description,
            icon = exp.icon,
            onDismiss = { activeExplainer = null }
        )
    }
}

/**
 * Dedicated Appearance Page: Shows Theme and Layout options with real-time feedback.
 */
@Composable
fun ThemeLayoutSubSheetContent(
    themeMode: AppThemeMode,
    onSetThemeMode: (AppThemeMode) -> Unit,
    layoutMode: WebStackLayoutMode,
    onSetLayoutMode: (WebStackLayoutMode) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppSubSheetHeader(
            title = "Appearance",
            onBack = onBack,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(16.dp))

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

                // Animated sliding thumb
                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = animatedOffset.roundToPx(), y = 0) }
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
                    color = if (isSelected) appColors.surface else (if (appColors.isDark) Color(0xFF1E1E20) else Color(0xFFECECEE)),
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
            label = "theme_layout_done_bouncy"
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

/**
 * Dedicated Favorites Customization Page: Shows Live Preview, Color Accent Selection,
 * and Symbol Selection (both arranged 2 in a row).
 */
@Composable
fun FavoritesCustomizationSubSheetContent(
    favoriteIconType: FavoriteIconType = FavoriteIconType.HEART,
    onSetFavoriteIconType: (FavoriteIconType) -> Unit = {},
    favoriteBadgeShape: FavoriteBadgeShape = FavoriteBadgeShape.PURE_FLOAT,
    onSetFavoriteBadgeShape: (FavoriteBadgeShape) -> Unit = {},
    favoriteColorPreset: FavoriteColorPreset = FavoriteColorPreset.CRIMSON_PINK,
    onSetFavoriteColorPreset: (FavoriteColorPreset) -> Unit = {},
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppSubSheetHeader(
            title = "Favorites",
            onBack = onBack,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(16.dp))

        // -------------------------------------------------------------
        // LIVE INTERACTIVE PREVIEW TILE
        // -------------------------------------------------------------
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.secondaryGroupedBackground,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Miniature bookmark card preview
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 56.dp)
                        .background(
                            color = if (appColors.isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = appColors.separator,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    // Clean floating favorite icon in top-left
                    Icon(
                        imageVector = favoriteIconType.getFilledIcon(),
                        contentDescription = "Preview Favorite",
                        tint = favoriteColorPreset.color,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 8.dp, start = 8.dp)
                            .size(18.dp)
                    )

                    // Simulated bookmark title and domain lines
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 10.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 42.dp, height = 4.dp)
                                .background(appColors.label.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 3.dp)
                                .background(appColors.tertiaryLabel, RoundedCornerShape(2.dp))
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Indicator Preview",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = appColors.label
                    )
                    Text(
                        text = "${favoriteIconType.displayName} • ${favoriteColorPreset.displayName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = favoriteColorPreset.color
                    )
                    Text(
                        text = "Shows on all favorited cards",
                        fontSize = 11.sp,
                        color = appColors.secondaryLabel
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -------------------------------------------------------------
        // SECTION 1: COLOR ACCENT (Above symbols, 2 in a row)
        // -------------------------------------------------------------
        Text(
            text = "COLOR ACCENT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = appColors.secondaryLabel,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        val colorRows = listOf(
            listOf(FavoriteColorPreset.CRIMSON_PINK, FavoriteColorPreset.ROYAL_GOLD),
            listOf(FavoriteColorPreset.ELECTRIC_PURPLE, FavoriteColorPreset.SKY_BLUE),
            listOf(FavoriteColorPreset.EMERALD_GREEN, FavoriteColorPreset.SUNSET_ORANGE)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            colorRows.forEach { colorPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorPair.forEach { preset ->
                        val isSelected = favoriteColorPreset == preset
                        val interactionSource = remember(preset) { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.94f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "fav_color_${preset.id}"
                        )

                        Surface(
                            onClick = {
                                if (favoriteColorPreset != preset) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetFavoriteColorPreset(preset)
                                }
                            },
                            interactionSource = interactionSource,
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) appColors.surface else (if (appColors.isDark) Color(0xFF1E1E20) else Color(0xFFECECEE)),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.75.dp,
                                color = if (isSelected) preset.color else appColors.separator
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(color = preset.color, shape = CircleShape)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.15f),
                                                shape = CircleShape
                                            )
                                    )
                                    Text(
                                        text = preset.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = appColors.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = preset.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(thickness = 0.5.dp, color = appColors.separator)
        Spacer(modifier = Modifier.height(20.dp))

        // -------------------------------------------------------------
        // SECTION 2: FAVORITE SYMBOL (Below colors, 2 in a row)
        // -------------------------------------------------------------
        Text(
            text = "FAVORITE SYMBOL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = appColors.secondaryLabel,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        val symbolRows = listOf(
            listOf(FavoriteIconType.HEART, FavoriteIconType.STAR),
            listOf(FavoriteIconType.BOOKMARK, FavoriteIconType.SPARKLE)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            symbolRows.forEach { symbolPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    symbolPair.forEach { symbol ->
                        val isSelected = favoriteIconType == symbol
                        val interactionSource = remember(symbol) { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.94f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "fav_symbol_${symbol.id}"
                        )

                        Surface(
                            onClick = {
                                if (favoriteIconType != symbol) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetFavoriteIconType(symbol)
                                }
                            },
                            interactionSource = interactionSource,
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) appColors.surface else (if (appColors.isDark) Color(0xFF1E1E20) else Color(0xFFECECEE)),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.75.dp,
                                color = if (isSelected) favoriteColorPreset.color else appColors.separator
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                color = if (isSelected) favoriteColorPreset.color.copy(alpha = 0.16f)
                                                        else (if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)),
                                                shape = RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = symbol.getFilledIcon(),
                                            contentDescription = symbol.displayName,
                                            tint = if (isSelected) favoriteColorPreset.color else appColors.secondaryLabel,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = symbol.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = appColors.label
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = favoriteColorPreset.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
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
            label = "fav_done_bouncy"
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

/**
 * Backward-compatible alias for CustomizationSubSheetContent.
 */
@Composable
fun AppearanceSubSheetContent(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onSetThemeMode: (AppThemeMode) -> Unit = {},
    layoutMode: WebStackLayoutMode = WebStackLayoutMode.LARGE_CARDS,
    onSetLayoutMode: (WebStackLayoutMode) -> Unit = {},
    fabShape: AppFabShape = AppFabShape.CIRCLE,
    onOpenFabShapePicker: () -> Unit = {},
    showCardDomain: Boolean = true,
    onToggleShowCardDomain: (Boolean) -> Unit = {},
    showCardCategory: Boolean = true,
    onToggleShowCardCategory: (Boolean) -> Unit = {},
    onOpenCardDetails: () -> Unit = {},
    onOpenCustomizeTags: () -> Unit = {},
    onOpenAppearance: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    favoriteIconType: FavoriteIconType = FavoriteIconType.HEART,
    onSetFavoriteIconType: (FavoriteIconType) -> Unit = {},
    favoriteBadgeShape: FavoriteBadgeShape = FavoriteBadgeShape.PURE_FLOAT,
    onSetFavoriteBadgeShape: (FavoriteBadgeShape) -> Unit = {},
    favoriteColorPreset: FavoriteColorPreset = FavoriteColorPreset.CRIMSON_PINK,
    onSetFavoriteColorPreset: (FavoriteColorPreset) -> Unit = {},
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    CustomizationSubSheetContent(
        themeMode = themeMode,
        layoutMode = layoutMode,
        onOpenAppearance = onOpenAppearance,
        onOpenFavorites = onOpenFavorites,
        fabShape = fabShape,
        onOpenFabShapePicker = onOpenFabShapePicker,
        onOpenCardDetails = onOpenCardDetails,
        onOpenCustomizeTags = onOpenCustomizeTags,
        favoriteIconType = favoriteIconType,
        favoriteBadgeShape = favoriteBadgeShape,
        favoriteColorPreset = favoriteColorPreset,
        onBack = onBack,
        onDismiss = onDismiss
    )
}
