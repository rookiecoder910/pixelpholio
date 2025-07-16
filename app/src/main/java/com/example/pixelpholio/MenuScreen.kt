package com.example.pixelpholio
import kotlinx.coroutines.launch

import android.media.MediaPlayer
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(
    onPlayClick: () -> Unit,
    onSkillClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        MovingBackgroundImage()

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
            initialValue = Color.Cyan,
            targetValue = Color.Magenta,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "button border glow"
        )

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
                color = Color.Cyan
            )

            Spacer(modifier = Modifier.height(40.dp))

            BouncyButton(
                onClick = {
                    clickSound?.start()
                    onPlayClick()
                },
                borderColor = animatedBorderColor
            ) {
                Text(
                    text = "▶️ Play",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = animatedBorderColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            BouncyButton(
                onClick = {
                    clickSound?.start()
                    onSkillClick()
                },
                borderColor = animatedBorderColor
            ) {
                Text(
                    text = "🧠 Skill Showcase",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = animatedBorderColor
                )
            }
        }
    }
}
@Composable
fun BouncyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Cyan,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            coroutineScope.launch {
                scale.animateTo(
                    0.85f,
                    animationSpec = tween(durationMillis = 100)
                )
                scale.animateTo(
                    1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            onClick()
        },
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .fillMaxWidth(0.6f)
            .height(52.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = borderColor
        ),
        border = BorderStroke(2.dp, borderColor)
    ) {
        content()
    }
}
