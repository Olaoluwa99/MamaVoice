package com.tech.mamavoice.data.remote.interceptor

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

    override fun authenticate(route: Route?, response: Response): Request? {
        val currentToken = runBlocking { tokenManager.authToken.firstOrNull() }

        synchronized(this) {
            val newToken = runBlocking { tokenManager.authToken.firstOrNull() }

            // If token has already been refreshed by another thread
            if (currentToken != newToken && newToken != null) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            }

            val refreshToken = runBlocking { tokenManager.refreshToken.firstOrNull() }
            if (refreshToken.isNullOrBlank()) {
                return null
            }

            return try {
                val refreshResponse = runBlocking {
                    authApiProvider.get().refreshToken(RefreshTokenRequest(refreshToken))
                }

                runBlocking {
                    tokenManager.saveAuthData(
                        accessToken = refreshResponse.token,
                        refreshTokenStr = refreshResponse.refreshToken,
                        isExistingUser = tokenManager.isExistingUser.firstOrNull() ?: false
                    )
                }

                response.request.newBuilder()
                    .header("Authorization", "Bearer ${refreshResponse.token}")
                    .build()
            } catch (e: Exception) {
                runBlocking { tokenManager.clearSession() }
                null
            }
        }
    }
}
