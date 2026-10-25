package com.example.ui.sheets

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.theme.LocalAppColors

@Composable
fun WebStackFeaturesSubSheetContent(
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppSubSheetHeader(
            title = "WebStack Features",
            onBack = onBack,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Explore all major capabilities and features organized by release.",
            fontSize = 13.sp,
            color = appColors.secondaryLabel,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // VERSION 2.0.0
        // ==========================================
        FeatureVersionHeader(
            version = "VERSION 2.0.0",
            theme = "PC COMPANION & LOCAL SYNC",
            isLatest = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.DesktopWindows,
            title = "PC Companion Mode",
            description = "Browse, sync, and push links between your phone and computer over local Wi-Fi with zero cloud lock-in.",
            tag = "NEW"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.Devices,
            title = "Dedicated Desktop Client",
            description = "Lightweight native desktop application with global shortcuts and quick search for Windows and Linux.",
            tag = "DESKTOP"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.Lock,
            title = "Fixed 6-Digit PIN Security",
            description = "Persistent local PIN verification protecting bookmarks across desktop reconnection sessions.",
            tag = "SECURITY"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.Language,
            title = "Zero-Install Browser Mode",
            description = "Open directly in Chrome, Edge, Brave, or Firefox via a local URL without installing any software.",
            tag = "BROWSER"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.OfflineBolt,
            title = "Offline PC Vault Access",
            description = "Search and open cached bookmarks on your computer even when the phone is turned off or disconnected.",
            tag = "OFFLINE"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // VERSION 1.2.0
        // ==========================================
        FeatureVersionHeader(
            version = "VERSION 1.2.0",
            theme = "CUSTOMIZATION & BULK ACTIONS"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.Checklist,
            title = "Multi-Select & Bulk Actions",
            description = "Long-press any card to select multiple bookmarks to batch reassign categories, share, or delete in one tap.",
            tag = "TRIAGE"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.AutoAwesome,
            title = "12 Action Button Shapes",
            description = "Personalize your primary action button with dynamic Material 3 expressive shapes and smooth corner geometry.",
            tag = "SHAPES"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.Tune,
            title = "Visual Tag Reordering",
            description = "Drag and drop active and parked tags with visual insertion guides to organize your navigation ribbon.",
            tag = "TAGS"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // VERSION 1.1.1
        // ==========================================
        FeatureVersionHeader(
            version = "VERSION 1.1.1",
            theme = "STARTUP CACHE & BACKUP POLISH"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.Speed,
            title = "Instant Startup Cache",
            description = "Zero-delay cold start from local database cache with immediate bookmark visual hydration.",
            tag = "SPEED"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.Layers,
            title = "HTML & WebStack Backup",
            description = "Full-fidelity import and export engine supporting .html browser bookmarks, .webstack files, and JSON.",
            tag = "BACKUP"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureCardItem(
            icon = Icons.Outlined.Share,
            title = "Offline Visual Snapshots",
            description = "Locally stored website snapshot previews for rapid offline visual scanning without cellular data.",
            tag = "OFFLINE"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.fill,
                contentColor = appColors.label
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Back to App Info", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FeatureVersionHeader(
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
                    text = "LATEST",
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
private fun FeatureCardItem(
    icon: ImageVector,
    title: String,
    description: String,
    tag: String
) {
    val appColors = LocalAppColors.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = appColors.surface,
        border = BorderStroke(0.75.dp, appColors.separator),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
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
                        color = appColors.fill,
                        shape = RoundedCornerShape(10.dp),
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
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = appColors.label
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = appColors.secondaryLabel
            )
        }
    }
}
