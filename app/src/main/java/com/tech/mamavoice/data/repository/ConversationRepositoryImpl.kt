package com.tech.mamavoice.data.repository

import android.util.Log
import com.tech.mamavoice.data.remote.api.MamaVoiceApiService
import com.tech.mamavoice.data.remote.dto.ConversationDetailResponse
import com.tech.mamavoice.data.remote.dto.ConversationListResponse
import com.tech.mamavoice.data.remote.dto.MessageAudioResponse
import com.tech.mamavoice.domain.repository.ConversationRepository
import com.tech.mamavoice.domain.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val apiService: MamaVoiceApiService
) : ConversationRepository {

    override suspend fun getConversations(page: Int, limit: Int): Resource<ConversationListResponse> =
        try {
            val response = apiService.getConversations(page, limit)
            if (response.success) Resource.Success(response.data)
            else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }

    override suspend fun getConversation(id: String, page: Int, limit: Int): Resource<ConversationDetailResponse> =
        try {
            val response = apiService.getConversation(id, page, limit)
            if (response.success) Resource.Success(response.data)
            else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }

    override suspend fun deleteConversation(id: String): Resource<Boolean> =
        try {
            val response = apiService.deleteConversation(id)
            if (response.success) Resource.Success(true)
            else Resource.Error(response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }

    override suspend fun getMessageAudio(messageId: String): Resource<MessageAudioResponse> =
        try {
            val response = apiService.getMessageAudio(messageId)
            // Temporary diagnostics: watch the async TTS poll resolve (see VoiceRepositoryImpl).
            Log.d(
                "MamaVoiceAudio",
                "[messages/$messageId/audio] status=${response.data?.status} " +
                    "audioUrl=${response.data?.audioUrlOrNull}"
            )
            if (response.success) Resource.Success(response.data)
            else Resource.Error(response.message)
        } catch (e: Exception) {
            Log.d("MamaVoiceAudio", "[messages/$messageId/audio] poll failed: ${e.message}")
            Resource.Error(e.message ?: "An unknown error occurred")
        }
}
