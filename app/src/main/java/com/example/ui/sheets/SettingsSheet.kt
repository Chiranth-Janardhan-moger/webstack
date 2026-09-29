package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.model.WebStackLayoutMode
import com.example.ui.theme.LocalAppleColors

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
                            painter = painterResource(id = R.drawable.ic_nav_more),
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
                                painter = painterResource(id = R.drawable.ic_logo),
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
