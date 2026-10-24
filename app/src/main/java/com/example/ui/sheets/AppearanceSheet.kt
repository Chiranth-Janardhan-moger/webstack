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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DashboardCustomize
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

private data class AppearanceExplainer(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppearanceSubSheetContent(
    themeMode: AppThemeMode,
    onSetThemeMode: (AppThemeMode) -> Unit,
    layoutMode: WebStackLayoutMode,
    onSetLayoutMode: (WebStackLayoutMode) -> Unit,
    fabShape: AppFabShape = AppFabShape.CIRCLE,
    onOpenFabShapePicker: () -> Unit = {},
    showCardDomain: Boolean = true,
    onToggleShowCardDomain: (Boolean) -> Unit = {},
    showCardCategory: Boolean = true,
    onToggleShowCardCategory: (Boolean) -> Unit = {},
    onOpenCardDetails: () -> Unit = {},
    onOpenCustomizeTags: () -> Unit = {},
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
    var activeExplainer by remember { mutableStateOf<AppearanceExplainer?>(null) }

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
        // CARD 1: APPEARANCE (Theme + Layout unified)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Section Subtitle: Theme
                Text(
                    text = "Theme",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.label
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = appColors.separator)
                Spacer(modifier = Modifier.height(14.dp))

                // Section Subtitle: Layout
                Text(
                    text = "Layout",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.label
                )

                Spacer(modifier = Modifier.height(8.dp))

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
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // -------------------------------------------------------------
        // CARD 2: INTERFACE (Button Shape, Card Details, Tags & Categories)
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
                                activeExplainer = AppearanceExplainer(
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
                                activeExplainer = AppearanceExplainer(
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
                                activeExplainer = AppearanceExplainer(
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

        Spacer(modifier = Modifier.height(24.dp))

        // -------------------------------------------------------------
        // CARD 3: FAVORITES (Symbol, Badge Shape, Vibrant Colors)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Interactive Mini Card Preview Tile
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 68.dp, height = 50.dp)
                            .background(
                                color = if (appColors.isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = appColors.separator,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        val badgeShape = favoriteBadgeShape.toComposeShape()
                        if (favoriteBadgeShape == FavoriteBadgeShape.PURE_FLOAT) {
                            Icon(
                                imageVector = favoriteIconType.getFilledIcon(),
                                contentDescription = "Preview Favorite",
                                tint = favoriteColorPreset.color,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 6.dp, start = 6.dp)
                                    .size(16.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 5.dp, start = 5.dp)
                                    .size(24.dp)
                                    .background(
                                        color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
                                        shape = badgeShape
                                    )
                                    .border(
                                        width = 0.5.dp,
                                        color = favoriteColorPreset.color.copy(alpha = 0.4f),
                                        shape = badgeShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = favoriteIconType.getFilledIcon(),
                                    contentDescription = "Preview Favorite",
                                    tint = favoriteColorPreset.color,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // Simulated miniature card lines
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 8.dp, bottom = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 32.dp, height = 4.dp)
                                    .background(appColors.tertiaryLabel, RoundedCornerShape(2.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = 20.dp, height = 3.dp)
                                    .background(appColors.separator, RoundedCornerShape(2.dp))
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Indicator Preview",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = appColors.label
                        )
                        Text(
                            text = "${favoriteIconType.displayName} • ${favoriteBadgeShape.displayName} • ${favoriteColorPreset.displayName}",
                            fontSize = 12.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = appColors.separator)
                Spacer(modifier = Modifier.height(14.dp))

                // 1. Symbol Selection
                Text(
                    text = "Symbol",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.label
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FavoriteIconType.entries.forEach { iconType ->
                        val isSelected = favoriteIconType == iconType
                        val interactionSource = remember(iconType) { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.90f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "fav_icon_${iconType.id}"
                        )

                        Surface(
                            onClick = {
                                if (favoriteIconType != iconType) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetFavoriteIconType(iconType)
                                }
                            },
                            interactionSource = interactionSource,
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) appColors.surface else (if (appColors.isDark) Color(0xFF1E1E20) else Color(0xFFECECEE)),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.75.dp,
                                color = if (isSelected) appColors.label else appColors.separator
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconType.getFilledIcon(),
                                    contentDescription = iconType.displayName,
                                    tint = if (isSelected) favoriteColorPreset.color else appColors.secondaryLabel,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = appColors.separator)
                Spacer(modifier = Modifier.height(14.dp))

                // 2. Expressive Badge Shape Selection
                Text(
                    text = "Badge Shape",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.label
                )
                Spacer(modifier = Modifier.height(8.dp))

                val shapeRows = listOf(
                    listOf(FavoriteBadgeShape.PURE_FLOAT, FavoriteBadgeShape.CIRCLE, FavoriteBadgeShape.SQUIRCLE),
                    listOf(FavoriteBadgeShape.CLOVER, FavoriteBadgeShape.FLOWER, FavoriteBadgeShape.SOFT_BURST)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    shapeRows.forEach { rowShapes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowShapes.forEach { shapeOption ->
                                val isSelected = favoriteBadgeShape == shapeOption
                                val interactionSource = remember(shapeOption) { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()
                                val scale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.92f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "fav_shape_${shapeOption.id}"
                                )

                                Surface(
                                    onClick = {
                                        if (favoriteBadgeShape != shapeOption) {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSetFavoriteBadgeShape(shapeOption)
                                        }
                                    },
                                    interactionSource = interactionSource,
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) appColors.surface else (if (appColors.isDark) Color(0xFF1E1E20) else Color(0xFFECECEE)),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.75.dp,
                                        color = if (isSelected) appColors.label else appColors.separator
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        val optShape = shapeOption.toComposeShape()
                                        if (shapeOption == FavoriteBadgeShape.PURE_FLOAT) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (isSelected) favoriteColorPreset.color else appColors.tertiaryLabel,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(
                                                        color = if (isSelected) favoriteColorPreset.color else appColors.secondaryLabel.copy(alpha = 0.4f),
                                                        shape = optShape
                                                    )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = shapeOption.displayName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) appColors.label else appColors.secondaryLabel
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = appColors.separator)
                Spacer(modifier = Modifier.height(14.dp))

                // 3. Vibrant Color Palettes Selection
                Text(
                    text = "Color Accent",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.label
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FavoriteColorPreset.entries.forEach { colorPreset ->
                        val isSelected = favoriteColorPreset == colorPreset
                        val interactionSource = remember(colorPreset) { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.88f else if (isSelected) 1.15f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "fav_color_${colorPreset.id}"
                        )

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (favoriteColorPreset != colorPreset) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSetFavoriteColorPreset(colorPreset)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(
                                            width = 2.dp,
                                            color = colorPreset.color,
                                            shape = CircleShape
                                        )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 26.dp else 30.dp)
                                    .background(color = colorPreset.color, shape = CircleShape)
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

    activeExplainer?.let { exp ->
        AppExplainerDialog(
            title = exp.title,
            description = exp.description,
            icon = exp.icon,
            onDismiss = { activeExplainer = null }
        )
    }
}
