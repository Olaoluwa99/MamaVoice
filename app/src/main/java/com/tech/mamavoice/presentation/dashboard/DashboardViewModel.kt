package com.tech.mamavoice.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.local.SettingsManager
import com.tech.mamavoice.data.remote.dto.DashboardResponse
import com.tech.mamavoice.domain.repository.DashboardRepository
import com.tech.mamavoice.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Home tab's greeting card. The AI conversation was moved to its own
 * [com.tech.mamavoice.presentation.voice.VoiceConversationViewModel].
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _dashboardData = MutableStateFlow<Resource<DashboardResponse>>(Resource.Loading())
    val dashboardData: StateFlow<Resource<DashboardResponse>> = _dashboardData.asStateFlow()

    init {
        // Load once, then reload whenever the app language changes so the localized
        // response updates without an app restart. The ViewModel survives the locale
        // config change, so an init-only fetch would otherwise go stale.
        viewModelScope.launch {
            settingsManager.appLanguage.distinctUntilChanged().collectLatest {
                _dashboardData.value = Resource.Loading()
                _dashboardData.value = repository.getDashboard()
            }
        }
    }
}
