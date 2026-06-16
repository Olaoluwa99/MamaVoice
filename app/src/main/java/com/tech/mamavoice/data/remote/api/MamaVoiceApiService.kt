package com.tech.mamavoice.data.remote.api

import com.tech.mamavoice.data.remote.dto.AiQueryRequest
import com.tech.mamavoice.data.remote.dto.AiQueryResponse
import com.tech.mamavoice.data.remote.dto.ApiResponse
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

import com.tech.mamavoice.data.remote.dto.AppEnumsWrapperResponse

interface MamaVoiceApiService {

    @GET("api/generic/enums")
    suspend fun getAppEnums(): AppEnumsWrapperResponse

    @GET("api/dashboard")
    suspend fun getDashboard(): ApiResponse<DashboardResponse>

    @POST("api/ai/query")
    suspend fun queryAi(
        @Body request: AiQueryRequest
    ): AiQueryResponse

    @GET("api/foods")
    suspend fun getFoods(): List<FoodItem>

    @GET("api/vaccines")
    suspend fun getVaccines(): List<VaccineItem>

    @POST("api/vaccines/log")
    suspend fun logVaccine(
        @Body request: VaccineLogRequest
    ): StatusResponse

    @GET("api/tracker/history")
    suspend fun getTrackerHistory(): List<HealthLog>

    @POST("api/tracker/log")
    suspend fun logHealth(
        @Body request: HealthLogRequest
    ): StatusResponse
}
