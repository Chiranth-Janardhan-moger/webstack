package com.example.ui.sheets

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalAppleColors

@Composable
fun AppleWhatsNewBottomSheetContent(
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
                    colorFilter = ColorFilter.tint(appleColors.label)
                )
                Text(
                    text = "WebStack Features",
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

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "WebStack is a modern visual bookmark stack designed for fast scanning, offline reliability, and aesthetic clarity.",
            fontSize = 13.sp,
            color = appleColors.secondaryLabel,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Feature items
        AppleWhatsNewFeatureItem(
            icon = Icons.Default.GridView,
            title = "3 Distinct Layout Modes",
            description = "Effortlessly toggle between Large Visual Cards, 2-Card Grid, and high-density Compact List in Settings."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Outlined.Speed,
            title = "Offline Snapshot Caching",
            description = "Screenshots are saved locally on your device. After the first load, previews appear instantly with zero data consumption."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Outlined.Share,
            title = "System Share Sheet Integration",
            description = "Share links directly from Safari, Chrome, Twitter/X, or any app straight into WebStack in 1 tap."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Default.ContentPaste,
            title = "Instant Clipboard Detection",
            description = "Opening the add sheet automatically detects copied website links and presents a 1-tap 'Paste' banner."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Default.FilterList,
            title = "Smart Categories & Filter",
            description = "Organize bookmarks by All, Personal, Design, Tools, Work, and Reading with color-coded count badges."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Outlined.Search,
            title = "Fast Instant Search",
            description = "Search through your entire bookmark library in real time by title, website URL, or domain."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Outlined.Layers,
            title = "Offline Data & Backup",
            description = "100% on-device private SQLite database. Safely export your stack to JSON and restore anytime."
        )

        AppleWhatsNewFeatureItem(
            icon = Icons.Outlined.Refresh,
            title = "Manual Snapshot Refresh",
            description = "Re-capture any website snapshot whenever page visuals change with the instant refresh action."
        )

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
            Text("Got It", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AppleWhatsNewFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    val appleColors = LocalAppleColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = appleColors.fill,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = appleColors.label,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = appleColors.label
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = appleColors.secondaryLabel,
                lineHeight = 17.sp
            )
        }
    }
}
