package com.tech.mamavoice.data.repository

import com.tech.mamavoice.data.local.TokenManager
import com.tech.mamavoice.data.remote.api.AuthApiService
import com.tech.mamavoice.data.remote.dto.AuthRequest
import com.tech.mamavoice.data.remote.dto.ProfileRequest
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
    override suspend fun register(email: String, password: String): Resource<Boolean> {
        return try {
            val response = api.register(AuthRequest(email, password))
            tokenManager.saveAuthData(response.token, response.isExistingUser)
            Resource.Success(response.isExistingUser)
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun login(email: String, password: String): Resource<Boolean> {
        return try {
            val response = api.login(AuthRequest(email, password))
            tokenManager.saveAuthData(response.token, response.isExistingUser)
            Resource.Success(response.isExistingUser)
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun verifyOtp(email: String, otp: String): Resource<Unit> {
        return try {
            api.verifyOtp(com.tech.mamavoice.data.remote.dto.OtpRequest(email, otp))
            Resource.Success(Unit)
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun updateProfile(firstName: String, type: String, targetDate: String): Resource<Unit> {
        return try {
            val token = tokenManager.authToken.first() ?: return Resource.Error("Not authenticated")
            api.updateProfile("Bearer $token", ProfileRequest(firstName, type, targetDate))
            // After successful profile update, mark as existing user
            tokenManager.saveAuthData(token, true)
            Resource.Success(Unit)
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        }
    }
}
