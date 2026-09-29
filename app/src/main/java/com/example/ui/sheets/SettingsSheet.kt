package com.example.ui.sheets

import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.ui.model.AppThemeMode
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.model.WebStackLayoutMode
import com.example.ui.theme.LocalAppleColors
import com.example.ui.viewmodel.WebsiteViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppleSettingsBottomSheetContent(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onSetThemeMode: (AppThemeMode) -> Unit = {},
    layoutMode: WebStackLayoutMode,
    onSetLayoutMode: (WebStackLayoutMode) -> Unit,
    fetchWebPreviews: Boolean,
    onToggleFetchWebPreviews: (Boolean) -> Unit,
    onOpenWhatsNew: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: WebsiteViewModel? = null
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val appleColors = LocalAppleColors.current
    var showExportFormatDialog by remember { mutableStateOf(false) }

    val exportWebstackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null && viewModel != null) {
            coroutineScope.launch {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val result = viewModel.exportBackup(outputStream)
                    result.onSuccess { count ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "Exported $count bookmarks (.webstack)", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Export failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && viewModel != null) {
            coroutineScope.launch {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val result = viewModel.exportBackup(outputStream)
                    result.onSuccess { count ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "Exported $count bookmarks (.json)", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Export failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && viewModel != null) {
            val displayName = try {
                context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                } ?: ""
            } catch (_: Exception) {
                ""
            }

            val isSupported = displayName.endsWith(".webstack", ignoreCase = true) ||
                    displayName.endsWith(".json", ignoreCase = true)

            if (!isSupported && displayName.isNotEmpty()) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                Toast.makeText(
                    context,
                    "Invalid format. Please select a .webstack or .json backup",
                    Toast.LENGTH_LONG
                ).show()
                return@rememberLauncherForActivityResult
            }

            coroutineScope.launch {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val result = viewModel.restoreBackup(inputStream)
                        result.onSuccess { (imported, newCats) ->
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            val msg = if (imported > 0) {
                                "Restored $imported bookmarks ($newCats new categories)"
                            } else {
                                "All bookmarks in backup are already in your stack"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            Toast.makeText(context, "Restore failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open backup file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
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
                    color = appleColors.label,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_nav_more),
                            contentDescription = null,
                            tint = appleColors.systemBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = appleColors.label,
                    letterSpacing = (-0.4).sp
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appleColors.secondaryLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Appearance
        Text(
            text = "APPEARANCE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appleColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        var themeDropdownExpanded by remember { mutableStateOf(false) }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = appleColors.surface,
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = appleColors.accent,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.BrightnessMedium,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Appearance",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = appleColors.label
                        )
                        Text(
                            text = "Theme preference",
                            fontSize = 12.sp,
                            color = appleColors.secondaryLabel
                        )
                    }
                }

                Box {
                    Surface(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            themeDropdownExpanded = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = appleColors.secondaryGroupedBackground,
                        border = BorderStroke(0.75.dp, appleColors.separator)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = themeMode.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appleColors.label
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = appleColors.secondaryLabel,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    MaterialTheme(
                        colorScheme = MaterialTheme.colorScheme.copy(
                            surface = appleColors.surface
                        ),
                        shapes = MaterialTheme.shapes.copy(
                            extraSmall = RoundedCornerShape(14.dp)
                        )
                    ) {
                        DropdownMenu(
                            expanded = themeDropdownExpanded,
                            onDismissRequest = { themeDropdownExpanded = false },
                            modifier = Modifier
                                .background(appleColors.surface, RoundedCornerShape(14.dp))
                                .border(BorderStroke(0.5.dp, appleColors.separator), RoundedCornerShape(14.dp))
                        ) {
                            AppThemeMode.entries.forEach { mode ->
                                val isSelected = themeMode == mode
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = mode.title,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) appleColors.accent else appleColors.label
                                        )
                                    },
                                    trailingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = appleColors.accent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSetThemeMode(mode)
                                        themeDropdownExpanded = false
                                    },
                                    modifier = Modifier.height(42.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Layout
        Text(
            text = "LAYOUT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appleColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Large Cards Option
        val isLarge = layoutMode == WebStackLayoutMode.LARGE_CARDS
        Surface(
            onClick = { onSetLayoutMode(WebStackLayoutMode.LARGE_CARDS) },
            shape = RoundedCornerShape(14.dp),
            color = if (isLarge) appleColors.surface else appleColors.secondaryGroupedBackground,
            border = BorderStroke(
                width = if (isLarge) 1.5.dp else 0.5.dp,
                color = if (isLarge) appleColors.accent else appleColors.separator
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = if (isLarge) appleColors.accent else appleColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = null,
                                tint = if (isLarge) Color.White else appleColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "Large Cards",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = appleColors.label
                    )
                }

                Icon(
                    imageVector = if (isLarge) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isLarge) appleColors.accent else appleColors.tertiaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2-Card Grid Option
        val isGrid = layoutMode == WebStackLayoutMode.GRID_CARDS
        Surface(
            onClick = { onSetLayoutMode(WebStackLayoutMode.GRID_CARDS) },
            shape = RoundedCornerShape(14.dp),
            color = if (isGrid) appleColors.surface else appleColors.secondaryGroupedBackground,
            border = BorderStroke(
                width = if (isGrid) 1.5.dp else 0.5.dp,
                color = if (isGrid) appleColors.accent else appleColors.separator
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = if (isGrid) appleColors.accent else appleColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = if (isGrid) Color.White else appleColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "2-Card Grid",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = appleColors.label
                    )
                }

                Icon(
                    imageVector = if (isGrid) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isGrid) appleColors.accent else appleColors.tertiaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Compact List View Option
        val isCompact = layoutMode == WebStackLayoutMode.COMPACT_LIST
        Surface(
            onClick = { onSetLayoutMode(WebStackLayoutMode.COMPACT_LIST) },
            shape = RoundedCornerShape(14.dp),
            color = if (isCompact) appleColors.surface else appleColors.secondaryGroupedBackground,
            border = BorderStroke(
                width = if (isCompact) 1.5.dp else 0.5.dp,
                color = if (isCompact) appleColors.accent else appleColors.separator
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = if (isCompact) appleColors.accent else appleColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ViewList,
                                contentDescription = null,
                                tint = if (isCompact) Color.White else appleColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "Compact List",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = appleColors.label
                    )
                }

                Icon(
                    imageVector = if (isCompact) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isCompact) appleColors.accent else appleColors.tertiaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Network
        Text(
            text = "NETWORK",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appleColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = appleColors.surface,
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (fetchWebPreviews) appleColors.accent else appleColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = if (fetchWebPreviews) Color.White else appleColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "Fetch Web Previews",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appleColors.label
                    )
                }

                Switch(
                    checked = fetchWebPreviews,
                    onCheckedChange = onToggleFetchWebPreviews,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = appleColors.accent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Storage
        Text(
            text = "STORAGE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appleColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Card 1: Export Backup
        Surface(
            onClick = {
                showExportFormatDialog = true
            },
            shape = RoundedCornerShape(14.dp),
            color = appleColors.surface,
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = appleColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_export),
                                contentDescription = null,
                                tint = appleColors.label,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Text(
                        text = "Export Backup",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = appleColors.label
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = appleColors.tertiaryLabel,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Card 2: Restore Bookmarks
        Surface(
            onClick = {
                restoreLauncher.launch(
                    arrayOf(
                        "application/json",
                        "application/octet-stream",
                        "text/plain"
                    )
                )
            },
            shape = RoundedCornerShape(14.dp),
            color = appleColors.surface,
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = appleColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_import),
                                contentDescription = null,
                                tint = appleColors.label,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Text(
                        text = "Restore Bookmarks",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = appleColors.label
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = appleColors.tertiaryLabel,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (showExportFormatDialog) {
            Dialog(onDismissRequest = { showExportFormatDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = appleColors.secondaryGroupedBackground,
                    border = BorderStroke(0.75.dp, appleColors.separator),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = appleColors.fill,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_export),
                                        contentDescription = null,
                                        tint = appleColors.label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Export Backup",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = appleColors.label
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Option 1: .webstack
                        Surface(
                            onClick = {
                                showExportFormatDialog = false
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                exportWebstackLauncher.launch("webstack_backup_$timeStamp.webstack")
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = appleColors.surface,
                            border = BorderStroke(0.5.dp, appleColors.separator),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "WebStack Backup",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = appleColors.label
                                    )
                                    Text(
                                        text = "(.webstack)",
                                        fontSize = 13.sp,
                                        color = appleColors.secondaryLabel
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = appleColors.tertiaryLabel,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Option 2: .json
                        Surface(
                            onClick = {
                                showExportFormatDialog = false
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                exportJsonLauncher.launch("webstack_backup_$timeStamp.json")
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = appleColors.surface,
                            border = BorderStroke(0.5.dp, appleColors.separator),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Standard JSON",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = appleColors.label
                                    )
                                    Text(
                                        text = "(.json)",
                                        fontSize = 13.sp,
                                        color = appleColors.secondaryLabel
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = appleColors.tertiaryLabel,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        TextButton(
                            onClick = { showExportFormatDialog = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = appleColors.secondaryLabel
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: About
        Text(
            text = "ABOUT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appleColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // What's New Entry
        Surface(
            onClick = onOpenWhatsNew,
            shape = RoundedCornerShape(14.dp),
            color = appleColors.surface,
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = appleColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                colorFilter = ColorFilter.tint(appleColors.label)
                            )
                        }
                    }

                    Text(
                        text = "What's New in WebStack",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appleColors.label
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = appleColors.tertiaryLabel,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // App Info Entry
        Surface(
            onClick = onOpenAppInfo,
            shape = RoundedCornerShape(14.dp),
            color = appleColors.surface,
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = appleColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = appleColors.label
                            )
                        }
                    }

                    Text(
                        text = "App Info",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appleColors.label
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = appleColors.tertiaryLabel,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appleColors.label,
                contentColor = appleColors.systemBackground
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
