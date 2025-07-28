package com.example.pixelpholio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

// =================================================================================
// DATA CLASSES AND ENUMS
// =================================================================================

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
    Q_MARK(5),
    pipe(6)
}

class Cloud(x: Float, val y: Float, val speed: Float) {
    var x by mutableStateOf(x)
}

// =================================================================================
// GAME CONSTANTS AND TILEMAP
// =================================================================================

val TILE_SIZE: Float = 64f
val PLAYER_START_X = 600f
val PLAYER_START_Y = 550f
const val INITIAL_TIME = 400 // Timer duration in seconds
val FINISH_LINE_X = 210 * TILE_SIZE // The X-coordinate to finish the level

val tileMap = mutableListOf(
    MutableList(225) { 0 }, // Row 0
    MutableList(225) { 0 }, // Row 1
    MutableList(225) { 0 }, // Row 2
    mutableListOf<Int>().apply { addAll(List(87) { 0 }); add(1); addAll(List(21) { 0 }); add(1); addAll(List(115) { 0 }) }, // Row 3
    MutableList(225) { 0 }, // Row 4
    MutableList(225) { 0 }, // Row 5
    MutableList(225) { 0 }, // Row 6
    mutableListOf<Int>().apply { addAll(List(30) { 0 }); add(5); addAll(List(3) { 0 }); add(1); addAll(List(3) { 0 }); add(5); addAll(List(3) { 0 }); add(1); addAll(List(3) { 0 }); add(5); addAll(List(36) { 0 }); addAll(List(3) { 1 }); addAll(List(4) { 0 }); addAll(List(4) { 1 }); addAll(List(10) { 0 }); addAll(List(8) { 1 }); addAll(List(100) { 0 }) }, // Row 7
    mutableListOf<Int>().apply { addAll(List(38) { 0 }); addAll(listOf(1, 1)); addAll(List(185) { 0 }) }, // Row 8
    mutableListOf<Int>().apply { addAll(List(38) { 0 }); addAll(listOf(1, 1)); addAll(List(9) { 0 }); addAll(listOf(1, 1)); addAll(List(10) { 0 }); addAll(listOf(1, 1)); addAll(List(6) { 0 }); addAll(listOf(1, 1)); addAll(List(34) { 0 }); addAll(List(4) { 1 }); addAll(List(1) { 0 }); addAll(List(4) { 1 }); addAll(List(101) { 0 }) }, // Row 9
    mutableListOf<Int>().apply { addAll(List(53) { 0 }); addAll(listOf(1, 1)); addAll(List(1) { 0 }); addAll(listOf(1, 1, 1)); addAll(List(1) { 0 }); addAll(listOf(1, 1, 1, 1)); addAll(List(82) { 0 }); addAll(listOf(1)); addAll(List(4) { 0 }); addAll(listOf(1, 1)); addAll(List(3) { 0 }); addAll(listOf(1, 1, 1)); addAll(List(2) { 0 }); addAll(listOf(1, 1, 1, 1)); addAll(List(55) { 0 }) }, // Row 10
    mutableListOf<Int>().apply { addAll(List(38) { 4 }); addAll(listOf(1, 1)); addAll(List(9) { 4 }); addAll(listOf(1, 1)); addAll(List(1) { 4 }); addAll(listOf(1, 1, 1)); addAll(List(1) { 4 }); addAll(listOf(1, 1, 1, 1)); addAll(List(6) { 4 }); addAll(listOf(1, 1)); addAll(List(14) { 4 }); addAll(List(2) { 0 }); addAll(List(15) { 4 }); addAll(listOf(1, 1)); addAll(List(1) { 4 }); addAll(listOf(1, 1, 1)); addAll(List(1) { 4 }); addAll(listOf(1, 1, 1, 1)); addAll(List(3) { 0 }); addAll(List(9) { 4 }); addAll(listOf(1)); addAll(List(4) { 4 }); addAll(listOf(1, 1)); addAll(List(3) { 4 }); addAll(listOf(1, 1, 1)); addAll(List(2) { 4 }); addAll(listOf(1, 1, 1, 1)); addAll(List(8) { 4 }); addAll(listOf(1)); addAll(List(49) { 0 }) }, // Row 11
    mutableListOf<Int>().apply { addAll(List(79) { 4 }); addAll(List(2) { 0 }); addAll(List(15) { 4 }); addAll(listOf(1, 1)); addAll(List(1) { 4 }); addAll(listOf(1, 1, 1)); addAll(List(1) { 4 }); addAll(listOf(1, 1, 1, 1)); addAll(List(3) { 0 }); addAll(List(9) { 4 }); addAll(listOf(1)); addAll(List(4) { 4 }); addAll(listOf(1, 1)); addAll(List(3) { 4 }); addAll(listOf(1, 1, 1)); addAll(List(2) { 4 }); addAll(listOf(1, 1, 1, 1)); addAll(List(8) { 4 }); addAll(listOf(1)); addAll(List(10) { 4 }); addAll(listOf(1, 1, 1, 1, 1, 1, 1, 1, 1)); addAll(List(28) { 0 }) }, // Row 12
    mutableListOf<Int>().apply { addAll(List(79) { 1 }); addAll(List(2) { 0 }); addAll(List(15) { 1 }); addAll(listOf(1, 1)); addAll(List(1) { 1 }); addAll(listOf(1, 1, 1)); addAll(List(1) { 1 }); addAll(listOf(1, 1, 1, 1)); addAll(List(3) { 0 }); addAll(List(9) { 1 }); addAll(listOf(1)); addAll(List(4) { 1 }); addAll(listOf(1, 1)); addAll(List(3) { 1 }); addAll(listOf(1, 1, 1)); addAll(List(2) { 1 }); addAll(listOf(1, 1, 1, 1)); addAll(List(8) { 1 }); addAll(listOf(1)); addAll(List(10) { 1 }); addAll(listOf(1, 1, 1, 1, 1, 1, 1, 1, 1)); addAll(List(28) { 0 }) }, // Row 13
    MutableList(225) { 1 }  // Row 14
)
val originalTileMap = tileMap.map { it.toMutableList() }

