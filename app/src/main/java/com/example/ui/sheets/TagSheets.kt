package com.example.ui.sheets

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.util.getCategoryAccentColor

@Composable
fun TagOptionsBottomSheetContent(
    tagName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val appColors = LocalAppColors.current
    val catAccent = getCategoryAccentColor(tagName, appColors.isDark)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header: Tag Badge + Tag Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = catAccent.copy(alpha = if (appColors.isDark) 0.25f else 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Label,
                        contentDescription = null,
                        tint = catAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = tagName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = appColors.label
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Single Row, 2 Columns: Edit and Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                label = "Edit",
                icon = Icons.Outlined.Edit,
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                testTag = "action_edit_tag"
            )
            ActionTile(
                label = "Delete",
                icon = Icons.Outlined.Delete,
                onClick = onDelete,
                isDestructive = true,
                modifier = Modifier.weight(1f),
                testTag = "action_delete_tag"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
