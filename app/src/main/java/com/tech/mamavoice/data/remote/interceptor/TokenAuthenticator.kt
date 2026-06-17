package com.tech.mamavoice.data.remote.interceptor

import android.util.Log
import com.tech.mamavoice.data.local.TokenManager
import com.tech.mamavoice.data.remote.api.AuthApiService
import com.tech.mamavoice.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiProvider: Provider<AuthApiService>
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loop: if we've already retried once, give up
        val retryCount = response.request.header("X-Retry-Count")?.toIntOrNull() ?: 0
        if (retryCount >= 1) {
            Log.w(TAG, "Already retried once, clearing session")
            runBlocking { tokenManager.clearSession() }
            return null
        }

        val currentToken = runBlocking { tokenManager.authToken.firstOrNull() }

        synchronized(this) {
            val newToken = runBlocking { tokenManager.authToken.firstOrNull() }

            // If token has already been refreshed by another thread
            if (currentToken != newToken && newToken != null) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .header("X-Retry-Count", "${retryCount + 1}")
                    .build()
            }

            val refreshToken = runBlocking { tokenManager.refreshToken.firstOrNull() }
            if (refreshToken.isNullOrBlank()) {
                Log.w(TAG, "No refresh token available, clearing session")
                runBlocking { tokenManager.clearSession() }
                return null
            }

            return try {
                Log.d(TAG, "Attempting token refresh...")
                val refreshResponse = runBlocking {
                    authApiProvider.get().refreshToken(RefreshTokenRequest(refreshToken))
                }

                if (refreshResponse.success) {
                    val tokenData = refreshResponse.data
                    Log.d(TAG, "Token refresh successful")

                    runBlocking {
                        tokenManager.saveAuthData(
                            accessToken = tokenData.token,
                            refreshTokenStr = tokenData.refreshToken,
                            isExistingUser = tokenManager.isExistingUser.firstOrNull() ?: false
                        )
                    }

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${tokenData.token}")
                        .header("X-Retry-Count", "${retryCount + 1}")
                        .build()
                } else {
                    Log.w(TAG, "Refresh failed: ${refreshResponse.message}")
                    runBlocking { tokenManager.clearSession() }
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Refresh exception: ${e.message}", e)
                runBlocking { tokenManager.clearSession() }
                null
            }
        }
    }
}
