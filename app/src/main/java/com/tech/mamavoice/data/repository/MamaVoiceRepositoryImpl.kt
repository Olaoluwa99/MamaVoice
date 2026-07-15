package com.tech.mamavoice.data.repository

import com.tech.mamavoice.data.remote.api.MamaVoiceApiService
import com.tech.mamavoice.data.remote.dto.AppEnumsResponse
import com.tech.mamavoice.data.remote.toAppError
import com.tech.mamavoice.domain.repository.MamaVoiceRepository
import com.tech.mamavoice.domain.util.AppError
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
                Resource.Error(response.message, AppError.Message(response.message))
            }
        } catch (e: HttpException) {
            Resource.Error(e.message ?: "", e.toAppError())
        } catch (e: IOException) {
            Resource.Error(e.message ?: "", e.toAppError())
        }
    }
}
