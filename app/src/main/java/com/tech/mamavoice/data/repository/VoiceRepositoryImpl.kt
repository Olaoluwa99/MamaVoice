package com.tech.mamavoice.data.repository

import com.tech.mamavoice.data.remote.api.MamaVoiceApiService
import com.tech.mamavoice.data.remote.dto.TextQueryRequest
import com.tech.mamavoice.data.remote.dto.VoiceQueryResponse
import com.tech.mamavoice.domain.repository.VoiceRepository
import com.tech.mamavoice.domain.util.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRepositoryImpl @Inject constructor(
    private val apiService: MamaVoiceApiService
) : VoiceRepository {

    override suspend fun voiceQuery(audio: File): Resource<VoiceQueryResponse> {
        return try {
            val requestBody = audio.asRequestBody("audio/mp4".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("audio", audio.name, requestBody)
            val response = apiService.voiceQuery(part)
            if (response.success) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun textQuery(text: String): Resource<VoiceQueryResponse> {
        return try {
            val response = apiService.textQuery(TextQueryRequest(textQuery = text))
            if (response.success) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}
