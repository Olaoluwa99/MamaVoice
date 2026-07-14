package com.tech.mamavoice.presentation.voice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.audio.AudioPlayer
import com.tech.mamavoice.data.audio.AudioRecorder
import com.tech.mamavoice.data.remote.dto.ConversationSummaryDto
import com.tech.mamavoice.data.remote.dto.VoiceQueryResponse
import com.tech.mamavoice.domain.repository.ConversationRepository
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

/** Drawer-side state: the paginated list of past conversations. */
data class ConversationHistoryUiState(
    val items: List<ConversationSummaryDto> = emptyList(),
    val isLoading: Boolean = false,
    val page: Int = 0,          // last page loaded; 0 = not loaded yet
    val hasMore: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class VoiceConversationViewModel @Inject constructor(
    private val repository: VoiceRepository,
    private val conversationRepository: ConversationRepository,
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

    /** Id of the chat currently on screen; null means an unsaved new chat. */
    private val _conversationId = MutableStateFlow<String?>(null)
    val conversationId: StateFlow<String?> = _conversationId.asStateFlow()

    /** The history drawer's list of past conversations. */
    private val _history = MutableStateFlow(ConversationHistoryUiState())
    val history: StateFlow<ConversationHistoryUiState> = _history.asStateFlow()

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
            val result = repository.voiceQuery(file, _conversationId.value)
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
            val result = repository.textQuery(query, _conversationId.value)
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
            // Remember which chat this turn belongs to so follow-ups stay in the same conversation.
            data.conversationId?.let { _conversationId.value = it }
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
                audioUrl = data.audioUrlOrNull
            )
            _messages.update { it + assistant }

            // Keep the drawer in sync (new title / new-conversation row / re-ordering) if it's open.
            if (_history.value.page > 0) loadConversations(refresh = true)

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

    // --- Conversation history ------------------------------------------------------------------

    /**
     * Loads a page of past conversations for the drawer. Call with [refresh] = true to reload from
     * the first page (e.g. on drawer open or after a new turn); otherwise it appends the next page.
     */
    fun loadConversations(refresh: Boolean = false) {
        val current = _history.value
        if (current.isLoading) return
        if (!refresh && current.page > 0 && !current.hasMore) return

        val nextPage = if (refresh) 1 else current.page + 1
        _history.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = conversationRepository.getConversations(page = nextPage)) {
                is Resource.Success -> {
                    val data = result.data
                    val incoming = data?.conversations.orEmpty()
                    _history.update { state ->
                        val merged = if (refresh) incoming else state.items + incoming
                        state.copy(
                            items = merged.distinctBy { it.id },
                            isLoading = false,
                            page = data?.pagination?.page ?: nextPage,
                            hasMore = data?.pagination?.hasMore ?: false,
                            error = null
                        )
                    }
                }
                is Resource.Error -> _history.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    /** Loads the full (paginated) history of [id] into the chat and makes it the active conversation. */
    fun openConversation(id: String) {
        if (_conversationId.value == id && _messages.value.isNotEmpty()) return
        stopAudio()
        _conversationId.value = id
        _messages.value = emptyList()
        _state.value = VoiceState.PROCESSING
        val loadingId = appendLoading()
        viewModelScope.launch {
            when (val result = conversationRepository.getConversation(id)) {
                is Resource.Success -> {
                    // Assumes the API returns messages in chronological (oldest→newest) display order.
                    val loaded = result.data?.messages.orEmpty().map { it.toChatMessage() }
                    _messages.value = loaded
                    _state.value = VoiceState.IDLE
                }
                is Resource.Error -> {
                    _messages.update { list -> list.filterNot { it.id == loadingId } }
                    appendError(result.message)
                    _state.value = VoiceState.IDLE
                }
                is Resource.Loading -> Unit
            }
        }
    }

    /** Clears the screen to a fresh, unsaved conversation. */
    fun startNewConversation() {
        stopAudio()
        _conversationId.value = null
        _messages.value = emptyList()
        _draft.value = ""
        _state.value = VoiceState.IDLE
    }

    /** Deletes [id]; removes it from the drawer and resets the screen if it was the open one. */
    fun deleteConversation(id: String) {
        viewModelScope.launch {
            when (conversationRepository.deleteConversation(id)) {
                is Resource.Success -> {
                    _history.update { state -> state.copy(items = state.items.filterNot { it.id == id }) }
                    if (_conversationId.value == id) startNewConversation()
                }
                is Resource.Error, is Resource.Loading -> Unit
            }
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
