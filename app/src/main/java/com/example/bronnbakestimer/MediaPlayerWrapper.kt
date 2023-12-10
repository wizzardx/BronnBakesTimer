package com.example.bronnbakestimer

import android.content.Context
import android.media.MediaPlayer

/**
 * Implementation of the [IMediaPlayerWrapper] interface, providing media player functionality.
 * This class initializes and manages a [MediaPlayer] instance to play sounds.
 */
class MediaPlayerWrapper(
    private val context: Context,
    private val soundResId: Int
) : IMediaPlayerWrapper {
    private var mediaPlayer: MediaPlayer? = null

    init {
        initializeMediaPlayer()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun initializeMediaPlayer() {
        // Initialize MediaPlayer for playing the beep sound
        try {
            mediaPlayer = MediaPlayer.create(context, soundResId)
            if (mediaPlayer == null) {
                // Handle MediaPlayer creation failure
                logError("Error creating MediaPlayer instance.")
            }
        } catch (e: Exception) {
            // Handle exceptions
            logException(e)
        }
    }

    override fun playBeep() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.prepare() // Prepare the MediaPlayer to start from the beginning
            }
            it.start()
        } ?: run {
            // Handle case where MediaPlayer is null
            logError("MediaPlayer is null.")
        }
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
