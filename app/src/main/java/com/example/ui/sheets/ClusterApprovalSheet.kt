package com.example.ui.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookmarks.BookmarkCluster
import com.example.bookmarks.CategorySource
import com.example.bookmarks.ClassifiedBookmark
import com.example.bookmarks.ImportPreprocessResult
import com.example.ui.theme.LocalAppColors

@Composable
fun ClusterApprovalSheet(
    preprocessResult: ImportPreprocessResult,
    existingCategories: List<String>,
    onConfirmImport: (List<ClassifiedBookmark>) -> Unit,
    onCancel: () -> Unit
) {
    val appColors = LocalAppColors.current

    // State: cluster included map (clusterId -> Boolean)
    val includedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            preprocessResult.clusters.forEach { put(it.id, true) }
        }
    }

    // State: cluster category reassignment map (clusterId -> category)
    val categoryMap = remember {
        mutableStateMapOf<String, String>().apply {
            preprocessResult.clusters.forEach { put(it.id, it.assignedCategory) }
        }
    }

    var isOtherExpanded by remember { mutableStateOf(false) }
    var showNewTagDialogForCluster by remember { mutableStateOf<String?>(null) }
    var newTagInput by remember { mutableStateOf("") }

    val topClusters = remember(preprocessResult.clusters) {
        preprocessResult.clusters.take(7)
    }
    val otherClusters = remember(preprocessResult.clusters) {
        preprocessResult.clusters.drop(7)
    }

    // Calculate total included links count
    val totalIncludedCount by remember {
        derivedStateOf {
            preprocessResult.clusters.sumOf { cluster ->
                if (includedMap[cluster.id] == true) cluster.linkCount else 0
            }
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
            Text(
                text = "Review Import",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = appColors.label
            )
            IconButton(onClick = onCancel, modifier = Modifier.testTag("cancel_import_btn")) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = appColors.secondaryLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Summaries
        Text(
            text = "${preprocessResult.internalDuplicatesCount} duplicates skipped",
            fontSize = 13.sp,
            color = appColors.secondaryLabel,
            modifier = Modifier.testTag("summary_duplicates_text")
        )
        Text(
            text = "${preprocessResult.unsupportedSchemeCount} unsupported links skipped (javascript:, chrome://, ...)",
            fontSize = 13.sp,
            color = appColors.secondaryLabel,
            modifier = Modifier.testTag("summary_unsupported_text")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CATEGORIES (${preprocessResult.clusters.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Render top 7 clusters
        topClusters.forEach { cluster ->
            ClusterRow(
                cluster = cluster,
                isIncluded = includedMap[cluster.id] ?: true,
                assignedCategory = categoryMap[cluster.id] ?: cluster.assignedCategory,
                availableCategories = existingCategories + categoryMap.values.distinct(),
                onToggleInclude = { includedMap[cluster.id] = !(includedMap[cluster.id] ?: true) },
                onSelectCategory = { categoryMap[cluster.id] = it },
                onRequestNewTag = { showNewTagDialogForCluster = cluster.id }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Fold the rest into expandable "Other" row
        if (otherClusters.isNotEmpty()) {
            val otherTotalLinks = otherClusters.sumOf { it.linkCount }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = appColors.surface,
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isOtherExpanded = !isOtherExpanded }
                    .testTag("other_clusters_row")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Other (${otherClusters.size} categories)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = appColors.label
                        )
                        Text(
                            text = "$otherTotalLinks links total",
                            fontSize = 12.sp,
                            color = appColors.secondaryLabel
                        )
                    }
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
                        ClusterRow(
                            cluster = cluster,
                            isIncluded = includedMap[cluster.id] ?: true,
                            assignedCategory = categoryMap[cluster.id] ?: cluster.assignedCategory,
                            availableCategories = existingCategories + categoryMap.values.distinct(),
                            onToggleInclude = { includedMap[cluster.id] = !(includedMap[cluster.id] ?: true) },
                            onSelectCategory = { categoryMap[cluster.id] = it },
                            onRequestNewTag = { showNewTagDialogForCluster = cluster.id }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    // Collect all included bookmarks with updated categories
                    val finalBookmarks = preprocessResult.classifiedBookmarks.filter { b ->
                        includedMap[b.category] == true
                    }.map { b ->
                        val newCat = categoryMap[b.category] ?: b.category
                        b.copy(category = newCat)
                    }
                    onConfirmImport(finalBookmarks)
                },
                enabled = totalIncludedCount > 0,
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
                    text = "Import $totalIncludedCount Bookmarks",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // New Tag Dialog
    if (showNewTagDialogForCluster != null) {
        val clusterId = showNewTagDialogForCluster!!
        AlertDialog(
            onDismissRequest = {
                showNewTagDialogForCluster = null
                newTagInput = ""
            },
            title = { Text("New Category") },
            text = {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    label = { Text("Category name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newTagInput.trim()
                        if (trimmed.isNotEmpty()) {
                            categoryMap[clusterId] = trimmed
                        }
                        showNewTagDialogForCluster = null
                        newTagInput = ""
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNewTagDialogForCluster = null
                        newTagInput = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ClusterRow(
    cluster: BookmarkCluster,
    isIncluded: Boolean,
    assignedCategory: String,
    availableCategories: List<String>,
    onToggleInclude: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onRequestNewTag: () -> Unit
) {
    val appColors = LocalAppColors.current
    var isDropdownOpen by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = appColors.surface,
        border = BorderStroke(0.75.dp, appColors.separator),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cluster_row_${cluster.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isIncluded,
                onCheckedChange = { onToggleInclude() },
                colors = CheckboxDefaults.colors(checkedColor = appColors.label),
                modifier = Modifier.testTag("cluster_checkbox_${cluster.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cluster.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appColors.label
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${cluster.linkCount})",
                        fontSize = 12.sp,
                        color = appColors.secondaryLabel
                    )
                }
                if (cluster.sampleDomains.isNotEmpty()) {
                    Text(
                        text = cluster.sampleDomains.joinToString(", "),
                        fontSize = 11.sp,
                        color = appColors.tertiaryLabel,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Dropdown menu button
            Box {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                    modifier = Modifier
                        .clickable { isDropdownOpen = true }
                        .testTag("cluster_dropdown_${cluster.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = assignedCategory,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors.label
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
                    onDismissRequest = { isDropdownOpen = false }
                ) {
                    val distinctCats = availableCategories.filter { it.isNotBlank() }.distinct()
                    distinctCats.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                onSelectCategory(cat)
                                isDropdownOpen = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("+ New tag...") },
                        onClick = {
                            isDropdownOpen = false
                            onRequestNewTag()
                        }
                    )
                }
            }
        }
    }
}
