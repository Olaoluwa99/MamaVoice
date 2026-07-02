package com.tech.mamavoice.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Thin wrapper around [MediaRecorder] that captures the mic to an AAC/MP4 (.m4a) file — a format
 * the `api/voice/query` endpoint accepts and that stays well under the 5 MB limit for short clips.
 *
 * Also exposes [maxAmplitude] so the UI can draw a live waveform while recording.
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null

    /** Starts recording into [output]. Throws if the mic can't be acquired. */
    fun start(output: File) {
        val newRecorder = createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96_000)
            setAudioSamplingRate(44_100)
            setOutputFile(output.absolutePath)
            prepare()
            start()
        }
        recorder = newRecorder
    }

    /** Peak amplitude since the previous call (0..32767); 0 when not recording. */
    fun maxAmplitude(): Int = try {
        recorder?.maxAmplitude ?: 0
    } catch (e: Exception) {
        0
    }

    /**
     * Stops recording and releases the recorder.
     * @return true if a valid file was produced; false if the clip was too short / failed.
     */
    fun stop(): Boolean {
        val current = recorder ?: return false
        return try {
            current.stop()
            true
        } catch (e: Exception) {
            // stop() throws if it's called before any audio was captured (very short taps).
            false
        } finally {
            release()
        }
    }

    /** Aborts recording without treating the result as usable. */
    fun cancel() {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            // ignore — we're discarding anyway
        } finally {
            release()
        }
    }

    private fun release() {
        recorder?.apply {
            reset()
            release()
        }
        recorder = null
    }

    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
}
