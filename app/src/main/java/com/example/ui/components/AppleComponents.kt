package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Website
import com.example.ui.theme.LocalAppleColors
import com.example.ui.util.getCategoryAccentColor

@Composable
fun AppleSheetDragHandle() {
    val appleColors = LocalAppleColors.current
    Box(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 6.dp)
            .width(36.dp)
            .height(5.dp)
            .background(appleColors.secondaryLabel.copy(alpha = 0.25f), RoundedCornerShape(3.dp))
    )
}

@Composable
fun AppleFloatingIconButton(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String,
    testTag: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp
) {
    val appleColors = LocalAppleColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "floating_btn_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = if (appleColors.isDark) Color(0x24FFFFFF) else Color(0x0D000000),
        border = BorderStroke(0.5.dp, if (appleColors.isDark) Color(0x26FFFFFF) else Color(0x12000000)),
        shadowElevation = 0.dp,
        modifier = modifier
            .size(size)
            .scale(scale)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = appleColors.label,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun AppleFloatingIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp
) {
    AppleFloatingIconButton(
        onClick = onClick,
        painter = rememberVectorPainter(image = icon),
        contentDescription = contentDescription,
        testTag = testTag,
        modifier = modifier,
        size = size,
        iconSize = iconSize
    )
}

@Composable
fun AppleNavigationHeader(
    selectedCategory: String,
    searchQuery: String,
    isSearchExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val appleColors = LocalAppleColors.current
    val haptics = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        } else {
            keyboardController?.hide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        AnimatedContent(
            targetState = isSearchExpanded,
            transitionSpec = {
                if (targetState) {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideInHorizontally(
                                initialOffsetX = { it / 3 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ))
                        .togetherWith(
                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                    slideOutHorizontally(
                                        targetOffsetX = { -it / 3 },
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                        )
                } else {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideInHorizontally(
                                initialOffsetX = { -it / 3 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ))
                        .togetherWith(
                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                    slideOutHorizontally(
                                        targetOffsetX = { it / 3 },
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                        )
                }
            },
            label = "header_search_transition"
        ) { expanded ->
            if (expanded) {
                // Active Search State: Capsule Search Input + Floating Close Button
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = appleColors.fill,
                        border = BorderStroke(0.5.dp, appleColors.separator.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = "Search",
                                tint = appleColors.secondaryLabel,
                                modifier = Modifier.size(18.dp)
                            )

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search stack, URLs, tags...",
                                        color = appleColors.tertiaryLabel,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onQueryChange,
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = appleColors.label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(appleColors.accent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .testTag("search_bar_input"),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearQuery,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = appleColors.label.copy(alpha = 0.15f),
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = appleColors.label,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AppleFloatingIconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClearQuery()
                            onSearchExpandedChange(false)
                        },
                        icon = Icons.Default.Close,
                        contentDescription = "Close Search",
                        testTag = "close_search_button"
                    )
                }
            } else {
                // Resting State: Floating Settings Button + Centered WebStack Title + Floating Search Button
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppleFloatingIconButton(
                        onClick = onOpenSettings,
                        painter = painterResource(id = R.drawable.ic_nav_more),
                        contentDescription = "Settings and Preferences",
                        testTag = "settings_button"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "WebStack",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = appleColors.label,
                            letterSpacing = (-0.4).sp,
                            maxLines = 1
                        )
                        Text(
                            text = if (selectedCategory == "All") "VISUAL BOOKMARKS" else selectedCategory.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCategory == "All") appleColors.tertiaryLabel else appleColors.accent,
                            letterSpacing = 1.6.sp,
                            maxLines = 1
                        )
                    }

                    AppleFloatingIconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSearchExpandedChange(true)
                        },
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search Bookmarks",
                        testTag = "search_button"
                    )
                }
            }
        }
    }
}

