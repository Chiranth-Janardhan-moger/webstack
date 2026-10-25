package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

import com.example.data.Website
import androidx.compose.foundation.clickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.remember

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.getCategoryAccentColor

@Composable
fun TagOptionsBottomSheetContent(
    tagName: String,
    bookmarkCount: Int = 0,
    isParked: Boolean = false,
    onEdit: () -> Unit,
    onToggleHide: () -> Unit,
    onSelectAll: () -> Unit,
    onShareLinks: () -> Unit,
    onCustomizeAll: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val catAccent = getCategoryAccentColor(tagName, appColors.isDark)
    val isDefault = tagName.equals("Personal", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header: Tag Badge + Tag Name + Count + Dismiss 'X'
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = catAccent.copy(alpha = if (appColors.isDark) 0.25f else 0.15f),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tagName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = appColors.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$bookmarkCount ${if (bookmarkCount == 1) "bookmark" else "bookmarks"}",
                        fontSize = 12.sp,
                        color = appColors.secondaryLabel
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appColors.secondaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3x2 Action Grid: Layout A (6 Balanced Options)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Edit & Hide from Home (or Show on Home)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    label = "Edit",
                    icon = Icons.Outlined.Edit,
                    onClick = {
                        if (isDefault) {
                            Toast.makeText(context, "Personal is the default category and cannot be edited", Toast.LENGTH_SHORT).show()
                        } else {
                            onEdit()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "action_edit_tag"
                )
                ActionTile(
                    label = if (isParked) "Show on Home" else "Hide from Home",
                    icon = if (isParked) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    onClick = {
                        if (isDefault) {
                            Toast.makeText(context, "Personal is the default category and cannot be hidden", Toast.LENGTH_SHORT).show()
                        } else {
                            onToggleHide()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "action_toggle_hide_tag"
                )
            }

            // Row 2: Select All & Share Links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    label = "Select All",
                    icon = Icons.Outlined.Checklist,
                    onClick = onSelectAll,
                    modifier = Modifier.weight(1f),
                    testTag = "action_select_all_tag"
                )
                ActionTile(
                    label = "Share Links",
                    icon = Icons.Outlined.Share,
                    onClick = onShareLinks,
                    modifier = Modifier.weight(1f),
                    testTag = "action_share_tag_links"
                )
            }

            // Row 3: Customize & Delete (Red)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    label = "Customize",
                    icon = Icons.Outlined.Tune,
                    onClick = onCustomizeAll,
                    modifier = Modifier.weight(1f),
                    testTag = "action_customize_all_tags"
                )
                ActionTile(
                    label = "Delete",
                    icon = Icons.Outlined.Delete,
                    onClick = {
                        if (isDefault) {
                            Toast.makeText(context, "Personal is the default category and cannot be deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            onDelete()
                        }
                    },
                    isDestructive = true,
                    modifier = Modifier.weight(1f),
                    testTag = "action_delete_tag"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}


@Composable
fun MoveToCategoryBottomSheetContent(
    selectedCount: Int,
    categories: List<String>,
    allWebsites: List<Website>?,
    onSelectCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current

    val categoryCounts = remember(allWebsites) {
        val current = allWebsites ?: emptyList()
        current.groupingBy { it.category.lowercase() }.eachCount()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Move $selectedCount ${if (selectedCount == 1) "Bookmark" else "Bookmarks"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appColors.label
                )
                Text(
                    text = "Select a destination category",
                    fontSize = 13.sp,
                    color = appColors.secondaryLabel
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appColors.secondaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.secondaryGroupedBackground,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                categories.forEachIndexed { index, cat ->
                    val catAccent = getCategoryAccentColor(cat, appColors.isDark)
                    val count = categoryCounts[cat.lowercase()] ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectCategory(cat)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = catAccent.copy(alpha = if (appColors.isDark) 0.25f else 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Label,
                                        contentDescription = null,
                                        tint = catAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = cat,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = appColors.label
                            )
                        }

                        Text(
                            text = "($count)",
                            fontSize = 13.sp,
                            color = appColors.secondaryLabel
                        )
                    }

                    if (index < categories.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = appColors.separator
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
