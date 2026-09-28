package com.example

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Public
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.Website
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleBlueDark
import com.example.ui.theme.AppleCyan
import com.example.ui.theme.AppleGray
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleGreenDark
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.AppleOrange
import com.example.ui.theme.ApplePink
import com.example.ui.theme.ApplePurple
import com.example.ui.theme.AppleRed
import com.example.ui.theme.AppleRedDark
import com.example.ui.theme.AppleTeal
import com.example.ui.theme.AppleYellow
import com.example.ui.theme.LocalAppleColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WebsiteViewModel
import com.example.ui.viewmodel.WebsiteViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    private val sharedUrlState = MutableStateFlow<String?>(null)

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
                    WebStackScreen(
                        incomingSharedUrl = sharedUrl,
                        onClearIncomingUrl = { sharedUrlState.value = null }
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
            val extracted = extractUrlFromSharedText(text)
            if (extracted.isNotBlank()) {
                sharedUrlState.value = extracted
            }
        }
    }

    private fun extractUrlFromSharedText(text: String): String {
        val urlRegex = """(https?://[^\s]+)""".toRegex()
        val match = urlRegex.find(text)
        return match?.value ?: text.trim()
    }
}

val DEFAULT_CATEGORIES = listOf("Personal", "Design", "Tools", "Work", "Reading")

fun getCategoryAccentColor(category: String, isDark: Boolean): Color {
    val clean = category.trim().lowercase()
    if (clean.isEmpty() || clean == "all" || clean == "uncategorized") {
        return if (isDark) AppleGray else AppleGray
    }

    // Explicit curated mappings for common standard categories
    when (clean) {
        "personal" -> return ApplePurple
        "design" -> return AppleIndigo
        "tools", "dev", "development", "coding", "code" -> return if (isDark) AppleBlueDark else AppleBlue
        "reading", "books", "news", "articles", "article" -> return AppleOrange
        "work", "business", "office" -> return AppleTeal
        "social", "chat", "media" -> return ApplePink
        "finance", "money", "crypto" -> return if (isDark) AppleGreenDark else AppleGreen
        "entertainment", "video", "youtube", "music" -> return if (isDark) AppleRedDark else AppleRed
        "learning", "education", "study" -> return AppleYellow
        "travel", "places" -> return AppleCyan
    }

    // Deterministic vibrant Apple system color palette hashing for ANY custom tag
    val dynamicPalette = listOf(
        AppleIndigo,
        if (isDark) AppleBlueDark else AppleBlue,
        AppleTeal,
        AppleCyan,
        if (isDark) AppleGreenDark else AppleGreen,
        AppleOrange,
        ApplePink,
        ApplePurple,
        if (isDark) AppleRedDark else AppleRed,
        AppleYellow
    )

    val hash = kotlin.math.abs(clean.hashCode())
    return dynamicPalette[hash % dynamicPalette.size]
}

enum class WebStackLayoutMode(val title: String) {
    LARGE_CARDS("Large Cards"),
    GRID_CARDS("2-Card Grid"),
    COMPACT_LIST("Compact List")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebStackScreen(
    incomingSharedUrl: String? = null,
    onClearIncomingUrl: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val appleColors = LocalAppleColors.current
    val viewModel: WebsiteViewModel = viewModel(
        factory = WebsiteViewModelFactory(context.applicationContext as Application)
    )
    val websitesState by viewModel.websitesList.collectAsState()
    val categoriesState by viewModel.categories.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var initialAddUrl by remember { mutableStateOf("") }
    var showMenuSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showWhatsNewSheet by remember { mutableStateOf(false) }
    var showAppInfoSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("webstack_prefs", Context.MODE_PRIVATE) }
    val lastSeenVersion = remember { prefs.getInt("last_seen_version_code", 0) }
    var showVersion101Screen by remember { mutableStateOf(lastSeenVersion < 2) }
    val savedLayoutMode = prefs.getString("layout_mode", null)
    var layoutMode by remember {
        mutableStateOf(
            if (savedLayoutMode != null) {
                try {
                    WebStackLayoutMode.valueOf(savedLayoutMode)
                } catch (_: Exception) {
                    WebStackLayoutMode.LARGE_CARDS
                }
            } else {
                if (prefs.getBoolean("is_compact_list", false)) WebStackLayoutMode.COMPACT_LIST else WebStackLayoutMode.LARGE_CARDS
            }
        )
    }
    val isCompactList = layoutMode == WebStackLayoutMode.COMPACT_LIST
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
            if (!showVersion101Screen) {
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
                            elevation = 16.dp,
                            shape = CircleShape,
                            ambientColor = appleColors.label.copy(alpha = 0.25f),
                            spotColor = appleColors.label.copy(alpha = 0.35f)
                        )
                        .border(BorderStroke(1.dp, appleColors.glassHighlight), CircleShape)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(appleColors.groupedBackground)
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
                            when (layoutMode) {
                                WebStackLayoutMode.COMPACT_LIST -> {
                                    AppleCompactWebsiteRow(
                                        website = website,
                                        refreshToken = refreshToken,
                                        fetchWebPreviews = fetchWebPreviews,
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            openWebsiteInBrowser(context, website.url)
                                        },
                                        onLongClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            websiteForOptions = website
                                        },
                                        onRefreshScreenshot = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (!fetchWebPreviews) {
                                                Toast.makeText(context, "Enable 'Fetch Web Previews' in Settings to update snapshots", Toast.LENGTH_LONG).show()
                                            } else {
                                                viewModel.refreshScreenshot(website.id)
                                                refreshTokens[website.id] = System.currentTimeMillis()
                                                Toast.makeText(context, "Refreshing ${website.title}...", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                WebStackLayoutMode.GRID_CARDS -> {
                                    AppleGridWebsiteCard(
                                        website = website,
                                        refreshToken = refreshToken,
                                        fetchWebPreviews = fetchWebPreviews,
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            openWebsiteInBrowser(context, website.url)
                                        },
                                        onLongClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            websiteForOptions = website
                                        },
                                        onRefreshScreenshot = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (!fetchWebPreviews) {
                                                Toast.makeText(context, "Enable 'Fetch Web Previews' in Settings to update snapshots", Toast.LENGTH_LONG).show()
                                            } else {
                                                viewModel.refreshScreenshot(website.id)
                                                refreshTokens[website.id] = System.currentTimeMillis()
                                                Toast.makeText(context, "Updating snapshot for ${website.title}...", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                WebStackLayoutMode.LARGE_CARDS -> {
                                    AppleWebsiteCard(
                                        website = website,
                                        refreshToken = refreshToken,
                                        fetchWebPreviews = fetchWebPreviews,
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            openWebsiteInBrowser(context, website.url)
                                        },
                                        onLongClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            websiteForOptions = website
                                        },
                                        onRefreshScreenshot = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (!fetchWebPreviews) {
                                                Toast.makeText(context, "Enable 'Fetch Web Previews' in Settings to update snapshots", Toast.LENGTH_LONG).show()
                                            } else {
                                                viewModel.refreshScreenshot(website.id)
                                                refreshTokens[website.id] = System.currentTimeMillis()
                                                Toast.makeText(context, "Updating snapshot for ${website.title}...", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Apple Filter & Categories Bottom Sheet
        if (showMenuSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMenuSheet = false },
                containerColor = appleColors.secondaryGroupedBackground,
                scrimColor = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { AppleSheetDragHandle() }
            ) {
                AppleFilterMenuBottomSheetContent(
                    allWebsites = websitesState,
                    categories = categoriesState,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { cat ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedCategory = cat
                        showMenuSheet = false
                    },
                    onAddTagClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddTagDialog = true
                    },
                    onTagLongPress = { cat ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        categoryForOptions = cat
                    }
                )
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
                        showMenuSheet = false
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
                        showVersion101Screen = true
                    },
                    onOpenAppInfo = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showSettingsSheet = false
                        showAppInfoSheet = true
                    },
                    onDismiss = { showSettingsSheet = false }
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
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            websiteToDelete?.let { viewModel.deleteWebsite(it.id) }
                            websiteToDelete = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appleColors.destructive,
                            contentColor = Color.White
                        )
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
                },
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.border(BorderStroke(1.dp, appleColors.separator), RoundedCornerShape(22.dp))
            )
        }

