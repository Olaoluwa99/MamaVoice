package com.tech.mamavoice.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: T
)

@Serializable
data class DashboardResponse(
    val firstName: String,
    val statusText: String,
    val currentWeek: Int,
    val daysToNextVaccine: Int,
    val nextVaccineName: String
)

@Serializable
data class AiQueryRequest(
    val textQuery: String,
    /** BCP-47 code of the language the AI should answer in (en, pcm, yo, ig, ha). */
    val language: String = "en"
)

@Serializable
data class AiQueryResponse(
    val aiResponseText: String,
    val isDangerSign: Boolean
)

@Serializable
data class NutritionalValuesDto(
    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sodium: Double? = null,
    val iron: Double? = null,
    val calcium: Double? = null,
    val vitaminC: Double? = null,
    val folate: Double? = null,
    val vitaminA: Double? = null,
    val zinc: Double? = null
)

@Serializable
data class FoodDto(
    val id: String,
    val name: String,
    val category: String,
    val benefits: String,
    val mamaVoiceTip: String? = null,
    val dangerWarning: String? = null,
    val preparationTips: String? = null,
    val affordabilityRating: Int? = null,
    val availabilityRating: Int? = null,
    val imageUrls: List<String> = emptyList(),
    val nutritionalValues: NutritionalValuesDto? = null,
    val suitableFor: List<String> = emptyList(),
    val trimesterRecommendation: List<String> = emptyList(),
    val keyNutrients: List<String> = emptyList(),
    val servingSuggestion: String? = null,
    val pairsWellWith: List<String> = emptyList(),
    val avoidWith: List<String> = emptyList(),
    val isHighIron: Boolean? = null,
    val isHighFolate: Boolean? = null,
    val isHighCalcium: Boolean? = null,
    val isHighProtein: Boolean? = null,
    val isHighVitaminC: Boolean? = null
)

@Serializable
data class FoodsResponseData(
    val foods: List<FoodDto> = emptyList()
)

@Serializable
data class VaccineItem(
    val vaccineId: String,
    val vaccineName: String,
    val dueDateString: String,
    val dueDate: String? = null,
    val isCompleted: Boolean,
    val administeredDate: String? = null,
    val sideEffects: String? = null
)

@Serializable
data class VaccineLogRequest(
    val vaccineId: String,
    val administeredDate: String,
    val vaccineName: String,
    val isCompleted: Boolean,
    val sideEffects: String? = null
)

@Serializable
data class VaccineLogResponse(
    val id: String,
    val vaccineId: String,
    val vaccineName: String,
    val isCompleted: Boolean,
    val administeredDate: String? = null,
    val sideEffects: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class VaccinesData(
    val vaccines: List<VaccineItem> = emptyList()
)

@Serializable
data class TrackerHistoryData(
    val logs: List<HealthLog> = emptyList()
)

@Serializable
data class HealthLog(
    val id: String,
    val logDate: String,
    val weightKg: Double? = null,
    val bloodPressure: String? = null,
    val nutritionNotes: String? = null,
    val symptoms: String? = null
)

@Serializable
data class HealthLogRequest(
    val logDate: String,
    val weightKg: Double? = null,
    val bloodPressure: String? = null,
    val nutritionNotes: String? = null,
    val symptoms: String? = null
)

@Serializable
data class AppEnumsWrapperResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: AppEnumsResponse
)

@Serializable
data class AppEnumsResponse(
    val profileTypes: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val motherStages: List<String> = emptyList(),
    val foodCategories: List<String> = emptyList(),
    val foodStages: List<String> = emptyList(),
    val trimesters: List<String> = emptyList(),
    val devicePlatforms: List<String> = emptyList(),
    val states: List<String> = emptyList(),
    val stateLgas: Map<String, List<String>> = emptyMap()
)
