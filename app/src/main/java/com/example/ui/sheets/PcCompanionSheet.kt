package com.example.ui.sheets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.server.HostSyncClient
import com.example.server.SyncResult
import kotlinx.coroutines.launch
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

        var activeMode by remember { mutableStateOf(0) } // 0: Share as Host, 1: Connect to Host

        // Mode Switcher: Share as Host vs Connect to Host
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = appColors.fill,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = if (activeMode == 0) appColors.surface else Color.Transparent,
                    border = if (activeMode == 0) BorderStroke(0.5.dp, appColors.separator) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeMode = 0
                        }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Share as Host",
                            fontSize = 13.sp,
                            fontWeight = if (activeMode == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeMode == 0) appColors.label else appColors.secondaryLabel
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = if (activeMode == 1) appColors.surface else Color.Transparent,
                    border = if (activeMode == 1) BorderStroke(0.5.dp, appColors.separator) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeMode = 1
                        }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Connect to Host",
                            fontSize = 13.sp,
                            fontWeight = if (activeMode == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeMode == 1) appColors.label else appColors.secondaryLabel
                        )
                    }
                }
            }
        }

        if (activeMode == 0) {
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
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF34C759), CircleShape)
                                )
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
                                    Text(
                                        text = "PIN",
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
        } else {
            ConnectToHostSection(
                viewModel = viewModel,
                context = context,
                haptics = haptics
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ConnectToHostSection(
    viewModel: WebsiteViewModel?,
    context: Context,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val appColors = LocalAppColors.current
    val coroutineScope = rememberCoroutineScope()

    var hostAddress by remember { mutableStateOf("") }
    var hostPin by remember { mutableStateOf("") }
    var isDiscovering by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncSuccess by remember { mutableStateOf<String?>(null) }
    var syncError by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val decoded = QrCodeUtils.decodeQrBitmap(bitmap)
            if (decoded != null) {
                val parsed = QrCodeUtils.parseQrUrlAndPin(decoded)
                if (parsed != null) {
                    hostAddress = parsed.first
                    if (parsed.second.isNotBlank()) hostPin = parsed.second
                    syncError = null
                    Toast.makeText(context, "QR code recognized!", Toast.LENGTH_SHORT).show()
                } else {
                    hostAddress = decoded.trim()
                    syncError = null
                    Toast.makeText(context, "Host address detected", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "No QR code detected in photo. Try closer.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
                if (bitmap != null) {
                    val decoded = QrCodeUtils.decodeQrBitmap(bitmap)
                    if (decoded != null) {
                        val parsed = QrCodeUtils.parseQrUrlAndPin(decoded)
                        if (parsed != null) {
                            hostAddress = parsed.first
                            if (parsed.second.isNotBlank()) hostPin = parsed.second
                            syncError = null
                            Toast.makeText(context, "QR code recognized!", Toast.LENGTH_SHORT).show()
                        } else {
                            hostAddress = decoded.trim()
                            syncError = null
                            Toast.makeText(context, "Host address detected", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "No QR code found in selected image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cleanPin = hostPin.filter { it.isDigit() }.take(6)
    val canSync = hostAddress.isNotBlank() && cleanPin.length == 6 && !isSyncing

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Sync Configuration Card
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = appColors.fill,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.CloudDownload,
                                contentDescription = null,
                                tint = appColors.label,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Connect to WebStack Host",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                        Text(
                            text = "Sync bookmarks from phone or PC on this Wi-Fi",
                            fontSize = 12.sp,
                            color = appColors.secondaryLabel
                        )
                    }
                }

                // 3 Fast Action Buttons: Scan QR, Pick Image, Auto-Find
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Scan QR Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = appColors.fill,
                        border = BorderStroke(0.5.dp, appColors.separator),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                takePictureLauncher.launch(null)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = "Scan QR",
                                tint = appColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Scan QR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.label
                            )
                        }
                    }

                    // Pick Image Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = appColors.fill,
                        border = BorderStroke(0.5.dp, appColors.separator),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                pickImageLauncher.launch("image/*")
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoLibrary,
                                contentDescription = "Pick Image",
                                tint = appColors.label,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Pick Image",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.label
                            )
                        }
                    }

                    // Auto-Find on Wi-Fi Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDiscovering) appColors.label else appColors.fill,
                        border = BorderStroke(0.5.dp, if (isDiscovering) appColors.label else appColors.separator),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !isDiscovering) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                coroutineScope.launch {
                                    isDiscovering = true
                                    syncError = null
                                    val found = HostSyncClient.discoverHost()
                                    isDiscovering = false
                                    if (found != null) {
                                        hostAddress = found
                                        Toast.makeText(context, "Host found: $found", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "No WebStack host found on Wi-Fi", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isDiscovering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = appColors.systemBackground
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Sensors,
                                    contentDescription = "Auto-Find",
                                    tint = appColors.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = if (isDiscovering) "Scanning..." else "Auto-Find",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDiscovering) appColors.systemBackground else appColors.label
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = appColors.separator
                )

                // Host Address Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "HOST ADDRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.secondaryLabel,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        color = appColors.fill,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.75.dp, appColors.separator),
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
                                value = hostAddress,
                                onValueChange = { hostAddress = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = appColors.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                ),
                                cursorBrush = SolidColor(appColors.accent),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (hostAddress.isBlank()) {
                                        Text(
                                            text = "http://192.168.1.xxx:8080",
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = appColors.tertiaryLabel
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (hostAddress.isNotBlank()) {
                                IconButton(
                                    onClick = { hostAddress = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear",
                                        tint = appColors.secondaryLabel,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // PIN Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "HOST 6-DIGIT PIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.secondaryLabel,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        color = appColors.fill,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.75.dp, appColors.separator),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = hostPin,
                                onValueChange = { hostPin = it.filter { ch -> ch.isDigit() }.take(6) },
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
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (hostPin.isBlank()) {
                                        Text(
                                            text = "123456",
                                            fontSize = 17.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 2.sp,
                                            color = appColors.tertiaryLabel
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                // Sync Button
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch {
                            isSyncing = true
                            syncSuccess = null
                            syncError = null
                            val res = viewModel?.syncWithHost(hostAddress, cleanPin)
                            isSyncing = false
                            when (res) {
                                is SyncResult.Success -> {
                                    syncSuccess = "Synced ${res.addedCount} new bookmarks from ${res.hostDevice}! (${res.totalHostCount} total on host)"
                                    Toast.makeText(context, "Sync complete!", Toast.LENGTH_SHORT).show()
                                }
                                is SyncResult.Error -> {
                                    syncError = res.message
                                }
                                null -> {
                                    syncError = "Sync service unavailable"
                                }
                            }
                        }
                    },
                    enabled = canSync,
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
                    if (isSyncing) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = appColors.systemBackground
                            )
                            Text(
                                text = "Syncing with Host...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Connect & Sync Bookmarks",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Success Card
        syncSuccess?.let { successText ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF34C759).copy(alpha = 0.12f),
                border = BorderStroke(0.5.dp, Color(0xFF34C759).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = successText,
                        color = Color(0xFF28A745),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Error Card
        syncError?.let { errorText ->
            Surface(
                shape = RoundedCornerShape(14.dp),
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
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = errorText,
                        color = Color(0xFFFF3B30),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
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
                        text = "How APK-to-APK sync works",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = appColors.label
                    )
                }

                GuideStepRow(step = "1", text = "On the primary phone, open PC Companion and enable Local Wi-Fi Sharing.")
                GuideStepRow(step = "2", text = "On this Chromebook or second phone, tap 'Scan QR' or 'Auto-Find on Wi-Fi'.")
                GuideStepRow(step = "3", text = "Tap 'Connect & Sync Bookmarks' to pull and merge all bookmarks into your local offline storage.")
                GuideStepRow(step = "4", text = "100% private: All data stays directly between the two devices on your local router.")

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = appColors.separator
                )

                Text(
                    text = "Zero Cloud Accounts • 100% Offline Local Wi-Fi Peer Sync",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = appColors.secondaryLabel
                )
            }
        }
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

    var showSettingsInfoPage by remember { mutableStateOf(false) }

    if (showSettingsInfoPage) {
        PcCompanionSettingsInfoPage(
            onBack = { showSettingsInfoPage = false },
            onDismiss = onDismiss
        )
        return
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
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
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appColors.label,
                    letterSpacing = (-0.3).sp
                )
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showSettingsInfoPage = true
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Settings Info",
                        tint = appColors.secondaryLabel,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = appColors.secondaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

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

@Composable
fun PcCompanionSettingsInfoPage(
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
            title = "Settings Info",
            onBack = onBack,
            onDismiss = onDismiss,
            showClose = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Network Port
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
                                    imageVector = Icons.Outlined.Sensors,
                                    contentDescription = null,
                                    tint = appColors.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Network Port",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                    }
                    Text(
                        text = "The local TCP port where the WebStack embedded server listens. The default is 8080. You can change this to any port between 1024 and 65535 if 8080 conflicts with another service or is blocked by your firewall.",
                        fontSize = 13.sp,
                        color = appColors.secondaryLabel,
                        lineHeight = 18.sp
                    )
                }
            }

            // Card 2: Fixed vs Dynamic PIN
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
                            text = "Fixed vs Dynamic PIN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                    }
                    Text(
                        text = "Dynamic PIN creates a fresh random 6-digit code each time sharing is started for enhanced security. Fixed PIN preserves a chosen permanent 6-digit PIN across restarts, ideal for persistent paired computers, tablets, and Chromebooks.",
                        fontSize = 13.sp,
                        color = appColors.secondaryLabel,
                        lineHeight = 18.sp
                    )
                }
            }

            // Card 3: Offline Session Duration
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
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    tint = appColors.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Offline Session Duration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = appColors.label
                        )
                    }
                    Text(
                        text = "Determines how long a connected PC or browser can browse and queue bookmarks offline before re-authenticating with the host phone. When expired, the PC must re-verify credentials on the local network.",
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
