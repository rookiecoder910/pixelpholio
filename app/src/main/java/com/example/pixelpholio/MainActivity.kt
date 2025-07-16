package com.example.pixelpholio
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.pixelpholio.ui.theme.PixelpholioTheme

class MainActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.sm)
        mediaPlayer?.isLooping = true

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
    var currentScreen by remember { mutableStateOf("splash") }

    androidx.activity.compose.BackHandler(enabled = currentScreen != "menu") {
        currentScreen = "menu"
    }

    when (currentScreen) {
        "splash" -> SplashScreen {
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
