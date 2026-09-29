package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Website
import com.example.ui.theme.LocalAppleColors
import com.example.ui.util.getCategoryAccentColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleFilterMenuBottomSheetContent(
    allWebsites: List<Website>,
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAddTagClick: () -> Unit,
    onTagLongPress: (String) -> Unit
) {
    val appleColors = LocalAppleColors.current
    val categoryCounts = remember(allWebsites) {
        allWebsites.groupingBy { it.category.lowercase() }.eachCount()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tags & Categories",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = appleColors.label,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to filter • Long press to manage",
                    fontSize = 12.sp,
                    color = appleColors.secondaryLabel
                )
            }

            Surface(
                onClick = onAddTagClick,
                color = appleColors.fill,
                shape = CircleShape,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("menu_header_add_tag_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Tag",
                        tint = appleColors.label,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inset Grouped Section for All Links
        val isAllSelected = selectedCategory.equals("All", ignoreCase = true)
        Surface(
            onClick = { onSelectCategory("All") },
            color = if (isAllSelected) appleColors.label else appleColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, if (isAllSelected) Color.Transparent else appleColors.separator),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isAllSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = appleColors.systemBackground,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "All Links",
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp,
                        color = if (isAllSelected) appleColors.systemBackground else appleColors.label
                    )
                }

                Text(
                    text = "${allWebsites.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAllSelected) appleColors.systemBackground.copy(alpha = 0.7f) else appleColors.secondaryLabel
                )
            }
        }

        // Custom & Default Categories List
        categories.forEach { category ->
            val count = categoryCounts[category.lowercase()] ?: 0
            val isSelected = selectedCategory.equals(category, ignoreCase = true)
            val catAccent = getCategoryAccentColor(category, appleColors.isDark)

            Surface(
                color = if (isSelected) appleColors.label else appleColors.surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.75.dp, if (isSelected) Color.Transparent else appleColors.separator),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectCategory(category) },
                        onLongClick = { onTagLongPress(category) }
                    )
                    .testTag("category_item_$category")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = appleColors.systemBackground,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(catAccent, CircleShape)
                            )
                        }
                        Text(
                            text = category,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp,
                            color = if (isSelected) appleColors.systemBackground else appleColors.label
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$count",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) appleColors.systemBackground.copy(alpha = 0.7f) else appleColors.secondaryLabel
                        )
                        IconButton(
                            onClick = { onTagLongPress(category) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Tag Options",
                                tint = if (isSelected) appleColors.systemBackground.copy(alpha = 0.7f) else appleColors.tertiaryLabel,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
fun AppleTagOptionsBottomSheetContent(
    tagName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFilterByTag: () -> Unit,
    onDismiss: () -> Unit
) {
    val appleColors = LocalAppleColors.current
    val haptics = LocalHapticFeedback.current
    val catAccent = getCategoryAccentColor(tagName, appleColors.isDark)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = catAccent.copy(alpha = if (appleColors.isDark) 0.25f else 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Label,
                            contentDescription = null,
                            tint = catAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = tagName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appleColors.label
                    )
                    Text(
                        text = "Tag Options",
                        fontSize = 12.sp,
                        color = appleColors.secondaryLabel
                    )
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appleColors.secondaryLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action 1: Filter links by "[Tag Name]"
        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onFilterByTag()
            },
            color = appleColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = appleColors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Filter links by \"$tagName\"",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = appleColors.label
                )
            }
        }

        // Action 2: Edit Tag
        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onEdit()
            },
            color = appleColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = appleColors.label,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Edit Tag",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = appleColors.label
                )
            }
        }

        // Action 3: Delete Tag (Destructive red color text and icon)
        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            },
            color = appleColors.destructive.copy(alpha = if (appleColors.isDark) 0.15f else 0.08f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.destructive.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = appleColors.destructive,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Delete Tag",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = appleColors.destructive
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
