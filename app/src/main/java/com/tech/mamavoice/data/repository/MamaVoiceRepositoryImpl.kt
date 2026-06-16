package com.tech.mamavoice.data.repository

import com.tech.mamavoice.data.remote.api.MamaVoiceApiService
import com.tech.mamavoice.data.remote.dto.AppEnumsResponse
import com.tech.mamavoice.domain.repository.MamaVoiceRepository
import com.tech.mamavoice.domain.util.Resource
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MamaVoiceRepositoryImpl @Inject constructor(
    private val api: MamaVoiceApiService
) : MamaVoiceRepository {
    override suspend fun getAppEnums(): Resource<AppEnumsResponse> {
        return try {
            val response = api.getAppEnums()
            if (response.success) {
                Resource.Success(response.data)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        }
    }
}
