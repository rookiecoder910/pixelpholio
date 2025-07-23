package com.example.pixelpholio

import android.media.MediaPlayer
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 👇 StartScreen — Tap to continue
@Composable
fun StartScreen(onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onTap() }
    ) {
        MovingBackgroundImage()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎮 Pixelpholio",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "✨ Tap anywhere to begin",
                fontSize = 16.sp,
                color = Color.LightGray
            )
        }
    }
}

// 👇 MenuScreen — Actual menu with buttons
@Composable
fun MenuScreen(
    onPlayClick: () -> Unit,
    onSkillClick: () -> Unit
) {
    val context = LocalContext.current


    val clickSound = remember {
        try {
            MediaPlayer.create(context, R.raw.button_click)
        } catch (e: Exception) {
            null
        }
    }


    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val animatedBorderColor by infiniteTransition.animateColor(
        initialValue = Color.Black,
        targetValue = Color.White,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowColor"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MovingBackgroundImage()
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ▶ Play Button with fitted brick background
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp)) // Match the button shape
            ) {
                Image(
                    painter = painterResource(R.drawable.brick_texture),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds, // Stretch to fit
                    modifier = Modifier.matchParentSize()
                )

                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.matchParentSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Cyan
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(
                        text = "▶Play",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🧠 Showcase My Skills Button
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.40f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.brick_texture),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize()
                )

                Button(
                    onClick = onSkillClick,
                    modifier = Modifier.matchParentSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Cyan
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(
                        text = "Showcase My Skills",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

    }
}




