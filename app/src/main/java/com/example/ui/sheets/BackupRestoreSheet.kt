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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.components.AppExplainerDialog
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.components.BackupFormatsExplainerDialog
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
    JSON,
    HTML,
    TXT
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BackupRestoreSubSheetContent(
    totalBookmarks: Int,
    onExportWebstack: () -> Unit,
    onExportJson: () -> Unit,
    onExportHtml: () -> Unit = {},
    onExportTxt: () -> Unit = {},
    onRestore: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.WEBSTACK) }
    var showFormatsExplainer by remember { mutableStateOf(false) }
    var showImportExplainer by remember { mutableStateOf(false) }

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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showFormatsExplainer = true
                            }
                        )
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
                            text = "Export Backup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = appColors.label
                        )
                        Text(
                            text = if (totalBookmarks == 1) "1 bookmark ready" else "$totalBookmarks bookmarks ready",
                            fontSize = 13.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                        val segmentWidth = maxWidth / 4
                        val targetOffset = when (selectedFormat) {
                            ExportFormat.WEBSTACK -> 0.dp
                            ExportFormat.JSON -> segmentWidth
                            ExportFormat.HTML -> segmentWidth * 2
                            ExportFormat.TXT -> segmentWidth * 3
                        }
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
                                .offset { IntOffset(x = animatedOffset.roundToPx(), y = 0) }
                                .width(segmentWidth)
                                .fillMaxHeight()
                                .background(
                                    color = appColors.label,
                                    shape = RoundedCornerShape(9.dp)
                                )
                        )

                        // Interactive touch segments (Tap to select, long-press to explain)
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Option: .webstack
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            if (selectedFormat != ExportFormat.WEBSTACK) {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedFormat = ExportFormat.WEBSTACK
                                            }
                                        },
                                        onLongClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showFormatsExplainer = true
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ".webstack",
                                    fontWeight = if (selectedFormat == ExportFormat.WEBSTACK) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (selectedFormat == ExportFormat.WEBSTACK) appColors.systemBackground else appColors.secondaryLabel
                                )
                            }

                            // Option: .json
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            if (selectedFormat != ExportFormat.JSON) {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedFormat = ExportFormat.JSON
                                            }
                                        },
                                        onLongClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showFormatsExplainer = true
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ".json",
                                    fontWeight = if (selectedFormat == ExportFormat.JSON) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (selectedFormat == ExportFormat.JSON) appColors.systemBackground else appColors.secondaryLabel
                                )
                            }

                            // Option: .html
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            if (selectedFormat != ExportFormat.HTML) {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedFormat = ExportFormat.HTML
                                            }
                                        },
                                        onLongClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showFormatsExplainer = true
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ".html",
                                    fontWeight = if (selectedFormat == ExportFormat.HTML) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (selectedFormat == ExportFormat.HTML) appColors.systemBackground else appColors.secondaryLabel
                                )
                            }

                            // Option: .txt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            if (selectedFormat != ExportFormat.TXT) {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedFormat = ExportFormat.TXT
                                            }
                                        },
                                        onLongClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showFormatsExplainer = true
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ".txt",
                                    fontWeight = if (selectedFormat == ExportFormat.TXT) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (selectedFormat == ExportFormat.TXT) appColors.systemBackground else appColors.secondaryLabel
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
                        when (selectedFormat) {
                            ExportFormat.WEBSTACK -> onExportWebstack()
                            ExportFormat.JSON -> onExportJson()
                            ExportFormat.HTML -> onExportHtml()
                            ExportFormat.TXT -> onExportTxt()
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
                                text = when (selectedFormat) {
                                    ExportFormat.WEBSTACK -> "Export .webstack"
                                    ExportFormat.JSON -> "Export .json"
                                    ExportFormat.HTML -> "Export .html"
                                    ExportFormat.TXT -> "Export .txt"
                                },
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showImportExplainer = true
                            }
                        )
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
                            text = "Restore from .webstack, .json, or .html file",
                            fontSize = 13.sp,
                            color = appColors.secondaryLabel
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
                        .combinedClickable(
                            interactionSource = restoreInteractionSource,
                            indication = null,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onRestore()
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showImportExplainer = true
                            }
                        )
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
                                text = "Choose File",
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

    if (showFormatsExplainer) {
        BackupFormatsExplainerDialog(onDismiss = { showFormatsExplainer = false })
    }

    if (showImportExplainer) {
        AppExplainerDialog(
            title = "Import Backup",
            description = "Imports merge seamlessly. Existing bookmarks are never duplicated or overwritten.",
            icon = Icons.Outlined.Restore,
            onDismiss = { showImportExplainer = false }
        )
    }
}
