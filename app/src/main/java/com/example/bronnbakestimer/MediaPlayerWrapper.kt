package com.example.bronnbakestimer

import android.content.Context
import android.media.MediaPlayer
import org.koin.core.context.GlobalContext

/**
 * Implementation of the [IMediaPlayerWrapper] interface, providing media player functionality.
 * This class initializes and manages a [MediaPlayer] instance to play sounds.
 */
class MediaPlayerWrapper(
    private val context: Context,
    private val soundResId: Int
) : IMediaPlayerWrapper {
    private val errorRepository: IErrorRepository = GlobalContext.get().get()
    private val errorLoggerProvider: ErrorLoggerProvider = GlobalContext.get().get()

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
                logError("Error creating MediaPlayer instance.", errorRepository, errorLoggerProvider)
            }
        } catch (e: Exception) {
            // Handle exceptions
            logException(e, errorRepository, errorLoggerProvider)
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
            logError("MediaPlayer is null.", errorRepository, errorLoggerProvider)
        }
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
