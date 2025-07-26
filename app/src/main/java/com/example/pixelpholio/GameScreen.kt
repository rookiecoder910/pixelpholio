
package com.example.pixelpholio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color


import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.pixelpholio.audio.SfxManager

data class Enemy(
    var x: Float,
    var y: Float,
    var width: Float = 64f,
    var height: Float = 64f,
    var direction: Float = 1f,
    var speed: Float = 2f,
    var originX: Float = x,
    val patrolRange: Float = 128f,
    var frame: Int = 0
)


// Platform data class
data class Platform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)
data class PlayerState(
    var x: Float = 100f,
    var y: Float = 100f,
    var isMoving: Boolean = false,
    var direction: Direction = Direction.RIGHT
)

enum class Direction {
    LEFT, RIGHT
}
enum class TileType(val id: Int) {
    EMPTY(0),
    PLATFORM(1),
    SPIKE(2),
    MUSHROOM_COLLECTIBLE(3),
    FLOATING(4),
    Q_MARK(5)
}
fun isSolidTile(row: Int, col: Int): Boolean {
    val id = tileMap.getOrNull(row)?.getOrNull(col) ?: return false
    return id == TileType.PLATFORM.id ||
            id == TileType.Q_MARK.id ||
            id == TileType.FLOATING.id  // ✅ make floating platform solid
}




@Composable
fun TileMapView(
    tileMap: List<List<Int>>,
    tileSize: Int,
    cameraOffsetX: Float
) {
    val density = LocalDensity.current
    val tileMapOriginCol = tileMap[0].size / 2

    tileMap.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { colIndex, tile ->
            val tileRes = when (tile) {
                1 -> R.drawable.platy_tex  // Your platform image
                2 -> R.drawable.spikes2     // Your spike image
                3 -> R.drawable.mushy
                4 -> R.drawable.grass
                5 -> R.drawable.qmark

                else -> null
            }

            tileRes?.let {
                val x = ((colIndex - tileMapOriginCol) * tileSize) - cameraOffsetX
                val y = rowIndex * tileSize

                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .offset {
                            IntOffset(x.roundToInt(), y)
                        }
                        .size(with(density) { tileSize.toDp() })
                )
            }
        }
    }
}
fun fixUnevenTileMap(tileMap: MutableList<MutableList<Int>>): MutableList<MutableList<Int>> {
    val maxCols = tileMap.maxOfOrNull { it.size } ?: 0
    tileMap.forEach { row ->
        while (row.size < maxCols) {
            row.add(0) // Pad with EMPTY tiles
        }
    }
    return tileMap
}



val tileMap = mutableListOf(
    // Row 0–4: Sky (empty)
    MutableList(37) { 0 },
    MutableList(37) { 0 },
    MutableList(37) { 0 },
    MutableList(37) { 0 },
    MutableList(37) { 0 },

    // Row 5: Floating Q-MARK block
    mutableListOf(0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),

    MutableList(37) { 0 },

    // Row 7: Platform line with Q block in middle
    mutableListOf(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),

    MutableList(37) { 0 },

    // Row 9: Lower platform
    mutableListOf(0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),

    MutableList(37) { 0 },

    // Row 11: Ground and floating platform
    mutableListOf(1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),

    MutableList(37) { 0 },
    MutableList(37) { 0 },

    // ✅ Row 14: Final base ground (very bottom for fallback safety)
    MutableList(37) { 4 }
)

val TILE_SIZE: Float=64f
val enemies = listOf(
    Enemy(x = 3 * TILE_SIZE, y = 2 * TILE_SIZE),
    Enemy(x = 8 * TILE_SIZE, y = 2 * TILE_SIZE),
    Enemy(x = 1 * TILE_SIZE, y = 4 * TILE_SIZE),
    Enemy(x = 2 * TILE_SIZE, y = 6 * TILE_SIZE),
    Enemy(x = 9 * TILE_SIZE, y = 6 * TILE_SIZE),
    Enemy(x = 6 * TILE_SIZE, y = 4 * TILE_SIZE)
)





