package com.tech.mamavoice.presentation.dashboard

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.R
import com.tech.mamavoice.data.local.AppLanguage
import com.tech.mamavoice.data.local.LanguageManager
import com.tech.mamavoice.data.remote.dto.DashboardResponse
import com.tech.mamavoice.domain.repository.DashboardRepository
import com.tech.mamavoice.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Inject

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isDangerSign: Boolean = false,
    val isError: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val languageManager: LanguageManager,
    @ApplicationContext private val context: Context
) : ViewModel(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null

    /** Latest chosen language; drives both the AI request and the spoken (TTS) reply. */
    private var currentLanguage: AppLanguage = AppLanguage.ENGLISH

    private val _dashboardData = MutableStateFlow<Resource<DashboardResponse>>(Resource.Loading())
    val dashboardData: StateFlow<Resource<DashboardResponse>> = _dashboardData.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _showVoiceOverlay = MutableStateFlow(false)
    val showVoiceOverlay: StateFlow<Boolean> = _showVoiceOverlay.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _draftQuery = MutableStateFlow("")
    val draftQuery: StateFlow<String> = _draftQuery.asStateFlow()

    private val _isPlayingTts = MutableStateFlow(false)
    val isPlayingTts: StateFlow<Boolean> = _isPlayingTts.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context, this)
        fetchDashboardData()
        viewModelScope.launch {
            languageManager.appLanguage.collect { language ->
                currentLanguage = language
                applyTtsLanguage(language)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            applyTtsLanguage(currentLanguage)
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlayingTts.value = true
                }
                override fun onDone(utteranceId: String?) {
                    _isPlayingTts.value = false
                }
                override fun onError(utteranceId: String?) {
                    _isPlayingTts.value = false
                }
            })
        }
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _dashboardData.value = Resource.Loading()
            _dashboardData.value = repository.getDashboard()
        }
    }
    
    fun setRecordingState(isRecording: Boolean) {
        _isRecording.value = isRecording
    }

    /** BCP-47 tag the speech recognizer should listen for, based on the chosen language. */
    fun currentLanguageTag(): String = currentLanguage.code

    fun updateDraftQuery(text: String) {
        _draftQuery.value = text
    }

    fun discardDraftQuery() {
        _draftQuery.value = ""
    }

    fun stopTts() {
        textToSpeech?.stop()
        _isPlayingTts.value = false
    }

    fun replayLastTts() {
        val lastAiMessage = _chatHistory.value.lastOrNull { !it.isUser && !it.isLoading && !it.isError }
        if (lastAiMessage != null) {
            speakText(lastAiMessage.text)
        }
    }

    fun toggleVoiceOverlay(show: Boolean) {
        _showVoiceOverlay.value = show
        if (!show) {
            _draftQuery.value = ""
            _isRecording.value = false
            stopTts()
        }
    }

    fun clearHistory() {
        _chatHistory.value = emptyList()
    }

    fun submitDraftQuery() {
        val query = _draftQuery.value.trim()
        if (query.isEmpty()) return
        
        // Stop any currently playing TTS
        stopTts()

        // Add user message to history
        val userMsg = ChatMessage(text = query, isUser = true)
        
        // Add loading message
        val loadingMsg = ChatMessage(text = context.getString(R.string.chat_thinking), isUser = false, isLoading = true)
        
        _chatHistory.update { current -> current + listOf(userMsg, loadingMsg) }
        _draftQuery.value = ""

        viewModelScope.launch {
            val result = repository.queryAi(query, currentLanguage.code)
            
            _chatHistory.update { current ->
                val listWithoutLoading = current.filterNot { it.isLoading }
                if (result is Resource.Success) {
                    val responseText = result.data?.aiResponseText ?: ""
                    val isDanger = result.data?.isDangerSign ?: false
                    speakText(responseText)
                    listWithoutLoading + ChatMessage(text = responseText, isUser = false, isDangerSign = isDanger)
                } else {
                    listWithoutLoading + ChatMessage(text = context.getString(R.string.chat_error_prefix, result.message ?: ""), isUser = false, isError = true)
                }
            }
        }
    }

    /**
     * Points the TTS engine at the chosen language, falling back to US English when the
     * device has no voice for it (common for Pidgin/Yoruba/Igbo/Hausa today — Spitch TTS
     * is the planned replacement for those).
     */
    private fun applyTtsLanguage(language: AppLanguage) {
        val tts = textToSpeech ?: return
        val result = tts.setLanguage(Locale.forLanguageTag(language.code))
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.language = Locale.US
        }
    }

    private fun speakText(text: String) {
        if (text.isNotBlank()) {
            val utteranceId = java.util.UUID.randomUUID().toString()
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    override fun onCleared() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onCleared()
    }
}
