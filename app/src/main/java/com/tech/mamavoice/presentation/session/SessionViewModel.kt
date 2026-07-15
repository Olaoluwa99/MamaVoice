package com.tech.mamavoice.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Watches the auth token and emits [sessionExpired] when an authenticated session is lost *without*
 * a deliberate logout — i.e. the [TokenAuthenticator][com.tech.mamavoice.data.remote.interceptor.TokenAuthenticator]
 * cleared the session after a failed refresh (an expired/revoked token). The UI observes this to
 * bounce the user to the welcome screen with a friendly "please sign in again" message.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    // CONFLATED so a missed collection (e.g. mid-navigation) still delivers the latest signal.
    private val _sessionExpired = Channel<Unit>(Channel.CONFLATED)
    val sessionExpired = _sessionExpired.receiveAsFlow()

    init {
        viewModelScope.launch {
            var wasAuthenticated = false
            tokenManager.authToken
                .map { !it.isNullOrBlank() }
                .distinctUntilChanged()
                .collect { authenticated ->
                    // Only a true -> false transition is an expiry; skip deliberate logouts, which
                    // already navigate on their own.
                    if (wasAuthenticated && !authenticated && !tokenManager.consumeUserInitiatedLogout()) {
                        _sessionExpired.trySend(Unit)
                    }
                    wasAuthenticated = authenticated
                }
        }
    }
}
