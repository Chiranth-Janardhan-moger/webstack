package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.getCategoryAccentColor

@Composable
fun TagOptionsBottomSheetContent(
    tagName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFilterByTag: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    val catAccent = getCategoryAccentColor(tagName, appColors.isDark)

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
                Column {
                    Text(
                        text = tagName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.label
                    )
                    Text(
                        text = "Tag Options",
                        fontSize = 12.sp,
                        color = appColors.secondaryLabel
                    )
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appColors.secondaryLabel
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
            color = appColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appColors.separator),
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
                    tint = appColors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Filter links by \"$tagName\"",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = appColors.label
                )
            }
        }

        // Action 2: Edit Tag
        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onEdit()
            },
            color = appColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appColors.separator),
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
                    tint = appColors.label,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Edit Tag",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = appColors.label
                )
            }
        }

        // Action 3: Delete Tag (Destructive red color text and icon)
        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            },
            color = appColors.destructive.copy(alpha = if (appColors.isDark) 0.15f else 0.08f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appColors.destructive.copy(alpha = 0.3f)),
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
                    tint = appColors.destructive,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Delete Tag",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = appColors.destructive
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
