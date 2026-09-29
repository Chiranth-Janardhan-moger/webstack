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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Website
import com.example.ui.cards.AppleCompactWebsiteRow
import com.example.ui.cards.AppleGridWebsiteCard
import com.example.ui.cards.AppleWebsiteCard
import com.example.ui.components.AppleCategoryCapsuleBar
import com.example.ui.components.AppleEmptyState
import com.example.ui.components.AppleNavigationHeader
import com.example.ui.components.AppleSheetDragHandle
import com.example.ui.dialogs.AppleAddTagDialog
import com.example.ui.dialogs.AppleDeleteTagConfirmDialog
import com.example.ui.dialogs.AppleEditTagDialog
import com.example.ui.model.WebStackLayoutMode
import com.example.ui.sheets.AppleAddWebsiteSheetContent
import com.example.ui.sheets.AppleAppInfoBottomSheetContent
import com.example.ui.sheets.AppleEditWebsiteSheetContent
import com.example.ui.sheets.AppleItemOptionsBottomSheetContent
import com.example.ui.sheets.AppleSettingsBottomSheetContent
import com.example.ui.sheets.AppleTagOptionsBottomSheetContent
import com.example.ui.sheets.AppleVersionUpdateScreen
import com.example.ui.sheets.AppleWhatsNewBottomSheetContent
import com.example.ui.theme.LocalAppleColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.util.extractUrlFromText
import com.example.ui.util.openWebsiteInBrowser
import com.example.ui.util.shareWebsiteLink
import com.example.ui.viewmodel.WebsiteViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val sharedUrlState = MutableStateFlow<String?>(null)
    private val sharedBackupUriState = MutableStateFlow<android.net.Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val appleColors = LocalAppleColors.current
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = appleColors.groupedBackground
                ) {
                    val sharedUrl by sharedUrlState.collectAsState()
                    val sharedBackupUri by sharedBackupUriState.collectAsState()
                    WebStackScreen(
                        incomingSharedUrl = sharedUrl,
                        onClearIncomingUrl = { sharedUrlState.value = null },
                        incomingBackupUri = sharedBackupUri,
                        onClearIncomingBackupUri = { sharedBackupUriState.value = null }
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
    incomingSharedUrl: String? = null,
    onClearIncomingUrl: () -> Unit = {},
    incomingBackupUri: android.net.Uri? = null,
    onClearIncomingBackupUri: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val appleColors = LocalAppleColors.current
    val viewModel: WebsiteViewModel = viewModel()
    val websitesState by viewModel.websitesList.collectAsState()
    val categoriesState by viewModel.categories.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var initialAddUrl by remember { mutableStateOf("") }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showWhatsNewSheet by remember { mutableStateOf(false) }
    var showAppInfoSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE) }
    val lastSeenVersion = remember { prefs.getInt("last_seen_version_code", 0) }
    var showVersion110Screen by remember { mutableStateOf(lastSeenVersion < 3) }
    var layoutMode by remember {
        mutableStateOf(
            runCatching { WebStackLayoutMode.valueOf(prefs.getString("layout_mode", "") ?: "") }
                .getOrElse {
                    if (prefs.getBoolean("is_compact_list", false)) WebStackLayoutMode.COMPACT_LIST else WebStackLayoutMode.LARGE_CARDS
                }
        )
    }
    var fetchWebPreviews by remember { mutableStateOf(prefs.getBoolean("fetch_web_previews", false)) }
    var websiteForOptions by remember { mutableStateOf<Website?>(null) }
    var websiteToEdit by remember { mutableStateOf<Website?>(null) }
    var websiteToDelete by remember { mutableStateOf<Website?>(null) }

    // Intercept system back press when search is expanded to collapse search first
    BackHandler(enabled = isSearchExpanded) {
        searchQuery = ""
        isSearchExpanded = false
    }

    // Tag management states
    var showAddTagDialog by remember { mutableStateOf(false) }
    var categoryForOptions by remember { mutableStateOf<String?>(null) }
    var categoryToEdit by remember { mutableStateOf<String?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    // Refresh token map for forcing screenshot reload
    val refreshTokens = remember { mutableStateMapOf<Long, Long>() }

    // Handle incoming shared URL from System Share Sheet
    LaunchedEffect(incomingSharedUrl) {
        if (!incomingSharedUrl.isNullOrBlank()) {
            initialAddUrl = incomingSharedUrl
            showAddSheet = true
            onClearIncomingUrl()
        }
    }

    // Handle incoming .webstack backup file opened from external apps / file manager
    LaunchedEffect(incomingBackupUri) {
        incomingBackupUri?.let { uri ->
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

    // Filter websites according to selected category and search query
    val filteredWebsites = remember(websitesState, selectedCategory, searchQuery) {
        val categoryFiltered = if (selectedCategory == "All") {
            websitesState
        } else {
            websitesState.filter {
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
        containerColor = appleColors.groupedBackground,
        floatingActionButton = {
            if (!showVersion110Screen) {
                // Apple Liquid Glass Floating Action Button
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val fabScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "fab_scale_anim"
                )

                FloatingActionButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        initialAddUrl = ""
                        showAddSheet = true
                    },
                    containerColor = appleColors.label,
                    contentColor = appleColors.systemBackground,
                    shape = CircleShape,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .padding(bottom = 12.dp, end = 8.dp)
                        .size(58.dp)
                        .graphicsLayer {
                            scaleX = fabScale
                            scaleY = fabScale
                        }
                        .shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            ambientColor = appleColors.label.copy(alpha = 0.12f),
                            spotColor = appleColors.label.copy(alpha = 0.22f)
                        )
                        .testTag("add_website_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Website Link",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(appleColors.groupedBackground)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Apple Navigation Header (Settings on Left, WebStack in Center, Expanding Search on Right)
                AppleNavigationHeader(
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
                        showSettingsSheet = true
                    }
                )

                // Apple Category / Tag Capsule Selector Bar (Single tap filter, long press options)
                AppleCategoryCapsuleBar(
                    allWebsites = websitesState,
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
                if (filteredWebsites.isEmpty()) {
                    AppleEmptyState(
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
                    val gridColumns = when (layoutMode) {
                        WebStackLayoutMode.GRID_CARDS -> GridCells.Fixed(2)
                        else -> GridCells.Fixed(1)
                    }
                    LazyVerticalGrid(
                        columns = gridColumns,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
                            Box(modifier = Modifier.animateItem()) {
                                val onCardClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    openWebsiteInBrowser(context, website.url)
                                }
                                val onCardLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    websiteForOptions = website
                                }
                                val onCardRefresh = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!fetchWebPreviews) {
                                        Toast.makeText(context, "Enable 'Fetch Web Previews' in Settings to update snapshots", Toast.LENGTH_LONG).show()
                                    } else {
                                        viewModel.refreshScreenshot(website.id)
                                        refreshTokens[website.id] = System.currentTimeMillis()
                                        Toast.makeText(context, "Updating snapshot for ${website.title}...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                when (layoutMode) {
                                    WebStackLayoutMode.COMPACT_LIST -> AppleCompactWebsiteRow(
                                        website = website,
                                        refreshToken = refreshToken,
                                        fetchWebPreviews = fetchWebPreviews,
                                        onClick = onCardClick,
                                        onLongClick = onCardLongClick,
                                        onRefreshScreenshot = onCardRefresh
                                    )
                                    WebStackLayoutMode.GRID_CARDS -> AppleGridWebsiteCard(
                                        website = website,
                                        refreshToken = refreshToken,
                                        fetchWebPreviews = fetchWebPreviews,
                                        onClick = onCardClick,
                                        onLongClick = onCardLongClick,
                                        onRefreshScreenshot = onCardRefresh
                                    )
                                    WebStackLayoutMode.LARGE_CARDS -> AppleWebsiteCard(
                                        website = website,
                                        refreshToken = refreshToken,
                                        fetchWebPreviews = fetchWebPreviews,
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

        // Apple Tag Options Sheet
        if (categoryForOptions != null) {
            val targetTag = categoryForOptions!!
            ModalBottomSheet(
                onDismissRequest = { categoryForOptions = null },
                containerColor = appleColors.secondaryGroupedBackground,
                scrimColor = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { AppleSheetDragHandle() }
            ) {
                AppleTagOptionsBottomSheetContent(
                    tagName = targetTag,
                    onEdit = {
                        categoryToEdit = targetTag
                        categoryForOptions = null
                    },
                    onDelete = {
                        categoryToDelete = targetTag
                        categoryForOptions = null
                    },
                    onFilterByTag = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedCategory = targetTag
                        categoryForOptions = null
                    },
                    onDismiss = { categoryForOptions = null }
                )
            }
        }

        // Apple Add Tag Dialog
        if (showAddTagDialog) {
            AppleAddTagDialog(
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

        // Apple Edit Tag Dialog
        if (categoryToEdit != null) {
            val oldName = categoryToEdit!!
            AppleEditTagDialog(
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

        // Apple Delete Tag Confirmation Alert
        if (categoryToDelete != null) {
            val tagToDelete = categoryToDelete!!
            AppleDeleteTagConfirmDialog(
                tagName = tagToDelete,
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

        // Apple Settings Bottom Sheet
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                containerColor = appleColors.secondaryGroupedBackground,
                scrimColor = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { AppleSheetDragHandle() }
            ) {
                AppleSettingsBottomSheetContent(
                    layoutMode = layoutMode,
                    onSetLayoutMode = { mode ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        layoutMode = mode
                        prefs.edit()
                            .putString("layout_mode", mode.name)
                            .putBoolean("is_compact_list", mode == WebStackLayoutMode.COMPACT_LIST)
                            .apply()
                    },
                    fetchWebPreviews = fetchWebPreviews,
                    onToggleFetchWebPreviews = { enabled ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        fetchWebPreviews = enabled
                        prefs.edit().putBoolean("fetch_web_previews", enabled).apply()
                    },
                    onOpenWhatsNew = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showSettingsSheet = false
                        showVersion110Screen = true
                    },
                    onOpenAppInfo = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showSettingsSheet = false
                        showAppInfoSheet = true
                    },
                    onDismiss = { showSettingsSheet = false },
                    viewModel = viewModel
                )
            }
        }

        // Apple "What's New" Sheet
        if (showWhatsNewSheet) {
            ModalBottomSheet(
                onDismissRequest = { showWhatsNewSheet = false },
                containerColor = appleColors.secondaryGroupedBackground,
                scrimColor = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { AppleSheetDragHandle() }
            ) {
                AppleWhatsNewBottomSheetContent(
                    onDismiss = { showWhatsNewSheet = false }
                )
            }
        }

        // Apple "App Info" Sheet
        if (showAppInfoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAppInfoSheet = false },
                containerColor = appleColors.secondaryGroupedBackground,
                scrimColor = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { AppleSheetDragHandle() }
            ) {
                AppleAppInfoBottomSheetContent(
                    onDismiss = { showAppInfoSheet = false }
                )
            }
        }

        // Apple Add Website Bottom Sheet
        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddSheet = false
                    viewModel.clearError()
                },
                containerColor = appleColors.secondaryGroupedBackground,
                scrimColor = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { AppleSheetDragHandle() }
            ) {
                AppleAddWebsiteSheetContent(
                    initialUrl = initialAddUrl,
                    categories = categoriesState,
                    isSaving = isSaving,
                    saveError = saveError,
                    onAddNewTag = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddTagDialog = true
                    },
                    onSave = { rawUrl, category ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveWebsite(rawUrl, category) {
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

        // Apple Item Options Sheet (Long Press)
        if (websiteForOptions != null) {
            val targetWebsite = websiteForOptions!!
            ModalBottomSheet(
                onDismissRequest = { websiteForOptions = null },
                containerColor = appleColors.secondaryGroupedBackground,
                scrimColor = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { AppleSheetDragHandle() }
            ) {
                AppleItemOptionsBottomSheetContent(
                    website = targetWebsite,
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
                    onRefresh = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!fetchWebPreviews) {
                            Toast.makeText(context, "Enable 'Fetch Web Previews' in Settings to update snapshots", Toast.LENGTH_LONG).show()
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
                    onDelete = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        websiteToDelete = targetWebsite
                        websiteForOptions = null
                    },
                    onDismiss = { websiteForOptions = null }
                )
            }
        }

        // Apple Edit Website Sheet
        if (websiteToEdit != null) {
            val targetWebsite = websiteToEdit!!
            ModalBottomSheet(
                onDismissRequest = { websiteToEdit = null },
                containerColor = appleColors.secondaryGroupedBackground,
                scrimColor = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { AppleSheetDragHandle() }
            ) {
                AppleEditWebsiteSheetContent(
                    website = targetWebsite,
                    categories = categoriesState,
                    onAddNewTag = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddTagDialog = true
                    },
                    onSave = { updatedWebsite ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.updateWebsite(updatedWebsite)
                        Toast.makeText(context, "Saved changes to ${updatedWebsite.title}", Toast.LENGTH_SHORT).show()
                        websiteToEdit = null
                    },
                    onDismiss = { websiteToEdit = null }
                )
            }
        }

        // Apple Remove Link Confirmation Alert
        if (websiteToDelete != null) {
            AlertDialog(
                onDismissRequest = { websiteToDelete = null },
                containerColor = appleColors.secondaryGroupedBackground,
                tonalElevation = 0.dp,
                title = {
                    Text(
                        text = "Remove Link",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appleColors.label
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to remove \"${websiteToDelete?.title}\" from your stack?",
                        fontSize = 14.sp,
                        color = appleColors.secondaryLabel,
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
                            containerColor = appleColors.destructive,
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
                        colors = ButtonDefaults.textButtonColors(contentColor = appleColors.secondaryLabel)
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
            AppleVersionUpdateScreen(
                onDismiss = {
                    prefs.edit().putInt("last_seen_version_code", 3).apply()
                    showVersion110Screen = false
                },
                onViewAllFeatures = {
                    showWhatsNewSheet = true
                }
            )
        }
    }
}
