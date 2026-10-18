package com.example.ui.sheets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppSubSheetHeader
import com.example.ui.model.AppFabShape
import com.example.ui.theme.LocalAppColors

@Composable
fun FabShapePickerSubSheetContent(
    selectedShape: AppFabShape,
    onSelectShape: (AppFabShape) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AppSubSheetHeader(
            title = "Button Shape",
            onBack = onBack,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(14.dp))

        // All Shapes (1st Circle, 2nd Square, followed by Expressive Shapes)
        val allShapes = remember { AppFabShape.entries }
        ShapeGridSection(
            title = "EXPRESSIVE SHAPES",
            shapes = allShapes,
            selectedShape = selectedShape,
            onSelectShape = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSelectShape(it)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        val doneInteractionSource = remember { MutableInteractionSource() }
        val isDonePressed by doneInteractionSource.collectIsPressedAsState()
        val doneScale by animateFloatAsState(
            targetValue = if (isDonePressed) 0.94f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "done_bouncy_shape"
        )

        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onBack()
            },
            interactionSource = doneInteractionSource,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.label,
                contentColor = appColors.systemBackground
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .graphicsLayer {
                    scaleX = doneScale
                    scaleY = doneScale
                }
        ) {
            Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ShapeGridSection(
    title: String,
    shapes: List<AppFabShape>,
    selectedShape: AppFabShape,
    onSelectShape: (AppFabShape) -> Unit
) {
    val appColors = LocalAppColors.current

    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = appColors.secondaryLabel,
        letterSpacing = 1.3.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    val shapeRows = remember(shapes) { shapes.chunked(3) }
    shapeRows.forEachIndexed { rowIndex, rowShapes ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowShapes.forEach { shape ->
                val isSelected = selectedShape == shape
                val interactionSource = remember(shape) { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "shape_bouncy_${shape.id}"
                )

                Surface(
                    onClick = { onSelectShape(shape) },
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) appColors.surface else appColors.secondaryGroupedBackground,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 0.75.dp,
                        color = if (isSelected) appColors.label else appColors.separator
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    color = if (appColors.isDark) Color(0xFF141416) else Color(0xFFF7FAFE),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val itemShape = shape.toComposeShape()
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        color = if (isSelected) appColors.label else appColors.label.copy(alpha = 0.72f),
                                        shape = itemShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (isSelected) appColors.systemBackground else appColors.systemBackground.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = shape.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) appColors.label else appColors.secondaryLabel,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (rowShapes.size < 3) {
                repeat(3 - rowShapes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (rowIndex < shapeRows.lastIndex) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
