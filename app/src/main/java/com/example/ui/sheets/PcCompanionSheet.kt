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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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

    val currentPort by (viewModel?.companionPort?.collectAsState() ?: remember { mutableStateOf(8080) })
    val currentUseFixedPin by (viewModel?.companionUseFixedPin?.collectAsState() ?: remember { mutableStateOf(false) })
    val currentCustomPin by (viewModel?.companionCustomPin?.collectAsState() ?: remember { mutableStateOf("") })
    val currentOfflineExpiryHours by (viewModel?.companionOfflineExpiryHours?.collectAsState() ?: remember { mutableStateOf(24) })

    var showSettingsPage by remember { mutableStateOf(false) }
    var showOfflineInfoPage by remember { mutableStateOf(false) }

    var copiedUrl by remember { mutableStateOf(false) }
    var copiedPin by remember { mutableStateOf(false) }

    val effectiveIp = serverStatus.ipAddress ?: com.example.server.WifiUtils.getLocalIpAddress() ?: "192.168.x.x"
    val companionHostUrl = if (fullUrl.isNotBlank()) fullUrl else "http://$effectiveIp:$currentPort"

    if (showOfflineInfoPage) {
        PcCompanionOfflineInfoPage(
            hostUrl = companionHostUrl,
            port = currentPort,
            onBack = { showOfflineInfoPage = false },
            onDismiss = onDismiss
        )
        return
    }

    if (showSettingsPage) {
        PcCompanionSettingsPage(
            initialPort = currentPort,
            initialUseFixedPin = currentUseFixedPin,
            initialCustomPin = currentCustomPin,
            initialOfflineExpiryHours = currentOfflineExpiryHours,
            isRunning = isRunning,
            onBack = { showSettingsPage = false },
            onDismiss = onDismiss,
            onSave = { port, useFixed, customPin, expiryHours ->
                viewModel?.setCompanionPort(port)
                viewModel?.setCompanionUseFixedPin(useFixed)
                viewModel?.setCompanionCustomPin(customPin)
                viewModel?.setCompanionOfflineExpiryHours(expiryHours)
                if (isRunning) {
                    viewModel?.restartLocalServer()
                    Toast.makeText(context, "Server restarted on port $port", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
                }
                showSettingsPage = false
            }
        )
        return
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBack()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = appColors.label,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "PC Companion",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appColors.label,
                    letterSpacing = (-0.3).sp
                )
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showOfflineInfoPage = true
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "PC Companion Info",
                        tint = appColors.secondaryLabel,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showSettingsPage = true
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "PC Companion Settings",
                    tint = appColors.label,
                    modifier = Modifier.size(21.dp)
                )
            }
        }

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
                                    shape = CircleShape,
                                    color = Color(0xFF34C759).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34C759),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isRunning) "Active • Port ${serverStatus.port}" else "Port $currentPort • ${if (currentUseFixedPin) "Fixed PIN" else "Dynamic PIN"}",
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
                            Spacer(modifier = Modifier.height(12.dp))

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
                                        text = "Browser URL",
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
                                shape = RoundedCornerShape(10.dp),
                                color = appColors.fill,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("WebStack URL", fullUrl))
                                    copiedUrl = true
                                    Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (copiedUrl) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy URL",
                                        tint = appColors.label,
                                        modifier = Modifier.size(16.dp)
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "PIN",
                                            fontSize = 12.sp,
                                            color = appColors.secondaryLabel
                                        )
                                        if (currentUseFixedPin) {
                                            Surface(
                                                shape = CircleShape,
                                                color = appColors.fill
                                            ) {
                                                Text(
                                                    text = "FIXED",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = appColors.secondaryLabel,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
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
                                shape = RoundedCornerShape(10.dp),
                                color = appColors.fill,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("WebStack PIN", serverStatus.pin))
                                    copiedPin = true
                                    Toast.makeText(context, "PIN copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (copiedPin) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy PIN",
                                        tint = appColors.label,
                                        modifier = Modifier.size(16.dp)
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

@Composable
fun PcCompanionSettingsPage(
    initialPort: Int,
    initialUseFixedPin: Boolean,
    initialCustomPin: String,
    initialOfflineExpiryHours: Int = 24,
    isRunning: Boolean,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (port: Int, useFixedPin: Boolean, customPin: String, offlineExpiryHours: Int) -> Unit
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var portText by remember { mutableStateOf(initialPort.toString()) }
    var useFixedPin by remember { mutableStateOf(initialUseFixedPin) }
    var pinText by remember {
        mutableStateOf(
            if (initialCustomPin.isNotBlank() && initialCustomPin.length == 6 && initialCustomPin.all { it.isDigit() }) initialCustomPin
            else (100000..999999).random().toString()
        )
    }
    var selectedExpiryHours by remember { mutableStateOf(initialOfflineExpiryHours) }

    val parsedPort = portText.trim().toIntOrNull()
    val isPortValid = parsedPort != null && parsedPort in 1024..65535
    val cleanPin = pinText.filter { it.isDigit() }.take(6)
    val isPinValid = !useFixedPin || cleanPin.length == 6
    val canSave = isPortValid && isPinValid

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppSubSheetHeader(
            title = "Settings",
            onBack = onBack,
            onDismiss = onDismiss,
            showClose = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Network Port Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = appColors.surface,
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NETWORK PORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.secondaryLabel,
                            letterSpacing = 0.5.sp
                        )
                        if (portText.trim() != "8080") {
                            Text(
                                text = "Reset to 8080",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.accent,
                                modifier = Modifier.clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    portText = "8080"
                                }
                            )
                        }
                    }

                    Surface(
                        color = appColors.fill,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            0.75.dp,
                            if (!isPortValid && portText.isNotBlank()) Color(0xFFFF3B30).copy(alpha = 0.6f) else appColors.separator
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = portText,
                                onValueChange = { portText = it.filter { ch -> ch.isDigit() }.take(5) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = appColors.label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                cursorBrush = SolidColor(appColors.accent),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Text(
                        text = if (!isPortValid && portText.isNotBlank()) "Port must be between 1024 and 65535" else "Default port is 8080. Range 1024 to 65535.",
                        fontSize = 12.sp,
                        color = if (!isPortValid && portText.isNotBlank()) Color(0xFFFF3B30) else appColors.secondaryLabel
                    )
                }
            }

            // Fixed PIN Security Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = appColors.surface,
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fixed PIN Code",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.label
                            )
                            Text(
                                text = if (useFixedPin) "Keep this PIN across all sessions" else "Generate fresh PIN on every start",
                                fontSize = 12.sp,
                                color = appColors.secondaryLabel
                            )
                        }
                        AppSwitch(
                            checked = useFixedPin,
                            onCheckedChange = { checked ->
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                useFixedPin = checked
                            }
                        )
                    }

                    AnimatedVisibility(visible = useFixedPin) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Surface(
                                color = appColors.fill,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    0.75.dp,
                                    if (!isPinValid && pinText.isNotBlank()) Color(0xFFFF3B30).copy(alpha = 0.6f) else appColors.separator
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    BasicTextField(
                                        value = pinText,
                                        onValueChange = { pinText = it.filter { ch -> ch.isDigit() }.take(6) },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = appColors.label,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 2.sp
                                        ),
                                        cursorBrush = SolidColor(appColors.accent),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            pinText = (100000..999999).random().toString()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Refresh,
                                            contentDescription = "Randomize PIN",
                                            tint = appColors.secondaryLabel,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (!isPinValid && pinText.isNotBlank()) "PIN must be exactly 6 digits" else "This permanent PIN will be required whenever your PC connects.",
                                fontSize = 12.sp,
                                color = if (!isPinValid && pinText.isNotBlank()) Color(0xFFFF3B30) else appColors.secondaryLabel
                            )
                        }
                    }
                }
            }

            // Offline Session Access Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = appColors.surface,
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "OFFLINE SESSION DURATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.secondaryLabel,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Duration PC can browse & queue bookmarks without reconnecting to phone",
                            fontSize = 12.sp,
                            color = appColors.secondaryLabel
                        )
                    }

                    val expiryOptions = listOf(
                        24 to "24 Hours",
                        168 to "7 Days",
                        720 to "30 Days",
                        0 to "Never"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        expiryOptions.forEach { (hours, label) ->
                            val isSelected = selectedExpiryHours == hours
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedExpiryHours = hours
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) appColors.label else appColors.fill,
                                border = BorderStroke(
                                    0.5.dp,
                                    if (isSelected) appColors.label else appColors.separator
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) appColors.systemBackground else appColors.label,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isRunning) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF9500).copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, Color(0xFFFF9500).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Server is active. Saving will restart it with new settings immediately.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF9500),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    val port = parsedPort ?: return@Button
                    if (canSave) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSave(port, useFixedPin, cleanPin, selectedExpiryHours)
                    }
                },
                enabled = canSave,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.label,
                    contentColor = appColors.systemBackground,
                    disabledContainerColor = appColors.fill,
                    disabledContentColor = appColors.tertiaryLabel
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isRunning) "Save & Restart Server" else "Save Settings",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PcCompanionOfflineInfoPage(
    hostUrl: String,
    port: Int,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppSubSheetHeader(
            title = "PC Companion Info",
            onBack = onBack,
            onDismiss = onDismiss,
            showClose = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Local Wi-Fi Sync
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = appColors.surface,
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    imageVector = Icons.Outlined.Wifi,
                                    contentDescription = null,
                                    tint = appColors.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Local Wi-Fi Sync",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                    }
                    Text(
                        text = "Access and manage all your bookmarks on any PC or laptop connected to the same Wi-Fi. Add, edit, or delete links seamlessly with instant bidirectional sync.",
                        fontSize = 13.sp,
                        color = appColors.secondaryLabel,
                        lineHeight = 18.sp
                    )
                }
            }

            // Card 2: Native Desktop App
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = appColors.surface,
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    imageVector = Icons.Outlined.DesktopWindows,
                                    contentDescription = null,
                                    tint = appColors.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Native Desktop App",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                    }
                    Text(
                        text = "Install the dedicated WebStack desktop client for Windows, macOS, or Linux. Enjoy keyboard shortcuts, tray integration, and a focused desktop workspace.",
                        fontSize = 13.sp,
                        color = appColors.secondaryLabel,
                        lineHeight = 18.sp
                    )
                }
            }

            // Card 3: Zero Cloud & Encrypted
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = appColors.surface,
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = appColors.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Zero Cloud & Private",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                    }
                    Text(
                        text = "Your data never leaves your local network. No external servers or cloud accounts are used. Connection is authenticated with a 6-digit PIN and encrypted with AES-256.",
                        fontSize = 13.sp,
                        color = appColors.secondaryLabel,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
