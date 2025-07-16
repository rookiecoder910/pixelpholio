package com.example.pixelpholio
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.layout.ContentScale
import kotlin.math.floor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.android.awaitFrame



@Composable
fun MovingBackgroundImage() {
    val background = painterResource(id = R.drawable.background)
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.toFloat()

    var offsetX by remember { mutableStateOf(0f) }
    val speed = 2f

    LaunchedEffect(Unit) {
        while (true) {
            awaitFrame()
            offsetX -= speed
            if (offsetX <= -screenWidthDp) {
                offsetX += screenWidthDp // smoother reset
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // First image
        Image(
            painter = background,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxHeight()
                .width(screenWidthDp.dp)
                .offset(x = offsetX.dp)
        )

        // Second image (slightly overlapped to remove flicker)
        Image(
            painter = background,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxHeight()
                .width(screenWidthDp.dp)
                .offset(x = (offsetX + screenWidthDp - 1f).dp) // ← minus 1dp overlap
        )
    }
}
