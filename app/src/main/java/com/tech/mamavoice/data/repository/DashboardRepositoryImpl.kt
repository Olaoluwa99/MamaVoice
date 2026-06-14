package com.tech.mamavoice.data.repository

import com.tech.mamavoice.data.local.TokenManager
import com.tech.mamavoice.data.remote.api.MamaVoiceApiService
import com.tech.mamavoice.data.remote.dto.AiQueryRequest
import com.tech.mamavoice.data.remote.dto.AiQueryResponse
import com.tech.mamavoice.data.remote.dto.DashboardResponse
import com.tech.mamavoice.domain.repository.DashboardRepository
import com.tech.mamavoice.domain.util.Resource
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: MamaVoiceApiService,
    private val tokenManager: TokenManager
) : DashboardRepository {

    private suspend fun getBearerToken(): String? {
        val token = tokenManager.authToken.firstOrNull()
        return if (token != null) "Bearer $token" else null
    }

    override suspend fun getDashboard(): Resource<DashboardResponse> {
        return try {
            val token = getBearerToken() ?: return Resource.Error("Unauthorized: No token found")
            val response = apiService.getDashboard(token)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun queryAi(textQuery: String): Resource<AiQueryResponse> {
        return try {
            val token = getBearerToken() ?: return Resource.Error("Unauthorized: No token found")
            val response = apiService.queryAi(
                token = token,
                request = AiQueryRequest(textQuery = textQuery)
            )
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}
