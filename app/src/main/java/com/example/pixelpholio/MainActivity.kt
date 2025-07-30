package com.example.pixelpholio

import MenuScreen
import StartScreen
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.pixelpholio.ui.theme.PixelpholioTheme

class MainActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🎵 Load bg music
        mediaPlayer = MediaPlayer.create(this, R.raw.sm)
        mediaPlayer?.isLooping = true
        mediaPlayer?.setVolume(0.7f, 0.7f)

        setContent {
            PixelpholioTheme {
                AppContent(startMusic = { mediaPlayer?.start() })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
@Composable
fun AppContent(startMusic: () -> Unit) {
    var currentScreen by remember { mutableStateOf("start") }
    BackHandler(enabled = currentScreen != "menu") {
        currentScreen = "menu"
    }
    when (currentScreen) {
        "start" -> StartScreen {
            currentScreen = "menu"
            startMusic()
        }

        "menu" -> MenuScreen(
            onPlayClick = { currentScreen = "game" },
            onSkillClick = { currentScreen = "skills" }
        )

        "game" -> GameScreen()

        "skills" -> SkillShowcaseScreen()
    }
}
