package com.example.ui.sheets

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import com.example.ui.components.AppCapsule
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.dialogs.EditTagDialog
import com.example.ui.theme.AppRed
import com.example.ui.theme.LocalAppColors
import com.example.ui.viewmodel.WebsiteViewModel

private enum class TagFilterTab {
    ALL,
    HIDDEN
}

/**
 * Battle-tested state holder for drag-and-drop list reordering in Jetpack Compose,
 * modeled after Google AOSP foundation-demos LazyColumnDragAndDropDemo and Calvin-LL/Reorderable.
 *
 * Edge cases handled:
 * 1. Container-Level Pointer Input: Detects drag gestures at the LazyColumn container level instead
 *    of individual child rows, preventing touch stream interruption when rows swap or recompose.
 * 2. Continuous Finger Tracking: Computes draggingItemOffset as (draggingItemInitialOffset + draggedDistance - item.offset),
 *    preventing visual jitter/teleportation when items swap positions.
 * 3. Bidirectional Boundary & Midpoint Detection: Detects midpoint crossing when moving up or down,
 *    supporting both slow drags and rapid multi-item sweeps without out-of-bounds errors.
 * 4. Asynchronous Boundary Auto-Scrolling: Uses a conflated Channel and coroutine-based scrollBy()
 *    to smoothly scroll LazyColumn when dragging near viewport thresholds, eliminating gesture cancellation.
 * 5. Layout Offset Anchoring: Invokes requestScrollToItem when reordering at firstVisibleItemIndex
 *    to prevent LazyColumn anchor jumping.
 * 6. Spring Settling Animation: Animates previousItemOffset back to 0f on drop with Spring physics.
 * 7. Overflow Menu Isolation: Ignores long-press on the far-right trailing area so 3-dot menus
 *    are never accidentally dragged.
 */
private class TagDragDropState(
    val state: LazyListState,
    private val scope: CoroutineScope,
    private val density: Float,
    private val onStartHaptic: () -> Unit,
    private val onSwapHaptic: () -> Unit,
    private val onMove: (Int, Int) -> Unit,
    private val onCommit: () -> Unit
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    internal val scrollChannel = Channel<Float>(capacity = Channel.CONFLATED)

    private var draggingItemDraggedDelta by mutableFloatStateOf(0f)
    private var draggingItemInitialOffset by mutableIntStateOf(0)

    val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggingItemDraggedDelta - item.offset
        } ?: 0f

    val draggingItemLayoutInfo: LazyListItemInfo?
        get() = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggingItemIndex }

    var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set

    val previousItemOffset = Animatable(0f)

    fun onDragStart(offset: Offset) {
        val viewportWidth = state.layoutInfo.viewportSize.width
        if (viewportWidth > 0 && offset.x > viewportWidth - 54 * density) {
            return
        }

        state.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.also {
                draggingItemIndex = it.index
                draggingItemInitialOffset = it.offset
                draggingItemDraggedDelta = 0f
                onStartHaptic()
            }
    }

    fun onDragInterrupted() {
        if (draggingItemIndex != null) {
            previousIndexOfDraggedItem = draggingItemIndex
            val startOffset = draggingItemOffset
            scope.launch {
                previousItemOffset.snapTo(startOffset)
                previousItemOffset.animateTo(
                    0f,
                    spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 1f)
                )
                previousIndexOfDraggedItem = null
            }
        }
        draggingItemDraggedDelta = 0f
        draggingItemIndex = null
        draggingItemInitialOffset = 0
        onCommit()
    }

    fun onDrag(offset: Offset) {
        draggingItemDraggedDelta += offset.y

        val draggingItem = draggingItemLayoutInfo ?: return
        val startOffset = draggingItem.offset + draggingItemOffset
        val endOffset = startOffset + draggingItem.size
        val middleOffset = startOffset + (endOffset - startOffset) / 2f

        val targetItem = state.layoutInfo.visibleItemsInfo.find { item ->
            middleOffset.toInt() in item.offset..item.offsetEnd && draggingItem.index != item.index
        }

        if (targetItem != null) {
            if (
                draggingItem.index == state.firstVisibleItemIndex ||
                targetItem.index == state.firstVisibleItemIndex
            ) {
                state.requestScrollToItem(
                    state.firstVisibleItemIndex,
                    state.firstVisibleItemScrollOffset
                )
            }
            onSwapHaptic()
            onMove(draggingItem.index, targetItem.index)
            draggingItemIndex = targetItem.index
        } else {
            val overscroll = when {
                draggingItemDraggedDelta > 0 ->
                    (endOffset - state.layoutInfo.viewportEndOffset).coerceAtLeast(0f)
                draggingItemDraggedDelta < 0 ->
                    (startOffset - state.layoutInfo.viewportStartOffset).coerceAtMost(0f)
                else -> 0f
            }
            if (overscroll != 0f) {
                scrollChannel.trySend(overscroll.coerceIn(-24f, 24f))
            }
        }
    }

    private val LazyListItemInfo.offsetEnd: Int
        get() = this.offset + this.size
}