        // Version 1.0.1 Update Screen
        AnimatedVisibility(
            visible = showVersion101Screen,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f)
        ) {
            AppleVersionUpdateScreen(
                onDismiss = {
                    prefs.edit().putInt("last_seen_version_code", 2).apply()
                    showVersion101Screen = false
                }
            )
        }
    }
}

@Composable
fun AppleSheetDragHandle() {
    val appleColors = LocalAppleColors.current
    Box(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 6.dp)
            .width(36.dp)
            .height(5.dp)
            .background(appleColors.secondaryLabel.copy(alpha = 0.25f), RoundedCornerShape(3.dp))
    )
}

@Composable
fun AppleFloatingIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    testTag: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp
) {
    val appleColors = LocalAppleColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "floating_btn_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = if (appleColors.isDark) Color(0x24FFFFFF) else Color(0x0D000000),
        border = BorderStroke(0.5.dp, if (appleColors.isDark) Color(0x26FFFFFF) else Color(0x12000000)),
        shadowElevation = 0.dp,
        modifier = modifier
            .size(size)
            .scale(scale)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = appleColors.label,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun AppleNavigationHeader(
    selectedCategory: String,
    searchQuery: String,
    isSearchExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val appleColors = LocalAppleColors.current
    val haptics = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        } else {
            keyboardController?.hide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        AnimatedContent(
            targetState = isSearchExpanded,
            transitionSpec = {
                if (targetState) {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideInHorizontally(
                                initialOffsetX = { it / 3 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ))
                        .togetherWith(
                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                    slideOutHorizontally(
                                        targetOffsetX = { -it / 3 },
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                        )
                } else {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideInHorizontally(
                                initialOffsetX = { -it / 3 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ))
                        .togetherWith(
                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                    slideOutHorizontally(
                                        targetOffsetX = { it / 3 },
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                        )
                }
            },
            label = "header_search_transition"
        ) { expanded ->
            if (expanded) {
                // Active Search State: Capsule Search Input + Floating Close Button
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = appleColors.fill,
                        border = BorderStroke(0.5.dp, appleColors.separator.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = appleColors.secondaryLabel,
                                modifier = Modifier.size(18.dp)
                            )

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search stack, URLs, tags...",
                                        color = appleColors.tertiaryLabel,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onQueryChange,
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = appleColors.label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(appleColors.accent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .testTag("search_bar_input"),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearQuery,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = appleColors.label.copy(alpha = 0.15f),
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = appleColors.label,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AppleFloatingIconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClearQuery()
                            onSearchExpandedChange(false)
                        },
                        icon = Icons.Default.Close,
                        contentDescription = "Close Search",
                        testTag = "close_search_button"
                    )
                }
            } else {
                // Resting State: Floating Settings Button + Centered WebStack Title + Floating Search Button
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppleFloatingIconButton(
                        onClick = onOpenSettings,
                        icon = Icons.Outlined.Settings,
                        contentDescription = "Settings and Preferences",
                        testTag = "settings_button"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "WebStack",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = appleColors.label,
                            letterSpacing = (-0.4).sp,
                            maxLines = 1
                        )
                        Text(
                            text = if (selectedCategory == "All") "VISUAL BOOKMARKS" else selectedCategory.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCategory == "All") appleColors.tertiaryLabel else appleColors.accent,
                            letterSpacing = 1.6.sp,
                            maxLines = 1
                        )
                    }

                    AppleFloatingIconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSearchExpandedChange(true)
                        },
                        icon = Icons.Outlined.Search,
                        contentDescription = "Search Bookmarks",
                        testTag = "search_button"
                    )
                }
            }
        }
    }
}

