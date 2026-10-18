package com.example.ui.sheets

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ui.DEVELOPER_EMAIL
import com.example.ui.GITHUB_ISSUES_URL
import com.example.ui.GITHUB_REPO_URL
import com.example.ui.SPONSOR_URL
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.theme.LocalAppColors

enum class AppInfoSubScreen {
    MAIN,
    ROADMAP,
    FEATURES
}

@Composable
fun AppInfoBottomSheetContent(
    onDismiss: () -> Unit,
    onBack: (() -> Unit)? = null,
    onOpenWhatsNew: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    val versionName = BuildConfig.VERSION_NAME

    var currentSubScreen by remember { mutableStateOf(AppInfoSubScreen.MAIN) }
    var showFeedbackOptionsDialog by remember { mutableStateOf(false) }

    when (currentSubScreen) {
        AppInfoSubScreen.MAIN -> {
            if (onBack != null) {
                BackHandler {
                    onBack()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                AppSubSheetHeader(
                    title = "App Info",
                    onBack = onBack,
                    onDismiss = onDismiss
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Top Full-Width Hero Card: App Branding (Icon squircle + WebStack + v1.2.0 + Author)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = appColors.surface,
                    border = BorderStroke(0.75.dp, appColors.separator),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = appColors.fill,
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier.size(58.dp),
                            border = BorderStroke(0.5.dp, appColors.separator.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_logo),
                                    contentDescription = "WebStack Logo",
                                    modifier = Modifier.size(32.dp),
                                    colorFilter = ColorFilter.tint(appColors.label)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "WebStack",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = appColors.label,
                                    letterSpacing = (-0.5).sp
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = appColors.fill
                                ) {
                                    Text(
                                        text = "v$versionName",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = appColors.label,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "by Chiranth Moger",
                                fontSize = 14.sp,
                                color = appColors.secondaryLabel
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Side-by-Side Cards: GitHub & Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. GitHub Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = appColors.surface,
                        border = BorderStroke(0.75.dp, appColors.separator),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO_URL))
                            context.startActivity(intent)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = appColors.fill,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_github),
                                        contentDescription = "GitHub",
                                        modifier = Modifier.size(20.dp),
                                        colorFilter = ColorFilter.tint(appColors.label)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "GitHub",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = appColors.label
                                )
                                Text(
                                    text = "View source code",
                                    fontSize = 12.sp,
                                    color = appColors.secondaryLabel,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // 2. Feedback Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = appColors.surface,
                        border = BorderStroke(0.75.dp, appColors.separator),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showFeedbackOptionsDialog = true
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = appColors.fill,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.BugReport,
                                        contentDescription = "Feedback",
                                        tint = appColors.label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Feedback",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = appColors.label
                                )
                                Text(
                                    text = "Report issues",
                                    fontSize = 12.sp,
                                    color = appColors.secondaryLabel,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Unified Grouped Info Card (Features, Updates, Roadmap, Sponsor)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = appColors.surface,
                    border = BorderStroke(0.75.dp, appColors.separator),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (onOpenWhatsNew != null) {
                            InfoGroupedRow(
                                title = "What's New",
                                subtitle = "Recent updates & improvements",
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onOpenWhatsNew()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = appColors.label,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = appColors.separator
                            )
                        }

                        InfoGroupedRow(
                            title = "WebStack Features",
                            subtitle = "How to access & use all features",
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentSubScreen = AppInfoSubScreen.FEATURES
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DashboardCustomize,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = appColors.separator
                        )

                        InfoGroupedRow(
                            title = "Roadmap",
                            subtitle = "Planned features & companion mode",
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentSubScreen = AppInfoSubScreen.ROADMAP
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = appColors.separator
                        )

                        InfoGroupedRow(
                            title = "Sponsor",
                            subtitle = "Support on GitHub",
                            iconBgColor = Color(0xFFFF2D55).copy(alpha = if (appColors.isDark) 0.22f else 0.12f),
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SPONSOR_URL))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Sponsor",
                                tint = Color(0xFFFF2D55),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                    Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        AppInfoSubScreen.ROADMAP -> {
            RoadmapSubSheetContent(
                onBack = { currentSubScreen = AppInfoSubScreen.MAIN },
                onDismiss = onDismiss
            )
        }
        AppInfoSubScreen.FEATURES -> {
            WebStackFeaturesSubSheetContent(
                onBack = { currentSubScreen = AppInfoSubScreen.MAIN },
                onDismiss = onDismiss
            )
        }
    }

    if (showFeedbackOptionsDialog) {
        FeedbackOptionsDialog(
            versionName = versionName,
            onDismiss = { showFeedbackOptionsDialog = false }
        )
    }
}

/**
 * Shared grouped card row: icon box (36dp) + title + subtitle + trailing arrow.
 * Used for What's New, Roadmap, and Sponsor rows in AppInfoSheet.
 */
@Composable
private fun InfoGroupedRow(
    title: String,
    subtitle: String? = null,
    iconBgColor: Color? = null,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val appColors = LocalAppColors.current
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = iconBgColor ?: appColors.fill,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        icon()
                    }
                }

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = appColors.label
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = appColors.tertiaryLabel,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Modal dialog presenting users with options to report issues/feedback via GitHub Issues or direct email.
 */
@Composable
private fun FeedbackOptionsDialog(
    versionName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.secondaryGroupedBackground,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(BorderStroke(0.75.dp, appColors.separator), RoundedCornerShape(24.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = appColors.fill,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.BugReport,
                            contentDescription = null,
                            tint = appColors.label,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Feedback & Support",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = appColors.label
                    )
                    Text(
                        text = "Choose your preferred channel",
                        fontSize = 12.sp,
                        color = appColors.secondaryLabel
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 1: GitHub Issues
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = appColors.surface,
                    border = BorderStroke(0.75.dp, appColors.separator),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_ISSUES_URL))
                        context.startActivity(intent)
                        onDismiss()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = appColors.fill,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_github),
                                    contentDescription = "GitHub Issues",
                                    modifier = Modifier.size(18.dp),
                                    colorFilter = ColorFilter.tint(appColors.label)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GitHub Issues",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = appColors.label
                            )
                            Text(
                                text = "Report bugs or request features",
                                fontSize = 12.sp,
                                color = appColors.secondaryLabel
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = appColors.tertiaryLabel,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Option 2: Send Email
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = appColors.surface,
                    border = BorderStroke(0.75.dp, appColors.separator),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$DEVELOPER_EMAIL?subject=${Uri.encode("WebStack Feedback [v$versionName]")}")
                        }
                        try {
                            context.startActivity(mailIntent)
                        } catch (e: Exception) {
                            try {
                                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:$DEVELOPER_EMAIL"))
                                context.startActivity(fallbackIntent)
                            } catch (e2: Exception) {
                                Toast.makeText(context, "No email app found ($DEVELOPER_EMAIL)", Toast.LENGTH_LONG).show()
                            }
                        }
                        onDismiss()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = appColors.fill,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Email",
                                    tint = appColors.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Send Email",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = appColors.label
                            )
                            Text(
                                text = DEVELOPER_EMAIL,
                                fontSize = 12.sp,
                                color = appColors.secondaryLabel
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = appColors.tertiaryLabel,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = appColors.secondaryLabel,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    )
}
