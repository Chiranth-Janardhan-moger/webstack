package com.example.ui.sheets

import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppSwitch
import com.example.ui.model.AppThemeMode
import com.example.ui.model.WebStackLayoutMode
import com.example.ui.theme.LocalAppColors
import com.example.ui.viewmodel.WebsiteViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SettingsSubScreen {
    MAIN,
    APPEARANCE,
    BACKUP_RESTORE
}

@Composable
fun SettingsBottomSheetContent(
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
    val appColors = LocalAppColors.current
    val prefs = remember {
        context.getSharedPreferences("webstack_settings", android.content.Context.MODE_PRIVATE)
    }
    var lastBackupTimestamp by remember {
        mutableStateOf(prefs.getLong("last_backup_timestamp", 0L))
    }
    val websitesState = viewModel?.websitesList?.collectAsState()
    val totalBookmarks = websitesState?.value?.size ?: 0

    val lastBackupText = remember(lastBackupTimestamp) {
        if (lastBackupTimestamp == 0L) {
            "Never backed up from this device"
        } else {
            val date = Date(lastBackupTimestamp)
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            "Last backup: ${sdf.format(date)}"
        }
    }

    var currentSubScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }

    val exportWebstackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null && viewModel != null) {
            coroutineScope.launch {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val result = viewModel.exportBackup(outputStream)
                    result.onSuccess { count ->
                        val now = System.currentTimeMillis()
                        prefs.edit().putLong("last_backup_timestamp", now).apply()
                        lastBackupTimestamp = now
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
                        val now = System.currentTimeMillis()
                        prefs.edit().putLong("last_backup_timestamp", now).apply()
                        lastBackupTimestamp = now
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

    when (currentSubScreen) {
        SettingsSubScreen.APPEARANCE -> {
            BackHandler { currentSubScreen = SettingsSubScreen.MAIN }
            AppearanceSubSheetContent(
                themeMode = themeMode,
                onSetThemeMode = onSetThemeMode,
                layoutMode = layoutMode,
                onSetLayoutMode = onSetLayoutMode,
                onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                onDismiss = onDismiss
            )
        }
        SettingsSubScreen.BACKUP_RESTORE -> {
            BackHandler { currentSubScreen = SettingsSubScreen.MAIN }
            BackupRestoreSubSheetContent(
                totalBookmarks = totalBookmarks,
                onExportWebstack = {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    exportWebstackLauncher.launch("webstack_backup_$timeStamp.webstack")
                },
                onExportJson = {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    exportJsonLauncher.launch("webstack_backup_$timeStamp.json")
                },
                onRestore = {
                    restoreLauncher.launch(
                        arrayOf(
                            "application/json",
                            "application/octet-stream",
                            "text/plain",
                            "*/*"
                        )
                    )
                },
                onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                onDismiss = onDismiss
            )
        }
        SettingsSubScreen.MAIN -> {
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
                    color = appColors.label,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_nav_more),
                            contentDescription = null,
                            tint = appColors.systemBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = appColors.label,
                    letterSpacing = (-0.4).sp
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appColors.secondaryLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Look and Feel
        Text(
            text = "LOOK AND FEEL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        val appearanceInteractionSource = remember { MutableInteractionSource() }
        val isAppearancePressed by appearanceInteractionSource.collectIsPressedAsState()
        val appearanceScale by animateFloatAsState(
            targetValue = if (isAppearancePressed) 0.96f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "appearance_card_bouncy"
        )

        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                currentSubScreen = SettingsSubScreen.APPEARANCE
            },
            interactionSource = appearanceInteractionSource,
            shape = RoundedCornerShape(14.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = appearanceScale
                    scaleY = appearanceScale
                }
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
                        color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_appearance),
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Text(
                        text = "Appearance",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = appColors.label
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = appColors.tertiaryLabel,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Network
        Text(
            text = "NETWORK",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
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
                        color = if (fetchWebPreviews) appColors.label else appColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = if (fetchWebPreviews) appColors.systemBackground else appColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "Fetch Web Previews",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appColors.label
                    )
                }

                AppSwitch(
                    checked = fetchWebPreviews,
                    onCheckedChange = onToggleFetchWebPreviews
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Storage
        Text(
            text = "STORAGE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        val backupInteractionSource = remember { MutableInteractionSource() }
        val isBackupPressed by backupInteractionSource.collectIsPressedAsState()
        val backupScale by animateFloatAsState(
            targetValue = if (isBackupPressed) 0.96f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "backup_card_bouncy"
        )

        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                currentSubScreen = SettingsSubScreen.BACKUP_RESTORE
            },
            interactionSource = backupInteractionSource,
            shape = RoundedCornerShape(14.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = backupScale
                    scaleY = backupScale
                }
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
                            text = "Backup and restore",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                        Text(
                            text = lastBackupText,
                            fontSize = 12.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = appColors.tertiaryLabel,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: About
        Text(
            text = "ABOUT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // App Info Entry
        Surface(
            onClick = onOpenAppInfo,
            shape = RoundedCornerShape(14.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
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
                        color = appColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = appColors.label
                            )
                        }
                    }

                    Text(
                        text = "App Info",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appColors.label
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = appColors.tertiaryLabel,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
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
    }
}
