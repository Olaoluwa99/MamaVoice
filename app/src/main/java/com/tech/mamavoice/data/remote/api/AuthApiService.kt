package com.tech.mamavoice.data.remote.api

import com.tech.mamavoice.data.remote.dto.AuthRequest
import com.tech.mamavoice.data.remote.dto.AuthResponse
import com.tech.mamavoice.data.remote.dto.OtpRequest
import com.tech.mamavoice.data.remote.dto.ProfileRequest
import com.tech.mamavoice.data.remote.dto.StatusResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("api/user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileRequest
    ): StatusResponse
    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpRequest): StatusResponse
}
