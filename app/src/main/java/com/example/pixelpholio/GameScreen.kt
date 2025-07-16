package com.example.pixelpholio
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.android.awaitFrame
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.android.awaitFrame
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.res.painterResource


@Composable
fun GameScreen() {
    val screenHeightPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    val playerHeight = 60f
    val groundY = screenHeightPx - playerHeight - 100f

    var playerX by remember { mutableStateOf(200f) }
    var playerY by remember { mutableStateOf(800f) }
    var velocityY by remember { mutableStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }
    var joystickOffset by remember { mutableStateOf(Offset.Zero) }

    // Gravity simulation
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                velocityY += 1.5f
                playerY += velocityY

                if (playerY >= groundY) {
                    playerY = groundY
                    velocityY = 0f
                    isJumping = false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ✅ Background added inside main Box
        ScrollingGameBackground()

        // 🎮 Player
        Image(
            painter = painterResource(id = R.drawable.player_idle),
            contentDescription = "Player Sprite",
            modifier = Modifier
                .offset { IntOffset(playerX.toInt(), playerY.toInt()) }
                .size(64.dp), // or adjust as needed
            contentScale = ContentScale.Fit
        )


        // 🕹️ Joystick
        Joystick(
            onMove = { offset ->
                joystickOffset = offset
                playerX += offset.x * 5f
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        )

        // ⬆️ Jump Button
        Button(
            onClick = {
                if (!isJumping) {
                    velocityY = -35f
                    isJumping = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp),
            shape = CircleShape
        ) {
            Text("🕊️")
        }
    }
}

@Composable
fun ScrollingGameBackground() {
    val image = painterResource(id = R.drawable.background)
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenWidthPx = with(LocalDensity.current) { screenWidth.dp.toPx() }

    var offsetX by remember { mutableStateOf(0f) }
    val scrollSpeed = 1.5f

    LaunchedEffect(Unit) {
        while (true) {
            awaitFrame()
            offsetX -= scrollSpeed
            if (offsetX <= -screenWidthPx) {
                offsetX = 0f
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.toInt(), 0) }
        )
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset((offsetX + screenWidthPx).toInt(), 0) }
        )
    }
}
