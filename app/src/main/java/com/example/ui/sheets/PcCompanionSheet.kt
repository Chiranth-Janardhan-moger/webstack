package com.example.ui.sheets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.components.AppSwitch
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.QrCodeUtils
import com.example.ui.viewmodel.WebsiteViewModel

@Composable
fun PcCompanionSubSheetContent(
    viewModel: WebsiteViewModel?,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val appColors = LocalAppColors.current

    val serverStatus = viewModel?.serverStatus?.collectAsState()?.value
        ?: com.example.server.ServerStatus()
    val isRunning = serverStatus.isRunning
    val fullUrl = if (serverStatus.ipAddress != null) {
        "http://${serverStatus.ipAddress}:${serverStatus.port}"
    } else ""
    val qrUrl = if (fullUrl.isNotBlank() && serverStatus.pin.isNotBlank()) {
        "$fullUrl/?pin=${serverStatus.pin}"
    } else fullUrl

    val qrBitmap = remember(qrUrl) {
        if (qrUrl.isNotBlank()) QrCodeUtils.generateQrBitmap(qrUrl, 512) else null
    }

    var copiedUrl by remember { mutableStateOf(false) }
    var copiedPin by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppSubSheetHeader(
            title = "PC Companion",
            onBack = onBack,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Power Card: Server Switch
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (isRunning) appColors.label else appColors.fill,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isRunning) Icons.Outlined.Wifi else Icons.Outlined.WifiOff,
                                contentDescription = null,
                                tint = if (isRunning) appColors.systemBackground else appColors.label,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Local Wi-Fi Sharing",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = appColors.label
                            )
                            if (isRunning) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF34C759).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34C759),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isRunning) "Active • Port ${serverStatus.port}" else "Turn on to access from PC",
                            fontSize = 13.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                }

                AppSwitch(
                    checked = isRunning,
                    onCheckedChange = { checked ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (checked) {
                            viewModel?.startLocalServer()
                        } else {
                            viewModel?.stopLocalServer()
                        }
                    }
                )
            }
        }

        serverStatus.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFF3B30).copy(alpha = 0.1f),
                border = BorderStroke(0.5.dp, Color(0xFFFF3B30).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = error,
                        color = Color(0xFFFF3B30),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Running State Content
        AnimatedVisibility(
            visible = isRunning,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // QR Code Card
                if (qrBitmap != null) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = appColors.surface,
                        border = BorderStroke(0.75.dp, appColors.separator),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Scan with PC or Tablet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.label
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Points desktop browser directly to your phone",
                                fontSize = 12.sp,
                                color = appColors.secondaryLabel
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // High-contrast QR Container
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                modifier = Modifier.size(200.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = qrBitmap,
                                        contentDescription = "Connection QR Code",
                                        modifier = Modifier.size(176.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Connection Credentials Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = appColors.surface,
                    border = BorderStroke(0.75.dp, appColors.separator),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // URL Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = appColors.fill,
                                    shape = CircleShape,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Link,
                                            contentDescription = null,
                                            tint = appColors.label,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Desktop Browser URL",
                                        fontSize = 12.sp,
                                        color = appColors.secondaryLabel
                                    )
                                    Text(
                                        text = fullUrl,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = appColors.label
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = appColors.fill,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("WebStack URL", fullUrl))
                                    copiedUrl = true
                                    Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copiedUrl) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                        contentDescription = null,
                                        tint = appColors.label,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (copiedUrl) "Copied" else "Copy",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = appColors.label
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = appColors.separator
                        )

                        // PIN Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = appColors.fill,
                                    shape = CircleShape,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = appColors.label,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Security PIN",
                                        fontSize = 12.sp,
                                        color = appColors.secondaryLabel
                                    )
                                    Text(
                                        text = formatPin(serverStatus.pin),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = appColors.label
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = appColors.fill,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("WebStack PIN", serverStatus.pin))
                                    copiedPin = true
                                    Toast.makeText(context, "PIN copied", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copiedPin) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                        contentDescription = null,
                                        tint = appColors.label,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (copiedPin) "Copied" else "Copy",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = appColors.label
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = appColors.separator
                        )

                        // Active Clients Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sensors,
                                contentDescription = null,
                                tint = if (serverStatus.connectedClients > 0) Color(0xFF34C759) else appColors.secondaryLabel,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (serverStatus.connectedClients > 0)
                                    "${serverStatus.connectedClients} active connection(s)"
                                else
                                    "Waiting for PC connection...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (serverStatus.connectedClients > 0) Color(0xFF34C759) else appColors.secondaryLabel
                            )
                        }
                    }
                }
            }
        }

        // Instructions Guide Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = appColors.surface,
            border = BorderStroke(0.75.dp, appColors.separator),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DesktopWindows,
                        contentDescription = null,
                        tint = appColors.label,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "How to connect from PC",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appColors.label
                    )
                }

                GuideStepRow(step = "1", text = "Connect your PC/laptop to the same Wi-Fi router as your phone.")
                GuideStepRow(step = "2", text = "Turn ON the server toggle and open the URL or scan the QR code.")
                GuideStepRow(step = "3", text = "Enter the 6-digit PIN on your computer to securely access your bookmarks.")
                GuideStepRow(step = "4", text = "Paste links from PC to instantly save them into WebStack on your phone.")

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = appColors.separator
                )

                Text(
                    text = "Zero Internet Required • 100% Private Local Subnet",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = appColors.secondaryLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GuideStepRow(step: String, text: String) {
    val appColors = LocalAppColors.current
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = appColors.fill,
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = step,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.label
                )
            }
        }
        Text(
            text = text,
            fontSize = 13.sp,
            color = appColors.secondaryLabel,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatPin(raw: String): String {
    if (raw.length != 6) return raw
    return "${raw.substring(0, 3)}  ${raw.substring(3, 6)}"
}
