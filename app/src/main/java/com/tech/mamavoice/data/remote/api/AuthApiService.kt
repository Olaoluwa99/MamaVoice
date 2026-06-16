package com.tech.mamavoice.data.remote.api

import com.tech.mamavoice.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): RegisterResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthSuccessResponse

    @POST("api/auth/verify-email")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): AuthSuccessResponse

    @POST("api/auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): RegisterResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthSuccessResponse

    @POST("api/user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileRequest
    ): StatusResponse
}
