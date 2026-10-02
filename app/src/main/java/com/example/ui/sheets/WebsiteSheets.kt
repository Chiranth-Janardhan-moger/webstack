package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Website
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.DEFAULT_CATEGORIES
import com.example.ui.util.extractDomain
import com.example.ui.util.getClipboardUrl
import kotlinx.coroutines.delay

@Composable
fun AddWebsiteSheetContent(
    initialUrl: String = "",
    categories: List<String> = DEFAULT_CATEGORIES,
    isSaving: Boolean,
    saveError: String?,
    onAddNewTag: () -> Unit = {},
    onClearError: () -> Unit = {},
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    var inputUrl by remember(initialUrl) { mutableStateOf(initialUrl) }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "Personal") }

    // Dynamic, reactive clipboard URL detection
    var clipboardUrl by remember { mutableStateOf(getClipboardUrl(context)) }

    LaunchedEffect(Unit) {
        if (!clipboardUrl.isNullOrBlank()) return@LaunchedEffect
        for (d in longArrayOf(100L, 250L)) {
            delay(d)
            val url = getClipboardUrl(context)
            if (!url.isNullOrBlank()) {
                clipboardUrl = url
                break
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Save Website Link",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = appColors.label,
                letterSpacing = (-0.3).sp
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel and Close",
                    tint = appColors.secondaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Instant Clipboard Pill Button
        val activeClip = clipboardUrl
        if (!activeClip.isNullOrBlank() && inputUrl.isBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    inputUrl = activeClip
                    if (saveError != null) {
                        onClearError()
                    }
                },
                color = appColors.fill,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(0.75.dp, appColors.separator),
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste from Clipboard",
                        tint = appColors.accent,
                        modifier = Modifier.size(14.dp)
                    )
                    val displayUrl = if (activeClip.length > 28) activeClip.take(26) + "..." else activeClip
                    Text(
                        text = "Paste: $displayUrl",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.label
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TextInput
        OutlinedTextField(
            value = inputUrl,
            onValueChange = {
                inputUrl = it
                if (saveError != null) {
                    onClearError()
                }
            },
            placeholder = {
                Text(
                    text = "e.g., linear.app or https://github.com",
                    color = appColors.tertiaryLabel,
                    fontSize = 14.sp
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("url_text_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = appColors.surface,
                unfocusedContainerColor = appColors.surface,
                focusedBorderColor = appColors.accent,
                unfocusedBorderColor = appColors.separator,
                cursorColor = appColors.accent,
                focusedTextColor = appColors.label,
                unfocusedTextColor = appColors.label
            ),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (inputUrl.isNotBlank() && !isSaving) {
                        onSave(inputUrl, selectedCategory)
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Tag Selection
        Text(
            text = "Category Tag",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        CategorySelectionRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onSelectCategory = { selectedCategory = it },
            onAddNewTag = onAddNewTag
        )

        // Show fetching / error state
        if (isSaving) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = appColors.accent
                )
                Text(
                    text = "Resolving URL & capturing visuals...",
                    color = appColors.secondaryLabel,
                    fontSize = 12.sp
                )
            }
        }

        if (saveError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = saveError,
                color = appColors.destructive,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Save CTA button
        Button(
            onClick = { onSave(inputUrl, selectedCategory) },
            enabled = inputUrl.isNotBlank() && !isSaving,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.label,
                contentColor = appColors.systemBackground,
                disabledContainerColor = appColors.fill,
                disabledContentColor = appColors.tertiaryLabel
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_website_button")
        ) {
            Text(
                text = "Save Link",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

@Composable
fun EditWebsiteSheetContent(
    website: Website,
    categories: List<String>,
    onAddNewTag: () -> Unit,
    onSave: (Website) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    var title by remember { mutableStateOf(website.title) }
    var url by remember { mutableStateOf(website.url) }
    var selectedCategory by remember { mutableStateOf(website.category) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = appColors.label,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Edit Website Link",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = appColors.label,
                    letterSpacing = (-0.3).sp
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = appColors.secondaryLabel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title Field
        Text(
            text = "Title",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Website Title", color = appColors.tertiaryLabel, fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = appColors.surface,
                unfocusedContainerColor = appColors.surface,
                focusedBorderColor = appColors.accent,
                unfocusedBorderColor = appColors.separator,
                cursorColor = appColors.accent,
                focusedTextColor = appColors.label,
                unfocusedTextColor = appColors.label
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // URL Field
        Text(
            text = "URL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            placeholder = { Text("https://example.com", color = appColors.tertiaryLabel, fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = appColors.surface,
                unfocusedContainerColor = appColors.surface,
                focusedBorderColor = appColors.accent,
                unfocusedBorderColor = appColors.separator,
                cursorColor = appColors.accent,
                focusedTextColor = appColors.label,
                unfocusedTextColor = appColors.label
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Tag Selection
        Text(
            text = "Category Tag",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.secondaryLabel,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        CategorySelectionRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onSelectCategory = { selectedCategory = it },
            onAddNewTag = onAddNewTag
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Save Button
        Button(
            onClick = {
                var finalUrl = url.trim()
                if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                    finalUrl = "https://$finalUrl"
                }
                val domain = extractDomain(finalUrl)

                val updated = website.copy(
                    title = title.trim().ifBlank { website.title },
                    url = finalUrl,
                    domain = domain,
                    category = selectedCategory
                )
                onSave(updated)
            },
            enabled = title.isNotBlank() && url.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
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
                text = "Save Changes",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

@Composable
private fun CategorySelectionRow(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAddNewTag: () -> Unit
) {
    val appColors = LocalAppColors.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(categories) { cat ->
            val isSelected = selectedCategory.equals(cat, ignoreCase = true)
            Surface(
                onClick = { onSelectCategory(cat) },
                color = if (isSelected) appColors.label else appColors.surface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.75.dp, if (isSelected) Color.Transparent else appColors.separator)
            ) {
                Text(
                    text = cat,
                    color = if (isSelected) appColors.systemBackground else appColors.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        item {
            Surface(
                onClick = onAddNewTag,
                color = appColors.fill,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, appColors.separator)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Tag",
                        tint = appColors.label,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "New Tag",
                        color = appColors.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

