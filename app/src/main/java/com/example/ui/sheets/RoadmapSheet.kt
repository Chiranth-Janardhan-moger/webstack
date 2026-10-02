package com.example.ui.sheets

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GITHUB_ISSUES_URL
import com.example.ui.SPONSOR_URL
import com.example.ui.theme.LocalAppColors

@Composable
fun RoadmapSubSheetContent(
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
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
        // Navigation Header: Back, Title, Sponsor Pill + Close Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BouncyIconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBack()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Back",
                    tint = appColors.label,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Roadmap",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = appColors.label
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Top-right corner Sponsor pill with heart icon and Sponsor text
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SPONSOR_URL))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFFFF2D55).copy(alpha = if (appColors.isDark) 0.22f else 0.12f),
                    border = BorderStroke(0.75.dp, Color(0xFFFF2D55).copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Sponsor",
                            tint = Color(0xFFFF2D55),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Sponsor",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF2D55)
                        )
                    }
                }

                BouncyIconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = appColors.secondaryLabel,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Intro Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = appColors.fill,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Future Developments",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = appColors.label,
                            letterSpacing = (-0.4).sp
                        )
                        Text(
                            text = "Features currently under consideration",
                            fontSize = 12.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "We are exploring several high-impact additions for upcoming WebStack releases. Your feedback shapes what gets built first.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = appColors.secondaryLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "IN PLANNING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        RoadmapFeatureCard(
            number = 1,
            title = "Backup Encryption",
            description = "Optional password-protected AES encryption for .webstack and .json backup files. Keeps sensitive bookmarks and private reading lists secure even when exported to external cloud drives or shared between devices.",
            icon = Icons.Outlined.Lock
        )

        Spacer(modifier = Modifier.height(14.dp))

        RoadmapFeatureCard(
            number = 2,
            title = "PC Companion Mode",
            description = "Turn your phone into a lightweight local server. WebStack gives you a local Wi-Fi URL (e.g., http://192.168.1.10:8080) that you open on your computer's browser to view, search, and manage your bookmarks on PC without requiring any third-party cloud accounts.",
            icon = Icons.Outlined.Devices
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feedback & Community Request Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                                imageVector = Icons.Outlined.Feedback,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "Have Feature Requests or Ideas?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = appColors.label
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Would you use backup encryption or PC companion mode? Do you need other features? Let us know on GitHub by opening an issue or replying saying yes!",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = appColors.secondaryLabel
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_ISSUES_URL))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.label,
                        contentColor = appColors.systemBackground
                    ),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Give Feedback on GitHub", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sponsor Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFFF2D55).copy(alpha = if (appColors.isDark) 0.22f else 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFFF2D55),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "Sponsor Development",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = appColors.label
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "WebStack is built with dedication to privacy and clean design. Supporting through GitHub Sponsors helps speed up future feature releases.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = appColors.secondaryLabel
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SPONSOR_URL))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF2D55).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF2D55)
                    ),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF2D55),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Sponsor on GitHub", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

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

/**
 * Shared planning feature card: numbered icon + title + Planning badge + description.
 * Eliminates the duplicated block structure between Backup Encryption and PC Companion Mode.
 */
@Composable
private fun RoadmapFeatureCard(
    number: Int,
    title: String,
    description: String,
    icon: ImageVector
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
                .padding(16.dp)
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
                        text = "$number. $title",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = appColors.label
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = appColors.fill
                ) {
                    Text(
                        text = "Planning",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.secondaryLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = appColors.secondaryLabel
            )
        }
    }
}

/**
 * Bouncy IconButton with spring press-scale animation.
 * Eliminates the MutableInteractionSource + animateFloatAsState + graphicsLayer repetition
 * for back and close buttons across Roadmap and other sheets.
 */
@Composable
private fun BouncyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bouncy_icon_scale"
    )
    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        content()
    }
}
