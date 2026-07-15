package com.tech.mamavoice.data.repository

import android.util.Log
import com.tech.mamavoice.data.remote.api.MamaVoiceApiService
import com.tech.mamavoice.data.remote.dto.TextQueryRequest
import com.tech.mamavoice.data.remote.dto.VoiceQueryResponse
import com.tech.mamavoice.data.remote.toAppError
import com.tech.mamavoice.domain.repository.VoiceRepository
import com.tech.mamavoice.domain.util.AppError
import com.tech.mamavoice.domain.util.Resource
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRepositoryImpl @Inject constructor(
    private val apiService: MamaVoiceApiService
) : VoiceRepository {

    override suspend fun voiceQuery(audio: File, conversationId: String?): Resource<VoiceQueryResponse> {
        return try {
            val requestBody = audio.asRequestBody("audio/mp4".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("audio", audio.name, requestBody)
            val idPart = conversationId?.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = apiService.voiceQuery(part, idPart)
            logAudio("voice/query", response.success, response.data)
            if (response.success) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message, AppError.Message(response.message))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "", e.toAppError())
        }
    }

    override suspend fun textQuery(text: String, conversationId: String?): Resource<VoiceQueryResponse> {
        return try {
            val response = apiService.textQuery(TextQueryRequest(textQuery = text, conversationId = conversationId))
            logAudio("voice/text-query", response.success, response.data)
            if (response.success) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message, AppError.Message(response.message))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "", e.toAppError())
        }
    }

    /** Temporary diagnostics: inspect what the backend actually returns for the TTS audio field. */
    private fun logAudio(endpoint: String, success: Boolean, data: VoiceQueryResponse?) {
        Log.d(AUDIO_TAG, "[$endpoint] success=$success")
        Log.d(AUDIO_TAG, "[$endpoint] audioUrl(raw)=${data?.audioUrl}")
        Log.d(AUDIO_TAG, "[$endpoint] audioUrl(extracted)=${data?.audioUrlOrNull}")
        Log.d(AUDIO_TAG, "[$endpoint] audioContentType=${data?.audioContentType}")

        // A copy-pasteable JSON of the parsed response to share with the backend developer.
        // audioUrl is kept as a raw JSON element, so its true shape (string / {} / null) is preserved.
        val json = if (data == null) "null" else try {
            loggingJson.encodeToString(VoiceQueryResponse.serializer(), data)
        } catch (e: Exception) {
            "Failed to serialize response: ${e.message}"
        }
        Log.d(AUDIO_TAG, "[$endpoint] JSON response:\n$json")
    }

    private companion object {
        const val AUDIO_TAG = "MamaVoiceAudio"
        val loggingJson = Json { prettyPrint = true; encodeDefaults = true }
    }
}
