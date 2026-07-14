package com.tech.mamavoice.data.remote.api

import com.tech.mamavoice.data.remote.dto.ApiResponse
import com.tech.mamavoice.data.remote.dto.DashboardResponse
import com.tech.mamavoice.data.remote.dto.HealthLog
import com.tech.mamavoice.data.remote.dto.HealthLogRequest
import com.tech.mamavoice.data.remote.dto.StatusResponse
import com.tech.mamavoice.data.remote.dto.TextQueryRequest
import com.tech.mamavoice.data.remote.dto.VaccineItem
import com.tech.mamavoice.data.remote.dto.VaccineLogRequest
import com.tech.mamavoice.data.remote.dto.VoiceQueryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

import com.tech.mamavoice.data.remote.dto.AppEnumsWrapperResponse
import com.tech.mamavoice.data.remote.dto.ConversationDetailResponse
import com.tech.mamavoice.data.remote.dto.ConversationListResponse
import com.tech.mamavoice.data.remote.dto.DeleteConversationResponse
import com.tech.mamavoice.data.remote.dto.FoodsResponseData
import com.tech.mamavoice.data.remote.dto.MessageAudioResponse

interface MamaVoiceApiService {

    @GET("api/generic/enums")
    suspend fun getAppEnums(): AppEnumsWrapperResponse

    @GET("api/dashboard")
    suspend fun getDashboard(): ApiResponse<DashboardResponse>

    /**
     * Upload a voice recording; server does STT + AI + native-language TTS.
     * [conversationId] continues an existing chat; omit it to start a new one.
     */
    @Multipart
    @POST("api/voice/query")
    suspend fun voiceQuery(
        @Part audio: MultipartBody.Part,
        @Part("conversationId") conversationId: RequestBody? = null
    ): ApiResponse<VoiceQueryResponse>

    /** Submit a typed health question; server does AI + native-language TTS. */
    @POST("api/voice/text-query")
    suspend fun textQuery(
        @Body request: TextQueryRequest
    ): ApiResponse<VoiceQueryResponse>

    /** List the user's conversations, most-recent first, paginated. */
    @GET("api/conversations")
    suspend fun getConversations(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<ConversationListResponse>

    /** Fetch one conversation with a page of its message history. */
    @GET("api/conversations/{id}")
    suspend fun getConversation(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<ConversationDetailResponse>

    /** Delete a conversation and all of its messages. */
    @DELETE("api/conversations/{id}")
    suspend fun deleteConversation(
        @Path("id") id: String
    ): ApiResponse<DeleteConversationResponse>

    /**
     * Poll the async TTS status of an assistant message. Called after a voice/text query came back
     * with a null `audioUrl`; keep polling until the status is "ready" (or "failed").
     */
    @GET("api/conversations/messages/{messageId}/audio")
    suspend fun getMessageAudio(
        @Path("messageId") messageId: String
    ): ApiResponse<MessageAudioResponse>

    @GET("api/foods")
    suspend fun getFoods(
        @Query("stage") stage: String? = null
    ): ApiResponse<FoodsResponseData>

    @GET("api/vaccines")
    suspend fun getVaccines(): ApiResponse<com.tech.mamavoice.data.remote.dto.VaccinesData>

    @POST("api/vaccines/log")
    suspend fun logVaccine(
        @Body request: VaccineLogRequest
    ): ApiResponse<com.tech.mamavoice.data.remote.dto.VaccineLogResponse>

    @GET("api/tracker/history")
    suspend fun getTrackerHistory(): ApiResponse<com.tech.mamavoice.data.remote.dto.TrackerHistoryData>

    @POST("api/tracker/log")
    suspend fun logHealth(
        @Body request: HealthLogRequest
    ): ApiResponse<HealthLog>
}
