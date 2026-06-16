package com.tech.mamavoice.data.repository

import com.tech.mamavoice.data.local.TokenManager
import com.tech.mamavoice.data.remote.api.MamaVoiceApiService
import com.tech.mamavoice.data.remote.dto.HealthLogRequest
import com.tech.mamavoice.data.remote.dto.VaccineLogRequest
import com.tech.mamavoice.domain.model.FoodItem
import com.tech.mamavoice.domain.model.HealthLog
import com.tech.mamavoice.domain.model.VaccineItem
import com.tech.mamavoice.domain.repository.CoreFeaturesRepository
import com.tech.mamavoice.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Singleton
class CoreFeaturesRepositoryImpl @Inject constructor(
    private val apiService: MamaVoiceApiService,
    private val tokenManager: TokenManager
) : CoreFeaturesRepository {

    private suspend fun getBearerToken(): String? {
        val token = tokenManager.authToken.firstOrNull()
        return if (token != null) "Bearer $token" else null
    }

    override fun getFoodItems(): Flow<Resource<List<FoodItem>>> = flow {
        emit(Resource.Loading())
        try {
            val dtos = apiService.getFoods()
            val domainModels = dtos.map { dto ->
                FoodItem(
                    id = dto.id,
                    name = dto.name,
                    category = dto.category,
                    benefits = dto.benefits,
                    imageUrl = dto.imageUrl
                )
            }
            emit(Resource.Success(domainModels))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch foods"))
        }
    }

    override fun getImmunizationTimeline(): Flow<Resource<List<VaccineItem>>> = flow {
        emit(Resource.Loading())
        try {
            val dtos = apiService.getVaccines()
            val domainModels = dtos.map { dto ->
                VaccineItem(
                    id = dto.vaccineId,
                    name = dto.name,
                    dueDateString = dto.dueDateString,
                    isCompleted = dto.isCompleted
                )
            }
            emit(Resource.Success(domainModels))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch vaccines"))
        }
    }

    override suspend fun markVaccineCompleted(vaccineId: String): Resource<Unit> {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            apiService.logVaccine(VaccineLogRequest(vaccineId, date))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to log vaccine")
        }
    }

    override fun getHealthLogs(): Flow<Resource<List<HealthLog>>> = flow {
        emit(Resource.Loading())
        try {
            val dtos = apiService.getTrackerHistory()
            val domainModels = dtos.map { dto ->
                HealthLog(
                    id = dto.logId,
                    dateString = dto.logDate,
                    weight = dto.weightKg,
                    bp = dto.bloodPressure,
                    nutrition = dto.nutritionNotes,
                    symptoms = dto.symptoms
                )
            }
            emit(Resource.Success(domainModels))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch tracker history"))
        }
    }

    override suspend fun submitHealthLog(
        weight: Double?,
        bp: String?,
        nutrition: String?,
        symptoms: String?
    ): Resource<Unit> {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val request = HealthLogRequest(
                logDate = date,
                weightKg = weight,
                bloodPressure = bp,
                nutritionNotes = nutrition,
                symptoms = symptoms
            )
            apiService.logHealth(request)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to log health data")
        }
    }
}
