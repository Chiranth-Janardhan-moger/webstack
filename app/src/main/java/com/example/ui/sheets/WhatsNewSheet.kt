package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalAppColors

@Composable
fun WhatsNewBottomSheetContent(
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header: Logo + "What's New" + v2.0.0 Pill + Close Button
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
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(appColors.label)
                )
                Text(
                    text = "What's New",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = appColors.label,
                    letterSpacing = (-0.4).sp
                )
                Surface(
                    color = appColors.fill,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, appColors.separator)
                ) {
                    Text(
                        text = "v2.0.0",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.secondaryLabel,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appColors.secondaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // VERSION 2.0.0 (FLAGSHIP)
        // ==========================================
        VersionSectionHeader(version = "v2.0.0")
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = appColors.surface,
            border = BorderStroke(1.dp, if (appColors.isDark) Color.White else Color.Black),
            shadowElevation = if (appColors.isDark) 0.dp else 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                WhatsNewRichRow(
                    icon = Icons.Outlined.DesktopWindows,
                    title = "PC Companion Mode",
                    description = "Browse and push links between phone and computer over local Wi-Fi."
                )
                WhatsNewRichRow(
                    icon = Icons.Outlined.DesktopWindows,
                    title = "Dedicated Desktop Client",
                    description = "Lightweight native Windows & Linux client with quick keyboard navigation."
                )
                WhatsNewRichRow(
                    icon = Icons.Outlined.Lock,
                    title = "6-Digit PIN & Permanent Security",
                    description = "Secure access with option to keep a persistent PIN across reconnects."
                )
                WhatsNewRichRow(
                    icon = Icons.Outlined.Language,
                    title = "Zero-Install Browser Mode",
                    description = "Open directly in Chrome, Edge, or Brave via local URL without extra setup."
                )
                WhatsNewRichRow(
                    icon = Icons.Outlined.OfflineBolt,
                    title = "Offline PC Vault Access",
                    description = "Search cached bookmarks on PC even when phone is turned off or disconnected."
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // VERSION 1.2.0
        // ==========================================
        VersionSectionHeader(version = "v1.2.0")
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.5.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChangelogBulletRow(
                    title = "Multi-Select Mode",
                    description = "Long-press any card to select multiple bookmarks and delete in bulk."
                )
                ChangelogBulletRow(
                    title = "12 Action Button Shapes",
                    description = "Personalize floating button with shapes like Ghost-ish, Clover, Arch, and Cookie."
                )
                ChangelogBulletRow(
                    title = "Visual Tag Reordering",
                    description = "Drag and drop active and parked tags with visual insertion markers."
                )
                ChangelogBulletRow(
                    title = "Redesigned Import Review",
                    description = "Expandable bookmark inspection with batch category assignment."
                )
                ChangelogBulletRow(
                    title = "Grouped Settings",
                    description = "Clean card layouts with hairline dividers across Settings and App Info."
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // VERSION 1.1.2
        // ==========================================
        VersionSectionHeader(version = "v1.1.2")
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.5.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChangelogBulletRow(
                    title = "Adaptive Foldable & Tablet Canvas",
                    description = "Dynamic multi-column grid tailored for foldables, flips, and large screens."
                )
                ChangelogBulletRow(
                    title = "6-Tile Action Grid",
                    description = "Inset options sheet with tactile haptics and long-press explainers."
                )
                ChangelogBulletRow(
                    title = "Unified Spring Physics",
                    description = "Fluid sheet animations and haptic feedback across settings."
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // VERSION 1.1.1
        // ==========================================
        VersionSectionHeader(version = "v1.1.1")
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.5.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChangelogBulletRow(
                    title = "Universal HTML Import & Export",
                    description = "Full support for bookmarks from Chrome, Brave, Firefox, Edge, and Safari."
                )
                ChangelogBulletRow(
                    title = "Instant Optimistic Save",
                    description = "Bookmarks persist immediately into Room database with zero UI delay."
                )
                ChangelogBulletRow(
                    title = "Typographic Title Skeleton",
                    description = "Smooth shimmer placeholder while page titles resolve."
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // VERSION 1.1.0
        // ==========================================
        VersionSectionHeader(version = "v1.1.0")
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.5.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChangelogBulletRow(
                    title = "Instant Startup Rendering",
                    description = "Zero empty-state flicker or blank screen during cold launch."
                )
                ChangelogBulletRow(
                    title = "Full Library Backup",
                    description = "Export and restore entire library supporting .webstack and .json formats."
                )
                ChangelogBulletRow(
                    title = "Duplicate URL Detection",
                    description = "Automatic duplicate identification with instant conflict feedback."
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // VERSION 1.0.2
        // ==========================================
        VersionSectionHeader(version = "v1.0.2")
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.5.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChangelogBulletRow(
                    title = "2-Card Grid Layout Mode",
                    description = "Side-by-side visual bookmark browsing mode."
                )
                ChangelogBulletRow(
                    title = "Light & Dark Themes",
                    description = "Full dynamic system theme support for comfortable browsing."
                )
                ChangelogBulletRow(
                    title = "Floating Circular Header Controls",
                    description = "Refined touch targets with cushioned spring feedback."
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // VERSION 1.0.0
        // ==========================================
        VersionSectionHeader(version = "v1.0.0")
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.5.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChangelogBulletRow(
                    title = "Automated Website Snapshots",
                    description = "Local visual card previews and offline caching."
                )
                ChangelogBulletRow(
                    title = "Dual Layout Modes",
                    description = "Full visual cards and high-density compact list rows."
                )
                ChangelogBulletRow(
                    title = "Hardware-Accelerated Search",
                    description = "Real-time search across titles, URLs, and custom category tags."
                )
                ChangelogBulletRow(
                    title = "Android Share Sheet & Clipboard",
                    description = "Save links with 1 tap from Chrome, Firefox, or copied URLs."
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Got It Button
        Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.label,
                contentColor = appColors.systemBackground
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
private fun VersionSectionHeader(
    version: String
) {
    val appColors = LocalAppColors.current
    Text(
        text = version,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = appColors.label,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun WhatsNewRichRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = appColors.fill,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = appColors.label,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.label,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = appColors.secondaryLabel,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ChangelogBulletRow(
    title: String,
    description: String
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(appColors.secondaryLabel)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = appColors.label,
                letterSpacing = (-0.2).sp
            )
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = appColors.secondaryLabel,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
