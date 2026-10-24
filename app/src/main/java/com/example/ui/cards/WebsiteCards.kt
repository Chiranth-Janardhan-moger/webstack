package com.example.ui.cards

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.Website
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.getCategoryAccentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
@Composable
fun TiltedShimmerBox(
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val shimmerColors = remember(appColors.isDark) {
        val base = if (appColors.isDark) Color(0xFF222224) else Color(0xFFE5E5EA)
        val highlight = if (appColors.isDark) Color(0xFF38383C) else Color(0xFFF4F4F6)
        listOf(base, highlight, base)
    }

    val transition = rememberInfiniteTransition(label = "tilted_shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    Spacer(
        modifier = modifier.drawBehind {
            val width = size.width
            val height = size.height

            val startX = progress * width
            val startY = progress * height
            val endX = startX + width * 0.75f
            val endY = startY + height * 0.75f

            val brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            )

            drawRect(brush = brush)
        }
    )
}

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
    hasCachedImage: Boolean = false,
    isFavorite: Boolean = false,
    onImageSaved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val coroutineScope = rememberCoroutineScope()
    val localFile = remember(website.id, refreshToken) { File(context.filesDir, "screenshot_${website.id}.jpg") }
    var hasLocalImage by remember(website.id, refreshToken, hasCachedImage) { mutableStateOf(hasCachedImage) }

    val cornerShape = if (isCompact) RoundedCornerShape(topCornerRadius) else RoundedCornerShape(topStart = topCornerRadius, topEnd = topCornerRadius)

    Box(
        modifier = modifier
            .then(if (isCompact) Modifier.size(width = 76.dp, height = height) else Modifier.fillMaxWidth().height(height))
            .clip(cornerShape)
            .background(appColors.secondaryBackground)
    ) {
        if (hasLocalImage) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(localFile)
                    .crossfade(false)
                    .build(),
                contentDescription = "Preview snapshot of ${website.title}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cornerShape),
                contentScale = ContentScale.Crop
            )
        } else if (fetchWebPreviews) {
            var activePreviewUrl by remember(website.url, refreshToken) {
                val initialUrl = try {
                    val encodedUrl = java.net.URLEncoder.encode(website.url, "UTF-8")
                    val ts = if (refreshToken > 0) "&t=$refreshToken" else ""
                    "https://api.microlink.io/?url=$encodedUrl&screenshot=true&embed=screenshot.url$ts"
                } catch (e: Exception) {
                    website.url
                }
                mutableStateOf(initialUrl)
            }

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(activePreviewUrl)
                    .crossfade(true)
                    .build(),
                onError = {
                    if (activePreviewUrl.startsWith("https://api.microlink.io")) {
                        val fullUrl = if (website.url.startsWith("http://") || website.url.startsWith("https://")) {
                            website.url
                        } else {
                            "https://${website.url}"
                        }
                        val target = if (refreshToken > 0) {
                            val sep = if (fullUrl.contains("?")) "&" else "?"
                            "$fullUrl${sep}t=$refreshToken"
                        } else {
                            fullUrl
                        }
                        activePreviewUrl = "https://image.thum.io/get/noanimate/width/800/crop/600/$target"
                    }
                },
                onSuccess = { state ->
                    val drawable = state.result.drawable
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val bitmap = drawable.toBitmap()
                            FileOutputStream(localFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }
                            withContext(Dispatchers.Main) {
                                hasLocalImage = true
                                onImageSaved()
                            }
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
                    TiltedShimmerBox(modifier = Modifier.fillMaxSize())
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(appColors.fill),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGrid || isCompact) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = appColors.secondaryLabel,
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
                                    tint = appColors.secondaryLabel,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = website.domain,
                                    fontSize = 14.sp,
                                    color = appColors.secondaryLabel,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                )
                            }
                        }
                    }
                }
            )
        } else {
            // Card representation when Fetch Web Previews is OFF
            val catAccent = getCategoryAccentColor(website.category, appColors.isDark)
            val domainInitial = website.domain.trimStart().removePrefix("www.").firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "W"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cornerShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                catAccent.copy(alpha = if (appColors.isDark) 0.16f else 0.08f),
                                appColors.secondaryBackground
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isGrid || isCompact) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = catAccent.copy(alpha = if (appColors.isDark) 0.25f else 0.15f),
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
                            color = catAccent.copy(alpha = if (appColors.isDark) 0.25f else 0.15f),
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
                            color = appColors.secondaryLabel,
                            letterSpacing = (-0.2).sp
                        )
                    }
                }
            }
        }

        // Top-Left Floating Favorite Heart Indicator (pure vector icon, zero box shadow)
        if (isFavorite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorited",
                tint = Color(0xFFFF2D55),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        top = if (isCompact) 5.dp else if (isGrid) 8.dp else 10.dp,
                        start = if (isCompact) 5.dp else if (isGrid) 8.dp else 10.dp
                    )
                    .size(if (isCompact) 13.dp else if (isGrid) 16.dp else 20.dp)
            )
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
                    color = (if (appColors.isDark) Color(0xCC1C1C1E) else Color(0xEBFFFFFF)),
                    shape = CircleShape,
                    border = BorderStroke(0.5.dp, if (!isGrid) appColors.separator else appColors.glassHighlight),
                    shadowElevation = if (!isGrid) 3.dp else 2.dp,
                    modifier = Modifier.size(if (!isGrid) 32.dp else 28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh Screenshot",
                            tint = appColors.label,
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
private fun CardContainer(
    cardShape: RoundedCornerShape,
    pressedScale: Float = 0.985f,
    testTag: String,
    selectionIndex: Int? = null,
    isInSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    border: BorderStroke? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val appColors = LocalAppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val isSelected = selectionIndex != null
    var lastValidSelectionIndex by remember { mutableStateOf(selectionIndex ?: 1) }
    if (selectionIndex != null) {
        lastValidSelectionIndex = selectionIndex
    }

    val targetScale = when {
        isPressed -> 0.94f
        isSelected -> 0.96f
        else -> 1f
    }

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_scale"
    )

    Card(
        modifier = modifier
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
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isSelected) BorderStroke(1.dp, Color(0xFF007AFF).copy(alpha = 0.55f)) else border
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()

            // Subtle Apple-style scrim when selected (keeps snapshot & typography completely clear)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(if (appColors.isDark) Color(0x22000000) else Color(0x0C000000))
                )
            }

            // Apple iOS Corner Selection Indicator (Clean - only shows badge when selected)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 9.dp, end = 9.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isInSelectionMode && isSelected,
                    enter = androidx.compose.animation.scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF007AFF), // Apple System Blue
                        shadowElevation = 3.dp,
                        border = BorderStroke(1.75.dp, Color.White),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = (selectionIndex ?: lastValidSelectionIndex).toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = (-0.3).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WebsiteCard(
    website: Website,
    refreshToken: Long,
    fetchWebPreviews: Boolean = false,
    showDomain: Boolean = true,
    showCategory: Boolean = true,
    hasCachedImage: Boolean = false,
    isFavorite: Boolean = false,
    onImageSaved: () -> Unit = {},
    selectionIndex: Int? = null,
    isInSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRefreshScreenshot: () -> Unit
) {
    val appColors = LocalAppColors.current
    val cardShape = RoundedCornerShape(22.dp)
    CardContainer(
        cardShape = cardShape,
        pressedScale = 0.985f,
        border = BorderStroke(0.5.dp, appColors.separator),
        testTag = "website_card_${website.id}",
        selectionIndex = selectionIndex,
        isInSelectionMode = isInSelectionMode,
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Preview Slot displaying Website Screenshot
            WebsiteSnapshotImage(
                website = website,
                refreshToken = refreshToken,
                fetchWebPreviews = fetchWebPreviews,
                height = 185.dp,
                topCornerRadius = 22.dp,
                showRefreshButton = !isInSelectionMode,
                hasCachedImage = hasCachedImage,
                isFavorite = isFavorite,
                onImageSaved = onImageSaved,
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
                    if (website.title.isBlank()) {
                        TiltedShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    } else {
                        Text(
                            text = website.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = appColors.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = (-0.2).sp
                        )
                    }
                    if (showDomain) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = website.url,
                            fontSize = 12.sp,
                            color = appColors.secondaryLabel,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (showCategory) {
                    Spacer(modifier = Modifier.width(12.dp))

                    // Semantic Category Badge
                    val catAccent = getCategoryAccentColor(website.category, appColors.isDark)
                    Surface(
                        color = catAccent.copy(alpha = if (appColors.isDark) 0.2f else 0.12f),
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridWebsiteCard(
    website: Website,
    refreshToken: Long,
    fetchWebPreviews: Boolean = false,
    showDomain: Boolean = true,
    showCategory: Boolean = true,
    hasCachedImage: Boolean = false,
    isFavorite: Boolean = false,
    onImageSaved: () -> Unit = {},
    selectionIndex: Int? = null,
    isInSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRefreshScreenshot: () -> Unit
) {
    val appColors = LocalAppColors.current
    val cardShape = RoundedCornerShape(18.dp)
    CardContainer(
        cardShape = cardShape,
        pressedScale = 0.96f,
        border = BorderStroke(0.5.dp, appColors.separator),
        testTag = "website_grid_card_${website.id}",
        selectionIndex = selectionIndex,
        isInSelectionMode = isInSelectionMode,
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Snapshot Preview Header (105dp)
            WebsiteSnapshotImage(
                website = website,
                refreshToken = refreshToken,
                fetchWebPreviews = fetchWebPreviews,
                height = 105.dp,
                topCornerRadius = 18.dp,
                isGrid = true,
                showRefreshButton = !isInSelectionMode,
                hasCachedImage = hasCachedImage,
                isFavorite = isFavorite,
                onImageSaved = onImageSaved,
                onRefreshScreenshot = onRefreshScreenshot
            )

            // Info Details (Title & Domain with Category dot)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                if (website.title.isBlank()) {
                    TiltedShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(13.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                } else {
                    Text(
                        text = website.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = appColors.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showDomain || showCategory) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (showCategory) {
                            val catAccent = getCategoryAccentColor(website.category, appColors.isDark)
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(catAccent, CircleShape)
                            )
                        }
                        if (showDomain) {
                            Text(
                                text = website.domain,
                                fontSize = 11.sp,
                                color = appColors.secondaryLabel,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactWebsiteRow(
    website: Website,
    refreshToken: Long,
    fetchWebPreviews: Boolean = false,
    showDomain: Boolean = true,
    showCategory: Boolean = true,
    hasCachedImage: Boolean = false,
    isFavorite: Boolean = false,
    onImageSaved: () -> Unit = {},
    selectionIndex: Int? = null,
    isInSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRefreshScreenshot: () -> Unit
) {
    val appColors = LocalAppColors.current
    CardContainer(
        cardShape = RoundedCornerShape(16.dp),
        pressedScale = 0.985f,
        border = BorderStroke(0.5.dp, appColors.separator),
        testTag = "compact_website_row_${website.id}",
        selectionIndex = selectionIndex,
        isInSelectionMode = isInSelectionMode,
        onClick = onClick,
        onLongClick = onLongClick
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
                hasCachedImage = hasCachedImage,
                isFavorite = isFavorite,
                onImageSaved = onImageSaved,
                onRefreshScreenshot = onRefreshScreenshot
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (website.title.isBlank()) {
                    TiltedShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                } else {
                    Text(
                        text = website.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appColors.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showDomain || showCategory) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showDomain) {
                            Text(
                                text = website.domain,
                                modifier = Modifier.weight(1f, fill = false),
                                fontSize = 12.sp,
                                color = appColors.secondaryLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (showDomain && showCategory) {
                            Text(
                                text = "•",
                                fontSize = 10.sp,
                                color = appColors.tertiaryLabel
                            )
                        }
                        if (showCategory) {
                            val catAccent = getCategoryAccentColor(website.category, appColors.isDark)
                            Text(
                                text = website.category,
                                fontSize = 11.sp,
                                color = catAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (!isInSelectionMode) {
                // Quick Refresh Button with 44dp Touch Target
                IconButton(
                    onClick = onRefreshScreenshot,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh Screenshot",
                        tint = appColors.secondaryLabel,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(28.dp))
            }
        }
    }
}
