package com.tech.mamavoice.presentation.dashboard

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.remote.dto.DashboardResponse
import com.tech.mamavoice.domain.repository.DashboardRepository
import com.tech.mamavoice.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    @ApplicationContext private val context: Context
) : ViewModel(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null

    private val _dashboardData = MutableStateFlow<Resource<DashboardResponse>>(Resource.Loading())
    val dashboardData: StateFlow<Resource<DashboardResponse>> = _dashboardData.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isDangerSign = MutableStateFlow(false)
    val isDangerSign: StateFlow<Boolean> = _isDangerSign.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context, this)
        fetchDashboardData()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.US
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

    fun queryAi(text: String) {
        viewModelScope.launch {
            _aiResponse.value = "Thinking..."
            _isDangerSign.value = false
            
            val result = repository.queryAi(text)
            if (result is Resource.Success) {
                val responseText = result.data?.aiResponseText ?: ""
                _aiResponse.value = responseText
                _isDangerSign.value = result.data?.isDangerSign ?: false
                
                textToSpeech?.speak(responseText, TextToSpeech.QUEUE_FLUSH, null, null)
            } else if (result is Resource.Error) {
                _aiResponse.value = "Error: ${result.message}"
            }
        }
    }

    override fun onCleared() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onCleared()
    }
}
