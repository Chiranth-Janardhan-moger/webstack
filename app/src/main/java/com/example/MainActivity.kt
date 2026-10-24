package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.model.AppThemeMode
import com.example.ui.model.BookmarkSortOrder
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Website
import com.example.ui.cards.CompactWebsiteRow
import com.example.ui.cards.GridWebsiteCard
import com.example.ui.cards.WebsiteCard
import com.example.ui.components.AppBottomSheet
import com.example.ui.components.AppCategoryCapsuleBar
import com.example.ui.components.AppEmptyState
import com.example.ui.components.AppFloatingActionButton
import com.example.ui.components.AppNavigationHeader
import com.example.ui.dialogs.AddTagDialog
import com.example.ui.dialogs.DeleteTagConfirmDialog
import com.example.ui.dialogs.EditTagDialog
import com.example.ui.model.AppFabShape
import com.example.ui.model.WebStackLayoutMode
import com.example.ui.sheets.AddWebsiteSheetContent
import com.example.ui.sheets.CustomizeTagsSheetContent
import com.example.ui.sheets.EditWebsiteSheetContent
import com.example.ui.sheets.ItemOptionsBottomSheetContent
import com.example.ui.sheets.SettingsBottomSheetContent
import com.example.ui.sheets.TagOptionsBottomSheetContent
import com.example.ui.sheets.VersionUpdateScreen
import com.example.ui.sheets.WhatsNewBottomSheetContent
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.util.copyWebsiteLink
import com.example.ui.util.extractUrlFromText
import com.example.ui.util.openWebsiteInBrowser
import com.example.ui.util.shareWebsiteLink
import com.example.ui.viewmodel.WebsiteViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val sharedUrlState = MutableStateFlow<String?>(null)
    private val sharedBackupUriState = MutableStateFlow<android.net.Uri?>(null)
    private val shortcutActionState = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)
        enableEdgeToEdge()
        setContent {
            val prefs = remember { getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE) }
            var themeMode by remember {
                mutableStateOf(
                    runCatching { AppThemeMode.valueOf(prefs.getString("app_theme", "") ?: "") }
                        .getOrDefault(AppThemeMode.SYSTEM)
                )
            }
            val isDark = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                val appColors = LocalAppColors.current
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = appColors.groupedBackground
                ) {
                    val sharedUrl by sharedUrlState.collectAsState()
                    val sharedBackupUri by sharedBackupUriState.collectAsState()
                    val shortcutAction by shortcutActionState.collectAsState()
                    WebStackScreen(
                        themeMode = themeMode,
                        onSetThemeMode = { newMode ->
                            themeMode = newMode
                            prefs.edit().putString("app_theme", newMode.name).apply()
                        },
                        incomingSharedUrl = sharedUrl,
                        onClearIncomingUrl = { sharedUrlState.value = null },
                        incomingBackupUri = sharedBackupUri,
                        onClearIncomingBackupUri = { sharedBackupUriState.value = null },
                        incomingShortcut = shortcutAction,
                        onClearIncomingShortcut = { shortcutActionState.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val shortcut = intent?.getStringExtra("shortcut")
        if (!shortcut.isNullOrBlank()) {
            shortcutActionState.value = shortcut
        }

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            val extracted = extractUrlFromText(text) ?: text.trim()
            if (extracted.isNotBlank()) {
                sharedUrlState.value = extracted
            }
        } else if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            sharedBackupUriState.value = intent.data
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebStackScreen(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onSetThemeMode: (AppThemeMode) -> Unit = {},
    incomingSharedUrl: String? = null,
    onClearIncomingUrl: () -> Unit = {},
    incomingBackupUri: android.net.Uri? = null,
    onClearIncomingBackupUri: () -> Unit = {},
    incomingShortcut: String? = null,
    onClearIncomingShortcut: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    val viewModel: WebsiteViewModel = viewModel()
    val websitesState by viewModel.websitesList.collectAsState()
    val categoriesState by viewModel.categories.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val cachedScreenshotIds by viewModel.cachedScreenshotIds.collectAsState()
    val favoriteIds by viewModel.favoriteWebsiteIds.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var initialAddUrl by remember { mutableStateOf("") }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showWhatsNewSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE) }
    val lastSeenVersion = remember { prefs.getInt("last_seen_version_code", 0) }
    var showVersion110Screen by remember { mutableStateOf(lastSeenVersion < 8) }
    var layoutMode by remember {
        mutableStateOf(
            runCatching { WebStackLayoutMode.valueOf(prefs.getString("layout_mode", "") ?: "") }
                .getOrElse {
                    if (prefs.getBoolean("is_compact_list", false)) WebStackLayoutMode.COMPACT_LIST else WebStackLayoutMode.LARGE_CARDS
                }
        )
    }
    var fabShape by remember {
        mutableStateOf(
            prefs.getString("fab_shape", null)?.let { saved ->
                runCatching { AppFabShape.valueOf(saved) }.getOrNull() ?: AppFabShape.fromId(saved)
            } ?: AppFabShape.CIRCLE
        )
    }
    var fetchWebPreviews by remember { mutableStateOf(prefs.getBoolean("fetch_web_previews", false)) }
    var showCardDomain by remember { mutableStateOf(prefs.getBoolean("show_card_domain", true)) }
    var showCardCategory by remember { mutableStateOf(prefs.getBoolean("show_card_category", true)) }
    var bookmarkSortOrder by remember {
        val saved = prefs.getString("bookmark_sort_order", BookmarkSortOrder.NEWEST.name) ?: BookmarkSortOrder.NEWEST.name
        mutableStateOf(runCatching { BookmarkSortOrder.valueOf(saved) }.getOrDefault(BookmarkSortOrder.NEWEST))
    }
    var websiteForOptions by remember { mutableStateOf<Website?>(null) }
    var websiteToEdit by remember { mutableStateOf<Website?>(null) }
    var websiteToDelete by remember { mutableStateOf<Website?>(null) }
    val selectedWebsiteIds = remember { mutableStateListOf<Long>() }
    var showBulkDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Intercept system back press when bulk selection is active
    BackHandler(enabled = selectedWebsiteIds.isNotEmpty()) {
        selectedWebsiteIds.clear()
    }

    // Intercept system back press when search is expanded to collapse search first
    BackHandler(enabled = isSearchExpanded && selectedWebsiteIds.isEmpty()) {
        searchQuery = ""
        isSearchExpanded = false
    }

    // Tag management states
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showCustomizeTagsSheet by remember { mutableStateOf(false) }
    var categoryForOptions by remember { mutableStateOf<String?>(null) }
    var categoryToEdit by remember { mutableStateOf<String?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    // Refresh token map for forcing screenshot reload
    val refreshTokens = remember { mutableStateMapOf<Long, Long>() }

    // Lazy grid scroll state and clean top-scroll on category switch
    val gridState = rememberLazyGridState()
    LaunchedEffect(selectedCategory) {
        if (gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0) {
            gridState.scrollToItem(0)
        }
    }

    // Handle incoming shared URL from System Share Sheet
    LaunchedEffect(incomingSharedUrl) {
        if (!incomingSharedUrl.isNullOrBlank()) {
            initialAddUrl = incomingSharedUrl
            showAddSheet = true
            onClearIncomingUrl()
        }
    }

    // Handle incoming launcher shortcuts ("add" or "search")
    LaunchedEffect(incomingShortcut) {
        when (incomingShortcut) {
            "add" -> {
                showAddSheet = true
                onClearIncomingShortcut()
            }
            "search" -> {
                isSearchExpanded = true
                onClearIncomingShortcut()
            }
        }
    }

    // Handle incoming .webstack backup file opened from external apps / file manager
    LaunchedEffect(incomingBackupUri) {
        incomingBackupUri?.let { uri ->
            val displayName = try {
                context.contentResolver.query(
                    uri,
                    arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                } ?: ""
            } catch (_: Exception) {
                ""
            }

            val isWebstack = displayName.endsWith(".webstack", ignoreCase = true) ||
                    (uri.path?.endsWith(".webstack", ignoreCase = true) == true)

            if (!isWebstack && displayName.isNotEmpty()) {
                onClearIncomingBackupUri()
                return@LaunchedEffect
            }

            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val result = viewModel.restoreBackup(stream)
                    result.onSuccess { (imported, newCats) ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val msg = if (imported > 0) {
                            "Restored $imported bookmarks ($newCats new categories)"
                        } else {
                            "All bookmarks in backup are already in your stack"
                        }
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        android.widget.Toast.makeText(context, "Restore failed: ${err.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Could not open backup file", android.widget.Toast.LENGTH_SHORT).show()
            }
            onClearIncomingBackupUri()
        }
    }

    // Filter websites according to selected category, search query, and sort order
    val filteredWebsites = remember(websitesState, selectedCategory, searchQuery, bookmarkSortOrder) {
        val current = websitesState ?: return@remember emptyList()
        val sorted = when (bookmarkSortOrder) {
            BookmarkSortOrder.NEWEST -> current
            BookmarkSortOrder.OLDEST -> current.asReversed()
        }
        val categoryFiltered = if (selectedCategory == "All") {
            sorted
        } else {
            sorted.filter {
                it.category.equals(selectedCategory, ignoreCase = true)
            }
        }
        if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            val query = searchQuery.trim()
            categoryFiltered.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.url.contains(query, ignoreCase = true) ||
                it.domain.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = appColors.groupedBackground,
        floatingActionButton = {
            if (!showVersion110Screen) {
                AppFloatingActionButton(
                    fabShape = fabShape,
                    isSelectionMode = selectedWebsiteIds.isNotEmpty(),
                    selectedCount = selectedWebsiteIds.size,
                    onAddClick = {
                        initialAddUrl = ""
                        showAddSheet = true
                    },
                    onCancelSelection = {
                        selectedWebsiteIds.clear()
                    },
                    onDeleteSelected = {
                        showBulkDeleteConfirmDialog = true
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(appColors.groupedBackground)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Navigation Header (Settings on Left, WebStack in Center, Expanding Search on Right)
                AppNavigationHeader(
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    isSearchExpanded = isSearchExpanded,
                    onSearchExpandedChange = { expanded ->
                        isSearchExpanded = expanded
                        if (!expanded) searchQuery = ""
                    },
                    onQueryChange = { searchQuery = it },
                    onClearQuery = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        searchQuery = ""
                    },
                    onOpenSettings = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.setSettingsSubScreen(com.example.ui.sheets.SettingsSubScreen.MAIN)
                        showSettingsSheet = true
                    }
                )

                // Category / Tag Capsule Selector Bar (Single tap filter, long press options)
                AppCategoryCapsuleBar(
                    allWebsites = websitesState ?: emptyList(),
                    categories = categoriesState,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { cat ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedCategory = cat
                    },
                    onTagLongPress = { cat ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        categoryForOptions = cat
                    },
                    onAddNewTag = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddTagDialog = true
                    }
                )

                // Main Content Area (Cards or Inset Grouped Rows)
                if (websitesState == null) {
                    Box(modifier = Modifier.weight(1f))
                } else if (filteredWebsites.isEmpty()) {
                    AppEmptyState(
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onClearSearch = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            searchQuery = ""
                        },
                        onShowAll = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedCategory = "All"
                        },
                        onAddLink = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showAddSheet = true
                        }
                    )
                } else {
                    val screenWidthDp = LocalConfiguration.current.screenWidthDp
                    val gridColumns = when (layoutMode) {
                        WebStackLayoutMode.GRID_CARDS -> when {
                            screenWidthDp >= 840 -> GridCells.Fixed(4)
                            screenWidthDp >= 600 -> GridCells.Fixed(3)
                            else -> GridCells.Fixed(2)
                        }
                        WebStackLayoutMode.LARGE_CARDS -> when {
                            screenWidthDp >= 600 -> GridCells.Fixed(2)
                            else -> GridCells.Fixed(1)
                        }
                        WebStackLayoutMode.COMPACT_LIST -> when {
                            screenWidthDp >= 720 -> GridCells.Fixed(2)
                            else -> GridCells.Fixed(1)
                        }
                    }
                    LazyVerticalGrid(
                        state = gridState,
                        columns = gridColumns,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clipToBounds()
                            .testTag("websites_list"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(
                            when (layoutMode) {
                                WebStackLayoutMode.COMPACT_LIST -> 10.dp
                                WebStackLayoutMode.GRID_CARDS -> 12.dp
                                WebStackLayoutMode.LARGE_CARDS -> 18.dp
                            }
                        )
                    ) {
                        items(
                            items = filteredWebsites,
                            key = { it.id }
                        ) { website ->
                            val refreshToken = refreshTokens[website.id] ?: 0L
                            val hasCachedImage = cachedScreenshotIds.contains(website.id)
                            val isFavorite = favoriteIds.contains(website.id)
                            Box(
                                modifier = Modifier.animateItem(
                                    placementSpec = null,
                                    fadeInSpec = tween(durationMillis = 140),
                                    fadeOutSpec = tween(durationMillis = 100)
                                )
                            ) {
                                val isSelectionMode = selectedWebsiteIds.isNotEmpty()
                                val selIndex = if (isSelectionMode) {
                                    val idx = selectedWebsiteIds.indexOf(website.id)
                                    if (idx != -1) idx + 1 else null
                                } else null

                                val onCardClick: () -> Unit = remember(isSelectionMode, website.url, website.id, selectedWebsiteIds.size) {
                                    {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (isSelectionMode) {
                                            if (selectedWebsiteIds.contains(website.id)) {
                                                selectedWebsiteIds.remove(website.id)
                                            } else {
                                                selectedWebsiteIds.add(website.id)
                                            }
                                        } else {
                                            openWebsiteInBrowser(context, website.url)
                                        }
                                        Unit
                                    }
                                }
                                val onCardLongClick: () -> Unit = remember(isSelectionMode, website, selectedWebsiteIds.size) {
                                    {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (isSelectionMode) {
                                            if (selectedWebsiteIds.contains(website.id)) {
                                                selectedWebsiteIds.remove(website.id)
                                            } else {
                                                selectedWebsiteIds.add(website.id)
                                            }
                                        } else {
                                            websiteForOptions = website
                                        }
                                        Unit
                                    }
                                }
                                val onCardRefresh = remember(website.id, website.title, fetchWebPreviews) {
                                    {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (!fetchWebPreviews) {
                                            Toast.makeText(context, "Enable web previews in Settings to refresh", Toast.LENGTH_LONG).show()
                                        } else {
                                            viewModel.refreshScreenshot(website.id)
                                            refreshTokens[website.id] = System.currentTimeMillis()
                                            Toast.makeText(context, "Updating snapshot for ${website.title}...", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                val onImageSaved = remember(website.id) {
                                    { viewModel.onScreenshotSaved(website.id) }
                                }
                                when (layoutMode) {
                                    WebStackLayoutMode.COMPACT_LIST -> CompactWebsiteRow(
                                        website = website,
                                        refreshToken = refreshToken,
                                        fetchWebPreviews = fetchWebPreviews,
                                        showDomain = showCardDomain,
                                        showCategory = showCardCategory,
                                        hasCachedImage = hasCachedImage,
                                        isFavorite = isFavorite,
                                        onImageSaved = onImageSaved,
                                        selectionIndex = selIndex,
                                        isInSelectionMode = isSelectionMode,
                                        onClick = onCardClick,
                                        onLongClick = onCardLongClick,
                                        onRefreshScreenshot = onCardRefresh
                                    )
                                    WebStackLayoutMode.GRID_CARDS -> GridWebsiteCard(
                                        website = website,
                                        refreshToken = refreshToken,
                                        fetchWebPreviews = fetchWebPreviews,
                                        showDomain = showCardDomain,
                                        showCategory = showCardCategory,
                                        hasCachedImage = hasCachedImage,
                                        isFavorite = isFavorite,
                                        onImageSaved = onImageSaved,
                                        selectionIndex = selIndex,
                                        isInSelectionMode = isSelectionMode,
                                        onClick = onCardClick,
                                        onLongClick = onCardLongClick,
                                        onRefreshScreenshot = onCardRefresh
                                    )
                                    WebStackLayoutMode.LARGE_CARDS -> WebsiteCard(
                                        website = website,
                                        refreshToken = refreshToken,
                                        fetchWebPreviews = fetchWebPreviews,
                                        showDomain = showCardDomain,
                                        showCategory = showCardCategory,
                                        hasCachedImage = hasCachedImage,
                                        isFavorite = isFavorite,
                                        onImageSaved = onImageSaved,
                                        selectionIndex = selIndex,
                                        isInSelectionMode = isSelectionMode,
                                        onClick = onCardClick,
                                        onLongClick = onCardLongClick,
                                        onRefreshScreenshot = onCardRefresh
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tag Options Sheet
        if (categoryForOptions != null) {
            val targetTag = categoryForOptions!!
            AppBottomSheet(onDismissRequest = { categoryForOptions = null }) {
                TagOptionsBottomSheetContent(
                    tagName = targetTag,
                    onEdit = {
                        categoryToEdit = targetTag
                        categoryForOptions = null
                    },
                    onDelete = {
                        categoryToDelete = targetTag
                        categoryForOptions = null
                    },
                    onCustomizeAll = {
                        categoryForOptions = null
                        showCustomizeTagsSheet = true
                    },
                    onDismiss = { categoryForOptions = null }
                )
            }
        }

        // Add Tag Dialog
        if (showAddTagDialog) {
            AddTagDialog(
                onAdd = { newTag ->
                    val success = viewModel.addCategory(newTag)
                    if (success) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "Tag \"$newTag\" created", Toast.LENGTH_SHORT).show()
                        showAddTagDialog = false
                    } else {
                        Toast.makeText(context, "Tag already exists or invalid", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { showAddTagDialog = false }
            )
        }

        // Edit Tag Dialog
        if (categoryToEdit != null) {
            val oldName = categoryToEdit!!
            EditTagDialog(
                currentName = oldName,
                onSave = { newName ->
                    val success = viewModel.renameCategory(oldName, newName)
                    if (success) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (selectedCategory.equals(oldName, ignoreCase = true)) {
                            selectedCategory = newName
                        }
                        Toast.makeText(context, "Tag renamed to \"$newName\"", Toast.LENGTH_SHORT).show()
                        categoryToEdit = null
                    } else {
                        Toast.makeText(context, "Name invalid or already taken", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { categoryToEdit = null }
            )
        }

        // Delete Tag Confirmation Alert
        if (categoryToDelete != null) {
            val tagToDelete = categoryToDelete!!
            if (tagToDelete.equals("Personal", ignoreCase = true)) {
                categoryToDelete = null
                Toast.makeText(context, "Personal is the default category and cannot be deleted", Toast.LENGTH_SHORT).show()
            } else {
                val fallbackCategory by viewModel.fallbackCategory.collectAsState()
                DeleteTagConfirmDialog(
                    tagName = tagToDelete,
                    fallbackCategory = fallbackCategory,
                    onConfirm = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteCategory(tagToDelete)
                        if (selectedCategory.equals(tagToDelete, ignoreCase = true)) {
                            selectedCategory = "All"
                        }
                        Toast.makeText(context, "Tag \"$tagToDelete\" deleted", Toast.LENGTH_SHORT).show()
                        categoryToDelete = null
                    },
                    onDismiss = { categoryToDelete = null }
                )
            }
        }

        // Settings Bottom Sheet
        if (showSettingsSheet) {
            AppBottomSheet(
                onDismissRequest = {
                    viewModel.setSettingsSubScreen(com.example.ui.sheets.SettingsSubScreen.MAIN)
                    showSettingsSheet = false
                },
                enableSwipeToDismiss = false
            ) {
                SettingsBottomSheetContent(
                    themeMode = themeMode,
                    onSetThemeMode = onSetThemeMode,
                    layoutMode = layoutMode,
                    onSetLayoutMode = { mode ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        layoutMode = mode
                        prefs.edit()
                            .putString("layout_mode", mode.name)
                            .putBoolean("is_compact_list", mode == WebStackLayoutMode.COMPACT_LIST)
                            .apply()
                    },
                    fabShape = fabShape,
                    onSetFabShape = { shape ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        fabShape = shape
                        prefs.edit().putString("fab_shape", shape.name).apply()
                    },
                    fetchWebPreviews = fetchWebPreviews,
                    onToggleFetchWebPreviews = { enabled ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        fetchWebPreviews = enabled
                        prefs.edit().putBoolean("fetch_web_previews", enabled).apply()
                    },
                    showCardDomain = showCardDomain,
                    onToggleShowCardDomain = { enabled ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showCardDomain = enabled
                        prefs.edit().putBoolean("show_card_domain", enabled).apply()
                    },
                    showCardCategory = showCardCategory,
                    onToggleShowCardCategory = { enabled ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showCardCategory = enabled
                        prefs.edit().putBoolean("show_card_category", enabled).apply()
                    },
                    sortOrder = bookmarkSortOrder,
                    onSetSortOrder = { order ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        bookmarkSortOrder = order
                        prefs.edit().putString("bookmark_sort_order", order.name).apply()
                    },
                    onOpenAddTagDialog = {
                        showAddTagDialog = true
                    },
                    onOpenWhatsNew = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showSettingsSheet = false
                        viewModel.setSettingsSubScreen(com.example.ui.sheets.SettingsSubScreen.MAIN)
                        showVersion110Screen = true
                    },
                    onDismiss = {
                        viewModel.setSettingsSubScreen(com.example.ui.sheets.SettingsSubScreen.MAIN)
                        showSettingsSheet = false
                    },
                    viewModel = viewModel
                )
            }
        }

        // Customize Tags Sheet (Live Preview + 2-Column Drag & Drop)
        if (showCustomizeTagsSheet) {
            AppBottomSheet(
                onDismissRequest = { showCustomizeTagsSheet = false },
                enableSwipeToDismiss = false
            ) {
                CustomizeTagsSheetContent(
                    viewModel = viewModel,
                    onOpenAddTagDialog = {
                        showAddTagDialog = true
                    },
                    onDismiss = { showCustomizeTagsSheet = false }
                )
            }
        }

        // "What's New" Sheet
        if (showWhatsNewSheet) {
            AppBottomSheet(
                onDismissRequest = { showWhatsNewSheet = false },
                enableSwipeToDismiss = false
            ) {
                WhatsNewBottomSheetContent(
                    onDismiss = { showWhatsNewSheet = false }
                )
            }
        }

        // Add Website Bottom Sheet
        if (showAddSheet) {
            AppBottomSheet(
                onDismissRequest = {
                    showAddSheet = false
                    viewModel.clearError()
                }
            ) {
                AddWebsiteSheetContent(
                    initialUrl = initialAddUrl,
                    categories = categoriesState,
                    isSaving = isSaving,
                    saveError = saveError,
                    onAddNewTag = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddTagDialog = true
                    },
                    onClearError = { viewModel.clearError() },
                    onSave = { rawUrl, category, isFavorite ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveWebsite(rawUrl, category, isFavorite = isFavorite) {
                            showAddSheet = false
                            viewModel.clearError()
                        }
                    },
                    onDismiss = {
                        showAddSheet = false
                        viewModel.clearError()
                    }
                )
            }
        }

        // Item Options Sheet (Long Press)
        if (websiteForOptions != null) {
            val targetWebsite = websiteForOptions!!
            AppBottomSheet(onDismissRequest = { websiteForOptions = null }) {
                ItemOptionsBottomSheetContent(
                    website = targetWebsite,
                    isFavorite = favoriteIds.contains(targetWebsite.id),
                    onToggleFavorite = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleFavorite(targetWebsite.id)
                    },
                    onOpen = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        openWebsiteInBrowser(context, targetWebsite.url)
                        websiteForOptions = null
                    },
                    onEdit = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        websiteToEdit = targetWebsite
                        websiteForOptions = null
                    },
                    onCopy = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        copyWebsiteLink(context, targetWebsite.url)
                        websiteForOptions = null
                    },
                    onRefresh = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!fetchWebPreviews) {
                            Toast.makeText(context, "Enable web previews in Settings to refresh", Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.refreshScreenshot(targetWebsite.id)
                            refreshTokens[targetWebsite.id] = System.currentTimeMillis()
                            Toast.makeText(context, "Refreshing ${targetWebsite.title}...", Toast.LENGTH_SHORT).show()
                        }
                        websiteForOptions = null
                    },
                    onShare = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        shareWebsiteLink(context, targetWebsite)
                        websiteForOptions = null
                    },
                    onSelect = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (!selectedWebsiteIds.contains(targetWebsite.id)) {
                            selectedWebsiteIds.add(targetWebsite.id)
                        }
                        websiteForOptions = null
                    },
                    onDelete = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        websiteToDelete = targetWebsite
                        websiteForOptions = null
                    },
                    onDismiss = { websiteForOptions = null }
                )
            }
        }

        // Edit Website Sheet
        if (websiteToEdit != null) {
            val targetWebsite = websiteToEdit!!
            AppBottomSheet(onDismissRequest = { websiteToEdit = null }) {
                EditWebsiteSheetContent(
                    website = targetWebsite,
                    categories = categoriesState,
                    isFavorite = favoriteIds.contains(targetWebsite.id),
                    onToggleFavorite = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleFavorite(targetWebsite.id)
                    },
                    onAddNewTag = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddTagDialog = true
                    },
                    onSave = { updatedWebsite ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.updateWebsite(updatedWebsite) { success, errorMsg ->
                            if (success) {
                                Toast.makeText(context, "Saved changes to ${updatedWebsite.title}", Toast.LENGTH_SHORT).show()
                                websiteToEdit = null
                            } else {
                                Toast.makeText(context, errorMsg ?: "Could not update", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onDismiss = { websiteToEdit = null }
                )
            }
        }

        // Remove Link Confirmation Alert
        if (websiteToDelete != null) {
            AlertDialog(
                onDismissRequest = { websiteToDelete = null },
                containerColor = appColors.secondaryGroupedBackground,
                tonalElevation = 0.dp,
                title = {
                    Text(
                        text = "Delete Bookmark",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.label
                    )
                },
                text = {
                    Text(
                        text = "Delete \"${websiteToDelete?.title}\" from your stack?",
                        fontSize = 14.sp,
                        color = appColors.secondaryLabel,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = websiteToDelete
                            if (target != null) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteWebsite(target.id)
                                Toast.makeText(context, "Removed \"${target.title}\"", Toast.LENGTH_SHORT).show()
                            }
                            websiteToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.destructive,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Remove", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { websiteToDelete = null },
                        colors = ButtonDefaults.textButtonColors(contentColor = appColors.secondaryLabel)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }
                }
            )
        }

        // Bulk Delete Confirmation Alert
        if (showBulkDeleteConfirmDialog) {
            val count = selectedWebsiteIds.size
            AlertDialog(
                onDismissRequest = { showBulkDeleteConfirmDialog = false },
                containerColor = appColors.secondaryGroupedBackground,
                tonalElevation = 0.dp,
                title = {
                    Text(
                        text = "Delete $count Bookmarks?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.label
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete these $count bookmarks from your stack? This action cannot be undone.",
                        fontSize = 14.sp,
                        color = appColors.secondaryLabel,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            val toDelete = selectedWebsiteIds.toList()
                            viewModel.deleteWebsites(toDelete)
                            selectedWebsiteIds.clear()
                            showBulkDeleteConfirmDialog = false
                            Toast.makeText(context, "Deleted $count bookmarks", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.destructive,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showBulkDeleteConfirmDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = appColors.secondaryLabel)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }
                }
            )
        }

        // Version Update Screen
        AnimatedVisibility(
            visible = showVersion110Screen,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f)
        ) {
            VersionUpdateScreen(
                onDismiss = {
                    prefs.edit().putInt("last_seen_version_code", 8).apply()
                    showVersion110Screen = false
                },
                onViewAllFeatures = {
                    showWhatsNewSheet = true
                }
            )
        }
    }
}
