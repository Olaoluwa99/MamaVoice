package com.tech.mamavoice.data.repository

import com.tech.mamavoice.data.local.TokenManager
import com.tech.mamavoice.data.remote.api.AuthApiService
import com.tech.mamavoice.data.remote.dto.AuthRequest
import com.tech.mamavoice.data.remote.dto.ProfileRequest
import com.tech.mamavoice.data.remote.dto.ResendOtpRequest
import com.tech.mamavoice.data.remote.dto.VerifyOtpRequest
import com.tech.mamavoice.domain.repository.AuthRepository
import com.tech.mamavoice.domain.util.Resource
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {
    override suspend fun register(email: String, password: String): Resource<String> {
        return try {
            val response = api.register(AuthRequest(email = email, password = password))
            Resource.Success(response.otp.id)
        } catch (e: HttpException) {
            if (e.code() == 409) {
                Resource.Error("Email already registered. Please log in.")
            } else {
                Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun login(email: String, password: String): Resource<Boolean> {
        return try {
            val response = api.login(AuthRequest(email = email, password = password))
            tokenManager.saveAuthData(
                accessToken = response.accessToken,
                refreshTokenStr = response.refreshToken,
                isExistingUser = response.user.profileCompleted
            )
            Resource.Success(response.user.profileCompleted)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Resource.Error("Invalid email or password.")
            } else {
                Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun verifyOtp(otpId: String, otp: String): Resource<Boolean> {
        return try {
            val response = api.verifyOtp(VerifyOtpRequest(otpId = otpId, otp = otp))
            tokenManager.saveAuthData(
                accessToken = response.accessToken,
                refreshTokenStr = response.refreshToken,
                isExistingUser = response.user.profileCompleted
            )
            Resource.Success(response.user.profileCompleted)
        } catch (e: HttpException) {
            if (e.code() == 400) {
                Resource.Error("Invalid or expired OTP.")
            } else {
                Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun resendOtp(email: String): Resource<String> {
        return try {
            val response = api.resendOtp(ResendOtpRequest(email = email))
            Resource.Success(response.otp.id)
        } catch (e: HttpException) {
            if (e.code() == 400) {
                Resource.Error("Email already verified or not found.")
            } else {
                Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun updateProfile(firstName: String, type: String, targetDate: String): Resource<Unit> {
        return try {
            val token = tokenManager.authToken.first() ?: return Resource.Error("Not authenticated")
            api.updateProfile("Bearer $token", ProfileRequest(firstName, type, targetDate))
            // After successful profile update, mark as existing user
            val refreshToken = tokenManager.refreshToken.first() ?: ""
            tokenManager.saveAuthData(token, refreshToken, true)
            Resource.Success(Unit)
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        }
    }
}