// =================================================================================
// HELPER FUNCTIONS & OBJECTS
// =================================================================================

fun isSolidTile(row: Int, col: Int): Boolean {
    val id = tileMap.getOrNull(row)?.getOrNull(col) ?: return false
    return id == TileType.PLATFORM.id ||
            id == TileType.Q_MARK.id ||
            id == TileType.FLOATING.id
}

// Placeholder for Sound Effects Manager
object SfxManager {
    fun play(context: Context, resourceId: Int) {
        // In a real app, you would use MediaPlayer or SoundPool here
    }
}

// =================================================================================
// MAIN GAME COMPOSABLE
// =================================================================================

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val goombaBitmap = remember { BitmapFactory.decodeResource(context.resources, R.drawable.goom) }
    val enemies = remember {
        mutableStateListOf(
            Enemy(x = 34 * TILE_SIZE, y = 10 * TILE_SIZE, originX = 34 * TILE_SIZE, patrolRange = 100f),
            Enemy(x = 74 * TILE_SIZE, y = 11 * TILE_SIZE, originX = 74 * TILE_SIZE, patrolRange = 120f),
            Enemy(x = 132 * TILE_SIZE, y = 11 * TILE_SIZE, originX = 132 * TILE_SIZE, patrolRange = 80f)
        )
    }

    var playerX by remember { mutableStateOf(PLAYER_START_X) }
    var playerY by remember { mutableStateOf(PLAYER_START_Y) }
    var jumpCount by remember { mutableStateOf(0) }
    val maxJumps = 3

    var isGameOver by remember { mutableStateOf(false) }
    var isGameFinished by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(INITIAL_TIME) }

    val isGameActive = !isGameOver && !isGameFinished

    val cloudPainter = painterResource(id = R.drawable.clouds1)
    val screenHeightPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    var velocityX by remember { mutableStateOf(0f) }
    val playerWidthPx = with(LocalDensity.current) { 42.dp.toPx() }
    val playerHeightPx = with(LocalDensity.current) { 42.dp.toPx() }
    var velocityY by remember { mutableStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }
    var joystickOffset by remember { mutableStateOf(Offset.Zero) }
    var facingLeft by remember { mutableStateOf(false) }

    val clouds = remember {
        mutableStateListOf(
            Cloud(screenWidthPx * 0.8f, 25f, 1f),
            Cloud(screenWidthPx * 1f, 60f, 0.8f),
            Cloud(screenWidthPx * 0.2f, 10f, 1f),
            Cloud(screenWidthPx * 0.4f, 20f, 1f)
        )
    }

    var playerLives by remember { mutableStateOf(3) }
    val allBadges = listOf("Firebase", "Compose", "Debugging")
    val collectedBadges = remember { mutableStateListOf<String>() }

    fun restartGame() {
        playerX = PLAYER_START_X
        playerY = PLAYER_START_Y
        velocityY = 0f
        velocityX = 0f
        isJumping = false
        jumpCount = 0
        playerLives = 3
        collectedBadges.clear()
        isGameOver = false
        isGameFinished = false
        timeLeft = INITIAL_TIME
        tileMap.clear()
        tileMap.addAll(originalTileMap.map { it.toMutableList() })
    }

    LaunchedEffect(isGameActive) {
        if (isGameActive) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            if (timeLeft == 0 && isGameActive) {
                isGameOver = true
            }
        }
    }

    LaunchedEffect(isGameActive) {
        if (isGameActive) {
            while (true) {
                withFrameNanos {
                    enemies.forEach { enemy ->
                        enemy.x += enemy.speed * enemy.direction
                        if (enemy.x <= enemy.originX - enemy.patrolRange || enemy.x >= enemy.originX + enemy.patrolRange) {
                            enemy.direction *= -1
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(isGameActive) {
        if (isGameActive) {
            while (isActive) {
                playerX += velocityX
                val playerLeftPx = playerX
                val playerRightPx = playerX + playerWidthPx
                var playerTopPx = playerY
                var playerBottomPx = playerY + playerHeightPx

                if (velocityX != 0f) {
                    val checkCol = if (velocityX > 0) (playerRightPx / TILE_SIZE).toInt() else (playerLeftPx / TILE_SIZE).toInt()
                    val topRow = (playerTopPx / TILE_SIZE).toInt()
                    val bottomRow = ((playerBottomPx - 1) / TILE_SIZE).toInt()
                    for (row in topRow..bottomRow) {
                        if (isSolidTile(row, checkCol)) {
                            playerX = if (velocityX > 0) checkCol * TILE_SIZE - playerWidthPx else (checkCol + 1) * TILE_SIZE
                            velocityX = 0f
                            break
                        }
                    }
                }

                velocityY += 2f
                val nextY = playerY + velocityY
                playerTopPx = nextY
                playerBottomPx = nextY + playerHeightPx
                val colLeft = (playerX / TILE_SIZE).toInt()
                val colRight = ((playerX + playerWidthPx - 1) / TILE_SIZE).toInt()
                var hasCollidedVertically = false

                if (velocityY > 0) {
                    val feetRow = (playerBottomPx / TILE_SIZE).toInt()
                    for (col in colLeft..colRight) {
                        if (isSolidTile(feetRow, col)) {
                            val tileTopY = feetRow * TILE_SIZE
                            if (playerY + playerHeightPx <= tileTopY) {
                                playerY = tileTopY - playerHeightPx
                                velocityY = 0f
                                isJumping = false
                                jumpCount = 0
                                hasCollidedVertically = true
                                break
                            }
                        }
                    }
                } else if (velocityY < 0) {
                    val headRow = (playerTopPx / TILE_SIZE).toInt()
                    for (col in colLeft..colRight) {
                        if (isSolidTile(headRow, col)) {
                            val tileBottomY = (headRow + 1) * TILE_SIZE
                            if (playerY >= tileBottomY) {
                                playerY = tileBottomY
                                velocityY = 1f
                                hasCollidedVertically = true
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
                }

                if (!hasCollidedVertically) {
                    playerY = nextY
                }

                if (playerX >= FINISH_LINE_X) {
                    SfxManager.play(context, R.raw.level_clear)
                    isGameFinished = true
                }

                if (playerY > screenHeightPx + playerHeightPx * 2) {
                    playerLives--
                    if (playerLives > 0) {
                        playerX = PLAYER_START_X
                        playerY = PLAYER_START_Y
                        velocityY = 0f
                        velocityX = 0f
                        delay(1000)
                    } else {
                        SfxManager.play(context, R.raw.death_sound)
                        isGameOver = true
                    }
                }

                delay(16L)
            }
        }
    }

    LaunchedEffect(isGameActive) {
        if (isGameActive) {
            while (isActive) {
                delay(16L)
                clouds.forEach { cloud ->
                    cloud.x -= cloud.speed
                    if (cloud.x + 160f < 0) cloud.x = screenWidthPx
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E90FF))) {
        if (isGameFinished) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.level_complete_background), // ACTION: Add this drawable
                    contentDescription = "Level Complete Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Level Complete!", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.Green, fontFamily = pixelFontFamily)
                    Text("Time Left: $timeLeft", fontSize = 24.sp, color = Color.White, modifier = Modifier.padding(top = 16.dp))
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { restartGame() }) { Text("Play Again", fontSize = 20.sp) }
                }
            }
        } else if (isGameOver) {
            Column(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Game Over", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { restartGame() }) { Text("Restart Game", fontSize = 20.sp) }
            }
        } else {
            val cameraOffsetX = playerX - screenWidthPx / 2
            Image(painter = painterResource(id = R.drawable.mountain), contentDescription = "Mountain", contentScale = ContentScale.FillWidth, modifier = Modifier.fillMaxWidth().height(200.dp).align(Alignment.BottomCenter))
            clouds.forEach { cloud -> Image(painter = cloudPainter, contentDescription = "Cloud", modifier = Modifier.offset { IntOffset(cloud.x.toInt(), cloud.y.toInt()) }.size(120.dp), alpha = 0.9f) }
            TileMapView(tileMap = tileMap, tileSize = TILE_SIZE.toInt(), cameraOffsetX = cameraOffsetX)
            enemies.forEach { enemy -> GoombaEnemy(enemy = enemy.copy(x = enemy.x - cameraOffsetX), bitmap = goombaBitmap) }
            PlayerSprite(playerState = PlayerState(x = playerX - cameraOffsetX, y = playerY, isMoving = joystickOffset.x != 0f, direction = if (facingLeft) Direction.LEFT else Direction.RIGHT))


            Joystick(
                onMove = { offset ->
                    joystickOffset = offset
                    velocityX = offset.x * 7f
                    if (offset.x != 0f) facingLeft = offset.x < 0
                },
                modifier = Modifier.align(Alignment.BottomStart)
            )

            Button(onClick = { restartGame() }, modifier = Modifier.align(Alignment.TopEnd).padding(top = 54.dp, end = 16.dp).size(80.dp), shape = RoundedCornerShape(40.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                Image(painter = painterResource(id = R.drawable.loop), contentDescription = "Restart", modifier = Modifier.fillMaxSize())
            }

            Button(onClick = {
                if (jumpCount < maxJumps) {
                    velocityY = -30f
                    isJumping = true
                    jumpCount++
                    SfxManager.play(context, R.raw.jump_sfx)
                }
            }, contentPadding = PaddingValues(0.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(72.dp)) {
                Image(painter = painterResource(id = R.drawable.jump_btn), contentDescription = "Jump", modifier = Modifier.fillMaxSize())
            }

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row {
                        repeat(playerLives) {
                            Icon(painter = painterResource(R.drawable.ic_heart), contentDescription = "Life", tint = Color.Red, modifier = Modifier.size(28.dp).padding(end = 4.dp))
                        }
                    }
                    Text("TIME\n$timeLeft", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Row {
                        allBadges.forEach { badge ->
                            Icon(painter = painterResource(if (collectedBadges.contains(badge)) R.drawable.ic_firebase else R.drawable.ic_firebase), contentDescription = badge, modifier = Modifier.size(28.dp).padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    }
}


// =================================================================================
// SUB-COMPONENTS
// =================================================================================

@Composable
fun TileMapView(tileMap: List<List<Int>>, tileSize: Int, cameraOffsetX: Float) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val startCol = (cameraOffsetX / tileSize).toInt()
    val endCol = ((cameraOffsetX + screenWidthPx) / tileSize).toInt() + 1
    val startRow = 0
    val endRow = (screenHeightPx / tileSize).toInt() + 1

    for (rowIndex in startRow..endRow.coerceAtMost(tileMap.size - 1)) {
        val row = tileMap[rowIndex]
        for (colIndex in startCol..endCol.coerceAtMost(row.size - 1)) {
            if (colIndex < 0) continue
            val tile = row[colIndex]
            if (tile == TileType.EMPTY.id) continue
            val tileRes = when (tile) {
                1 -> R.drawable.brick_texture
                2 -> R.drawable.spikes2
                3 -> R.drawable.mushy
                4 -> R.drawable.grass
                5 -> R.drawable.qmark
                6 -> R.drawable.s
                else -> null
            }
            tileRes?.let {
                Image(painter = painterResource(id = it), contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier.offset { IntOffset(((colIndex * tileSize) - cameraOffsetX).roundToInt(), rowIndex * tileSize) }.size(with(density) { tileSize.toDp() }))
            }
        }
    }
}

@Composable
fun GoombaEnemy(enemy: Enemy, bitmap: Bitmap) {
    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Goomba", modifier = Modifier.offset { IntOffset(enemy.x.toInt(), enemy.y.toInt()) }.size(64.dp))
}

@Composable
fun PlayerSprite(playerState: PlayerState) {
    val context = LocalContext.current
    val fullBitmap = remember { BitmapFactory.decodeResource(context.resources, R.drawable.sprite) }
    val frameCount = 4
    val frameWidth = fullBitmap.width / frameCount
    val frameHeight = fullBitmap.height
    val staticFrame = remember { Bitmap.createBitmap(fullBitmap, 0, 0, frameWidth, frameHeight) }
    val finalBitmap = remember(playerState.direction) {
        if (playerState.direction == Direction.LEFT) {
            val matrix = Matrix().apply { preScale(-1f, 1f) }
            Bitmap.createBitmap(staticFrame, 0, 0, staticFrame.width, staticFrame.height, matrix, true)
        } else staticFrame
    }
    Image(bitmap = finalBitmap.asImageBitmap(), contentDescription = "Player", modifier = Modifier.offset { IntOffset(playerState.x.toInt(), playerState.y.toInt()) }.size(68.dp))
}
