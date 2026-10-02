package com.example.ui.sheets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import com.example.ui.components.AppSubSheetHeader
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors

enum class ExportFormat {
    WEBSTACK,
    JSON
}

@Composable
fun BackupRestoreSubSheetContent(
    totalBookmarks: Int,
    onExportWebstack: () -> Unit,
    onExportJson: () -> Unit,
    onRestore: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.WEBSTACK) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
            AppSubSheetHeader(
                title = "Backup & Restore",
                onBack = onBack,
                onDismiss = onDismiss
            )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Backup
        Text(
            text = "CREATE BACKUP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Card 1: Backup
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.CloudUpload,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Full Library Backup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = appColors.label
                        )
                        Text(
                            text = if (totalBookmarks == 1) "1 bookmark ready to backup" else "$totalBookmarks bookmarks ready to backup",
                            fontSize = 13.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "FORMAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.secondaryLabel,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Segmented Slider Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .padding(3.dp)
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val segmentWidth = maxWidth / 2
                        val targetOffset = if (selectedFormat == ExportFormat.WEBSTACK) 0.dp else segmentWidth
                        val animatedOffset by animateDpAsState(
                            targetValue = targetOffset,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "segment_slider"
                        )

                        // Animated sliding thumb
                        Box(
                            modifier = Modifier
                                .offset(x = animatedOffset)
                                .width(segmentWidth)
                                .fillMaxHeight()
                                .background(
                                    color = appColors.label,
                                    shape = RoundedCornerShape(9.dp)
                                )
                        )

                        // Interactive touch segments
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Option: .webstack
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (selectedFormat != ExportFormat.WEBSTACK) {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedFormat = ExportFormat.WEBSTACK
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ".webstack",
                                    fontWeight = if (selectedFormat == ExportFormat.WEBSTACK) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (selectedFormat == ExportFormat.WEBSTACK) appColors.systemBackground else appColors.secondaryLabel
                                )
                            }

                            // Option: .json
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (selectedFormat != ExportFormat.JSON) {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedFormat = ExportFormat.JSON
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ".json",
                                    fontWeight = if (selectedFormat == ExportFormat.JSON) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (selectedFormat == ExportFormat.JSON) appColors.systemBackground else appColors.secondaryLabel
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bouncy Export Button
                val exportInteractionSource = remember { MutableInteractionSource() }
                val isExportPressed by exportInteractionSource.collectIsPressedAsState()
                val exportScale by animateFloatAsState(
                    targetValue = if (isExportPressed) 0.96f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "export_btn_bouncy"
                )

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (selectedFormat == ExportFormat.WEBSTACK) {
                            onExportWebstack()
                        } else {
                            onExportJson()
                        }
                    },
                    interactionSource = exportInteractionSource,
                    shape = RoundedCornerShape(12.dp),
                    color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                    border = BorderStroke(0.75.dp, appColors.separator),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .graphicsLayer {
                            scaleX = exportScale
                            scaleY = exportScale
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudUpload,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (selectedFormat == ExportFormat.WEBSTACK) "Export .webstack Backup" else "Export .json Backup",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = appColors.label
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 2: Restore
        Text(
            text = "RESTORE BOOKMARKS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Card 2: Restore
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Restore,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Import Backup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = appColors.label
                        )
                        Text(
                            text = "Restore from .webstack or .json file",
                            fontSize = 13.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Merge callout
                Surface(
                    color = if (appColors.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = appColors.secondaryLabel,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .size(14.dp)
                        )
                        Text(
                            text = "Merges new bookmarks without replacing existing ones.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors.secondaryLabel,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bouncy Select Backup File Button
                val restoreInteractionSource = remember { MutableInteractionSource() }
                val isRestorePressed by restoreInteractionSource.collectIsPressedAsState()
                val restoreScale by animateFloatAsState(
                    targetValue = if (isRestorePressed) 0.96f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "restore_btn_bouncy"
                )

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onRestore()
                    },
                    interactionSource = restoreInteractionSource,
                    shape = RoundedCornerShape(12.dp),
                    color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                    border = BorderStroke(0.75.dp, appColors.separator),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .graphicsLayer {
                            scaleX = restoreScale
                            scaleY = restoreScale
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Select Backup File",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = appColors.label
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Done button
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.label,
                contentColor = appColors.systemBackground
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
