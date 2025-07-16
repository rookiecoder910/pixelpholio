package com.example.pixelpholio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
fun Offset.coerceInCircle(center: Offset, maxDistance: Float): Offset {
    val dx = x - center.x
    val dy = y - center.y
    val distance = sqrt(dx * dx + dy * dy)
    return if (distance <= maxDistance) this
    else center + Offset(dx, dy) * (maxDistance / distance)
}
@Composable
fun Joystick(
    onMove: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val radius = 120f
    val handleRadius = 40f
    var center by remember { mutableStateOf(Offset.Zero) }
    var handlePosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(160.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        center = offset
                    },
                    onDrag = { change, dragAmount ->
                        val newOffset = handlePosition + dragAmount
                        val limited = newOffset.coerceInCircle(center, radius - handleRadius)
                        handlePosition = limited
                        val normalized = Offset(
                            x = (limited.x - center.x) / (radius - handleRadius),
                            y = (limited.y - center.y) / (radius - handleRadius)
                        )
                        onMove(normalized)
                    },
                    onDragEnd = {
                        handlePosition = center
                        onMove(Offset.Zero)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            center = size.center
            drawCircle(Color.DarkGray, radius)
            drawCircle(Color.LightGray, radius / 2)
            drawCircle(Color.Cyan, handleRadius, center = handlePosition.takeIf { it != Offset.Zero } ?: center)
        }
    }
}
