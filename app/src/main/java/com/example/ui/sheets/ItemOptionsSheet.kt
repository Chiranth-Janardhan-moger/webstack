package com.example.ui.sheets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Website
import com.example.ui.theme.LocalAppleColors
import com.example.ui.util.formatMiddleTruncatedDomain
import com.example.ui.util.getCategoryAccentColor

@Composable
private fun AppleActionTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    testTag: String = ""
) {
    val appleColors = LocalAppleColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "action_tile_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = if (isDestructive) {
            appleColors.destructive.copy(alpha = if (appleColors.isDark) 0.16f else 0.08f)
        } else {
            if (appleColors.isDark) Color(0x1CFFFFFF) else appleColors.surface
        },
        border = BorderStroke(
            0.5.dp,
            if (isDestructive) {
                appleColors.destructive.copy(alpha = if (appleColors.isDark) 0.35f else 0.22f)
            } else {
                if (appleColors.isDark) Color(0x22FFFFFF) else appleColors.separator.copy(alpha = 0.6f)
            }
        ),
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) appleColors.destructive else appleColors.label,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp,
                color = if (isDestructive) appleColors.destructive else appleColors.label,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun AppleItemOptionsBottomSheetContent(
    website: Website,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onRefresh: () -> Unit = {},
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
                            text = formatMiddleTruncatedDomain(website.domain),
                            fontSize = 12.sp,
                            color = appleColors.secondaryLabel,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
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
                                maxLines = 1,
                                softWrap = false,
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

        // 2x2 Apple Action Grid (Open, Edit, Share, Delete)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Open & Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppleActionTile(
                    label = "Open",
                    icon = Icons.AutoMirrored.Outlined.OpenInNew,
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    testTag = "action_open_website"
                )
                AppleActionTile(
                    label = "Edit",
                    icon = Icons.Outlined.Edit,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    testTag = "action_edit_website"
                )
            }

            // Row 2: Share & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppleActionTile(
                    label = "Share",
                    icon = Icons.Outlined.Share,
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    testTag = "action_share_website"
                )
                AppleActionTile(
                    label = "Delete",
                    icon = Icons.Outlined.Delete,
                    onClick = onDelete,
                    isDestructive = true,
                    modifier = Modifier.weight(1f),
                    testTag = "action_delete_website"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
