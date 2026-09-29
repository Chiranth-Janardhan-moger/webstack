package com.example.ui.cards

import android.graphics.Bitmap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.Website
import com.example.ui.theme.LocalAppleColors
import com.example.ui.util.getCategoryAccentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun WebsiteSnapshotImage(
    website: Website,
    refreshToken: Long,
    fetchWebPreviews: Boolean,
    height: Dp,
    topCornerRadius: Dp,
    onRefreshScreenshot: () -> Unit,
    isGrid: Boolean = false,
    isCompact: Boolean = false,
    showRefreshButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appleColors = LocalAppleColors.current
    val coroutineScope = rememberCoroutineScope()
    val localFile = remember(website.id, refreshToken) { File(context.filesDir, "screenshot_${website.id}.jpg") }
    var hasLocalImage by remember(website.id, refreshToken) { mutableStateOf(localFile.exists()) }

    val cornerShape = if (isCompact) RoundedCornerShape(topCornerRadius) else RoundedCornerShape(topStart = topCornerRadius, topEnd = topCornerRadius)

    Box(
        modifier = modifier
            .then(if (isCompact) Modifier.size(width = 76.dp, height = height) else Modifier.fillMaxWidth().height(height))
            .clip(cornerShape)
            .background(appleColors.secondaryBackground)
    ) {
        if (hasLocalImage) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(localFile)
                    .crossfade(true)
                    .build(),
                contentDescription = "Preview snapshot of ${website.title}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cornerShape),
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
                model = ImageRequest.Builder(context)
                    .data(previewUrl)
                    .crossfade(true)
                    .build(),
                onSuccess = { state ->
                    val drawable = state.result.drawable
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val bitmap = drawable.toBitmap()
                            FileOutputStream(localFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
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
                    .clip(cornerShape),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(if (isCompact) 16.dp else if (isGrid) 20.dp else 24.dp),
                            color = appleColors.accent,
                            strokeWidth = if (isCompact || isGrid) 2.dp else 2.5.dp
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
                        if (isGrid || isCompact) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = appleColors.secondaryLabel,
                                modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                            )
                        } else {
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
                }
            )
        } else {
            // Local Apple Card representation when Fetch Web Previews is OFF
            val catAccent = getCategoryAccentColor(website.category, appleColors.isDark)
            val domainInitial = website.domain.trimStart().removePrefix("www.").firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "W"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cornerShape)
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
                if (isGrid || isCompact) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = catAccent.copy(alpha = if (appleColors.isDark) 0.25f else 0.15f),
                        border = BorderStroke(0.75.dp, catAccent.copy(alpha = 0.40f)),
                        modifier = Modifier.size(if (isCompact) 32.dp else 38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = domainInitial,
                                fontSize = if (isCompact) 16.sp else 18.sp,
                                fontWeight = FontWeight.Black,
                                color = catAccent
                            )
                        }
                    }
                } else {
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
        }

        // Top-Right Action: Refresh Snapshot
        if (showRefreshButton && (fetchWebPreviews || hasLocalImage)) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .then(if (!isGrid) Modifier.size(44.dp) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = onRefreshScreenshot,
                    color = (if (appleColors.isDark) Color(0xCC1C1C1E) else Color(0xEBFFFFFF)),
                    shape = CircleShape,
                    border = BorderStroke(0.5.dp, if (!isGrid) appleColors.separator else appleColors.glassHighlight),
                    shadowElevation = if (!isGrid) 3.dp else 2.dp,
                    modifier = Modifier.size(if (!isGrid) 32.dp else 28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh Screenshot",
                            tint = appleColors.label,
                            modifier = Modifier.size(if (!isGrid) 15.dp else 13.dp)
                        )
                    }
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
    val appleColors = LocalAppleColors.current

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
            WebsiteSnapshotImage(
                website = website,
                refreshToken = refreshToken,
                fetchWebPreviews = fetchWebPreviews,
                height = 185.dp,
                topCornerRadius = 22.dp,
                onRefreshScreenshot = onRefreshScreenshot
            )

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
    val appleColors = LocalAppleColors.current

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
            WebsiteSnapshotImage(
                website = website,
                refreshToken = refreshToken,
                fetchWebPreviews = fetchWebPreviews,
                height = 105.dp,
                topCornerRadius = 18.dp,
                isGrid = true,
                onRefreshScreenshot = onRefreshScreenshot
            )

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
    val appleColors = LocalAppleColors.current
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
            WebsiteSnapshotImage(
                website = website,
                refreshToken = refreshToken,
                fetchWebPreviews = fetchWebPreviews,
                height = 52.dp,
                topCornerRadius = 10.dp,
                isCompact = true,
                showRefreshButton = false,
                onRefreshScreenshot = onRefreshScreenshot
            )

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
