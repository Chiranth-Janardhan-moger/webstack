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
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.example.ui.components.AppExplainerDialog
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.painter.Painter
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

import com.example.ui.model.AppFabShape
import com.example.ui.model.BookmarkSortOrder
import com.example.ui.model.FavoriteBadgeShape
import com.example.ui.model.FavoriteColorPreset
import com.example.ui.model.FavoriteIconType

enum class SettingsSubScreen {
    MAIN,
    APPEARANCE,
    BACKUP_RESTORE,
    CLUSTER_APPROVAL,
    CUSTOMIZE_TAGS,
    FAB_SHAPE,
    CARD_DETAILS,
    APP_INFO,
    PC_SHARE,
    THEME_LAYOUT,
    FAVORITES_CUSTOMIZE
}

private data class SettingsExplainer(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsBottomSheetContent(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onSetThemeMode: (AppThemeMode) -> Unit = {},
    layoutMode: WebStackLayoutMode,
    onSetLayoutMode: (WebStackLayoutMode) -> Unit,
    fabShape: AppFabShape = AppFabShape.CIRCLE,
    onSetFabShape: (AppFabShape) -> Unit = {},
    fetchWebPreviews: Boolean,
    onToggleFetchWebPreviews: (Boolean) -> Unit,
    showCardDomain: Boolean = true,
    onToggleShowCardDomain: (Boolean) -> Unit = {},
    showCardCategory: Boolean = true,
    onToggleShowCardCategory: (Boolean) -> Unit = {},
    sortOrder: BookmarkSortOrder = BookmarkSortOrder.NEWEST,
    onSetSortOrder: (BookmarkSortOrder) -> Unit = {},
    onOpenAddTagDialog: () -> Unit = {},
    onOpenWhatsNew: () -> Unit,
    onOpenAppInfo: () -> Unit = {},
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
    var activeExplainer by remember { mutableStateOf<SettingsExplainer?>(null) }
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

    var localSubScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }
    var localPreprocessResult by remember { mutableStateOf<com.example.bookmarks.ImportPreprocessResult?>(null) }

    val vmSubScreen = viewModel?.settingsSubScreen?.collectAsState()?.value
    val vmPreprocessResult = viewModel?.pendingPreprocessResult?.collectAsState()?.value
    val vmFavIcon = viewModel?.favoriteIconType?.collectAsState()?.value ?: FavoriteIconType.HEART
    val vmFavShape = viewModel?.favoriteBadgeShape?.collectAsState()?.value ?: FavoriteBadgeShape.PURE_FLOAT
    val vmFavColor = viewModel?.favoriteColorPreset?.collectAsState()?.value ?: FavoriteColorPreset.CRIMSON_PINK

    val currentSubScreen = if (viewModel != null && vmSubScreen != null) vmSubScreen else localSubScreen
    val pendingPreprocessResult = if (viewModel != null) vmPreprocessResult else localPreprocessResult

    fun navigateToSubScreen(screen: SettingsSubScreen) {
        if (viewModel != null) {
            viewModel.setSettingsSubScreen(screen)
        } else {
            localSubScreen = screen
        }
    }

    val handleDismiss = {
        navigateToSubScreen(SettingsSubScreen.MAIN)
        onDismiss()
    }

    fun setPreprocess(result: com.example.bookmarks.ImportPreprocessResult?) {
        if (viewModel != null) {
            viewModel.setPendingPreprocessResult(result)
        } else {
            localPreprocessResult = result
        }
    }

    val exportWebstackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null && viewModel != null) {
            coroutineScope.launch {
                context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
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
                context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
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

    val exportHtmlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        if (uri != null && viewModel != null) {
            coroutineScope.launch {
                try {
                    com.example.bookmarks.BookmarkDelivery.exportToSaf(
                        contentResolver = context.contentResolver,
                        uri = uri,
                        dao = viewModel.websiteDao
                    )
                    val now = System.currentTimeMillis()
                    prefs.edit().putLong("last_backup_timestamp", now).apply()
                    lastBackupTimestamp = now
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    Toast.makeText(context, "Exported bookmarks (.html)", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "HTML Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val exportTxtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null && viewModel != null) {
            coroutineScope.launch {
                try {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                        val result = viewModel.exportPlainText(outputStream)
                        result.onSuccess { count ->
                            val now = System.currentTimeMillis()
                            prefs.edit().putLong("last_backup_timestamp", now).apply()
                            lastBackupTimestamp = now
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast.makeText(context, "Exported $count links (.txt)", Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            Toast.makeText(context, "Plain Text Export failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Plain Text Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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

            val isHtmlExt = displayName.endsWith(".html", ignoreCase = true) || displayName.endsWith(".htm", ignoreCase = true)
            val isJsonExt = displayName.endsWith(".webstack", ignoreCase = true) || displayName.endsWith(".json", ignoreCase = true)
            val isTxtExt = displayName.endsWith(".txt", ignoreCase = true)
            val isSupportedExt = isHtmlExt || isJsonExt || isTxtExt

            if (!isSupportedExt && displayName.isNotEmpty() && displayName.contains(".")) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                Toast.makeText(
                    context,
                    "Invalid format. Please select a .webstack, .json, .html, .htm, or .txt file",
                    Toast.LENGTH_LONG
                ).show()
                return@rememberLauncherForActivityResult
            }

            coroutineScope.launch {
                try {
                    context.contentResolver.openInputStream(uri)?.use { rawStream ->
                        val bufferedStream = if (rawStream.markSupported()) rawStream else java.io.BufferedInputStream(rawStream, 8192)
                        bufferedStream.mark(1024)
                        val sniffBuffer = ByteArray(1024)
                        var bytesRead = 0
                        while (bytesRead < 1024) {
                            val r = bufferedStream.read(sniffBuffer, bytesRead, 1024 - bytesRead)
                            if (r == -1) break
                            bytesRead += r
                        }
                        bufferedStream.reset()

                        val sniffedFormat = com.example.bookmarks.BackupContentSniffer.sniff(sniffBuffer, bytesRead)
                        val isHtml = sniffedFormat == com.example.bookmarks.DetectedBackupFormat.HTML_BOOKMARKS ||
                                (sniffedFormat == com.example.bookmarks.DetectedBackupFormat.UNKNOWN && isHtmlExt)
                        val isJson = sniffedFormat == com.example.bookmarks.DetectedBackupFormat.JSON_BACKUP ||
                                (sniffedFormat == com.example.bookmarks.DetectedBackupFormat.UNKNOWN && isJsonExt)
                        val isTxt = sniffedFormat == com.example.bookmarks.DetectedBackupFormat.PLAIN_TEXT_URLS ||
                                (sniffedFormat == com.example.bookmarks.DetectedBackupFormat.UNKNOWN && isTxtExt)

                        if (!isHtml && !isJson && !isTxt) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast.makeText(
                                context,
                                "Invalid format. Please select a valid backup or bookmark export file (.webstack, .json, .html, .htm, or .txt)",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }

                        if (isHtml) {
                            val result = viewModel.preprocessHtmlBookmarks(bufferedStream)
                            result.onSuccess { prep ->
                                if (prep.classifiedBookmarks.isEmpty()) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(
                                        context,
                                        "No bookmarks found. Please select a valid browser bookmark export file.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    setPreprocess(prep)
                                    navigateToSubScreen(SettingsSubScreen.CLUSTER_APPROVAL)
                                }
                            }.onFailure { err ->
                                Toast.makeText(context, "Failed to parse bookmarks: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        } else if (isTxt) {
                            val result = viewModel.restorePlainText(bufferedStream)
                            result.onSuccess { (imported, _) ->
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                val msg = if (imported > 0) {
                                    "Restored $imported bookmarks (.txt)"
                                } else {
                                    "All URLs in file are already in your stack"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "Restore failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val result = viewModel.restoreBackup(bufferedStream)
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
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open backup file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    when (currentSubScreen) {
        SettingsSubScreen.APPEARANCE -> {
            BackHandler { navigateToSubScreen(SettingsSubScreen.MAIN) }
            CustomizationSubSheetContent(
                themeMode = themeMode,
                layoutMode = layoutMode,
                onOpenAppearance = { navigateToSubScreen(SettingsSubScreen.THEME_LAYOUT) },
                onOpenFavorites = { navigateToSubScreen(SettingsSubScreen.FAVORITES_CUSTOMIZE) },
                fabShape = fabShape,
                onOpenFabShapePicker = { navigateToSubScreen(SettingsSubScreen.FAB_SHAPE) },
                onOpenCardDetails = { navigateToSubScreen(SettingsSubScreen.CARD_DETAILS) },
                onOpenCustomizeTags = { navigateToSubScreen(SettingsSubScreen.CUSTOMIZE_TAGS) },
                favoriteIconType = vmFavIcon,
                favoriteBadgeShape = vmFavShape,
                favoriteColorPreset = vmFavColor,
                onBack = { navigateToSubScreen(SettingsSubScreen.MAIN) },
                onDismiss = handleDismiss
            )
        }
        SettingsSubScreen.THEME_LAYOUT -> {
            BackHandler { navigateToSubScreen(SettingsSubScreen.APPEARANCE) }
            ThemeLayoutSubSheetContent(
                themeMode = themeMode,
                onSetThemeMode = onSetThemeMode,
                layoutMode = layoutMode,
                onSetLayoutMode = onSetLayoutMode,
                onBack = { navigateToSubScreen(SettingsSubScreen.APPEARANCE) },
                onDismiss = handleDismiss
            )
        }
        SettingsSubScreen.FAVORITES_CUSTOMIZE -> {
            BackHandler { navigateToSubScreen(SettingsSubScreen.APPEARANCE) }
            FavoritesCustomizationSubSheetContent(
                favoriteIconType = vmFavIcon,
                onSetFavoriteIconType = { viewModel?.setFavoriteIconType(it) },
                favoriteBadgeShape = vmFavShape,
                onSetFavoriteBadgeShape = { viewModel?.setFavoriteBadgeShape(it) },
                favoriteColorPreset = vmFavColor,
                onSetFavoriteColorPreset = { viewModel?.setFavoriteColorPreset(it) },
                onBack = { navigateToSubScreen(SettingsSubScreen.APPEARANCE) },
                onDismiss = handleDismiss
            )
        }
        SettingsSubScreen.CARD_DETAILS -> {
            BackHandler { navigateToSubScreen(SettingsSubScreen.APPEARANCE) }
            CardDetailsSubSheetContent(
                showCardDomain = showCardDomain,
                onToggleShowCardDomain = onToggleShowCardDomain,
                showCardCategory = showCardCategory,
                onToggleShowCardCategory = onToggleShowCardCategory,
                sortOrder = sortOrder,
                onSetSortOrder = onSetSortOrder,
                onBack = { navigateToSubScreen(SettingsSubScreen.APPEARANCE) },
                onDismiss = handleDismiss
            )
        }
        SettingsSubScreen.FAB_SHAPE -> {
            BackHandler { navigateToSubScreen(SettingsSubScreen.APPEARANCE) }
            FabShapePickerSubSheetContent(
                selectedShape = fabShape,
                onSelectShape = onSetFabShape,
                onBack = { navigateToSubScreen(SettingsSubScreen.APPEARANCE) },
                onDismiss = handleDismiss
            )
        }
        SettingsSubScreen.BACKUP_RESTORE -> {
            BackHandler { navigateToSubScreen(SettingsSubScreen.MAIN) }
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
                onExportHtml = {
                    val fileName = com.example.bookmarks.BookmarkDelivery.formatExportFileName()
                    exportHtmlLauncher.launch(fileName)
                },
                onExportTxt = {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    exportTxtLauncher.launch("webstack_links_$timeStamp.txt")
                },
                onRestore = {
                    restoreLauncher.launch(
                        arrayOf(
                            "application/json",
                            "application/octet-stream",
                            "text/html",
                            "text/plain",
                            "*/*"
                        )
                    )
                },
                onBack = { navigateToSubScreen(SettingsSubScreen.MAIN) },
                onDismiss = handleDismiss
            )
        }
        SettingsSubScreen.CLUSTER_APPROVAL -> {
            BackHandler { navigateToSubScreen(SettingsSubScreen.BACKUP_RESTORE) }
            val prep = pendingPreprocessResult
            if (prep != null && viewModel != null) {
                val cats = viewModel.categories.collectAsState().value
                val progress = viewModel.importProgress.collectAsState().value
                ClusterApprovalSheet(
                    preprocessResult = prep,
                    existingCategories = cats,
                    importProgress = progress,
                    onConfirmImport = { bookmarksToImport ->
                        coroutineScope.launch {
                            val impResult = viewModel.commitImportBookmarks(bookmarksToImport)
                            impResult.onSuccess { res ->
                                val now = System.currentTimeMillis()
                                prefs.edit().putLong("last_backup_timestamp", now).apply()
                                lastBackupTimestamp = now
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                Toast.makeText(
                                    context,
                                    "Imported ${res.inserted} bookmarks (${res.duplicates} duplicates skipped)",
                                    Toast.LENGTH_LONG
                                ).show()
                                setPreprocess(null)
                                navigateToSubScreen(SettingsSubScreen.MAIN)
                            }.onFailure { err ->
                                Toast.makeText(context, "Import failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onCancel = {
                        setPreprocess(null)
                        navigateToSubScreen(SettingsSubScreen.BACKUP_RESTORE)
                    }
                )
            } else {
                navigateToSubScreen(SettingsSubScreen.BACKUP_RESTORE)
            }
        }
        SettingsSubScreen.CUSTOMIZE_TAGS -> {
            BackHandler { navigateToSubScreen(SettingsSubScreen.APPEARANCE) }
            if (viewModel != null) {
                CustomizeTagsSheetContent(
                    viewModel = viewModel,
                    onOpenAddTagDialog = onOpenAddTagDialog,
                    onBack = { navigateToSubScreen(SettingsSubScreen.APPEARANCE) },
                    onDismiss = handleDismiss
                )
            } else {
                navigateToSubScreen(SettingsSubScreen.APPEARANCE)
            }
        }
        SettingsSubScreen.APP_INFO -> {
            BackHandler { navigateToSubScreen(SettingsSubScreen.MAIN) }
            AppInfoBottomSheetContent(
                onDismiss = handleDismiss,
                onBack = { navigateToSubScreen(SettingsSubScreen.MAIN) },
                onOpenWhatsNew = onOpenWhatsNew
            )
        }
        SettingsSubScreen.PC_SHARE -> {
            BackHandler { navigateToSubScreen(SettingsSubScreen.MAIN) }
            PcCompanionSubSheetContent(
                viewModel = viewModel,
                onBack = { navigateToSubScreen(SettingsSubScreen.MAIN) },
                onDismiss = handleDismiss
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

            IconButton(onClick = handleDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appColors.secondaryLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.TouchApp,
                contentDescription = null,
                tint = appColors.secondaryLabel,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "Press and hold for details",
                fontSize = 12.sp,
                color = appColors.secondaryLabel
            )
        }

        val serverStatus = viewModel?.serverStatus?.collectAsState()?.value
        val isServerLive = serverStatus?.isRunning == true

        Spacer(modifier = Modifier.height(18.dp))

        // Single Unified Grouped Settings Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Customization
                SettingsGroupedNavRow(
                    title = "Customization",
                    iconPainter = painterResource(id = R.drawable.ic_appearance),
                    onClick = { navigateToSubScreen(SettingsSubScreen.APPEARANCE) },
                    onLongClick = {
                        activeExplainer = SettingsExplainer(
                            title = "Customization",
                            description = "Personalize your theme, layouts, interface controls, and favorite indicators.",
                            icon = Icons.Outlined.Palette
                        )
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = appColors.separator
                )

                // 2. PC Companion
                SettingsGroupedNavRow(
                    title = "PC Companion",
                    subtitle = if (isServerLive) "Online on local Wi-Fi" else "Browse & send links from desktop browser",
                    icon = Icons.Outlined.DesktopWindows,
                    iconBackground = if (isServerLive) appColors.label else null,
                    iconTint = if (isServerLive) appColors.systemBackground else null,
                    titleSuffix = if (isServerLive) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF34C759), CircleShape)
                            )
                        }
                    } else null,
                    onClick = { navigateToSubScreen(SettingsSubScreen.PC_SHARE) },
                    onLongClick = {
                        activeExplainer = SettingsExplainer(
                            title = "PC Companion",
                            description = "Connect your computer or tablet over local Wi-Fi to browse, search, and instantly save bookmarks to your phone.",
                            icon = Icons.Outlined.DesktopWindows
                        )
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = appColors.separator
                )

                // 3. Fetch Web Previews
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleFetchWebPreviews(!fetchWebPreviews)
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                activeExplainer = SettingsExplainer(
                                    title = "Fetch Web Previews",
                                    description = "Auto-generates card snapshot previews. Turn off for zero mobile data usage.",
                                    icon = Icons.Default.Public
                                )
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = if (fetchWebPreviews) appColors.label else if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = if (fetchWebPreviews) appColors.systemBackground else appColors.label,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Fetch Web Previews",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = appColors.label
                            )
                            Text(
                                text = "Generates visual card snapshots",
                                fontSize = 12.sp,
                                color = appColors.secondaryLabel
                            )
                        }
                    }

                    AppSwitch(
                        checked = fetchWebPreviews,
                        onCheckedChange = onToggleFetchWebPreviews
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = appColors.separator
                )

                // 3. Backup and restore
                SettingsGroupedNavRow(
                    title = "Backup and restore",
                    subtitle = lastBackupText,
                    icon = Icons.Outlined.Restore,
                    onClick = { navigateToSubScreen(SettingsSubScreen.BACKUP_RESTORE) },
                    onLongClick = {
                        activeExplainer = SettingsExplainer(
                            title = "Backup and Restore",
                            description = "Safely export or restore your stack. Imports merge cleanly without duplicates.",
                            icon = Icons.Outlined.Restore
                        )
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = appColors.separator
                )

                // 5. App Info
                SettingsGroupedNavRow(
                    title = "App Info",
                    icon = Icons.Outlined.Info,
                    onClick = { navigateToSubScreen(SettingsSubScreen.APP_INFO) },
                    onLongClick = {
                        activeExplainer = SettingsExplainer(
                            title = "App Info",
                            description = "Version details, what's new changelog, and open-source credits.",
                            icon = Icons.Outlined.Info
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = handleDismiss,
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

    activeExplainer?.let { exp ->
        AppExplainerDialog(
            title = exp.title,
            description = exp.description,
            icon = exp.icon,
            onDismiss = { activeExplainer = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SettingsGroupedNavRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    iconBackground: Color? = null,
    iconTint: Color? = null,
    titleSuffix: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = iconBackground ?: if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val resolvedTint = iconTint ?: appColors.label
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = resolvedTint,
                                modifier = Modifier.size(19.dp)
                            )
                        } else if (iconPainter != null) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = null,
                                tint = resolvedTint,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                        titleSuffix?.invoke()
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = appColors.secondaryLabel
                        )
                    }
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
}
