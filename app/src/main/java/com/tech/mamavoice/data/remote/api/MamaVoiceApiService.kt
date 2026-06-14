package com.tech.mamavoice.data.remote.api

import com.tech.mamavoice.data.remote.dto.AiQueryRequest
import com.tech.mamavoice.data.remote.dto.AiQueryResponse
import com.tech.mamavoice.data.remote.dto.DashboardResponse
import com.tech.mamavoice.data.remote.dto.FoodItem
import com.tech.mamavoice.data.remote.dto.HealthLog
import com.tech.mamavoice.data.remote.dto.HealthLogRequest
import com.tech.mamavoice.data.remote.dto.StatusResponse
import com.tech.mamavoice.data.remote.dto.VaccineItem
import com.tech.mamavoice.data.remote.dto.VaccineLogRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MamaVoiceApiService {

    @GET("api/dashboard")
    suspend fun getDashboard(@Header("Authorization") token: String): DashboardResponse

    @POST("api/ai/query")
    suspend fun queryAi(
        @Header("Authorization") token: String,
        @Body request: AiQueryRequest
    ): AiQueryResponse

    @GET("api/foods")
    suspend fun getFoods(@Header("Authorization") token: String): List<FoodItem>

    @GET("api/vaccines")
    suspend fun getVaccines(@Header("Authorization") token: String): List<VaccineItem>

    @POST("api/vaccines/log")
    suspend fun logVaccine(
        @Header("Authorization") token: String,
        @Body request: VaccineLogRequest
    ): StatusResponse

    @GET("api/tracker/history")
    suspend fun getTrackerHistory(@Header("Authorization") token: String): List<HealthLog>

    @POST("api/tracker/log")
    suspend fun logHealth(
        @Header("Authorization") token: String,
        @Body request: HealthLogRequest
    ): StatusResponse
}
