package com.tech.mamavoice.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
    val deviceId: String? = null,
    val platform: String? = "android",
    val deviceModel: String? = null,
    val pushNotificationToken: String? = null
)

@Serializable
data class OtpDetails(
    val id: String,
    val purpose: String,
    val expiresAt: String,
    val createdAt: String
)

@Serializable
data class RegisterResponseData(
    val email: String,
    val otpId: String
)

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: RegisterResponseData
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val language: String? = null,
    val state: String? = null,
    val lga: String? = null,
    val motherStage: String? = null,
    val accountStatus: String,
    val emailVerified: Boolean,
    val profileCompleted: Boolean,
    val targetDate: String? = null,
    val lastLoginAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class AuthSuccessData(
    val token: String,
    val refreshToken: String,
    val user: UserDto,
    val isExistingUser: Boolean? = null
)

@Serializable
data class AuthSuccessResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: AuthSuccessData
)

@Serializable
data class VerifyOtpRequest(
    val otpId: String,
    val otp: String
)

@Serializable
data class ResendOtpRequest(
    val email: String
)



@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class ProfileRequest(val firstName: String, val type: String, val targetDate: String)

@Serializable
data class StatusResponse(
    val status: String,
    val message: String = ""
)
