package com.example.ui.sheets

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.theme.LocalAppColors

private enum class GuideCategory(val label: String) {
    ALL("All"),
    SYNC("Sync & PC"),
    CUSTOMIZATION("Customization"),
    SHORTCUTS("Shortcuts & Gestures"),
    DATA("Data & Backup")
}

private data class FeatureGuideItem(
    val id: String,
    val title: String,
    val tagline: String,
    val icon: ImageVector,
    val category: GuideCategory,
    val breadcrumbs: List<String>,
    val tip: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WebStackFeaturesSubSheetContent(
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    BackHandler {
        onBack()
    }

    var selectedCategory by remember { mutableStateOf(GuideCategory.ALL) }
    var expandedItemId by remember { mutableStateOf<String?>(null) }

    val features = remember {
        listOf(
            FeatureGuideItem(
                id = "pc_companion",
                title = "PC Companion & Wi-Fi Sharing",
                tagline = "Browse and push links from your computer browser",
                icon = Icons.Outlined.DesktopWindows,
                category = GuideCategory.SYNC,
                breadcrumbs = listOf("Settings", "PC Companion", "Turn On Wi-Fi Sharing"),
                tip = "Connect both phone and computer to the same local Wi-Fi router."
            ),
            FeatureGuideItem(
                id = "layout_modes",
                title = "Three Layout Modes",
                tagline = "Switch between Card, Grid, and List views",
                icon = Icons.Outlined.Layers,
                category = GuideCategory.CUSTOMIZATION,
                breadcrumbs = listOf("Settings", "Appearance", "Layout"),
                tip = "Layout changes apply immediately across all category tabs on Home."
            ),
            FeatureGuideItem(
                id = "button_shape",
                title = "Action Button Shapes",
                tagline = "Choose from 12 expressive button shapes",
                icon = Icons.Default.Add,
                category = GuideCategory.CUSTOMIZATION,
                breadcrumbs = listOf("Settings", "Appearance", "Interface", "Button Shape"),
                tip = "Preview button styles in real time before selecting."
            ),
            FeatureGuideItem(
                id = "rearrange_tags",
                title = "Rearrange & Hide Tags",
                tagline = "Drag to reorder home tags or hide unused categories",
                icon = Icons.AutoMirrored.Outlined.Label,
                category = GuideCategory.CUSTOMIZATION,
                breadcrumbs = listOf("Settings", "Appearance", "Interface", "Tags & Categories"),
                tip = "Hidden tags keep all bookmarks intact without deleting them."
            ),
            FeatureGuideItem(
                id = "card_details",
                title = "Card Details & Visibility",
                tagline = "Hide domain URLs or category tag badges",
                icon = Icons.Outlined.DashboardCustomize,
                category = GuideCategory.CUSTOMIZATION,
                breadcrumbs = listOf("Settings", "Appearance", "Interface", "Card Details"),
                tip = "Hiding domains provides a minimalist, compact card look."
            ),
            FeatureGuideItem(
                id = "fallback_category",
                title = "Fallback Tag for Deleted Tags",
                tagline = "Safely redirect bookmarks when tags are removed",
                icon = Icons.Outlined.Tune,
                category = GuideCategory.CUSTOMIZATION,
                breadcrumbs = listOf("Settings", "Appearance", "Interface", "Tags & Categories", "Fallback Tag"),
                tip = "Prevents orphaned bookmarks when a custom tag is removed."
            ),
            FeatureGuideItem(
                id = "multi_select",
                title = "Multi-Select & Bulk Delete",
                tagline = "Select multiple bookmarks to delete in one tap",
                icon = Icons.Outlined.Checklist,
                category = GuideCategory.SHORTCUTS,
                breadcrumbs = listOf("Home Screen", "Long-press any bookmark"),
                tip = "Long-press any bookmark card on Home to trigger selection mode."
            ),
            FeatureGuideItem(
                id = "quick_paste",
                title = "Quick Clipboard Detection",
                tagline = "Save copied links with a single tap on launch",
                icon = Icons.Default.ContentPaste,
                category = GuideCategory.SHORTCUTS,
                breadcrumbs = listOf("Copy link anywhere", "Open WebStack"),
                tip = "WebStack detects copied URLs and shows an instant add prompt."
            ),
            FeatureGuideItem(
                id = "share_sheet",
                title = "Save Links from Any Browser",
                tagline = "Save bookmarks directly via Android Share Sheet",
                icon = Icons.Outlined.Share,
                category = GuideCategory.SHORTCUTS,
                breadcrumbs = listOf("Browser Menu", "Share", "WebStack"),
                tip = "Works with Chrome, Firefox, Brave, and other Android browsers."
            ),
            FeatureGuideItem(
                id = "backup_restore",
                title = "Standard Backup & HTML Import",
                tagline = "Import bookmarks from Chrome, Firefox, or Brave",
                icon = Icons.Outlined.Restore,
                category = GuideCategory.DATA,
                breadcrumbs = listOf("Settings", "Backup and restore"),
                tip = "Export HTML bookmarks from your browser to migrate your stack in seconds."
            )
        )
    }

    val filteredFeatures = remember(selectedCategory) {
        if (selectedCategory == GuideCategory.ALL) features
        else features.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppSubSheetHeader(
            title = "Features & Guide",
            onBack = onBack,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Category Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(GuideCategory.values()) { category ->
                val isSelected = selectedCategory == category
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) appColors.label else appColors.fill,
                    border = BorderStroke(
                        0.5.dp,
                        if (isSelected) appColors.label else appColors.separator
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedCategory = category
                    }
                ) {
                    Text(
                        text = category.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) appColors.systemBackground else appColors.label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feature cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filteredFeatures.forEach { feature ->
                val isExpanded = expandedItemId == feature.id
                val chevronRotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = spring(stiffness = 300f),
                    label = "chevron_rotation"
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = appColors.surface,
                    border = BorderStroke(
                        width = if (isExpanded) 1.dp else 0.75.dp,
                        color = if (isExpanded) appColors.label.copy(alpha = 0.35f) else appColors.separator
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header row (clickable)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    expandedItemId = if (isExpanded) null else feature.id
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = if (isExpanded) appColors.label else appColors.fill,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = feature.icon,
                                            contentDescription = null,
                                            tint = if (isExpanded) appColors.systemBackground else appColors.label,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = feature.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = appColors.label
                                    )
                                    Text(
                                        text = feature.tagline,
                                        fontSize = 12.sp,
                                        color = appColors.secondaryLabel
                                    )
                                    Text(
                                        text = feature.breadcrumbs.joinToString("  ›  "),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = appColors.tertiaryLabel,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = appColors.secondaryLabel,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(chevronRotation)
                            )
                        }

                        // Expanded content
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            ) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = appColors.separator
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "HOW TO ACCESS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.secondaryLabel,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    feature.breadcrumbs.forEachIndexed { index, crumb ->
                                        Surface(
                                            color = appColors.secondaryGroupedBackground,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(0.5.dp, appColors.separator)
                                        ) {
                                            Text(
                                                text = crumb,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = appColors.label,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }

                                        if (index < feature.breadcrumbs.size - 1) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = appColors.tertiaryLabel,
                                                modifier = Modifier
                                                    .size(11.dp)
                                                    .align(Alignment.CenterVertically)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = appColors.fill,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = appColors.secondaryLabel,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = feature.tip,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            color = appColors.secondaryLabel
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