@Composable
fun AppleCategoryCapsuleBar(
    allWebsites: List<Website>,
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onTagLongPress: (String) -> Unit,
    onAddNewTag: () -> Unit
) {
    val appleColors = LocalAppleColors.current
    val categoryCounts = remember(allWebsites) {
        allWebsites.groupingBy { it.category.lowercase() }.eachCount()
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" Capsule
        val isAllSelected = selectedCategory.equals("All", ignoreCase = true)
        item {
            AppleCapsule(
                text = "All",
                count = allWebsites.size,
                isSelected = isAllSelected,
                onClick = { onSelectCategory("All") },
                onLongClick = null
            )
        }

        // Category Capsules (Supports single-tap filter & long-press options)
        items(categories) { cat ->
            val isSelected = selectedCategory.equals(cat, ignoreCase = true)
            val count = categoryCounts[cat.lowercase()] ?: 0
            val accentColor = getCategoryAccentColor(cat, appleColors.isDark)

            AppleCapsule(
                text = cat,
                count = count,
                isSelected = isSelected,
                customAccentColor = if (isSelected) null else accentColor,
                onClick = { onSelectCategory(cat) },
                onLongClick = { onTagLongPress(cat) }
            )
        }

        // "+ Tag" Capsule with Apple Pill Design
        item {
            Surface(
                onClick = onAddNewTag,
                color = appleColors.fill,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(0.75.dp, appleColors.separator),
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom Tag",
                        tint = appleColors.label,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Tag",
                        color = appleColors.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleCapsule(
    text: String,
    count: Int,
    isSelected: Boolean,
    customAccentColor: Color? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val appleColors = LocalAppleColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "capsule_scale"
    )

    Surface(
        color = if (isSelected) appleColors.label else appleColors.surface,
        border = BorderStroke(
            0.5.dp,
            if (isSelected) Color.Transparent else appleColors.separator
        ),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = if (isSelected && !appleColors.isDark) 2.dp else 0.dp,
        modifier = Modifier
            .height(34.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("category_item_$text")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (customAccentColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(customAccentColor, CircleShape)
                )
            }
            Text(
                text = text,
                color = if (isSelected) appleColors.systemBackground else appleColors.label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (count > 0) {
                AppleCapsuleCountBadge(
                    count = count,
                    isSelected = isSelected
                )
            }
        }
    }
}

@Composable
private fun AppleCapsuleCountBadge(
    count: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val appleColors = LocalAppleColors.current
    val density = LocalDensity.current
    val text = "$count"
    val textColor = if (isSelected) appleColors.systemBackground else appleColors.secondaryLabel
    val bgColor = if (isSelected) appleColors.systemBackground.copy(alpha = 0.25f) else appleColors.fill

    val textPaint = remember(textColor, density) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textSize = with(density) { 10.sp.toPx() }
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            color = textColor.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    val bounds = remember(text, textPaint) {
        android.graphics.Rect().also {
            textPaint.getTextBounds(text, 0, text.length, it)
        }
    }

    val badgeWidthDp = remember(count, bounds, density) {
        if (count > 9) {
            val textWidthDp = with(density) { bounds.width().toDp() }
            maxOf(18.dp, textWidthDp + 10.dp)
        } else {
            18.dp
        }
    }

    Canvas(
        modifier = modifier
            .height(18.dp)
            .width(badgeWidthDp)
    ) {
        drawRoundRect(
            color = bgColor,
            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
        )
        val baselineY = size.height / 2f - (bounds.top + bounds.bottom) / 2f
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText(text, size.width / 2f, baselineY, textPaint)
        }
    }
}

@Composable
fun AppleEmptyState(
    searchQuery: String,
    selectedCategory: String,
    onClearSearch: () -> Unit,
    onShowAll: () -> Unit,
    onAddLink: () -> Unit
) {
    val appleColors = LocalAppleColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = appleColors.fill,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            searchQuery.isNotBlank() -> Icons.Outlined.Search
                            selectedCategory == "All" -> Icons.Outlined.Layers
                            else -> Icons.Default.FilterList
                        },
                        contentDescription = null,
                        tint = appleColors.secondaryLabel,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = when {
                    searchQuery.isNotBlank() -> "No Matching Bookmarks"
                    selectedCategory == "All" -> "No Links in Stack"
                    else -> "No Links in \"$selectedCategory\""
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = appleColors.label,
                letterSpacing = (-0.3).sp
            )

            val descriptionText = when {
                searchQuery.isNotBlank() -> "No bookmarks matched \"$searchQuery\". Try checking for typos or searching another term."
                selectedCategory == "All" -> "Tap the '+' button below or share links from Safari, Chrome, or any app."
                else -> null
            }

            if (descriptionText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = descriptionText,
                    fontSize = 13.sp,
                    color = appleColors.secondaryLabel,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(if (descriptionText != null) 20.dp else 16.dp))

            when {
                searchQuery.isNotBlank() -> {
                    Button(
                        onClick = onClearSearch,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appleColors.label,
                            contentColor = appleColors.systemBackground
                        )
                    ) {
                        Text("Clear Search", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                selectedCategory != "All" -> {
                    Button(
                        onClick = onShowAll,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appleColors.label,
                            contentColor = appleColors.systemBackground
                        )
                    ) {
                        Text("Show All Links", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = onAddLink,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appleColors.label,
                            contentColor = appleColors.systemBackground
                        )
                    ) {
                        Text("Add Your First Link", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
