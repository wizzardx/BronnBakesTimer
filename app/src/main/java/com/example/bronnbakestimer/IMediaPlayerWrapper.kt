package com.example.bronnbakestimer

/**
 * Interface defining the contract for a media player wrapper.
 * It provides methods to play a beep sound and to release media player resources.
 */
interface IMediaPlayerWrapper {
    /**
     * Plays a beep sound. This method should handle the media player's state
     * and play the beep sound appropriately.
     */
    fun playBeep()

    /**
     * Releases the media player resources. This method should be called to clean up
     * the media player instance when it is no longer needed.
     */
    fun release()
}
