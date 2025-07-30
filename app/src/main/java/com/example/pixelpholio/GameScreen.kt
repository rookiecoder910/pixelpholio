package com.example.pixelpholio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixelpholio.audio.SfxManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt
import com.example.pixelpholio.R


data class Enemy(
    val x: Float,
    val y: Float,
    val width: Float = 64f,
    val height: Float = 64f,
    val direction: Float = 1f,
    val speed: Float = 2f,
    val originX: Float = x,
    val patrolRange: Float = 128f,
    val frame: Int = 0,
    val isAlive: Boolean = true
)

data class Mushroom(
    val x: Float,
    val y: Float,
    val velocityX: Float = 2f,
    val velocityY: Float = 0f,
    val width: Float = 24f,
    val height: Float = 24f,
    val isActive: Boolean = true
)

data class PlayerState(
    var x: Float = 100f,
    var y: Float = 100f,
    var isMoving: Boolean = false,
    var direction: Direction = Direction.RIGHT,
    var isSuper: Boolean = false,
    var currentFrame: Int = 0
)

data class ScoreIndicator(
    val text: String,
    var x: Float,
    var y: Float,
    var lifetime: Int = 60
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
    pipe(6),
    castle(7),
    coin(8)
}

class Cloud(x: Float, val y: Float, val speed: Float) {
    var x by mutableStateOf(x)
}

// =================================================================================
// GAME CONSTANTS AND DATA
// =================================================================================

val TILE_SIZE: Float = 64f
val GRAVITY = 2f
val PLAYER_START_X = 600f
val PLAYER_START_Y = 630f
const val INITIAL_TIME = 400
val FINISH_LINE_X = 210 * TILE_SIZE

val tileMap = mutableListOf(
    MutableList(225) { 0 }, // Row 0
    MutableList(225) { 0 }, // Row 1
    MutableList(225) { 0 }, // Row 2
    MutableList(225) { 0 }.apply { this[87] = 1; this[109] = 1 }, // Row 3
    MutableList(225) { 0 }, // Row 4
    MutableList(225) { 0 }, // Row 5
    MutableList(225) { 0 }.apply { this[23] = 8; this[25] = 8 }, // Row 6
    MutableList(225) { 0 }.apply { this[22] = 5; this[24] = 5; this[26] = 5; this[30] = 1; this[34] = 5; this[38] = 1; this[42] = 5; for(i in 79..81) this[i] = 1; for(i in 86..89) this[i] = 1; for(i in 100..107) this[i] = 1 }, // Row 7
    MutableList(225) { 0 }.apply { this[38] = 1; this[39] = 1 }, // Row 8
    MutableList(225) { 0 }.apply { this[38] = 1; this[39] = 1; this[49] = 5; this[52] = 1; this[53] = 1; this[64] = 1; this[65] = 1; this[72] = 5; this[75] = 1; this[76] = 1; for(i in 111..114) this[i] = 1; for(i in 116..119) this[i] = 1 }, // Row 9
    MutableList(225) { 0 }.apply { this[53] = 1; this[54] = 1; this[56] = 1; this[57] = 1; this[58] = 1; this[60] = 1; this[61] = 1; this[62] = 1; this[63] = 1; this[84] = 5; this[86] = 5; this[145] = 1; for(i in 150..151) this[i] = 1; for(i in 155..157) this[i] = 1; for(i in 160..163) this[i] = 1 }, // Row 10
    MutableList(225) { 4 }.apply { for(i in 0..37) this[i] = 1; this[38] = 1; this[39] = 1; for(i in 49..57) this[i] = 1; this[58] = 1; this[59] = 1; this[61] = 1; this[62] = 1; this[63] = 1; this[65] = 1; for(i in 66..70) this[i] = 1; this[71] = 1; this[72] = 1; for(i in 87..100) this[i] = 1; for(i in 103..118) this[i] = 1; this[119] = 1; this[120] = 1; this[122] = 1; for(i in 123..125) this[i] = 1; this[127] = 1; for(i in 128..131) this[i] = 1; for(i in 135..143) this[i] = 1; this[144] = 1; for(i in 149..152) this[i] = 1; for(i in 156..158) this[i] = 1; for(i in 161..164) this[i] = 1; for(i in 173..180) this[i] = 1; this[181] = 1; for(i in 182..224) this[i] = 0 }, // Row 11
    MutableList(225) { 4 }.apply { for(i in 0..78) this[i] = 1; for(i in 81..96) this[i] = 1; this[97] = 1; this[98] = 1; this[100] = 1; for(i in 101..103) this[i] = 1; this[105] = 1; for(i in 106..109) this[i] = 1; for(i in 113..121) this[i] = 1; this[122] = 1; for(i in 127..130) this[i] = 1; for(i in 134..136) this[i] = 1; for(i in 139..142) this[i] = 1; for(i in 151..159) this[i] = 1; for(i in 160..168) this[i] = 1; for(i in 169..224) this[i] = 0 }, // Row 12
    MutableList(225) { 1 }.apply { for(i in 81..96) this[i] = 1; this[97] = 1; this[98] = 1; this[100] = 1; for(i in 101..103) this[i] = 1; this[105] = 1; for(i in 106..109) this[i] = 1; for(i in 113..121) this[i] = 1; this[122] = 1; for(i in 127..130) this[i] = 1; for(i in 134..136) this[i] = 1; for(i in 139..142) this[i] = 1; for(i in 151..159) this[i] = 1; for(i in 160..168) this[i] = 1; for(i in 169..224) this[i] = 0 }, // Row 13
    MutableList(225) { 1 }  // Row 14
).also {
    // Add Castle at the end of the level


//    it[13][211] = 7;

    // Add Coins
    it[10][30] = 8; it[10][31] = 8; it[10][32] = 8;
    it[9][65] = 8; it[9][66] = 8; it[9][67] = 8; it[9][68] = 8;
    it[6][100] = 8; it[6][101] = 8; it[6][102] = 8;
}
val originalTileMap = tileMap.map { it.toMutableList() }

