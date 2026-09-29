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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppleColors

@Composable
fun AppleAddTagDialog(
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val appleColors = LocalAppleColors.current
    val haptics = LocalHapticFeedback.current
    var tagName by remember { mutableStateOf("") }
    val suggestedTags = listOf("Inspiration", "Finance", "Social", "AI Tools", "Dev", "Articles", "Design", "Research")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appleColors.secondaryGroupedBackground,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(BorderStroke(0.75.dp, appleColors.separator), RoundedCornerShape(24.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = appleColors.accent.copy(alpha = if (appleColors.isDark) 0.25f else 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Label,
                            contentDescription = null,
                            tint = appleColors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "New Tag",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appleColors.label
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = appleColors.fill,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.75.dp, appleColors.separator.copy(alpha = 0.6f)),
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
                                    text = "e.g., AI, Finance, Inspo",
                                    color = appleColors.tertiaryLabel,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }
                            BasicTextField(
                                value = tagName,
                                onValueChange = { tagName = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = appleColors.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(appleColors.accent),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (tagName.isNotBlank()) {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onAdd(tagName.trim())
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
                                    color = appleColors.label.copy(alpha = 0.15f),
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear tag input",
                                            tint = appleColors.label,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SUGGESTED TAGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = appleColors.secondaryLabel,
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
                            color = if (isCurrent) appleColors.label else appleColors.surface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.75.dp, if (isCurrent) Color.Transparent else appleColors.separator)
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) appleColors.systemBackground else appleColors.label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tagName.isNotBlank()) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAdd(tagName.trim())
                    }
                },
                enabled = tagName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appleColors.label,
                    contentColor = appleColors.systemBackground,
                    disabledContainerColor = appleColors.fill,
                    disabledContentColor = appleColors.tertiaryLabel
                )
            ) {
                Text("Create Tag", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = appleColors.secondaryLabel)
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun AppleEditTagDialog(
    currentName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val appleColors = LocalAppleColors.current
    val haptics = LocalHapticFeedback.current
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appleColors.secondaryGroupedBackground,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(BorderStroke(0.75.dp, appleColors.separator), RoundedCornerShape(24.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = appleColors.fill,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = appleColors.label,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "Edit Tag",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appleColors.label
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = appleColors.fill,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.75.dp, appleColors.separator.copy(alpha = 0.6f)),
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
                            if (newName.isEmpty()) {
                                Text(
                                    text = "Enter tag name",
                                    color = appleColors.tertiaryLabel,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }
                            BasicTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = appleColors.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(appleColors.accent),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newName.isNotBlank() && !newName.equals(currentName, ignoreCase = false)) {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onSave(newName.trim())
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank()) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSave(newName.trim())
                    }
                },
                enabled = newName.isNotBlank() && !newName.equals(currentName, ignoreCase = false),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appleColors.label,
                    contentColor = appleColors.systemBackground,
                    disabledContainerColor = appleColors.fill,
                    disabledContentColor = appleColors.tertiaryLabel
                )
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = appleColors.secondaryLabel)
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun AppleDeleteTagConfirmDialog(
    tagName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val appleColors = LocalAppleColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appleColors.secondaryGroupedBackground,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.border(BorderStroke(0.75.dp, appleColors.separator), RoundedCornerShape(22.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = appleColors.destructive,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Delete Tag",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appleColors.label
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$tagName\"? Saved links will remain safe in \"Personal\".",
                fontSize = 14.sp,
                color = appleColors.secondaryLabel,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appleColors.destructive,
                    contentColor = Color.White
                )
            ) {
                Text("Delete Tag", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = appleColors.secondaryLabel)
            ) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        }
    )
}
