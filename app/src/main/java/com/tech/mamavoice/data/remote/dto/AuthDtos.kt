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
data class RegisterResponse(
    val message: String,
    val email: String,
    val otp: OtpDetails
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
    val profileCompleted: Boolean
)

@Serializable
data class AuthSuccessResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
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
data class ResendOtpResponse(
    val message: String,
    val email: String,
    val otp: OtpDetails
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
