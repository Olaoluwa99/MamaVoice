package com.tech.mamavoice.presentation.voice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.audio.AudioPlayer
import com.tech.mamavoice.data.audio.AudioRecorder
import com.tech.mamavoice.data.remote.dto.VoiceQueryResponse
import com.tech.mamavoice.domain.repository.VoiceRepository
import com.tech.mamavoice.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

/** High-level state of the conversation, driving the status pill and bottom controls. */
enum class VoiceState { IDLE, RECORDING, PROCESSING, PLAYING }

/**
 * One chat entry. A user entry uses [text]; an assistant entry uses [nativeText] / [englishText]
 * plus the optional [audioUrl] and risk metadata.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String? = null,
    val nativeText: String? = null,
    val englishText: String? = null,
    val riskLevel: String? = null,
    val isDangerSign: Boolean = false,
    val audioUrl: String? = null,
    val showEnglish: Boolean = false,
    val isLoading: Boolean = false,
    val isError: Boolean = false
) {
    /** Text currently shown in an assistant bubble, honouring the English toggle. */
    val displayText: String
        get() = when {
            isUser -> text.orEmpty()
            showEnglish -> englishText ?: nativeText ?: text.orEmpty()
            else -> nativeText ?: englishText ?: text.orEmpty()
        }

    /** True when an English translation is available to toggle to. */
    val hasEnglishAlternative: Boolean
        get() = !isUser && !englishText.isNullOrBlank() && englishText != nativeText
}

private const val AMPLITUDE_BUFFER = 40
private const val AMPLITUDE_POLL_MS = 80L

@HiltViewModel
class VoiceConversationViewModel @Inject constructor(
    private val repository: VoiceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val recorder = AudioRecorder(context)
    private val player = AudioPlayer()

    private val _state = MutableStateFlow(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()

    /** Rolling, normalized (0..1) mic levels for the recording waveform. */
    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    val amplitudes: StateFlow<List<Float>> = _amplitudes.asStateFlow()

    /** Id of the assistant message whose audio is currently playing, if any. */
    private val _playingMessageId = MutableStateFlow<String?>(null)
    val playingMessageId: StateFlow<String?> = _playingMessageId.asStateFlow()

    private var recordingFile: File? = null
    private var amplitudeJob: Job? = null

    fun updateDraft(text: String) {
        _draft.value = text
    }

    // --- Recording -----------------------------------------------------------------------------

    fun startRecording() {
        if (_state.value == VoiceState.RECORDING || _state.value == VoiceState.PROCESSING) return
        stopAudio()

        val file = File(context.cacheDir, "mamavoice_query_${System.currentTimeMillis()}.m4a")
        try {
            recorder.start(file)
        } catch (e: Exception) {
            appendError()
            return
        }
        recordingFile = file
        _amplitudes.value = emptyList()
        _state.value = VoiceState.RECORDING
        startAmplitudePolling()
    }

    /** Stops recording and uploads the clip. */
    fun stopRecordingAndSend() {
        if (_state.value != VoiceState.RECORDING) return
        stopAmplitudePolling()
        val produced = recorder.stop()
        _amplitudes.value = emptyList()
        val file = recordingFile
        if (!produced || file == null || file.length() == 0L) {
            file?.delete()
            recordingFile = null
            _state.value = VoiceState.IDLE
            return
        }

        _state.value = VoiceState.PROCESSING
        val loadingId = appendLoading()
        viewModelScope.launch {
            val result = repository.voiceQuery(file)
            file.delete()
            recordingFile = null
            handleResult(result, loadingId, userSpokenText = null)
        }
    }

    fun cancelRecording() {
        if (_state.value != VoiceState.RECORDING) return
        stopAmplitudePolling()
        recorder.cancel()
        recordingFile?.delete()
        recordingFile = null
        _amplitudes.value = emptyList()
        _state.value = VoiceState.IDLE
    }

    private fun startAmplitudePolling() {
        amplitudeJob = viewModelScope.launch {
            while (_state.value == VoiceState.RECORDING) {
                val level = (recorder.maxAmplitude() / 32_767f).coerceIn(0f, 1f)
                _amplitudes.update { current ->
                    (current + level).takeLast(AMPLITUDE_BUFFER)
                }
                delay(AMPLITUDE_POLL_MS)
            }
        }
    }

    private fun stopAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = null
    }

    // --- Text ----------------------------------------------------------------------------------

    fun submitText(text: String) {
        val query = text.trim()
        if (query.isEmpty() || _state.value == VoiceState.PROCESSING) return
        stopAudio()

        _messages.update { it + ChatMessage(isUser = true, text = query) }
        _draft.value = ""
        _state.value = VoiceState.PROCESSING
        val loadingId = appendLoading()
        viewModelScope.launch {
            val result = repository.textQuery(query)
            handleResult(result, loadingId, userSpokenText = null)
        }
    }

    // --- Response handling ---------------------------------------------------------------------

    private fun handleResult(
        result: Resource<VoiceQueryResponse>,
        loadingId: String,
        userSpokenText: String?
    ) {
        _messages.update { list -> list.filterNot { it.id == loadingId } }

        if (result is Resource.Success && result.data != null) {
            val data = result.data
            // For voice queries the transcript is what the user said — show it as their bubble.
            val transcript = data.transcript?.takeIf { it.isNotBlank() } ?: userSpokenText
            if (transcript != null) {
                _messages.update { it + ChatMessage(isUser = true, text = transcript) }
            }
            val assistant = ChatMessage(
                isUser = false,
                nativeText = data.spokenResponse ?: data.aiResponseText,
                englishText = data.spokenResponseEnglish ?: data.aiResponseText,
                riskLevel = data.riskLevel,
                isDangerSign = data.isDangerSign,
                audioUrl = data.audioUrl
            )
            _messages.update { it + assistant }

            if (!assistant.audioUrl.isNullOrBlank()) {
                playMessage(assistant)
            } else {
                _state.value = VoiceState.IDLE
            }
        } else {
            appendError(result.message)
            _state.value = VoiceState.IDLE
        }
    }

    private fun appendLoading(): String {
        val loading = ChatMessage(isUser = false, isLoading = true)
        _messages.update { it + loading }
        return loading.id
    }

    private fun appendError(message: String? = null) {
        _messages.update { list ->
            list.filterNot { it.isLoading } + ChatMessage(isUser = false, isError = true, text = message)
        }
    }

    // --- Playback ------------------------------------------------------------------------------

    fun playMessage(message: ChatMessage) {
        val url = message.audioUrl ?: return
        _playingMessageId.value = message.id
        _state.value = VoiceState.PLAYING
        player.play(
            url = url,
            onComplete = {
                _playingMessageId.value = null
                if (_state.value == VoiceState.PLAYING) _state.value = VoiceState.IDLE
            },
            onError = {
                _playingMessageId.value = null
                if (_state.value == VoiceState.PLAYING) _state.value = VoiceState.IDLE
            }
        )
    }

    fun stopAudio() {
        player.stop()
        _playingMessageId.value = null
        if (_state.value == VoiceState.PLAYING) _state.value = VoiceState.IDLE
    }

    fun toggleEnglish(messageId: String) {
        _messages.update { list ->
            list.map { if (it.id == messageId) it.copy(showEnglish = !it.showEnglish) else it }
        }
    }

    override fun onCleared() {
        stopAmplitudePolling()
        recorder.cancel()
        player.stop()
        recordingFile?.delete()
        super.onCleared()
    }
}
