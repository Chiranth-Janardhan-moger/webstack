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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
        // Header: Logo + "What's New" + Version Pill + Close Button
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
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = appColors.fill,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, appColors.separator)
                ) {
                    Text(
                        text = "v2.0.0 • Latest",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.secondaryLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
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
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Complete release changelog and version history across all updates.",
            fontSize = 13.sp,
            color = appColors.secondaryLabel,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // VERSION 2.0.0 (FLAGSHIP)
        // ==========================================
        VersionSectionHeader(
            version = "VERSION 2.0.0",
            theme = "PC COMPANION & LOCAL SYNC",
            isLatest = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            shadowElevation = if (appColors.isDark) 0.dp else 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WhatsNewRichRow(
                    icon = Icons.Outlined.DesktopWindows,
                    title = "PC Companion Mode",
                    description = "Browse and push links between phone and computer over local Wi-Fi.",
                    tag = "NEW"
                )
                HorizontalDivider(thickness = 0.5.dp, color = appColors.separator.copy(alpha = 0.5f))
                WhatsNewRichRow(
                    icon = Icons.Outlined.DesktopWindows,
                    title = "Dedicated Desktop Client",
                    description = "Lightweight native Windows & Linux client with quick keyboard navigation.",
                    tag = "NATIVE"
                )
                HorizontalDivider(thickness = 0.5.dp, color = appColors.separator.copy(alpha = 0.5f))
                WhatsNewRichRow(
                    icon = Icons.Outlined.Lock,
                    title = "6-Digit PIN & Permanent Security",
                    description = "Secure access with option to keep a persistent PIN across reconnects.",
                    tag = "AUTH"
                )
                HorizontalDivider(thickness = 0.5.dp, color = appColors.separator.copy(alpha = 0.5f))
                WhatsNewRichRow(
                    icon = Icons.Outlined.Language,
                    title = "Zero-Install Browser Mode",
                    description = "Open directly in Chrome, Edge, or Brave via local URL without extra setup.",
                    tag = "CHROME"
                )
                HorizontalDivider(thickness = 0.5.dp, color = appColors.separator.copy(alpha = 0.5f))
                WhatsNewRichRow(
                    icon = Icons.Outlined.OfflineBolt,
                    title = "Offline PC Vault Access",
                    description = "Search cached bookmarks on PC even when phone is turned off or disconnected.",
                    tag = "OFFLINE"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // VERSION 1.2.0
        // ==========================================
        VersionSectionHeader(
            version = "VERSION 1.2.0",
            theme = "CUSTOMIZATION & TRIAGE"
        )
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
        VersionSectionHeader(
            version = "VERSION 1.1.2",
            theme = "FOLDABLES & REFINEMENTS"
        )
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
        VersionSectionHeader(
            version = "VERSION 1.1.1",
            theme = "BROWSER MIGRATION"
        )
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
        VersionSectionHeader(
            version = "VERSION 1.1.0",
            theme = "BACKUP & STABILITY"
        )
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
        // VERSION 1.0.1 / 1.0.2
        // ==========================================
        VersionSectionHeader(
            version = "VERSION 1.0.1 & 1.0.2",
            theme = "GRID & THEMES"
        )
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
        VersionSectionHeader(
            version = "VERSION 1.0.0",
            theme = "INITIAL FOUNDATION"
        )
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
    version: String,
    theme: String,
    isLatest: Boolean = false
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = version,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLatest) appColors.label else appColors.secondaryLabel,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "•",
                fontSize = 11.sp,
                color = appColors.tertiaryLabel
            )
            Text(
                text = theme,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.secondaryLabel,
                letterSpacing = 0.8.sp
            )
        }

        if (isLatest) {
            Surface(
                color = appColors.label,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "CURRENT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.systemBackground,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun WhatsNewRichRow(
    icon: ImageVector,
    title: String,
    description: String,
    tag: String
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

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = appColors.fill,
            border = BorderStroke(0.5.dp, appColors.separator)
        ) {
            Text(
                text = tag,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.secondaryLabel,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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
