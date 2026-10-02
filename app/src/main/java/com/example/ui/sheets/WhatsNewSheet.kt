package com.example.ui.sheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import com.example.ui.components.AppFeatureTile
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
                    text = "WebStack Features",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = appColors.label,
                    letterSpacing = (-0.4).sp
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appColors.secondaryLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "WebStack is a modern visual bookmark stack designed for fast scanning, offline reliability, and aesthetic clarity.",
            fontSize = 13.sp,
            color = appColors.secondaryLabel,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Feature items
        WhatsNewFeatureItem(
            icon = Icons.Default.GridView,
            title = "3 Distinct Layout Modes",
            description = "Large Cards, 2-Card Grid, and Compact List."
        )

        WhatsNewFeatureItem(
            icon = Icons.Outlined.Speed,
            title = "Offline Snapshot Caching",
            description = "Previews saved locally for instant zero-data loading."
        )

        WhatsNewFeatureItem(
            icon = Icons.Outlined.Share,
            title = "System Share Integration",
            description = "Save links in 1 tap from Chrome, Firefox, or any app."
        )

        WhatsNewFeatureItem(
            icon = Icons.Default.ContentPaste,
            title = "Instant Clipboard Detection",
            description = "Automatic detection and 1-tap paste for copied URLs."
        )

        WhatsNewFeatureItem(
            icon = Icons.Default.FilterList,
            title = "Smart Categories & Filter",
            description = "Color-coded tags and badges to organize your bookmarks."
        )

        WhatsNewFeatureItem(
            icon = Icons.Outlined.Search,
            title = "Fast Instant Search",
            description = "Real-time search by title, website URL, or domain."
        )

        WhatsNewFeatureItem(
            icon = Icons.Outlined.Layers,
            title = "Offline Data & Backup",
            description = "Private local SQLite storage with .webstack and .json exports."
        )

        WhatsNewFeatureItem(
            icon = Icons.Outlined.Refresh,
            title = "Manual Snapshot Refresh",
            description = "Re-capture page visuals anytime with 1 tap."
        )

        Spacer(modifier = Modifier.height(24.dp))

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
fun WhatsNewFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    AppFeatureTile(
        title = title,
        description = description,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LocalAppColors.current.label,
            modifier = Modifier.size(18.dp)
        )
    }
}
