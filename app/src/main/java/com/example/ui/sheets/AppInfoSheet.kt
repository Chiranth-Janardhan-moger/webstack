package com.example.ui.sheets

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.GITHUB_ISSUES_URL
import com.example.ui.GITHUB_REPO_URL
import com.example.ui.SPONSOR_URL
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.theme.LocalAppColors

enum class AppInfoSubScreen {
    MAIN,
    ROADMAP
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

                Spacer(modifier = Modifier.height(20.dp))

                // Hero App Branding Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = appColors.surface,
                    border = BorderStroke(0.75.dp, appColors.separator),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = appColors.fill,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.size(68.dp),
                            border = BorderStroke(1.dp, appColors.separator.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_logo),
                                    contentDescription = "WebStack Logo",
                                    modifier = Modifier.size(34.dp),
                                    colorFilter = ColorFilter.tint(appColors.label)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "WebStack",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = appColors.label,
                            letterSpacing = (-0.6).sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Version $versionName • by Chiranth Moger",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors.secondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (onOpenWhatsNew != null) {
                    Text(
                        text = "UPDATES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.secondaryLabel,
                        letterSpacing = 1.4.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    StandaloneInfoRow(
                        title = "What's New in WebStack",
                        subtitle = "Explore latest features and changes",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenWhatsNew()
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            colorFilter = ColorFilter.tint(appColors.label)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Section: FUTURE DEVELOPMENTS
                Text(
                    text = "FUTURE DEVELOPMENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.secondaryLabel,
                    letterSpacing = 1.4.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                StandaloneInfoRow(
                    title = "Roadmap & Future Ideas",
                    subtitle = "Backup encryption, PC companion mode & roadmap",
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

                Spacer(modifier = Modifier.height(20.dp))

                // Section: SPONSOR & SUPPORT
                Text(
                    text = "SUPPORT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.secondaryLabel,
                    letterSpacing = 1.4.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                StandaloneInfoRow(
                    title = "Sponsor WebStack",
                    subtitle = "Support independent development on GitHub",
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

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Community
                Text(
                    text = "COMMUNITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.secondaryLabel,
                    letterSpacing = 1.4.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Grouped Actions Card (HIG List)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = appColors.surface,
                    border = BorderStroke(0.75.dp, appColors.separator),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Source Code / GitHub Row
                        Surface(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO_URL))
                                context.startActivity(intent)
                            },
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
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Surface(
                                        color = appColors.fill,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Code,
                                                contentDescription = null,
                                                tint = appColors.label,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Source Code",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = appColors.label
                                    )
                                }

                                Image(
                                    painter = painterResource(id = R.drawable.ic_github),
                                    contentDescription = "GitHub",
                                    modifier = Modifier.size(22.dp),
                                    colorFilter = ColorFilter.tint(appColors.label)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = appColors.separator
                        )

                        // Report Bugs / Suggest Features Row
                        Surface(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_ISSUES_URL))
                                context.startActivity(intent)
                            },
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

                                    Text(
                                        text = "Report Bugs or Suggest Features",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = appColors.label
                                    )
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
                }

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
    }
}

/**
 * Shared standalone card row: icon box (36dp) + title + subtitle + trailing arrow.
 * Used for What's New, Roadmap, and Sponsor rows in AppInfoSheet.
 */
@Composable
private fun StandaloneInfoRow(
    title: String,
    subtitle: String,
    iconBgColor: Color? = null,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val appColors = LocalAppColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = appColors.surface,
        border = BorderStroke(0.75.dp, appColors.separator),
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
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = appColors.secondaryLabel
                    )
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
