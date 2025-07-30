
import android.media.MediaPlayer
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixelpholio.MovingBackgroundImage
import com.example.pixelpholio.R
import com.example.pixelpholio.pixelFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 👇 StartScreen — Unchanged, for context
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
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PixelPholio",
                fontSize = 38.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontFamily = pixelFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "✨ Tap anywhere to begin",
                fontSize = 16.sp,
                color = Color.LightGray,
                fontFamily = pixelFontFamily
            )
        }
    }
}

// ✨👇 NEW AND IMPROVED MENU SCREEN 👇✨
@Composable
fun MenuScreen(
    onPlayClick: () -> Unit,
    onSkillClick: () -> Unit
) {
    val context = LocalContext.current

    // --- Sound Effect Management ---
    val clickSound = remember { MediaPlayer.create(context, R.raw.button_click) }
    DisposableEffect(Unit) {
        onDispose {
            clickSound?.release()
        }
    }

    // --- Entrance Animations ---
    val coroutineScope = rememberCoroutineScope()
    val buttonsOffsetY = remember { Animatable(100f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Staggered animation: fade in, then slide buttons up
        contentAlpha.animateTo(1f, animationSpec = tween(durationMillis = 300))
        buttonsOffsetY.animateTo(
            0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MovingBackgroundImage()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha.value) // Apply fade-in to all content
                .offset(y = buttonsOffsetY.value.dp), // Apply slide-up to all content
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Reusable, animated menu buttons
            MenuButton(
                text = " Play",
                onClick = {
                    coroutineScope.launch {
                        clickSound?.start()
                        delay(200) // Wait for sound to play
                        onPlayClick()
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            MenuButton(
                text = " Showcase My Skills",
                onClick = {
                    coroutineScope.launch {
                        clickSound?.start()
                        delay(200)
                        onSkillClick()
                    }
                }
            )
        }
    }
}

// ✨👇 REUSABLE, ANIMATED MENU BUTTON COMPOSABLE 👇✨

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // --- Animations for the button ---
    val scale = animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val animatedBorderColor by infiniteTransition.animateColor(
        initialValue = Color.White.copy(alpha = 0.5f),
        targetValue = Color.Cyan.copy(alpha = 0.8f),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowColor"
    )
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer"
    )

    // --- Button Layout ---
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(60.dp)
            .scale(scale.value), // Apply press-down scale effect
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp),
        border = BorderStroke(2.dp, animatedBorderColor) // Glowing border
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // 1. Brick Texture Background
            Image(
                painter = painterResource(R.drawable.brick_texture),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // ✅ 2. CORRECTED Shimmer Effect Overlay
            // The Brush is now created inside the drawBehind modifier,
            // which provides the correct `size`.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.0f),
                                Color.White.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.0f)
                            ),
                            startX = (shimmerTranslate * size.width) - size.width,
                            endX = (shimmerTranslate * size.width)
                        )
                        drawRect(brush = brush)
                    }
            )

            // 3. Button Text
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}



// 👇 This would be your implementation