private fun Modifier.dragContainer(dragDropState: TagDragDropState): Modifier {
    return pointerInput(dragDropState) {
        detectDragGesturesAfterLongPress(
            onDrag = { change, offset ->
                change.consume()
                dragDropState.onDrag(offset)
            },
            onDragStart = { offset ->
                dragDropState.onDragStart(offset)
            },
            onDragEnd = {
                dragDropState.onDragInterrupted()
            },
            onDragCancel = {
                dragDropState.onDragInterrupted()
            }
        )
    }
}

@Composable
private fun rememberTagDragDropState(
    listState: LazyListState,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onCommit: () -> Unit
): TagDragDropState {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current.density
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnCommit by rememberUpdatedState(onCommit)

    val state = remember(listState) {
        TagDragDropState(
            state = listState,
            scope = scope,
            density = density,
            onStartHaptic = { haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
            onSwapHaptic = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
            onMove = { from, to -> currentOnMove(from, to) },
            onCommit = { currentOnCommit() }
        )
    }

    LaunchedEffect(state) {
        while (true) {
            val diff = state.scrollChannel.receive()
            listState.scrollBy(diff)
        }
    }

    return state
}

@Composable
fun CustomizeTagsSheetContent(
    viewModel: WebsiteViewModel,
    onOpenAddTagDialog: () -> Unit,
    onBack: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    val density = LocalDensity.current

    val activeCategories by viewModel.categories.collectAsState()
    val parkedCategories by viewModel.parkedCategories.collectAsState()
    val fallbackCategory by viewModel.fallbackCategory.collectAsState()
    val allWebsites by viewModel.websitesList.collectAsState()

    var selectedTab by remember { mutableStateOf(TagFilterTab.ALL) }
    var localActiveCategories by remember(activeCategories) { mutableStateOf(activeCategories) }

    var expandedMenuTag by remember { mutableStateOf<String?>(null) }
    var tagToRename by remember { mutableStateOf<String?>(null) }
    var pendingDeleteTag by remember { mutableStateOf<String?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showFallbackMenu by remember { mutableStateOf(false) }

    // Battle-tested DragDropState for tags reordering
    val tagsListState = rememberLazyListState()
    val dragDropState = rememberTagDragDropState(
        listState = tagsListState,
        onMove = { fromIndex, toIndex ->
            if (fromIndex in localActiveCategories.indices && toIndex in localActiveCategories.indices) {
                val updated = localActiveCategories.toMutableList().apply {
                    val item = removeAt(fromIndex)
                    add(toIndex.coerceIn(0, size), item)
                }
                localActiveCategories = updated
            }
        },
        onCommit = {
            viewModel.updateCategoriesOrder(localActiveCategories)
        }
    )

    val categoryCounts = remember(allWebsites) {
        val current = allWebsites ?: emptyList()
        current.groupingBy { it.category.lowercase() }.eachCount()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            AppSubSheetHeader(
                title = "Customize Tags",
                onBack = onBack,
                onDismiss = onDismiss
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Live Preview Section (Home screen preview)
            Surface(
                color = if (appColors.isDark) Color(0xFF1C1C1E) else Color(0xFFF4F4F6),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = "PREVIEW",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.secondaryLabel,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            AppCapsule(
                                text = "All",
                                isSelected = true,
                                count = allWebsites?.size ?: 0,
                                onClick = {}
                            )
                        }

                        items(activeCategories, key = { it }) { cat ->
                            val count = categoryCounts[cat.lowercase()] ?: 0
                            AppCapsule(
                                text = cat,
                                isSelected = false,
                                count = count,
                                onClick = {}
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Segmented Slider (Matching Sort Order shape: Black thumb on white theme, White thumb on dark theme)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(3.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val segmentWidth = maxWidth / 2
                    val selectedIndex = if (selectedTab == TagFilterTab.ALL) 0 else 1
                    val targetOffset = segmentWidth * selectedIndex
                    val animatedOffset by animateDpAsState(
                        targetValue = targetOffset,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "tag_filter_segment_slider"
                    )

                    val thumbColor by animateColorAsState(
                        targetValue = appColors.label,
                        animationSpec = tween(durationMillis = 200),
                        label = "tag_filter_thumb_color"
                    )

                    // Animated sliding thumb
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x = animatedOffset.roundToPx(), y = 0) }
                            .width(segmentWidth)
                            .fillMaxHeight()
                            .background(
                                color = thumbColor,
                                shape = RoundedCornerShape(9.dp)
                            )
                    )

                    // Interactive touch segments: [ All tags (X) | Hidden (Y) ]
                    Row(modifier = Modifier.fillMaxSize()) {
                        // All tags tab
                        val isAllSelected = selectedTab == TagFilterTab.ALL
                        val allTextColor by animateColorAsState(
                            targetValue = if (isAllSelected) appColors.systemBackground else appColors.secondaryLabel,
                            animationSpec = tween(durationMillis = 200),
                            label = "all_tags_text_color"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (selectedTab != TagFilterTab.ALL) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedTab = TagFilterTab.ALL
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "All tags (${activeCategories.size})",
                                fontSize = 13.sp,
                                fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = allTextColor
                            )
                        }

                        // Hidden tab
                        val isHiddenSelected = selectedTab == TagFilterTab.HIDDEN
                        val hiddenTextColor by animateColorAsState(
                            targetValue = if (isHiddenSelected) appColors.systemBackground else appColors.secondaryLabel,
                            animationSpec = tween(durationMillis = 200),
                            label = "hidden_tags_text_color"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (selectedTab != TagFilterTab.HIDDEN) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedTab = TagFilterTab.HIDDEN
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Hidden (${parkedCategories.size})",
                                fontSize = 13.sp,
                                fontWeight = if (isHiddenSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = hiddenTextColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Tag Items Grouped Card (Consistent fixed height prevents layout shifts between tabs)
            val currentList = if (selectedTab == TagFilterTab.ALL) localActiveCategories else parkedCategories

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (appColors.isDark) appColors.secondaryGroupedBackground else Color.White,
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                if (currentList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == TagFilterTab.ALL) {
                                "No active tags"
                            } else {
                                "No hidden tags\nTags hidden from Home will appear here."
                            },
                            fontSize = 13.sp,
                            color = appColors.secondaryLabel,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = tagsListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (selectedTab == TagFilterTab.ALL) Modifier.dragContainer(dragDropState) else Modifier)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        itemsIndexed(currentList, key = { _, tag -> tag }) { index, tag ->
                            val count = categoryCounts[tag.lowercase()] ?: 0
                            val isBeingDragged = selectedTab == TagFilterTab.ALL && dragDropState.draggingItemIndex == index
                            val isPrevious = selectedTab == TagFilterTab.ALL && dragDropState.previousIndexOfDraggedItem == index

                            Surface(
                                color = if (isBeingDragged) {
                                    if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
                                } else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                                border = if (isBeingDragged) BorderStroke(1.dp, appColors.accent.copy(alpha = 0.6f)) else null,
                                shadowElevation = if (isBeingDragged) 8.dp else 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .zIndex(if (isBeingDragged || isPrevious) 10f else 0f)
                                    .graphicsLayer {
                                        if (isBeingDragged) {
                                            translationY = dragDropState.draggingItemOffset
                                            scaleX = 1.02f
                                            scaleY = 1.02f
                                        } else if (isPrevious) {
                                            translationY = dragDropState.previousItemOffset.value
                                        }
                                    }
                                    .then(if (isBeingDragged || isPrevious) Modifier else Modifier.animateItem())
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left/Middle Area: Drag Handle + Tag Name & Count on a Single Line: Work (6)
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Drag Handle Icon
                                            Box(
                                                modifier = Modifier.size(28.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DragIndicator,
                                                    contentDescription = "Reorder",
                                                    tint = if (isBeingDragged) appColors.accent else appColors.tertiaryLabel,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Tag Name & Count on a Single Line: Work (6)
                                            Text(
                                                text = tag,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = appColors.label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "($count)",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = appColors.secondaryLabel
                                            )
                                        }

                                        // 3-Dot Overflow Menu
                                        Box {
                                            IconButton(
                                                onClick = {
                                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    expandedMenuTag = tag
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = appColors.secondaryLabel,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = expandedMenuTag == tag,
                                                onDismissRequest = { expandedMenuTag = null },
                                                shape = RoundedCornerShape(16.dp),
                                                containerColor = if (appColors.isDark) Color(0xFF1C1C1E) else Color.White,
                                                tonalElevation = 0.dp,
                                                shadowElevation = 6.dp,
                                                border = BorderStroke(
                                                    0.75.dp,
                                                    if (appColors.isDark) Color(0xFF38383A) else Color(0xFFE5E5EA)
                                                ),
                                                modifier = Modifier.widthIn(min = 180.dp)
                                            ) {
                                                // Reorder Actions in 3-Dot Menu for Quick Accessibility
                                                if (selectedTab == TagFilterTab.ALL && localActiveCategories.size > 1) {
                                                    val currentIndex = localActiveCategories.indexOf(tag)
                                                    if (currentIndex > 0) {
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text(
                                                                    text = "Move Up",
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = appColors.label
                                                                )
                                                            },
                                                            leadingIcon = {
                                                                Icon(
                                                                    imageVector = Icons.Default.ArrowUpward,
                                                                    contentDescription = null,
                                                                    tint = appColors.label,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            },
                                                            onClick = {
                                                                expandedMenuTag = null
                                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                val updated = localActiveCategories.toMutableList().apply {
                                                                    add(currentIndex - 1, removeAt(currentIndex))
                                                                }
                                                                localActiveCategories = updated
                                                                viewModel.updateCategoriesOrder(updated)
                                                            }
                                                        )
                                                    }
                                                    if (currentIndex < localActiveCategories.lastIndex) {
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text(
                                                                    text = "Move Down",
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = appColors.label
                                                                )
                                                            },
                                                            leadingIcon = {
                                                                Icon(
                                                                    imageVector = Icons.Default.ArrowDownward,
                                                                    contentDescription = null,
                                                                    tint = appColors.label,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            },
                                                            onClick = {
                                                                expandedMenuTag = null
                                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                val updated = localActiveCategories.toMutableList().apply {
                                                                    add(currentIndex + 1, removeAt(currentIndex))
                                                                }
                                                                localActiveCategories = updated
                                                                viewModel.updateCategoriesOrder(updated)
                                                            }
                                                        )
                                                    }
                                                    HorizontalDivider(
                                                        color = appColors.separator.copy(alpha = 0.5f),
                                                        thickness = 0.5.dp
                                                    )
                                                }

                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "Rename",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = appColors.label
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Edit,
                                                            contentDescription = null,
                                                            tint = appColors.label,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    },
                                                    onClick = {
                                                        expandedMenuTag = null
                                                        if (tag.equals("Personal", ignoreCase = true)) {
                                                            Toast.makeText(context, "Personal is the default tag and cannot be renamed", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            tagToRename = tag
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = if (selectedTab == TagFilterTab.ALL) "Hide from Home" else "Show on Home",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = appColors.label
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = if (selectedTab == TagFilterTab.ALL) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                            contentDescription = null,
                                                            tint = appColors.label,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    },
                                                    onClick = {
                                                        expandedMenuTag = null
                                                        if (selectedTab == TagFilterTab.ALL) {
                                                            if (tag.equals("Personal", ignoreCase = true)) {
                                                                Toast.makeText(context, "Personal is the default category and cannot be hidden", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                viewModel.parkCategory(tag)
                                                                Toast.makeText(context, "Hidden \"$tag\" from Home", Toast.LENGTH_SHORT).show()
                                                            }
                                                        } else {
                                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            viewModel.unparkCategory(tag)
                                                            Toast.makeText(context, "Showing \"$tag\" on Home", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "Delete",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = AppRed
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Delete,
                                                            contentDescription = null,
                                                            tint = AppRed,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    },
                                                    onClick = {
                                                        expandedMenuTag = null
                                                        if (tag.equals("Personal", ignoreCase = true)) {
                                                            Toast.makeText(context, "Personal is the default category and cannot be deleted", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            pendingDeleteTag = tag
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    if (index < currentList.lastIndex) {
                                        HorizontalDivider(
                                            color = appColors.separator.copy(alpha = 0.4f),
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fallback Destination Card for Deleted Tags (matching Screenshot 2026-10-18 094037.png)
            Surface(
                color = if (appColors.isDark) appColors.secondaryGroupedBackground else Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                Toast.makeText(
                                    context,
                                    "Bookmarks from deleted tags automatically move here so they're never lost.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "When a tag is deleted",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = appColors.secondaryLabel
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Move bookmarks to",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors.label
                        )
                    }

                    Box {
                        Surface(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showFallbackMenu = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (appColors.isDark) Color(0xFF2C2C2E) else Color.White,
                            border = BorderStroke(1.dp, if (appColors.isDark) Color(0xFF38383A) else Color(0xFFE5E5EA)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = fallbackCategory,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = appColors.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 110.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = appColors.secondaryLabel,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showFallbackMenu,
                            onDismissRequest = { showFallbackMenu = false },
                            offset = DpOffset(x = (-12).dp, y = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            containerColor = if (appColors.isDark) Color(0xFF1C1C1E) else Color.White,
                            tonalElevation = 0.dp,
                            shadowElevation = 4.dp,
                            border = BorderStroke(
                                width = 0.75.dp,
                                color = if (appColors.isDark) Color(0xFF38383A) else Color(0xFFE5E5EA)
                            ),
                            modifier = Modifier
                                .widthIn(min = 165.dp, max = 230.dp)
                                .heightIn(max = 320.dp)
                        ) {
                            val availableFallbackCandidates = remember(activeCategories) {
                                val list = activeCategories.toMutableList()
                                if (!list.any { it.equals("Personal", ignoreCase = true) }) {
                                    list.add(0, "Personal")
                                }
                                list
                            }
                            availableFallbackCandidates.forEach { cat ->
                                val isSelected = cat.equals(fallbackCategory, ignoreCase = true)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = cat,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) appColors.accent else appColors.label
                                        )
                                    },
                                    trailingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = appColors.accent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setFallbackCategory(cat)
                                        showFallbackMenu = false
                                        Toast.makeText(context, "Deleted tags will move bookmarks to \"$cat\"", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Action Bar: Reset Defaults on Left, + New Tag on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Reset Defaults
                TextButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showResetConfirmDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        tint = appColors.secondaryLabel,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reset Defaults",
                        fontSize = 12.5.sp,
                        color = appColors.secondaryLabel
                    )
                }

                // Right: + New Tag (prominent CTA: black background in light theme, white in dark theme)
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenAddTagDialog()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (appColors.isDark) Color.White else Color(0xFF1C1C1E),
                        contentColor = if (appColors.isDark) Color.Black else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = if (appColors.isDark) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New Tag",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = if (appColors.isDark) Color.Black else Color.White
                    )
                }
            }
        }
    }

    // Rename Tag Dialog
    if (tagToRename != null) {
        val currentTag = tagToRename!!
        EditTagDialog(
            currentName = currentTag,
            onSave = { newName ->
                val success = viewModel.renameCategory(currentTag, newName)
                if (success) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    Toast.makeText(context, "Tag renamed to \"$newName\"", Toast.LENGTH_SHORT).show()
                }
                tagToRename = null
            },
            onDismiss = { tagToRename = null }
        )
    }

    // Confirmation Dialog: ALWAYS asks before deleting ANY tag
    if (pendingDeleteTag != null) {
        val tagToDelete = pendingDeleteTag!!
        val count = categoryCounts[tagToDelete.lowercase()] ?: 0

        AlertDialog(
            onDismissRequest = { pendingDeleteTag = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = AppRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Delete \"$tagToDelete\"?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appColors.label
                )
            },
            text = {
                val effectiveFallback = if (tagToDelete.equals(fallbackCategory, ignoreCase = true)) {
                    activeCategories.firstOrNull { !it.equals(tagToDelete, ignoreCase = true) } ?: "Personal"
                } else {
                    fallbackCategory
                }
                val message = if (count > 0) {
                    "This tag is assigned to $count bookmark${if (count == 1) "" else "s"}.\n\nDeleting will move these bookmarks directly to \"$effectiveFallback\". Bookmarks will not be lost."
                } else {
                    "Are you sure you want to permanently delete the tag \"$tagToDelete\"?"
                }
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = appColors.secondaryLabel,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteCategory(tagToDelete)
                        pendingDeleteTag = null
                        Toast.makeText(context, "Deleted tag \"$tagToDelete\"", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete Tag", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTag = null }) {
                    Text("Cancel", color = appColors.secondaryLabel)
                }
            },
            containerColor = appColors.secondaryGroupedBackground,
            shape = RoundedCornerShape(22.dp)
        )
    }

    // Reset Defaults Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = "Reset Tags to Default?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appColors.label
                )
            },
            text = {
                Text(
                    text = "This will restore the default tags (Personal, Design, Tools, Work, Reading) and unpark all hidden tags.",
                    fontSize = 14.sp,
                    color = appColors.secondaryLabel,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.resetCategoriesToDefaults()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Tags reset to defaults", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = appColors.secondaryLabel)
                }
            },
            containerColor = appColors.secondaryGroupedBackground,
            shape = RoundedCornerShape(22.dp)
        )
    }
}
