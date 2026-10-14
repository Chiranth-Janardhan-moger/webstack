package com.example.ui.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookmarks.BookmarkCluster
import com.example.bookmarks.ClassifiedBookmark
import com.example.bookmarks.ImportPreprocessResult
import com.example.ui.dialogs.AddTagDialog
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.getCategoryAccentColor

@Composable
fun ClusterApprovalSheet(
    preprocessResult: ImportPreprocessResult,
    existingCategories: List<String>,
    importProgress: Pair<Int, Int>? = null,
    onConfirmImport: (List<ClassifiedBookmark>) -> Unit,
    onCancel: () -> Unit
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current

    // State: cluster included map (clusterId -> Boolean)
    val includedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            preprocessResult.clusters.forEach { put(it.id, true) }
        }
    }

    // State: individual bookmark excluded URLs map (url -> Boolean)
    val excludedUrls = remember {
        mutableStateMapOf<String, Boolean>()
    }

    // State: cluster category reassignment map (clusterId -> category)
    val categoryMap = remember {
        mutableStateMapOf<String, String>().apply {
            preprocessResult.clusters.forEach { put(it.id, it.assignedCategory) }
        }
    }

    // State: expanded clusters map (clusterId -> Boolean)
    val expandedClusters = remember {
        mutableStateMapOf<String, Boolean>()
    }

    var isOtherExpanded by remember { mutableStateOf(false) }
    var showNewTagDialogForCluster by remember { mutableStateOf<String?>(null) }

    val bookmarksByCategory = remember(preprocessResult.classifiedBookmarks) {
        preprocessResult.classifiedBookmarks.groupBy { it.category }
    }

    val topClusters = remember(preprocessResult.clusters) {
        preprocessResult.clusters.take(7)
    }
    val otherClusters = remember(preprocessResult.clusters) {
        preprocessResult.clusters.drop(7)
    }

    // Calculate total included links count
    val totalIncludedCount by remember {
        derivedStateOf {
            preprocessResult.classifiedBookmarks.count { b ->
                (includedMap[b.category] == true) && (excludedUrls[b.url] != true)
            }
        }
    }

    val allClustersSelected by remember {
        derivedStateOf {
            preprocessResult.clusters.isNotEmpty() && preprocessResult.clusters.all { includedMap[it.id] == true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("cluster_approval_sheet")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Review Import",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = appColors.label,
                    letterSpacing = (-0.4).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = buildString {
                    append("${preprocessResult.classifiedBookmarks.size} bookmarks in ${preprocessResult.clusters.size} categories")
                    if (preprocessResult.internalDuplicatesCount > 0) {
                        append(" • ${preprocessResult.internalDuplicatesCount} duplicates skipped")
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = appColors.secondaryLabel
                )
            }
            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCancel()
                },
                modifier = Modifier.testTag("cancel_import_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = appColors.secondaryLabel
                )
            }
        }

        if (!preprocessResult.hasNetscapeHeader) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFF3CD),
                border = BorderStroke(1.dp, Color(0xFFFFEEBA)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("netscape_header_warning_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Non-standard file format. May not be an official browser export.",
                        color = Color(0xFF856404),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categories section header + bulk toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CATEGORIES (${preprocessResult.clusters.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.secondaryLabel,
                letterSpacing = 1.2.sp
            )

            Surface(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val target = !allClustersSelected
                    preprocessResult.clusters.forEach { includedMap[it.id] = target }
                    if (target) {
                        excludedUrls.clear()
                    }
                },
                shape = RoundedCornerShape(6.dp),
                color = Color.Transparent
            ) {
                Text(
                    text = if (allClustersSelected) "Deselect All" else "Select All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.accent,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Render top 7 clusters
        topClusters.forEach { cluster ->
            val clusterBookmarks = bookmarksByCategory[cluster.id] ?: emptyList()
            val excludedInCluster = clusterBookmarks.count { excludedUrls[it.url] == true }
            val isExpanded = expandedClusters[cluster.id] == true

            ClusterRow(
                cluster = cluster,
                isIncluded = includedMap[cluster.id] ?: true,
                isExpanded = isExpanded,
                excludedCount = excludedInCluster,
                clusterBookmarks = clusterBookmarks,
                excludedUrls = excludedUrls,
                assignedCategory = categoryMap[cluster.id] ?: cluster.assignedCategory,
                availableCategories = existingCategories + categoryMap.values.distinct(),
                onToggleInclude = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val willInclude = !(includedMap[cluster.id] ?: true)
                    includedMap[cluster.id] = willInclude
                    if (willInclude) {
                        clusterBookmarks.forEach { excludedUrls.remove(it.url) }
                    }
                },
                onToggleExpand = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    expandedClusters[cluster.id] = !isExpanded
                },
                onToggleBookmark = { url ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (excludedUrls[url] == true) {
                        excludedUrls.remove(url)
                    } else {
                        excludedUrls[url] = true
                    }
                },
                onSelectCategory = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    categoryMap[cluster.id] = it
                },
                onRequestNewTag = { showNewTagDialogForCluster = cluster.id }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Fold the rest into expandable "Other" row
        if (otherClusters.isNotEmpty()) {
            Surface(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isOtherExpanded = !isOtherExpanded
                },
                shape = RoundedCornerShape(12.dp),
                color = appColors.surface,
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("other_clusters_row")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Other (${otherClusters.size} categories)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = appColors.label
                    )
                    Icon(
                        imageVector = if (isOtherExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle other categories",
                        tint = appColors.secondaryLabel
                    )
                }
            }

            AnimatedVisibility(visible = isOtherExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    otherClusters.forEach { cluster ->
                        val clusterBookmarks = bookmarksByCategory[cluster.id] ?: emptyList()
                        val excludedInCluster = clusterBookmarks.count { excludedUrls[it.url] == true }
                        val isExpanded = expandedClusters[cluster.id] == true

                        ClusterRow(
                            cluster = cluster,
                            isIncluded = includedMap[cluster.id] ?: true,
                            isExpanded = isExpanded,
                            excludedCount = excludedInCluster,
                            clusterBookmarks = clusterBookmarks,
                            excludedUrls = excludedUrls,
                            assignedCategory = categoryMap[cluster.id] ?: cluster.assignedCategory,
                            availableCategories = existingCategories + categoryMap.values.distinct(),
                            onToggleInclude = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val willInclude = !(includedMap[cluster.id] ?: true)
                                includedMap[cluster.id] = willInclude
                                if (willInclude) {
                                    clusterBookmarks.forEach { excludedUrls.remove(it.url) }
                                }
                            },
                            onToggleExpand = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                expandedClusters[cluster.id] = !isExpanded
                            },
                            onToggleBookmark = { url ->
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (excludedUrls[url] == true) {
                                    excludedUrls.remove(url)
                                } else {
                                    excludedUrls[url] = true
                                }
                            },
                            onSelectCategory = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                categoryMap[cluster.id] = it
                            },
                            onRequestNewTag = { showNewTagDialogForCluster = cluster.id }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (importProgress != null) {
            val (done, total) = importProgress
            val fraction = if (total > 0) (done.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .testTag("import_progress_indicator"),
                    color = appColors.label,
                    trackColor = appColors.separator
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Importing: $done / $total bookmarks (${(fraction * 100).toInt()}%)",
                    fontSize = 12.sp,
                    color = appColors.secondaryLabel,
                    modifier = Modifier.testTag("import_progress_text")
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCancel()
                },
                enabled = importProgress == null,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val finalBookmarks = preprocessResult.classifiedBookmarks.filter { b ->
                        (includedMap[b.category] == true) && (excludedUrls[b.url] != true)
                    }.map { b ->
                        val newCat = categoryMap[b.category] ?: b.category
                        b.copy(category = newCat)
                    }
                    onConfirmImport(finalBookmarks)
                },
                enabled = totalIncludedCount > 0 && importProgress == null,
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp)
                    .testTag("confirm_import_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.label,
                    contentColor = appColors.systemBackground
                )
            ) {
                Text(
                    text = "Import ($totalIncludedCount)",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // New Tag Dialog
    if (showNewTagDialogForCluster != null) {
        val clusterId = showNewTagDialogForCluster!!
        AddTagDialog(
            onAdd = { newTag ->
                val trimmed = newTag.trim()
                if (trimmed.isNotEmpty()) {
                    categoryMap[clusterId] = trimmed
                }
                showNewTagDialogForCluster = null
            },
            onDismiss = {
                showNewTagDialogForCluster = null
            }
        )
    }
}

@Composable
private fun ClusterRow(
    cluster: BookmarkCluster,
    isIncluded: Boolean,
    isExpanded: Boolean,
    excludedCount: Int,
    clusterBookmarks: List<ClassifiedBookmark>,
    excludedUrls: Map<String, Boolean>,
    assignedCategory: String,
    availableCategories: List<String>,
    onToggleInclude: () -> Unit,
    onToggleExpand: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onRequestNewTag: () -> Unit
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    var isDropdownOpen by remember { mutableStateOf(false) }
    val accentColor = remember(assignedCategory, appColors.isDark) {
        getCategoryAccentColor(assignedCategory, appColors.isDark)
    }

    val activeCount = (cluster.linkCount - excludedCount).coerceAtLeast(0)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = appColors.surface,
        border = BorderStroke(
            0.5.dp,
            if (isIncluded) appColors.separator else appColors.separator.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (isIncluded) 1f else 0.52f }
            .testTag("cluster_row_${cluster.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Checkbox + Title & Count + Expand Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isIncluded && activeCount > 0,
                    onCheckedChange = { onToggleInclude() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = appColors.label,
                        uncheckedColor = appColors.secondaryLabel
                    ),
                    modifier = Modifier.testTag("cluster_checkbox_${cluster.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = cluster.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appColors.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isIncluded && excludedCount > 0) "($activeCount/${cluster.linkCount})" else "(${cluster.linkCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isIncluded) appColors.secondaryLabel else appColors.tertiaryLabel
                    )
                }

                if (clusterBookmarks.isNotEmpty()) {
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("expand_cluster_${cluster.id}")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse links" else "Expand links",
                            tint = appColors.secondaryLabel,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Row 2: Metadata (Sample domains) + Category Tag Dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 38.dp, top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (cluster.sampleDomains.isNotEmpty()) {
                    Text(
                        text = cluster.sampleDomains.joinToString(", "),
                        fontSize = 11.sp,
                        color = appColors.tertiaryLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Dropdown menu button with category accent dot
                Box {
                    Surface(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isDropdownOpen = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                        modifier = Modifier.testTag("cluster_dropdown_${cluster.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(accentColor, CircleShape)
                            )
                            Text(
                                text = assignedCategory,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 110.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = appColors.secondaryLabel,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownOpen,
                        onDismissRequest = { isDropdownOpen = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (appColors.isDark) Color(0xFF1C1C1E) else Color.White,
                        tonalElevation = 0.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = if (appColors.isDark) Color(0xFF38383A) else Color(0xFFE5E5EA)
                        ),
                        modifier = Modifier
                            .widthIn(min = 180.dp)
                            .heightIn(max = 280.dp)
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = appColors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = "Tag",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = appColors.accent
                                )
                            },
                            onClick = {
                                isDropdownOpen = false
                                onRequestNewTag()
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = if (appColors.isDark) Color(0xFF38383A) else Color(0xFFE5E5EA)
                        )
                        val distinctCats = availableCategories.filter { it.isNotBlank() }.distinct()
                        distinctCats.forEach { cat ->
                            val isSelected = cat.equals(assignedCategory, ignoreCase = true)
                            val catAccent = getCategoryAccentColor(cat, appColors.isDark)
                            DropdownMenuItem(
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(catAccent, CircleShape)
                                    )
                                },
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
                                    onSelectCategory(cat)
                                    isDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }

            // Expanded Link Inspector
            AnimatedVisibility(visible = isExpanded && clusterBookmarks.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp, top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(
                        color = appColors.separator.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val visibleBookmarks = clusterBookmarks.take(40)
                    visibleBookmarks.forEach { bookmark ->
                        val isLinkIncluded = isIncluded && (excludedUrls[bookmark.url] != true)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isLinkIncluded,
                                onCheckedChange = { onToggleBookmark(bookmark.url) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = accentColor,
                                    uncheckedColor = appColors.secondaryLabel
                                ),
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("link_checkbox_${bookmark.dedupeKey}")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onToggleBookmark(bookmark.url) }
                                    )
                            ) {
                                Text(
                                    text = bookmark.title.ifBlank { bookmark.url },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isLinkIncluded) appColors.label else appColors.secondaryLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = bookmark.url,
                                    fontSize = 10.sp,
                                    color = appColors.tertiaryLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (clusterBookmarks.size > 40) {
                        Text(
                            text = "+ ${clusterBookmarks.size - 40} more bookmarks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors.secondaryLabel,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