// Cloud data class
class Cloud(
    x: Float,
    val y: Float,
    val speed: Float
) {
    var x by mutableStateOf(x)
}
val originalTileMap = tileMap.map { it.toMutableList() } // Add this outside GameScreen
@Composable
fun GoombaEnemy(enemy: Enemy, bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Goomba",
        modifier = Modifier
            .offset {
                IntOffset(
                    enemy.x.toInt(),
                    enemy.y.toInt()
                )
            }
            .size(64.dp)
    )
}


@Composable
fun GameScreen() {
    val context = LocalContext.current
    val goombaBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.goom)
    }


    var jumpCount by remember { mutableStateOf(0) }
    val maxJumps = 3

    // ✅ Game Over State
    var isGameOver by remember { mutableStateOf(false) }


    var playerX by remember { mutableStateOf(200f) }
    val cloudPainter = painterResource(id = R.drawable.clouds1)

    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val density = LocalDensity.current
    val screenHeightPx = with(density) { screenHeightDp.toPx() }
    val screenWidthPx = with(density) { screenWidthDp.toPx() }


    val playerWidthDp = 42.dp
    val playerHeightDp = 42.dp
    val platformHeightDp = 48.dp

    val playerWidthPx = with(density) { playerWidthDp.toPx() }
    val playerHeightPx = with(density) { playerHeightDp.toPx() }
    val platformHeightPx = with(density) { platformHeightDp.toPx() }

    var playerY by remember { mutableStateOf(screenHeightPx - platformHeightPx - playerHeightPx) }
    var velocityY by remember { mutableStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }
    var joystickOffset by remember { mutableStateOf(Offset.Zero) }
    var facingLeft by remember { mutableStateOf(false) }

    // ✅ Key the effect to !isGameOver to pause it
    LaunchedEffect(!isGameOver) {
        if (!isGameOver) {
            while (true) {
                withFrameNanos {
                    enemies.forEach { enemy ->
                        enemy.x += enemy.speed * enemy.direction

                        if (enemy.x <= enemy.originX - enemy.patrolRange ||
                            enemy.x >= enemy.originX + enemy.patrolRange
                        ) {
                            enemy.direction *= -1
                        }
                    }
                }
            }
        }
    }



    val clouds = remember {
        mutableStateListOf(
            Cloud(screenWidthPx * 0.8f, 25f, 1f),
            Cloud(screenWidthPx * 1f, 60f, 0.8f),
            Cloud(screenWidthPx * 0.8f, 25f, 1.2f),
            Cloud(screenWidthPx * 1f, 40f, 1.2f),
            Cloud(screenWidthPx * 0.2f, 10f, 1f),
            Cloud(screenWidthPx * 0.4f, 20f, 1f),
            Cloud(screenWidthPx * 0.6f, 50f, 1f)
        )
    }

    var playerLives by remember { mutableStateOf(3) }
    val allBadges = listOf("Firebase", "Compose", "Debugging")
    val collectedBadges = remember { mutableStateListOf<String>() }

    fun restartGame() {
        playerX = 200f
        playerY = screenHeightPx - platformHeightPx - playerHeightPx
        velocityY = 0f
        isJumping = false
        jumpCount = 0
        playerLives = 3
        collectedBadges.clear()
        isGameOver = false // ✅ Reset game over state

        // Optional: Reset tileMap if modified (e.g., Q_MARKs cleared)
        tileMap.clear()
        tileMap.addAll(originalTileMap.map { it.toMutableList() })
    }
    val enemies = remember {
        mutableStateListOf(
            Enemy(x = 800f, y = 700f, originX = 800f, patrolRange = 150f),
            Enemy(x = 1500f, y = 700f, originX = 1500f, patrolRange = 100f)
        )
    }

    // ✅ Key the effect to !isGameOver to pause it
    LaunchedEffect(!isGameOver) {
        if (!isGameOver) {
            while (isActive) {
                // Apply gravity
                velocityY += 2f
                playerY += velocityY

                // Get player bounding box
                val playerTop = playerY
                val playerBottom = playerY + playerHeightPx
                val playerLeft = playerX
                val playerRight = playerX + playerWidthPx
                val playerMidX = playerX + playerWidthPx / 2

                // Convert to tile indices
                val topRow = (playerTop / TILE_SIZE).toInt()
                val bottomRow = (playerBottom / TILE_SIZE).toInt()
                val colLeft = (playerLeft / TILE_SIZE).toInt()
                val colRight = ((playerRight - 1) / TILE_SIZE).toInt()
                val colCenter = (playerMidX / TILE_SIZE).toInt()

                var collided = false

                // ✅ 2. TILE-BASED VERTICAL COLLISION
                if (velocityY >= 0) {
                    // --- FALLING DOWN ---
                    val feetRow = ((playerY + playerHeightPx + velocityY) / TILE_SIZE).toInt()
                    for (col in colLeft..colRight) {
                        if (!isSolidTile(feetRow, col)) continue

                        val tileTopY = feetRow * TILE_SIZE
                        val nextBottom = playerY + playerHeightPx + velocityY

                        if (nextBottom >= tileTopY && playerY + playerHeightPx <= tileTopY + 20f) {
                            // Small margin (10f) ensures we only snap when "just about" to land
                            playerY = tileTopY - playerHeightPx
                            velocityY = 0f
                            isJumping = false
                            collided = true
                            jumpCount = 0
                            break
                        }
                    }
                } else {
                    // --- JUMPING UP ---
                    val headRow = ((playerY + velocityY) / TILE_SIZE).toInt()
                    for (col in colLeft..colRight) {
                        if (!isSolidTile(headRow, col)) continue

                        val tileBottomY = (headRow + 1) * TILE_SIZE
                        val nextTop = playerY + velocityY

                        if (nextTop <= tileBottomY && playerY >= tileBottomY - 10f) {
                            playerY = tileBottomY
                            velocityY = 12f  // bounce down
                            Log.d("Bounce", "Hit tile above at row=$headRow, col=$col")

                            if (tileMap[headRow][col] == TileType.Q_MARK.id) {
                                tileMap[headRow][col] = TileType.EMPTY.id
                                val spawnRow = headRow - 1
                                if (spawnRow >= 0 && tileMap[spawnRow][col] == TileType.EMPTY.id) {
                                    tileMap[spawnRow][col] = TileType.MUSHROOM_COLLECTIBLE.id
                                    SfxManager.play(context, R.raw.powerup)
                                }
                            }
                            break
                        }
                    }
                }


                // ✅ 3. FALL OFF SCREEN & GAME OVER LOGIC
                if (!collided && playerY > screenHeightPx + playerHeightPx) {
                    playerLives--
                    if (playerLives > 0) {
                        // Respawn if lives are left
                        playerX = 200f
                        playerY = screenHeightPx - platformHeightPx - playerHeightPx
                        velocityY = 0f
                        isJumping = false
                        delay(1000)
                    } else {
                        // ☠️ Game Over
                        // Ensure you have a 'death_sound.mp3' or similar in res/raw
                        SfxManager.play(context, R.raw.death_sound)
                        isGameOver = true
                    }
                }

                delay(16L)
            }
        }
    }


    // ✅ Key the effect to !isGameOver to pause it
    LaunchedEffect(!isGameOver) {
        if (!isGameOver) {
            while (isActive) {
                delay(16L)
                clouds.forEach { cloud ->
                    cloud.x -= cloud.speed
                    if (cloud.x + 160f < 0) {
                        cloud.x = screenWidthPx
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E90FF))
    ) {

        if (isGameOver) {
            // --- ☠️ GAME OVER UI ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Game Over",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { restartGame() }) {
                    Text("Restart Game", fontSize = 20.sp)
                }
            }
        }
        else {
            // --- 🎮 MAIN GAME UI ---
            Image(
                painter = painterResource(id = R.drawable.mountain),
                contentDescription = "Mountain",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.BottomCenter)
            )

            clouds.forEach { cloud ->
                Image(
                    painter = cloudPainter,
                    contentDescription = "Cloud",
                    modifier = Modifier
                        .offset { IntOffset(cloud.x.toInt(), cloud.y.toInt()) }
                        .size(120.dp),
                    alpha = 0.9f
                )
            }

            TileMapView(
                tileMap = tileMap,
                tileSize = TILE_SIZE.toInt(), // ✅ Match TILE_SIZE used in collision
                cameraOffsetX = playerX - screenWidthPx / 2
            )

            enemies.forEach { enemy ->
                GoombaEnemy(enemy, goombaBitmap)
            }

            PlayerSprite(
                playerState = PlayerState(
                    x = playerX,
                    y = playerY,
                    isMoving = joystickOffset.x != 0f,
                    direction = if (facingLeft) Direction.LEFT else Direction.RIGHT
                )
            )

            Joystick(
                onMove = { offset ->
                    joystickOffset = offset
                    playerX += offset.x * 5f
                    if (offset.x < 0) facingLeft = true else if (offset.x > 0) facingLeft = false
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            )
            Button(
                onClick = { restartGame() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 54.dp, end = 16.dp)
                    .size(80.dp), // adjust size for icon look
                shape = RoundedCornerShape(40.dp),
                contentPadding = PaddingValues(0.dp), // removes default padding
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent) // transparent if image has background
            ) {
                Image(
                    painter = painterResource(id = R.drawable.loop), // 🌀 your PNG resource
                    contentDescription = "Restart",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Button(
                onClick = {
                    if (jumpCount < maxJumps) {
                        velocityY = -30f
                        isJumping = true
                        jumpCount++
                        SfxManager.play(context, R.raw.jump_sfx)
                    }
                },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(72.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.jump_btn),
                    contentDescription = "Jump",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        repeat(playerLives) {
                            Icon(
                                painter = painterResource(R.drawable.ic_heart),
                                contentDescription = "Life",
                                tint = Color.Red,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                    }

                    Row {
                        allBadges.forEach { badge ->
                            Icon(
                                painter = painterResource(
                                    if (collectedBadges.contains(badge)) R.drawable.ic_firebase
                                    else R.drawable.ic_firebase
                                ),
                                contentDescription = badge,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(3000)
        collectedBadges.add("Firebase")
        playerLives
    }
}



@Composable
fun PlayerSprite(playerState: PlayerState) {
    val context = LocalContext.current
    val fullBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.sprite)
    }

    val frameCount = 4
    val frameWidth = fullBitmap.width / frameCount
    val frameHeight = fullBitmap.height

    // Always use the **first frame** (static)
    val staticFrame = remember {
        Bitmap.createBitmap(fullBitmap, 0, 0, frameWidth, frameHeight)
    }

    // Flip if facing left
    val finalBitmap = remember(playerState.direction) {
        if (playerState.direction == Direction.LEFT) {
            val matrix = Matrix().apply { preScale(-1f, 1f) }
            Bitmap.createBitmap(staticFrame, 0, 0, staticFrame.width, staticFrame.height, matrix, true)
        } else staticFrame
    }

    Image(
        bitmap = finalBitmap.asImageBitmap(),
        contentDescription = "Static Mario Frame",
        modifier = Modifier
            .offset {
                IntOffset(playerState.x.toInt(), playerState.y.toInt())
            }
            .size(68.dp) // Or match the frame size if exact pixel-fit is needed
    )
}
