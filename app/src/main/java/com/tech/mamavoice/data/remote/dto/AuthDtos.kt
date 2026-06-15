package com.tech.mamavoice.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val isExistingUser: Boolean
)

@Serializable
data class ProfileRequest(val firstName: String, val type: String, val targetDate: String)

@Serializable
data class OtpRequest(val email: String, val otp: String)

@Serializable
data class StatusResponse(
    val status: String,
    val message: String = ""
)
