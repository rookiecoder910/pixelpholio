// SfxManager.kt
package com.example.pixelpholio.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

object SfxManager {
    fun play(context: Context, @RawRes soundResId: Int) {
        val mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()
    }
}
