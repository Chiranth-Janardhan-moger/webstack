package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors

@Composable
private fun TagBaseDialog(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    initialValue: String = "",
    placeholder: String,
    confirmText: String,
    suggestedTags: List<String> = emptyList(),
    canConfirm: (String) -> Boolean = { it.isNotBlank() },
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    var tagName by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.secondaryGroupedBackground,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(BorderStroke(0.75.dp, appColors.separator), RoundedCornerShape(24.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = iconBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appColors.label
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = appColors.fill,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.75.dp, appColors.separator.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (tagName.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = appColors.tertiaryLabel,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }
                            BasicTextField(
                                value = tagName,
                                onValueChange = { tagName = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = appColors.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(appColors.accent),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (canConfirm(tagName.trim())) {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onConfirm(tagName.trim())
                                        }
                                    }
                                )
                            )
                        }

                        if (tagName.isNotEmpty()) {
                            IconButton(
                                onClick = { tagName = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = appColors.label.copy(alpha = 0.15f),
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear tag input",
                                            tint = appColors.label,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (suggestedTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "SUGGESTED TAGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.secondaryLabel,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestedTags) { suggestion ->
                            val isCurrent = tagName.equals(suggestion, ignoreCase = true)
                            Surface(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    tagName = suggestion
                                },
                                color = if (isCurrent) appColors.label else appColors.surface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.75.dp, if (isCurrent) Color.Transparent else appColors.separator)
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) appColors.systemBackground else appColors.label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canConfirm(tagName.trim())) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConfirm(tagName.trim())
                    }
                },
                enabled = canConfirm(tagName.trim()),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.label,
                    contentColor = appColors.systemBackground,
                    disabledContainerColor = appColors.fill,
                    disabledContentColor = appColors.tertiaryLabel
                )
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = appColors.secondaryLabel)
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun AddTagDialog(
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    TagBaseDialog(
        title = "New Tag",
        icon = Icons.AutoMirrored.Outlined.Label,
        iconTint = appColors.accent,
        iconBg = appColors.accent.copy(alpha = if (appColors.isDark) 0.25f else 0.15f),
        placeholder = "e.g., AI, Finance, Inspo",
        confirmText = "Create Tag",
        suggestedTags = listOf("Inspiration", "Finance", "Social", "AI Tools", "Dev", "Articles", "Design", "Research"),
        onConfirm = onAdd,
        onDismiss = onDismiss
    )
}

@Composable
fun EditTagDialog(
    currentName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    TagBaseDialog(
        title = "Edit Tag",
        icon = Icons.Outlined.Edit,
        iconTint = appColors.label,
        iconBg = appColors.fill,
        initialValue = currentName,
        placeholder = "Enter tag name",
        confirmText = "Save",
        canConfirm = { it.isNotBlank() && !it.equals(currentName, ignoreCase = false) },
        onConfirm = onSave,
        onDismiss = onDismiss
    )
}

@Composable
fun DeleteTagConfirmDialog(
    tagName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.secondaryGroupedBackground,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.border(BorderStroke(0.75.dp, appColors.separator), RoundedCornerShape(22.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = appColors.destructive,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Delete Tag",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appColors.label
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$tagName\"? Saved links will remain safe in \"Personal\".",
                fontSize = 14.sp,
                color = appColors.secondaryLabel,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.destructive,
                    contentColor = Color.White
                )
            ) {
                Text("Delete Tag", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = appColors.secondaryLabel)
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
}
