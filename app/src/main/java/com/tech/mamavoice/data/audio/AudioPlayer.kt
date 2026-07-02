package com.tech.mamavoice.data.audio

import android.media.AudioAttributes
import android.media.MediaPlayer

/**
 * Plays the native-language TTS clips returned by the backend (`audioUrl`, remote MP3) via
 * [MediaPlayer]. One clip at a time — starting a new clip stops the previous one.
 */
class AudioPlayer {

    private var player: MediaPlayer? = null

    /**
     * Streams and plays [url].
     * @param onComplete invoked when playback finishes naturally.
     * @param onError invoked if preparation or playback fails.
     */
    fun play(
        url: String,
        onComplete: () -> Unit,
        onError: () -> Unit
    ) {
        stop()
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener { it.start() }
            setOnCompletionListener {
                onComplete()
            }
            setOnErrorListener { _, _, _ ->
                onError()
                true
            }
            try {
                setDataSource(url)
                prepareAsync()
            } catch (e: Exception) {
                onError()
            }
        }
    }

    /** Stops and releases the current player, if any. */
    fun stop() {
        player?.apply {
            try {
                if (isPlaying) stop()
            } catch (e: Exception) {
                // ignore illegal state
            }
            release()
        }
        player = null
    }
}
