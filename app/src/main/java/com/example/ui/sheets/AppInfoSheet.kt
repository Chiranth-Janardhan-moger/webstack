package com.example.ui.sheets

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Public
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
import androidx.compose.ui.text.style.TextOverflow
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

    var showFeedbackOptionsDialog by remember { mutableStateOf(false) }
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

                // Center-Aligned App Branding (Minimalist Spotlight)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = appColors.fill,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = "WebStack Logo",
                                modifier = Modifier.size(38.dp),
                                colorFilter = ColorFilter.tint(appColors.label)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "WebStack",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = appColors.label,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "by Chiranth Moger",
                        fontSize = 13.sp,
                        color = appColors.secondaryLabel
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4 Borderless Circular Action Items in One Line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    // 1. Android
                    AppInfoActionItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Android,
                                contentDescription = "Android",
                                tint = appColors.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "Android",
                        subtitle = "v$versionName",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            Toast.makeText(context, "WebStack for Android v$versionName", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // 2. Desktop (Windows & Linux)
                    AppInfoActionItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.DesktopWindows,
                                contentDescription = "Desktop Companion",
                                tint = appColors.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "Desktop",
                        subtitle = "v1.0.0",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://webstack7.vercel.app/download"))
                            context.startActivity(intent)
                        }
                    )

                    // 3. GitHub
                    AppInfoActionItem(
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = "GitHub",
                                colorFilter = ColorFilter.tint(appColors.label),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "GitHub",
                        subtitle = "Source",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO_URL))
                            context.startActivity(intent)
                        }
                    )

                    // 4. Website
                    AppInfoActionItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Public,
                                contentDescription = "Website",
                                tint = appColors.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = "Website",
                        subtitle = "webstack7",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://webstack7.vercel.app"))
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Unified Grouped Info Card (Features, Updates, Feedback, Sponsor)
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
                            title = "Feedback & Support",
                            subtitle = "Report issues or request features",
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showFeedbackOptionsDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BugReport,
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

    if (showFeedbackOptionsDialog) {
        FeedbackOptionsDialog(
            versionName = versionName,
            onDismiss = { showFeedbackOptionsDialog = false }
        )
    }
}

/**
 * Borderless circular action item for the App Info top dock.
 * Displays a 44dp circular icon container with zero borders, followed by title and subtitle.
 */
@Composable
private fun AppInfoActionItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = appColors.fill,
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.label
        )

        Text(
            text = subtitle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = appColors.secondaryLabel
        )
    }
}

/**
 * Shared grouped card row: icon box (36dp) + title + subtitle + trailing arrow.
 * Used for What's New, Feedback & Support, and Sponsor rows in AppInfoSheet.
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Feedback & Support",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        color = appColors.label
                    )
                    Text(
                        text = "Choose your preferred channel",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
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
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "GitHub Issues",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                color = appColors.label
                            )
                            Text(
                                text = "Bugs & feature requests",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Send Email",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                color = appColors.label
                            )
                            Text(
                                text = DEVELOPER_EMAIL,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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
