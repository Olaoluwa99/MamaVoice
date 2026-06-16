package com.tech.mamavoice.domain.repository

import com.tech.mamavoice.domain.util.Resource

interface AuthRepository {
    suspend fun register(email: String, password: String): Resource<String> // Returns otpId
    suspend fun login(email: String, password: String): Resource<Boolean> // Returns isProfileCompleted
    suspend fun verifyOtp(otpId: String, otp: String): Resource<Boolean> // Returns isProfileCompleted
    suspend fun resendOtp(email: String): Resource<String> // Returns new otpId
    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        language: String,
        state: String,
        lga: String,
        motherStage: String,
        targetDate: String
    ): Resource<Unit>
}