val skillsToAcquire = listOf(
    SkillBadge("Firebase", R.drawable.ic_firebase, "Integrated  authentication, real-time database, Firestore in my emedibot app"),
    SkillBadge("Jetpack Compose", R.drawable.ic_compose, "Used to build the entire UI declaratively, from game elements to menus using canvas and compose"),
    SkillBadge("2D Game Development", R.drawable.ic_gamedev, "Created physics, collision detection, and character state management from scratch.")
)


fun isSolidTile(row: Int, col: Int): Boolean {
    val id = tileMap.getOrNull(row)?.getOrNull(col) ?: return false
    // Pipes are now solid
    return id == TileType.PLATFORM.id || id == TileType.Q_MARK.id || id == TileType.FLOATING.id || id == TileType.pipe.id
}



@Composable
fun GameScreen() {
    val context = LocalContext.current
    val goombaBitmap = remember { BitmapFactory.decodeResource(context.resources, R.drawable.goom) }
    val squishedGoombaBitmap = remember { BitmapFactory.decodeResource(context.resources, R.drawable.goom_squished) }
    val mushroomBitmap = remember { BitmapFactory.decodeResource(context.resources, R.drawable.mushy).asImageBitmap() }

    // Game state
    var playerX by remember { mutableStateOf(PLAYER_START_X) }
    var playerY by remember { mutableStateOf(PLAYER_START_Y) }
    var jumpCount by remember { mutableStateOf(0) }
    val maxJumps = 3
    var isSuper by remember { mutableStateOf(false) }
    var isInvincible by remember { mutableStateOf(false) }
    var lastSafeX by remember { mutableStateOf(PLAYER_START_X) }
    var lastSafeY by remember { mutableStateOf(PLAYER_START_Y) }
    var isGameOver by remember { mutableStateOf(false) }
    var isGameFinished by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(INITIAL_TIME) }
    var playerLives by remember { mutableStateOf(3) }
    var coinCount by remember { mutableStateOf(0) } // Coin counter state

    // State for pausing and skill dialog
    var isGamePaused by remember { mutableStateOf(false) }
    var skillToShow by remember { mutableStateOf<SkillBadge?>(null) }
    val collectedBadges = remember { mutableStateListOf<String>() }

    // Entity lists
    val enemies = remember { mutableStateListOf<Enemy>() }
    val mushrooms = remember { mutableStateListOf<Mushroom>() }
    val scoreIndicators = remember { mutableStateListOf<ScoreIndicator>() }

    // Physics and animation
    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }
    var joystickOffset by remember { mutableStateOf(Offset.Zero) }
    var facingLeft by remember { mutableStateOf(false) }
    var playerFrame by remember { mutableStateOf(0) }
    var animationCounter by remember { mutableStateOf(0) }

    // Screen and environment
    val density = LocalDensity.current
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val clouds = remember {
        mutableStateListOf(
            Cloud(screenWidthPx * 0.8f, 25f, 1f), Cloud(screenWidthPx * 1f, 60f, 0.8f),
            Cloud(screenWidthPx * 0.2f, 10f, 1f), Cloud(screenWidthPx * 0.4f, 20f, 1f)
        )
    }

    // Player hitbox
    val playerWidthPx by remember(isSuper) { derivedStateOf { with(density) { (if (isSuper) 40.dp else 32.dp).toPx() } } }
    val playerHeightPx by remember(isSuper) { derivedStateOf { with(density) { (if (isSuper) 64.dp else 32.dp).toPx() } } }

    val isGameActive = !isGameOver && !isGameFinished

    fun populateEnemies() {
        enemies.clear()
        val enemyY = 9 * TILE_SIZE
        enemies.addAll(listOf(
            Enemy(x = 20 * TILE_SIZE, y = enemyY, originX = 20 * TILE_SIZE, patrolRange = 150f),
            Enemy(x = 34 * TILE_SIZE, y = enemyY, originX = 34 * TILE_SIZE, patrolRange = 100f),
            Enemy(x = 50 * TILE_SIZE, y = enemyY, originX = 50 * TILE_SIZE, patrolRange = 120f),
            Enemy(x = 74 * TILE_SIZE, y = enemyY, originX = 74 * TILE_SIZE, patrolRange = 120f),
            Enemy(x = 90 * TILE_SIZE, y = enemyY, originX = 90 * TILE_SIZE, patrolRange = 100f),
            Enemy(x = 110 * TILE_SIZE, y = enemyY, originX = 110 * TILE_SIZE, patrolRange = 150f)

        ))
    }

    fun restartGame() {
        playerX = PLAYER_START_X; playerY = PLAYER_START_Y
        lastSafeX = PLAYER_START_X; lastSafeY = PLAYER_START_Y
        velocityY = 0f; velocityX = 0f
        isJumping = false; jumpCount = 0; playerLives = 3
        isSuper = false; isInvincible = false
        isGameOver = false; isGameFinished = false; isGamePaused = false
        timeLeft = INITIAL_TIME
        skillToShow = null
        collectedBadges.clear()
        scoreIndicators.clear()
        mushrooms.clear()
        coinCount = 0 // Reset coins
        populateEnemies()
        tileMap.clear()
        tileMap.addAll(originalTileMap.map { it.toMutableList() })
    }

    LaunchedEffect(Unit) { populateEnemies() }

    suspend fun handlePlayerDeath() {
        if (isInvincible) return

        if (isSuper) {
            val superHeightPx = with(density) { 64.dp.toPx() }
            val normalHeightPx = with(density) { 32.dp.toPx() }
            val heightDifference = superHeightPx - normalHeightPx
            playerY += heightDifference // Move player down so feet stay on the ground

            isSuper = false
            isInvincible = true
            SfxManager.play(context, R.raw.power_down)
            delay(2500)
            isInvincible = false
        } else {
            playerLives--
            if (playerLives > 0) {
                SfxManager.play(context, R.raw.mario_lose)
                playerX = lastSafeX
                playerY = lastSafeY
                velocityY = 0f
                velocityX = 0f

                isInvincible = true
                delay(2500)
                isInvincible = false
            } else {
                SfxManager.play(context, R.raw.death_sound)
                isGameOver = true
            }
        }
    }

    LaunchedEffect(isGameActive, isGamePaused) {
        if (isGameActive && !isGamePaused) {
            while (timeLeft > 0) {
                delay(1000L)
                if (!isGamePaused) timeLeft--
                else break
            }
            if (timeLeft == 0 ) {
                isGameOver = true
            }
        }
    }

    LaunchedEffect(isGameActive, isGamePaused) {
        if (isGameActive) {
            while (isActive) {
                if (!isGamePaused) {
                    // Player horizontal movement
                    playerX += velocityX
                    if (velocityX != 0f) {
                        animationCounter++; if (animationCounter > 5) { playerFrame = if (playerFrame >= 3) 1 else playerFrame + 1; animationCounter = 0 }
                    } else { playerFrame = 0 }

                    // Enemy movement
                    enemies.forEachIndexed { index, enemy ->
                        if (enemy.isAlive) {
                            val newX = enemy.x + enemy.speed * enemy.direction
                            var newDirection = enemy.direction
                            if (newX <= enemy.originX - enemy.patrolRange || newX >= enemy.originX + enemy.patrolRange) {
                                newDirection *= -1
                            }
                            enemies[index] = enemy.copy(x = newX, direction = newDirection)
                        }
                    }

                    // Cloud & Score Indicator movement
                    clouds.forEach { cloud -> cloud.x -= cloud.speed; if (cloud.x + 160f < 0) cloud.x = screenWidthPx }
                    scoreIndicators.forEach { it.y -= 2f; it.lifetime-- }; scoreIndicators.removeAll { it.lifetime <= 0 }

                    //  Player Horizontal Collision
                    val playerLeftPx = playerX; val playerRightPx = playerX + playerWidthPx
                    if (velocityX != 0f) {
                        val checkCol = if (velocityX > 0) (playerRightPx / TILE_SIZE).toInt() else (playerLeftPx / TILE_SIZE).toInt()
                        val topRow = (playerY / TILE_SIZE).toInt(); val bottomRow = ((playerY + playerHeightPx - 1) / TILE_SIZE).toInt()
                        for (row in topRow..bottomRow) {
                            if (isSolidTile(row, checkCol)) {
                                playerX = if (velocityX > 0) checkCol * TILE_SIZE - playerWidthPx else (checkCol + 1) * TILE_SIZE
                                velocityX = 0f; break
                            }
                        }
                    }

                    //  Player Vertical Collision & Gravity
                    val prevVelocityY = velocityY
                    velocityY += GRAVITY
                    val nextY = playerY + velocityY
                    val colLeft = (playerX / TILE_SIZE).toInt(); val colRight = ((playerX + playerWidthPx - 1) / TILE_SIZE).toInt()
                    var hasCollidedVertically = false

                    if (velocityY > 0) { // Moving Down
                        val feetRow = ((nextY + playerHeightPx) / TILE_SIZE).toInt()
                        for (col in colLeft..colRight) {
                            if (isSolidTile(feetRow, col)) {
                                playerY = feetRow * TILE_SIZE - playerHeightPx; velocityY = 0f
                                isJumping = false; jumpCount = 0; hasCollidedVertically = true
                                lastSafeX = playerX; lastSafeY = playerY; break
                            }
                        }
                    } else if (velocityY < 0) { // Moving Up
                        val headRow = (nextY / TILE_SIZE).toInt()
                        for (col in colLeft..colRight) {
                            if (isSolidTile(headRow, col)) {
                                playerY = (headRow + 1) * TILE_SIZE; velocityY = 1f; hasCollidedVertically = true
                                if (tileMap.getOrNull(headRow)?.getOrNull(col) == TileType.Q_MARK.id) {
                                    tileMap[headRow][col] = TileType.EMPTY.id
                                    if (collectedBadges.size < skillsToAcquire.size) {
                                        val mushroomX = col * TILE_SIZE
                                        val mushroomY = (headRow - 1) * TILE_SIZE
                                        mushrooms.add(Mushroom(x = mushroomX, y = mushroomY))
                                        SfxManager.play(context, R.raw.powerup)
                                    }
                                }
                                break
                            }
                        }
                    }
                    if (!hasCollidedVertically) playerY = nextY

                    // Coin Collision
                    val playerCol = (playerX / TILE_SIZE).toInt()
                    val playerRow = (playerY / TILE_SIZE).toInt()
                    for (row in playerRow..(playerRow + 1).coerceAtMost(tileMap.size - 1)) {
                        for (col in playerCol..(playerCol + 1).coerceAtMost(tileMap.getOrElse(row) { emptyList() }.size - 1)) {
                            if (tileMap[row][col] == TileType.coin.id) {
                                tileMap[row][col] = TileType.EMPTY.id
                                coinCount++
                                SfxManager.play(context, R.raw.collect)
                            }
                        }
                    }

                    // Mushroom physics
                    mushrooms.forEachIndexed { index, mushroom ->
                        if (mushroom.isActive) {
                            var newVelocityY = mushroom.velocityY + GRAVITY
                            var newY = mushroom.y + newVelocityY
                            var newX = mushroom.x + mushroom.velocityX
                            var newVelocityX = mushroom.velocityX

                            // Vertical Collision
                            val mushFeetRow = ((newY + mushroom.height) / TILE_SIZE).toInt()
                            val mushColLeft = (newX / TILE_SIZE).toInt()
                            val mushColRight = ((newX + mushroom.width - 1) / TILE_SIZE).toInt()
                            for (col in mushColLeft..mushColRight) {
                                if (isSolidTile(mushFeetRow, col)) {
                                    newY = mushFeetRow * TILE_SIZE - mushroom.height
                                    newVelocityY = 0f
                                    break
                                }
                            }

                            // Horizontal Collision
                            val checkMushCol = if (newVelocityX > 0) ((newX + mushroom.width) / TILE_SIZE).toInt() else (newX / TILE_SIZE).toInt()
                            val mushRowTop = (newY / TILE_SIZE).toInt()
                            val mushRowBottom = ((newY + mushroom.height - 1) / TILE_SIZE).toInt()
                            for (row in mushRowTop..mushRowBottom) {
                                if (isSolidTile(row, checkMushCol)) {
                                    newX = if (newVelocityX > 0) (checkMushCol * TILE_SIZE) - mushroom.width else (checkMushCol + 1) * TILE_SIZE
                                    newVelocityX *= -1
                                    break
                                }
                            }
                            mushrooms[index] = mushroom.copy(x = newX, y = newY, velocityX = newVelocityX, velocityY = newVelocityY)
                        }
                    }

                    // Player-Mushroom Collision
                    val mushroomIterator = mushrooms.iterator()
                    while (mushroomIterator.hasNext()) {
                        val mushroom = mushroomIterator.next()
                        if (mushroom.isActive && playerX < mushroom.x + mushroom.width && playerX + playerWidthPx > mushroom.x &&
                            playerY < mushroom.y + mushroom.height && playerY + playerHeightPx > mushroom.y) {

                            if (!isSuper) {
                                val normalHeightPx = with(density) { 32.dp.toPx() }
                                val superHeightPx = with(density) { 64.dp.toPx() }
                                val heightDifference = superHeightPx - normalHeightPx
                                playerY -= heightDifference
                                isSuper = true
                            }

                            val nextSkillIndex = collectedBadges.size
                            if (nextSkillIndex < skillsToAcquire.size) {
                                skillToShow = skillsToAcquire[nextSkillIndex]
                                collectedBadges.add(skillsToAcquire[nextSkillIndex].name)
                                isGamePaused = true
                            }

                            SfxManager.play(context, R.raw.powerup)
                            mushroomIterator.remove()
                        }
                    }

                    // Player-Enemy Collision
                    enemies.forEachIndexed { index, enemy ->
                        if (enemy.isAlive) {
                            if (playerX < enemy.x + enemy.width && playerX + playerWidthPx > enemy.x &&
                                playerY < enemy.y + enemy.height && playerY + playerHeightPx > enemy.y) {

                                if (prevVelocityY > 0 && (playerY + playerHeightPx) < (enemy.y + enemy.height / 2)) {
                                    enemies[index] = enemy.copy(isAlive = false)
                                    SfxManager.play(context, R.raw.enemy_stomp)
                                    velocityY = -20f
                                    scoreIndicators.add(ScoreIndicator("+100", enemy.x, enemy.y))
                                } else {
                                    handlePlayerDeath()
                                }
                            }
                        }
                    }

                    if (playerX >= FINISH_LINE_X) { SfxManager.play(context, R.raw.level_clear); isGameFinished = true }
                    if (playerY > screenHeightPx + playerHeightPx * 2) { handlePlayerDeath() }

                }
                delay(16L)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E90FF))) {
        if (isGameFinished) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(painter = painterResource(id = R.drawable.level_complete_background), contentDescription = "Level Complete Background", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Level Complete!", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                    Text("Time Left: $timeLeft", fontSize = 24.sp, color = Color.White, modifier = Modifier.padding(top = 16.dp))
                    Text("Coins: $coinCount", fontSize = 24.sp, color = Color.White)
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
            clouds.forEach { cloud -> Image(painter = painterResource(id = R.drawable.clouds1), contentDescription = "Cloud", modifier = Modifier.offset { IntOffset(cloud.x.toInt(), cloud.y.toInt()) }.size(120.dp), alpha = 0.9f) }
            TileMapView(tileMap = tileMap, tileSize = TILE_SIZE.toInt(), cameraOffsetX = cameraOffsetX)

            mushrooms.forEach { mushroom ->
                Image(
                    bitmap = mushroomBitmap,
                    contentDescription = "Mushroom",
                    modifier = Modifier.offset { IntOffset((mushroom.x - cameraOffsetX).roundToInt(), mushroom.y.roundToInt()) }.size(24.dp)
                )
            }

            enemies.forEach { enemy ->
                val bitmap = if (enemy.isAlive) goombaBitmap else squishedGoombaBitmap
                GoombaEnemy(enemy = enemy, cameraOffsetX = cameraOffsetX, bitmap = bitmap)
            }
            PlayerSprite(playerState = PlayerState(x = playerX - cameraOffsetX, y = playerY, isMoving = joystickOffset.x != 0f, direction = if (facingLeft) Direction.LEFT else Direction.RIGHT, isSuper = isSuper, currentFrame = playerFrame), isInvincible = isInvincible)

            scoreIndicators.forEach { indicator ->
                Text(
                    text = indicator.text,
                    color = Color(0xFFFFD700),
                  fontFamily = pixelFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset { IntOffset((indicator.x - cameraOffsetX).roundToInt(), indicator.y.roundToInt()) }
                )
            }

            // UI Controls
            Box(modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
            ) {
                Joystick(
                    onMove = { offset ->
                        joystickOffset = offset
                        velocityX = offset.x * 7f
                        if (offset.x != 0f) facingLeft = offset.x < 0
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Button(onClick = { restartGame() }, modifier = Modifier.align(Alignment.TopEnd).padding(top = 54.dp, end = 16.dp).size(80.dp), shape = RoundedCornerShape(40.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) { Image(painter = painterResource(id = R.drawable.loop), contentDescription = "Restart", modifier = Modifier.fillMaxSize()) }
            Button(onClick = { if (jumpCount < maxJumps) { velocityY = -30f; isJumping = true; jumpCount++; SfxManager.play(context, R.raw.jump) } }, contentPadding = PaddingValues(0.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(72.dp)) { Image(painter = painterResource(id = R.drawable.jump_btn), contentDescription = "Jump", modifier = Modifier.fillMaxSize()) }

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row { repeat(playerLives) { Icon(painter = painterResource(R.drawable.ic_heart), contentDescription = "Life", tint = Color.Red, modifier = Modifier.size(28.dp).padding(end = 4.dp)) } }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.coin), contentDescription = "Coin", modifier = Modifier.size(24.dp))
                        Text(" x $coinCount", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                    }
                    Text("TIME\n$timeLeft", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Row {
                        skillsToAcquire.forEach { skill ->
                            Icon(
                                painter = painterResource(skill.iconResId),
                                contentDescription = skill.name,
                                modifier = Modifier.size(28.dp).padding(start = 4.dp),
                                tint = if (collectedBadges.contains(skill.name)) Color.White else Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        if (isGamePaused && skillToShow != null) {
            SkillAcquiredDialog(
                skill = skillToShow!!,
                onDismiss = {
                    skillToShow = null
                    isGamePaused = false
                }
            )
        }
    }
}




@Composable
fun SkillAcquiredDialog(skill: SkillBadge, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(Color(0xFF2E2E2E), RoundedCornerShape(8.dp))
                .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SKILL ACQUIRED!",
                color = Color.Yellow,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(id = skill.iconResId),
                contentDescription = skill.name,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                skill.name,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                skill.description,
                color = Color.LightGray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    }
}

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
                1 -> R.drawable.brick_texture; 2 -> R.drawable.spikes2; 3 -> R.drawable.mushy
                4 -> R.drawable.grass; 5 -> R.drawable.qmark; 6 -> R.drawable.pipe ;7->R.drawable.castle;8->R.drawable.coin
                else -> null
            }
            tileRes?.let {
                Image(painter = painterResource(id = it), contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier.offset { IntOffset(((colIndex * tileSize) - cameraOffsetX).roundToInt(), rowIndex * tileSize) }.size(with(density) { tileSize.toDp() }))
            }
        }
    }
}

@Composable
fun GoombaEnemy(enemy: Enemy, cameraOffsetX: Float, bitmap: Bitmap) {
    // This composable now takes the camera offset and calculates the final position
    val screenX = enemy.x - cameraOffsetX
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Goomba",
        modifier = Modifier
            .offset { IntOffset(screenX.roundToInt(), enemy.y.roundToInt()) }
            .size(64.dp)
    )
}

@Composable
fun PlayerSprite(playerState: PlayerState, isInvincible: Boolean) {
    val context = LocalContext.current
    val normalSpriteSheet = remember { BitmapFactory.decodeResource(context.resources, R.drawable.mariosprite).asImageBitmap() }
    val superSpriteSheet = remember { BitmapFactory.decodeResource(context.resources, R.drawable.super_mariosheet).asImageBitmap() }
    val super2SpriteSheet = remember { BitmapFactory.decodeResource(context.resources, R.drawable.super1_mariosheett).asImageBitmap() }
    val fullBitmap = if (playerState.isSuper) superSpriteSheet else normalSpriteSheet

    val frameCount = 4
    val frameWidth = fullBitmap.width / frameCount
    val frameHeight = fullBitmap.height

    val density = LocalDensity.current
    val playerWidth: Dp by remember(playerState.isSuper) { derivedStateOf { if (playerState.isSuper) 38.dp else 28.dp } }
    val playerHeight: Dp by remember(playerState.isSuper) { derivedStateOf { if (playerState.isSuper) 48.dp else 48.dp } }
    val playerWidthPx = with(density) { playerWidth.toPx() }
    val playerHeightPx = with(density) { playerHeight.toPx() }


    var isVisible by remember { mutableStateOf(true) }
    LaunchedEffect(isInvincible) {
        if (isInvincible) {
            var elapsed = 0L
            while (elapsed < 2500) { // Blink for 2.5 seconds
                isVisible = !isVisible
                delay(100)
                elapsed += 100
            }
            isVisible = true
        } else {
            isVisible = true
        }
    }

    if (isVisible) {
        Canvas(modifier = Modifier
            .offset { IntOffset(playerState.x.toInt(), playerState.y.toInt()) }
            .size(width = playerWidth, height = playerHeight)) {
            val scaleX = if (playerState.direction == Direction.LEFT) -1f else 1f

            withTransform({
                scale(scaleX = scaleX, scaleY = 1f, pivot = this.center)
            }) {
                drawImage(
                    image = fullBitmap,
                    srcOffset = IntOffset(playerState.currentFrame * frameWidth, 0),
                    srcSize = IntSize(frameWidth, frameHeight),
                    dstSize = IntSize(playerWidthPx.roundToInt(), playerHeightPx.roundToInt())
                )
            }
        }
    }
}