@Composable
fun AppleCategoryCapsuleBar(
    allWebsites: List<Website>,
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onTagLongPress: (String) -> Unit,
    onAddNewTag: () -> Unit
) {
    val appleColors = LocalAppleColors.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" Capsule
        val isAllSelected = selectedCategory.equals("All", ignoreCase = true)
        item {
            AppleCapsule(
                text = "All",
                count = allWebsites.size,
                isSelected = isAllSelected,
                onClick = { onSelectCategory("All") },
                onLongClick = null
            )
        }

        // Category Capsules (Supports single-tap filter & long-press options)
        items(categories) { cat ->
            val isSelected = selectedCategory.equals(cat, ignoreCase = true)
            val count = allWebsites.count { it.category.equals(cat, ignoreCase = true) }
            val accentColor = getCategoryAccentColor(cat, appleColors.isDark)

            AppleCapsule(
                text = cat,
                count = count,
                isSelected = isSelected,
                customAccentColor = if (isSelected) null else accentColor,
                onClick = { onSelectCategory(cat) },
                onLongClick = { onTagLongPress(cat) }
            )
        }

        // "+ Tag" Capsule with Apple Pill Design
        item {
            Surface(
                onClick = onAddNewTag,
                color = appleColors.fill,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(0.75.dp, appleColors.separator),
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom Tag",
                        tint = appleColors.label,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Tag",
                        color = appleColors.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleCapsule(
    text: String,
    count: Int,
    isSelected: Boolean,
    customAccentColor: Color? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val appleColors = LocalAppleColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "capsule_scale"
    )

    Surface(
        color = if (isSelected) appleColors.label else appleColors.surface,
        border = BorderStroke(
            0.5.dp,
            if (isSelected) Color.Transparent else appleColors.separator
        ),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = if (isSelected && !appleColors.isDark) 2.dp else 0.dp,
        modifier = Modifier
            .height(34.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("category_item_$text")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (customAccentColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(customAccentColor, CircleShape)
                )
            }
            Text(
                text = text,
                color = if (isSelected) appleColors.systemBackground else appleColors.label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .defaultMinSize(minWidth = 18.dp)
                        .background(
                            color = if (isSelected) appleColors.systemBackground.copy(alpha = 0.25f) else appleColors.fill,
                            shape = CircleShape
                        )
                        .padding(horizontal = if (count > 9) 5.dp else 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        color = if (isSelected) appleColors.systemBackground else appleColors.secondaryLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = (-1).dp),
                        style = TextStyle(
                            platformStyle = @Suppress("DEPRECATION") PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            lineHeight = 10.sp,
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleWebsiteCard(
    website: Website,
    refreshToken: Long,
    fetchWebPreviews: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRefreshScreenshot: () -> Unit
) {
    val context = LocalContext.current
    val appleColors = LocalAppleColors.current
    val coroutineScope = rememberCoroutineScope()
    val localFile = remember(website.id, refreshToken) { File(context.filesDir, "screenshot_${website.id}.jpg") }
    var hasLocalImage by remember(website.id, refreshToken) { mutableStateOf(localFile.exists()) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "apple_card_scale"
    )

    val cardShape = RoundedCornerShape(22.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(cardShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("website_card_${website.id}"),
        colors = CardDefaults.cardColors(containerColor = appleColors.surface),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (appleColors.isDark) 3.dp else 6.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Color.Black.copy(alpha = if (appleColors.isDark) 0.2f else 0.03f),
                    spotColor = Color.Black.copy(alpha = if (appleColors.isDark) 0.35f else 0.05f)
                )
                .background(appleColors.surface, RoundedCornerShape(22.dp))
                .border(BorderStroke(0.5.dp, appleColors.separator), RoundedCornerShape(22.dp))
        ) {
            // Top Preview Slot displaying Website Screenshot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
                    .background(appleColors.secondaryBackground)
            ) {
                if (hasLocalImage) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(localFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview snapshot of ${website.title}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else if (fetchWebPreviews) {
                    val previewUrl = remember(website.url, refreshToken) {
                        try {
                            val encodedUrl = java.net.URLEncoder.encode(website.url, "UTF-8")
                            val ts = if (refreshToken > 0) "&t=$refreshToken" else ""
                            "https://api.microlink.io/?url=$encodedUrl&screenshot=true&embed=screenshot.url$ts"
                        } catch (e: Exception) {
                            website.url
                        }
                    }

                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(previewUrl)
                            .crossfade(true)
                            .build(),
                        onSuccess = { state ->
                            val drawable = state.result.drawable
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val bitmap = drawable.toBitmap()
                                    FileOutputStream(localFile).use { out ->
                                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                                    }
                                    hasLocalImage = true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        contentDescription = "Preview snapshot of ${website.title}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = appleColors.accent,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(appleColors.fill),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = null,
                                        tint = appleColors.secondaryLabel,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = website.domain,
                                        fontSize = 14.sp,
                                        color = appleColors.secondaryLabel,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.2).sp
                                    )
                                }
                            }
                        }
                    )
                } else {
                    // Local Apple Card representation when Fetch Web Previews is OFF
                    val catAccent = getCategoryAccentColor(website.category, appleColors.isDark)
                    val domainInitial = website.domain.trimStart().removePrefix("www.").firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "W"
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        catAccent.copy(alpha = if (appleColors.isDark) 0.16f else 0.08f),
                                        appleColors.secondaryBackground
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = catAccent.copy(alpha = if (appleColors.isDark) 0.25f else 0.15f),
                                border = BorderStroke(1.dp, catAccent.copy(alpha = 0.40f)),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = domainInitial,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = catAccent
                                    )
                                }
                            }
                            Text(
                                text = website.domain,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appleColors.secondaryLabel,
                                letterSpacing = (-0.2).sp
                            )
                        }
                    }
                }

                // Top-Right Action: Refresh Snapshot with 44dp Touch Target (only when previews enabled or local file exists)
                if (fetchWebPreviews || hasLocalImage) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = onRefreshScreenshot,
                            color = (if (appleColors.isDark) Color(0xCC1C1C1E) else Color(0xEBFFFFFF)),
                            shape = CircleShape,
                            border = BorderStroke(0.5.dp, appleColors.glassHighlight),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "Refresh Screenshot",
                                    tint = appleColors.label,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Informational Metadata
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = website.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = appleColors.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = website.url,
                        fontSize = 12.sp,
                        color = appleColors.secondaryLabel,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Semantic Category Badge
                val catAccent = getCategoryAccentColor(website.category, appleColors.isDark)
                Surface(
                    color = catAccent.copy(alpha = if (appleColors.isDark) 0.2f else 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = website.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = catAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleGridWebsiteCard(
    website: Website,
    refreshToken: Long,
    fetchWebPreviews: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRefreshScreenshot: () -> Unit
) {
    val context = LocalContext.current
    val appleColors = LocalAppleColors.current
    val coroutineScope = rememberCoroutineScope()
    val localFile = remember(website.id, refreshToken) { File(context.filesDir, "screenshot_${website.id}.jpg") }
    var hasLocalImage by remember(website.id, refreshToken) { mutableStateOf(localFile.exists()) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "apple_grid_card_scale"
    )

    val cardShape = RoundedCornerShape(18.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(cardShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("website_grid_card_${website.id}"),
        colors = CardDefaults.cardColors(containerColor = appleColors.surface),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (appleColors.isDark) 3.dp else 6.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = Color.Black.copy(alpha = if (appleColors.isDark) 0.2f else 0.03f),
                    spotColor = Color.Black.copy(alpha = if (appleColors.isDark) 0.3f else 0.05f)
                )
                .background(appleColors.surface, RoundedCornerShape(18.dp))
                .border(BorderStroke(0.5.dp, appleColors.separator), RoundedCornerShape(18.dp))
        ) {
            // Snapshot Preview Header (105dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .background(appleColors.secondaryBackground)
            ) {
                if (hasLocalImage) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(localFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview snapshot of ${website.title}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else if (fetchWebPreviews) {
                    val previewUrl = remember(website.url, refreshToken) {
                        try {
                            val encodedUrl = java.net.URLEncoder.encode(website.url, "UTF-8")
                            val ts = if (refreshToken > 0) "&t=$refreshToken" else ""
                            "https://api.microlink.io/?url=$encodedUrl&screenshot=true&embed=screenshot.url$ts"
                        } catch (e: Exception) {
                            website.url
                        }
                    }

                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(previewUrl)
                            .crossfade(true)
                            .build(),
                        onSuccess = { state ->
                            val drawable = state.result.drawable
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val bitmap = drawable.toBitmap()
                                    FileOutputStream(localFile).use { out ->
                                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                                    }
                                    hasLocalImage = true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        contentDescription = "Preview snapshot of ${website.title}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = appleColors.accent,
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(appleColors.fill),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    tint = appleColors.secondaryLabel,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                } else {
                    val catAccent = getCategoryAccentColor(website.category, appleColors.isDark)
                    val domainInitial = website.domain.trimStart().removePrefix("www.").firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "W"
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        catAccent.copy(alpha = if (appleColors.isDark) 0.16f else 0.08f),
                                        appleColors.secondaryBackground
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = catAccent.copy(alpha = if (appleColors.isDark) 0.25f else 0.15f),
                            border = BorderStroke(0.75.dp, catAccent.copy(alpha = 0.40f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = domainInitial,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = catAccent
                                )
                            }
                        }
                    }
                }

                // Small Refresh Button on Top Right
                if (fetchWebPreviews || hasLocalImage) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Surface(
                            onClick = onRefreshScreenshot,
                            color = (if (appleColors.isDark) Color(0xCC1C1C1E) else Color(0xEBFFFFFF)),
                            shape = CircleShape,
                            border = BorderStroke(0.5.dp, appleColors.glassHighlight),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "Refresh Screenshot",
                                    tint = appleColors.label,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Info Details (Title & Domain with Category dot)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                Text(
                    text = website.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = appleColors.label,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val catAccent = getCategoryAccentColor(website.category, appleColors.isDark)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(catAccent, CircleShape)
                    )
                    Text(
                        text = website.domain,
                        fontSize = 11.sp,
                        color = appleColors.secondaryLabel,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleCompactWebsiteRow(
    website: Website,
    refreshToken: Long,
    fetchWebPreviews: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRefreshScreenshot: () -> Unit
) {
    val context = LocalContext.current
    val appleColors = LocalAppleColors.current
    val coroutineScope = rememberCoroutineScope()
    val localFile = remember(website.id, refreshToken) { File(context.filesDir, "screenshot_${website.id}.jpg") }
    var hasLocalImage by remember(website.id, refreshToken) { mutableStateOf(localFile.exists()) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "apple_compact_scale"
    )

    val cardShape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(cardShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("compact_website_row_${website.id}"),
        colors = CardDefaults.cardColors(containerColor = appleColors.surface),
        shape = cardShape,
        border = BorderStroke(0.5.dp, appleColors.separator),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Screenshot Preview
            Box(
                modifier = Modifier
                    .size(width = 76.dp, height = 52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(appleColors.secondaryBackground)
            ) {
                if (hasLocalImage) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(localFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (fetchWebPreviews) {
                    val previewUrl = remember(website.url, refreshToken) {
                        try {
                            val encodedUrl = java.net.URLEncoder.encode(website.url, "UTF-8")
                            val ts = if (refreshToken > 0) "&t=$refreshToken" else ""
                            "https://api.microlink.io/?url=$encodedUrl&screenshot=true&embed=screenshot.url$ts"
                        } catch (e: Exception) {
                            website.url
                        }
                    }

                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(previewUrl)
                            .crossfade(true)
                            .build(),
                        onSuccess = { state ->
                            if (!hasLocalImage) {
                                val drawable = state.result.drawable
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val bitmap = drawable.toBitmap()
                                        FileOutputStream(localFile).use { out ->
                                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                                        }
                                        hasLocalImage = true
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    tint = appleColors.secondaryLabel,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                } else {
                    // Local Compact Monogram when Fetch Web Previews is OFF
                    val catAccent = getCategoryAccentColor(website.category, appleColors.isDark)
                    val domainInitial = website.domain.trimStart().removePrefix("www.").firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "W"
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        catAccent.copy(alpha = if (appleColors.isDark) 0.22f else 0.12f),
                                        appleColors.secondaryBackground
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = domainInitial,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = catAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = website.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = appleColors.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = website.domain,
                        modifier = Modifier.weight(1f, fill = false),
                        fontSize = 12.sp,
                        color = appleColors.secondaryLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = appleColors.tertiaryLabel
                    )
                    val catAccent = getCategoryAccentColor(website.category, appleColors.isDark)
                    Text(
                        text = website.category,
                        fontSize = 11.sp,
                        color = catAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quick Refresh Button with 44dp Touch Target
            IconButton(
                onClick = onRefreshScreenshot,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh Screenshot",
                    tint = appleColors.secondaryLabel,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AppleEmptyState(
    searchQuery: String,
    selectedCategory: String,
    onClearSearch: () -> Unit,
    onShowAll: () -> Unit,
    onAddLink: () -> Unit
) {
    val appleColors = LocalAppleColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = appleColors.fill,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            searchQuery.isNotBlank() -> Icons.Outlined.Search
                            selectedCategory == "All" -> Icons.Outlined.Layers
                            else -> Icons.Default.FilterList
                        },
                        contentDescription = null,
                        tint = appleColors.secondaryLabel,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = when {
                    searchQuery.isNotBlank() -> "No Matching Bookmarks"
                    selectedCategory == "All" -> "No Links in Stack"
                    else -> "No Links in \"$selectedCategory\""
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = appleColors.label,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when {
                    searchQuery.isNotBlank() -> "No bookmarks matched \"$searchQuery\". Try checking for typos or searching another term."
                    selectedCategory == "All" -> "Tap the '+' button below or share links from Safari, Chrome, or any app."
                    else -> "No links tagged with '$selectedCategory'. Tap 'Show All' or create a new bookmark with this tag."
                },
                fontSize = 13.sp,
                color = appleColors.secondaryLabel,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            when {
                searchQuery.isNotBlank() -> {
                    Button(
                        onClick = onClearSearch,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appleColors.label,
                            contentColor = appleColors.systemBackground
                        )
                    ) {
                        Text("Clear Search", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                selectedCategory != "All" -> {
                    Button(
                        onClick = onShowAll,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appleColors.label,
                            contentColor = appleColors.systemBackground
                        )
                    ) {
                        Text("Show All Links", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = onAddLink,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appleColors.label,
                            contentColor = appleColors.systemBackground
                        )
                    ) {
                        Text("Add Your First Link", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleFilterMenuBottomSheetContent(
    allWebsites: List<Website>,
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAddTagClick: () -> Unit,
    onTagLongPress: (String) -> Unit
) {
    val appleColors = LocalAppleColors.current

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
            Column {
                Text(
                    text = "Tags & Categories",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = appleColors.label,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to filter • Long press to manage",
                    fontSize = 12.sp,
                    color = appleColors.secondaryLabel
                )
            }

            Surface(
                onClick = onAddTagClick,
                color = appleColors.fill,
                shape = CircleShape,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("menu_header_add_tag_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Tag",
                        tint = appleColors.label,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inset Grouped Section for All Links
        val isAllSelected = selectedCategory.equals("All", ignoreCase = true)
        Surface(
            onClick = { onSelectCategory("All") },
            color = if (isAllSelected) appleColors.label else appleColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, if (isAllSelected) Color.Transparent else appleColors.separator),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isAllSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = appleColors.systemBackground,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "All Links",
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp,
                        color = if (isAllSelected) appleColors.systemBackground else appleColors.label
                    )
                }

                Text(
                    text = "${allWebsites.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAllSelected) appleColors.systemBackground.copy(alpha = 0.7f) else appleColors.secondaryLabel
                )
            }
        }

        // Custom & Default Categories List
        categories.forEach { category ->
            val count = allWebsites.count { it.category.equals(category, ignoreCase = true) }
            val isSelected = selectedCategory.equals(category, ignoreCase = true)
            val catAccent = getCategoryAccentColor(category, appleColors.isDark)

            Surface(
                color = if (isSelected) appleColors.label else appleColors.surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.75.dp, if (isSelected) Color.Transparent else appleColors.separator),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectCategory(category) },
                        onLongClick = { onTagLongPress(category) }
                    )
                    .testTag("category_item_$category")
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = appleColors.systemBackground,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(catAccent, CircleShape)
                            )
                        }
                        Text(
                            text = category,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp,
                            color = if (isSelected) appleColors.systemBackground else appleColors.label
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$count",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) appleColors.systemBackground.copy(alpha = 0.7f) else appleColors.secondaryLabel
                        )
                        IconButton(
                            onClick = { onTagLongPress(category) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Tag Options",
                                tint = if (isSelected) appleColors.systemBackground.copy(alpha = 0.7f) else appleColors.tertiaryLabel,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
fun AppleTagOptionsBottomSheetContent(
    tagName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFilterByTag: () -> Unit,
    onDismiss: () -> Unit
) {
    val appleColors = LocalAppleColors.current
    val haptics = LocalHapticFeedback.current
    val catAccent = getCategoryAccentColor(tagName, appleColors.isDark)

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
                    color = catAccent.copy(alpha = if (appleColors.isDark) 0.25f else 0.15f),
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
                        color = appleColors.label
                    )
                    Text(
                        text = "Tag Options",
                        fontSize = 12.sp,
                        color = appleColors.secondaryLabel
                    )
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appleColors.secondaryLabel
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
            color = appleColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.separator),
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
                    tint = appleColors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Filter links by \"$tagName\"",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = appleColors.label
                )
            }
        }

        // Action 2: Edit Tag
        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onEdit()
            },
            color = appleColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.separator),
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
                    tint = appleColors.label,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Edit Tag",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = appleColors.label
                )
            }
        }

        // Action 3: Delete Tag (Destructive red color text and icon)
        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            },
            color = appleColors.destructive.copy(alpha = if (appleColors.isDark) 0.15f else 0.08f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.destructive.copy(alpha = 0.3f)),
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
                    tint = appleColors.destructive,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Delete Tag",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = appleColors.destructive
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

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
                Column {
                    Text(
                        text = "New Tag",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appleColors.label
                    )
                    Text(
                        text = "Organize bookmarks with custom tags",
                        fontSize = 12.sp,
                        color = appleColors.secondaryLabel
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Apple Inset Grouped Text Input Container
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
                Column {
                    Text(
                        text = "Edit Tag",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appleColors.label
                    )
                    Text(
                        text = "Update tag name across bookmarks",
                        fontSize = 12.sp,
                        color = appleColors.secondaryLabel
                    )
                }
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

@Composable
fun AppleSettingsBottomSheetContent(
    layoutMode: WebStackLayoutMode,
    onSetLayoutMode: (WebStackLayoutMode) -> Unit,
    fetchWebPreviews: Boolean,
    onToggleFetchWebPreviews: (Boolean) -> Unit,
    onOpenWhatsNew: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onDismiss: () -> Unit
) {
    val appleColors = LocalAppleColors.current

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
                            imageVector = Icons.Outlined.Settings,
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

        // Section: Layout Mode
        Text(
            text = "LAYOUT MODE",
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

        // Section: Privacy & Network
        Text(
            text = "PRIVACY & NETWORK",
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

        // Section: Discover & Guides
        Text(
            text = "DISCOVER",
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
                                painter = painterResource(id = R.drawable.ic_hexagon),
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

@Composable
fun AppleSettingsBottomSheetContent(
    isCompactList: Boolean,
    onSetCompactList: (Boolean) -> Unit,
    fetchWebPreviews: Boolean,
    onToggleFetchWebPreviews: (Boolean) -> Unit,
    onOpenWhatsNew: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onDismiss: () -> Unit
) {
    AppleSettingsBottomSheetContent(
        layoutMode = if (isCompactList) WebStackLayoutMode.COMPACT_LIST else WebStackLayoutMode.LARGE_CARDS,
        onSetLayoutMode = { onSetCompactList(it == WebStackLayoutMode.COMPACT_LIST) },
        fetchWebPreviews = fetchWebPreviews,
        onToggleFetchWebPreviews = onToggleFetchWebPreviews,
        onOpenWhatsNew = onOpenWhatsNew,
        onOpenAppInfo = onOpenAppInfo,
        onDismiss = onDismiss
    )
}

@Composable
fun AppleWhatsNewBottomSheetContent(
    onDismiss: () -> Unit
) {
    val appleColors = LocalAppleColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
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
                Image(
                    painter = painterResource(id = R.drawable.ic_hexagon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(appleColors.label)
                )
                Text(
                    text = "What's New",
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

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "WebStack is a modern visual bookmark stack designed for fast scanning, offline reliability, and aesthetic clarity.",
            fontSize = 13.sp,
            color = appleColors.secondaryLabel,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Feature items
        AppleWhatsNewFeatureItem(
            icon = Icons.Outlined.Speed,
            title = "Offline Snapshot Caching",
            description = "Screenshots are saved locally on your device. After the first load, previews appear instantly with zero data consumption."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Outlined.Share,
            title = "System Share Sheet Integration",
            description = "Share links directly from Safari, Chrome, Twitter/X, or any app straight into WebStack in 1 tap."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Default.ContentPaste,
            title = "Instant Clipboard Detection",
            description = "Opening the add sheet automatically detects copied website links and presents a 1-tap 'Paste' banner."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Default.GridView,
            title = "Dual Layout Switcher",
            description = "Toggle effortlessly between large visual screenshot cards and high-density compact list view."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Outlined.Refresh,
            title = "Manual Snapshot Refresh",
            description = "Re-capture any website snapshot whenever page visuals change with the instant refresh action."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Default.FilterList,
            title = "Smart Categories & Filter",
            description = "Organize and filter by All, Personal, Design, Tools, Work, and Reading with the hexagonal menu."
        )

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
            Text("Got It", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AppleWhatsNewFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    val appleColors = LocalAppleColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = appleColors.fill,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = appleColors.label,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = appleColors.label
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = appleColors.secondaryLabel,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun AppleVersionUpdateScreen(
    onDismiss: () -> Unit
) {
    val appleColors = LocalAppleColors.current
    val haptics = LocalHapticFeedback.current
    var targetDigit by remember { mutableStateOf("0") }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(450)
        targetDigit = "1"
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appleColors.systemBackground.copy(alpha = if (appleColors.isDark) 0.96f else 0.94f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("version_101_screen")
    ) {
        // Main Centered Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Version Odometer (1.0.0 -> 1.0.1)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "1.0.",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    color = appleColors.label,
                    letterSpacing = (-1.5).sp
                )
                AnimatedContent(
                    targetState = targetDigit,
                    transitionSpec = {
                        (slideInVertically(
                            animationSpec = spring(
                                dampingRatio = 0.72f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically(
                            animationSpec = spring(
                                dampingRatio = 0.72f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { height -> -height } + fadeOut())
                    },
                    label = "odometer_digit"
                ) { digit ->
                    Text(
                        text = digit,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        color = appleColors.label,
                        letterSpacing = (-1.5).sp
                    )
                }
            }

            // Compact Frosted What's New Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = appleColors.surface,
                border = BorderStroke(0.5.dp, appleColors.separator),
                shadowElevation = if (appleColors.isDark) 0.dp else 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Feature 1: 2-Card Grid
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = appleColors.fill,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = appleColors.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "2-Card Grid",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = appleColors.label,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                text = "New 2-column layout mode",
                                fontSize = 13.sp,
                                color = appleColors.secondaryLabel
                            )
                        }
                    }

                    // Feature 2: Enhanced UI
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = appleColors.fill,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Speed,
                                    contentDescription = null,
                                    tint = appleColors.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Enhanced UI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = appleColors.label,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                text = "Softer feel & refined animations",
                                fontSize = 13.sp,
                                color = appleColors.secondaryLabel
                            )
                        }
                    }

                    // Feature 3: Bug Fixes
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = appleColors.fill,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = appleColors.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Bug Fixes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = appleColors.label,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                text = "Stability & visual improvements",
                                fontSize = 13.sp,
                                color = appleColors.secondaryLabel
                            )
                        }
                    }
                }
            }
        }

        // Bottom-Right Black Pill Action Button
        val pillInteractionSource = remember { MutableInteractionSource() }
        val isPillPressed by pillInteractionSource.collectIsPressedAsState()
        val pillScale by animateFloatAsState(
            targetValue = if (isPillPressed) 0.90f else 1f,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
            label = "pill_enter_scale"
        )

        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onDismiss()
            },
            interactionSource = pillInteractionSource,
            shape = CircleShape,
            color = if (appleColors.isDark) Color.White else Color(0xFF0F0F12),
            contentColor = if (appleColors.isDark) Color.Black else Color.White,
            shadowElevation = if (appleColors.isDark) 0.dp else 4.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 28.dp, end = 24.dp)
                .size(width = 68.dp, height = 48.dp)
                .scale(pillScale)
                .testTag("version_101_enter_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Enter WebStack",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AppleAppInfoBottomSheetContent(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appleColors = LocalAppleColors.current
    val versionName = BuildConfig.VERSION_NAME

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header with Close Button
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
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = appleColors.systemBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "App Info",
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

        // Hero App Branding Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = appleColors.surface,
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = appleColors.fill,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.size(68.dp),
                    border = BorderStroke(1.dp, appleColors.separator.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_hexagon),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            colorFilter = ColorFilter.tint(appleColors.label)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "WebStack",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = appleColors.label,
                    letterSpacing = (-0.6).sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Version $versionName • by Chiranth Moger",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = appleColors.secondaryLabel
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = appleColors.fill,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Free & Open Source Software",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appleColors.secondaryLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Links & Community
        Text(
            text = "RESOURCES & COMMUNITY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appleColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Grouped Actions Card (Apple HIG List)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appleColors.surface,
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Source Code / GitHub Row
                Surface(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Chiranth-Janardhan-moger/webstack"))
                        context.startActivity(intent)
                    },
                    color = Color.Transparent,
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
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                color = appleColors.fill,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Code,
                                        contentDescription = null,
                                        tint = appleColors.label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Source Code",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = appleColors.label
                                )
                                Text(
                                    text = "github.com/Chiranth-Janardhan-moger/webstack",
                                    fontSize = 11.sp,
                                    color = appleColors.secondaryLabel
                                )
                            }
                        }

                        Image(
                            painter = painterResource(id = R.drawable.ic_github),
                            contentDescription = "GitHub",
                            modifier = Modifier.size(22.dp),
                            colorFilter = ColorFilter.tint(appleColors.label)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = appleColors.separator
                )

                // Report Bugs / Suggest Features Row
                Surface(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Chiranth-Janardhan-moger/webstack/issues"))
                        context.startActivity(intent)
                    },
                    color = Color.Transparent,
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
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                color = appleColors.fill,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.BugReport,
                                        contentDescription = null,
                                        tint = appleColors.label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Help Us Improve",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = appleColors.label
                                )
                                Text(
                                    text = "Report bugs or suggest features on GitHub Issues",
                                    fontSize = 11.sp,
                                    color = appleColors.secondaryLabel
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = appleColors.tertiaryLabel,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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

@Composable
fun AppleAddWebsiteSheetContent(
    initialUrl: String = "",
    categories: List<String> = DEFAULT_CATEGORIES,
    isSaving: Boolean,
    saveError: String?,
    onAddNewTag: () -> Unit = {},
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appleColors = LocalAppleColors.current
    var inputUrl by remember { mutableStateOf(initialUrl) }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "Personal") }

    // Check system clipboard for a URL
    val clipboardUrl = remember { getClipboardUrl(context) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Save Website Link",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = appleColors.label,
                letterSpacing = (-0.3).sp
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel and Close",
                    tint = appleColors.secondaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Instant Clipboard Pill Button
        if (!clipboardUrl.isNullOrBlank() && inputUrl.isBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                onClick = { inputUrl = clipboardUrl },
                color = appleColors.fill,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(0.75.dp, appleColors.separator),
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste from Clipboard",
                        tint = appleColors.label,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Clipboard",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appleColors.label
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TextInput
        OutlinedTextField(
            value = inputUrl,
            onValueChange = { inputUrl = it },
            placeholder = {
                Text(
                    text = "e.g., linear.app or https://github.com",
                    color = appleColors.tertiaryLabel,
                    fontSize = 14.sp
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("url_text_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = appleColors.surface,
                unfocusedContainerColor = appleColors.surface,
                focusedBorderColor = appleColors.accent,
                unfocusedBorderColor = appleColors.separator,
                cursorColor = appleColors.accent,
                focusedTextColor = appleColors.label,
                unfocusedTextColor = appleColors.label
            ),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (inputUrl.isNotBlank() && !isSaving) {
                        onSave(inputUrl, selectedCategory)
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Tag Selection
        Text(
            text = "Category Tag",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = appleColors.secondaryLabel,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    onClick = { selectedCategory = cat },
                    color = if (isSelected) appleColors.label else appleColors.surface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.75.dp, if (isSelected) Color.Transparent else appleColors.separator)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) appleColors.systemBackground else appleColors.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            item {
                Surface(
                    onClick = onAddNewTag,
                    color = appleColors.fill,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, appleColors.separator)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Tag",
                            tint = appleColors.label,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "New Tag",
                            color = appleColors.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Show fetching / error state
        if (isSaving) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = appleColors.accent
                )
                Text(
                    text = "Resolving URL & capturing visuals...",
                    color = appleColors.secondaryLabel,
                    fontSize = 12.sp
                )
            }
        }

        if (saveError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = saveError,
                color = appleColors.destructive,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Save CTA button
        Button(
            onClick = { onSave(inputUrl, selectedCategory) },
            enabled = inputUrl.isNotBlank() && !isSaving,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appleColors.label,
                contentColor = appleColors.systemBackground,
                disabledContainerColor = appleColors.fill,
                disabledContentColor = appleColors.tertiaryLabel
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_website_button")
        ) {
            Text(
                text = "Save Link",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

private fun getClipboardUrl(context: Context): String? {
    return try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0)?.text?.toString()?.trim() ?: ""
            if (text.startsWith("http://") || text.startsWith("https://") ||
                (text.contains(".") && !text.contains(" ") && text.length > 3 && !text.endsWith("."))
            ) {
                text
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun openWebsiteInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open browser. Invalid URL format.", Toast.LENGTH_SHORT).show()
    }
}

private fun shareWebsiteLink(context: Context, website: Website) {
    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, "${website.title}\n${website.url}")
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Link").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share link", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AppleItemOptionsBottomSheetContent(
    website: Website,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onRefresh: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val appleColors = LocalAppleColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header with Website Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                val catAccent = getCategoryAccentColor(website.category, appleColors.isDark)
                val domainInitial = website.domain.trimStart().removePrefix("www.").firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "W"
                Surface(
                    color = catAccent.copy(alpha = if (appleColors.isDark) 0.22f else 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, catAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = domainInitial,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = catAccent
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = website.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = appleColors.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = website.domain,
                            fontSize = 12.sp,
                            color = appleColors.secondaryLabel
                        )
                        val catAccent = getCategoryAccentColor(website.category, appleColors.isDark)
                        Surface(
                            color = catAccent.copy(alpha = if (appleColors.isDark) 0.2f else 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = website.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = catAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appleColors.secondaryLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action 1: Open in Browser
        Surface(
            onClick = onOpen,
            color = appleColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    tint = appleColors.label,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Open Website",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = appleColors.label
                )
            }
        }

        // Action 2: Edit Link & Tag
        Surface(
            onClick = onEdit,
            color = appleColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .testTag("action_edit_website")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = appleColors.label,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Edit Details & Tag",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = appleColors.label
                )
            }
        }

        // Action 3: Refresh Preview
        Surface(
            onClick = onRefresh,
            color = appleColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = appleColors.label,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Refresh Snapshot",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = appleColors.label
                )
            }
        }

        // Action 4: Share Link
        Surface(
            onClick = onShare,
            color = appleColors.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.separator),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = null,
                    tint = appleColors.label,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Share Website Link",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = appleColors.label
                )
            }
        }

        // Action 5: Delete from Stack
        Surface(
            onClick = onDelete,
            color = appleColors.destructive.copy(alpha = if (appleColors.isDark) 0.15f else 0.08f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.75.dp, appleColors.destructive.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .testTag("action_delete_website")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = appleColors.destructive,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Remove Link from Stack",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = appleColors.destructive
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AppleEditWebsiteSheetContent(
    website: Website,
    categories: List<String>,
    onAddNewTag: () -> Unit,
    onSave: (Website) -> Unit,
    onDismiss: () -> Unit
) {
    val appleColors = LocalAppleColors.current
    var title by remember { mutableStateOf(website.title) }
    var url by remember { mutableStateOf(website.url) }
    var selectedCategory by remember { mutableStateOf(website.category) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = appleColors.label,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Edit Website Link",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = appleColors.label,
                    letterSpacing = (-0.3).sp
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appleColors.secondaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title Field
        Text(
            text = "Title",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = appleColors.secondaryLabel,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Website Title", color = appleColors.tertiaryLabel, fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = appleColors.surface,
                unfocusedContainerColor = appleColors.surface,
                focusedBorderColor = appleColors.accent,
                unfocusedBorderColor = appleColors.separator,
                cursorColor = appleColors.accent,
                focusedTextColor = appleColors.label,
                unfocusedTextColor = appleColors.label
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // URL Field
        Text(
            text = "URL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = appleColors.secondaryLabel,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            placeholder = { Text("https://example.com", color = appleColors.tertiaryLabel, fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = appleColors.surface,
                unfocusedContainerColor = appleColors.surface,
                focusedBorderColor = appleColors.accent,
                unfocusedBorderColor = appleColors.separator,
                cursorColor = appleColors.accent,
                focusedTextColor = appleColors.label,
                unfocusedTextColor = appleColors.label
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Tag Selection
        Text(
            text = "Category Tag",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = appleColors.secondaryLabel,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                Surface(
                    onClick = { selectedCategory = cat },
                    color = if (isSelected) appleColors.label else appleColors.surface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.75.dp, if (isSelected) Color.Transparent else appleColors.separator)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) appleColors.systemBackground else appleColors.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            item {
                Surface(
                    onClick = onAddNewTag,
                    color = appleColors.fill,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, appleColors.separator)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Tag",
                            tint = appleColors.label,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "New Tag",
                            color = appleColors.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Save Button
        Button(
            onClick = {
                var finalUrl = url.trim()
                if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                    finalUrl = "https://$finalUrl"
                }
                val domain = try {
                    val uri = java.net.URI(finalUrl)
                    val host = uri.host ?: finalUrl.replace("https://", "").replace("http://", "").split("/")[0]
                    if (host.startsWith("www.")) host.substring(4) else host
                } catch (e: Exception) {
                    website.domain
                }
                val faviconUrl = ""

                val updated = website.copy(
                    title = title.trim().ifBlank { website.title },
                    url = finalUrl,
                    domain = domain,
                    faviconUrl = faviconUrl,
                    category = selectedCategory
                )
                onSave(updated)
            },
            enabled = title.isNotBlank() && url.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appleColors.label,
                contentColor = appleColors.systemBackground,
                disabledContainerColor = appleColors.fill,
                disabledContentColor = appleColors.tertiaryLabel
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Save Changes",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

