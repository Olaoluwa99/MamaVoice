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
            val response = apiService.getFoods()
            val dtos = response.data.foods
            val domainModels = dtos.map { dto ->
                val nutrition = dto.nutritionalValues?.let {
                    com.tech.mamavoice.domain.model.NutritionalValues(
                        calories = it.calories,
                        protein = it.protein,
                        carbs = it.carbs,
                        fat = it.fat,
                        fiber = it.fiber,
                        sodium = it.sodium,
                        iron = it.iron,
                        calcium = it.calcium,
                        vitaminC = it.vitaminC,
                        folate = it.folate,
                        vitaminA = it.vitaminA,
                        zinc = it.zinc
                    )
                }
                
                FoodItem(
                    id = dto.id,
                    name = dto.name,
                    category = dto.category,
                    benefits = dto.benefits,
                    mamaVoiceTip = dto.mamaVoiceTip,
                    dangerWarning = dto.dangerWarning,
                    preparationTips = dto.preparationTips,
                    affordabilityRating = dto.affordabilityRating,
                    availabilityRating = dto.availabilityRating,
                    imageUrls = dto.imageUrls,
                    nutritionalValues = nutrition,
                    suitableFor = dto.suitableFor,
                    trimesterRecommendation = dto.trimesterRecommendation,
                    keyNutrients = dto.keyNutrients,
                    servingSuggestion = dto.servingSuggestion,
                    pairsWellWith = dto.pairsWellWith,
                    avoidWith = dto.avoidWith,
                    isHighIron = dto.isHighIron ?: false,
                    isHighFolate = dto.isHighFolate ?: false,
                    isHighCalcium = dto.isHighCalcium ?: false,
                    isHighProtein = dto.isHighProtein ?: false,
                    isHighVitaminC = dto.isHighVitaminC ?: false
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
            val response = apiService.getVaccines()
            val dtos = response.data.vaccines
            val domainModels = dtos.map { dto ->
                VaccineItem(
                    id = dto.vaccineId,
                    name = dto.vaccineName,
                    dueDateString = dto.dueDateString,
                    dueDate = dto.dueDate,
                    isCompleted = dto.isCompleted,
                    administeredDate = dto.administeredDate,
                    sideEffects = dto.sideEffects
                )
            }
            emit(Resource.Success(domainModels))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch vaccines"))
        }
    }

    override suspend fun markVaccineCompleted(
        vaccineId: String,
        vaccineName: String,
        date: String,
        isCompleted: Boolean,
        sideEffects: String?
    ): Resource<Unit> {
        return try {
            val request = VaccineLogRequest(
                vaccineId = vaccineId,
                administeredDate = date,
                vaccineName = vaccineName,
                isCompleted = isCompleted,
                sideEffects = sideEffects
            )
            val response = apiService.logVaccine(request)
            if (response.success) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to log vaccine")
        }
    }

    override fun getHealthLogs(): Flow<Resource<List<HealthLog>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTrackerHistory()
            val dtos = response.data.logs
            val domainModels = dtos.map { dto ->
                HealthLog(
                    id = dto.id,
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
            val response = apiService.logHealth(request)
            if (response.success) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to log health data")
        }
    }
}
