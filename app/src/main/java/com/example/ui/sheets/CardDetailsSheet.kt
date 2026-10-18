package com.example.ui.sheets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.components.AppSwitch
import com.example.ui.model.BookmarkSortOrder
import com.example.ui.theme.LocalAppColors

@Composable
fun CardDetailsSubSheetContent(
    showCardDomain: Boolean,
    onToggleShowCardDomain: (Boolean) -> Unit,
    showCardCategory: Boolean,
    onToggleShowCardCategory: (Boolean) -> Unit,
    sortOrder: BookmarkSortOrder = BookmarkSortOrder.NEWEST,
    onSetSortOrder: (BookmarkSortOrder) -> Unit = {},
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppSubSheetHeader(
            title = "Card Details",
            onBack = onBack,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section: SORT ORDER
        Text(
            text = "SORT ORDER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Segmented Slider Pill (Black thumb on white theme, White thumb on black theme)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (appColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(3.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val segmentWidth = maxWidth / 2
                val selectedIndex = if (sortOrder == BookmarkSortOrder.NEWEST) 0 else 1
                val targetOffset = segmentWidth * selectedIndex
                val animatedOffset by animateDpAsState(
                    targetValue = targetOffset,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "sort_order_segment_slider"
                )

                val thumbColor by animateColorAsState(
                    targetValue = appColors.label,
                    animationSpec = tween(durationMillis = 200),
                    label = "sort_order_thumb_color"
                )

                // Animated sliding thumb: Black in light mode, White in dark mode
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

                // Interactive touch segments (Newest | Oldest)
                Row(modifier = Modifier.fillMaxSize()) {
                    val orders = listOf(BookmarkSortOrder.NEWEST, BookmarkSortOrder.OLDEST)
                    orders.forEach { order ->
                        val isSelected = sortOrder == order
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) appColors.systemBackground else appColors.secondaryLabel,
                            animationSpec = tween(durationMillis = 200),
                            label = "sort_order_text_${order.name}"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (sortOrder != order) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSetSortOrder(order)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = order.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Explainer Subtitle beneath the slider
        Text(
            text = sortOrder.subtitle,
            fontSize = 12.sp,
            color = appColors.secondaryLabel,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: VISIBILITY
        Text(
            text = "VISIBILITY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color = appColors.secondaryGroupedBackground,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Toggle: Hide Website Domain
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hide Domain",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                        Text(
                            text = "Hides domain or URL beneath titles",
                            fontSize = 12.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                    AppSwitch(
                        checked = !showCardDomain,
                        onCheckedChange = { isHiding ->
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleShowCardDomain(!isHiding)
                        }
                    )
                }

                HorizontalDivider(color = appColors.separator, thickness = 0.5.dp)

                // Toggle: Hide Category Tags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hide Tags",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                        Text(
                            text = "Hides category tag badges on cards",
                            fontSize = 12.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                    AppSwitch(
                        checked = !showCardCategory,
                        onCheckedChange = { isHiding ->
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleShowCardCategory(!isHiding)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
