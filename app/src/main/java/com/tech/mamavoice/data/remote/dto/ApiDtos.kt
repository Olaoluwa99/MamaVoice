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
    val textQuery: String
)

@Serializable
data class AiQueryResponse(
    val aiResponseText: String,
    val isDangerSign: Boolean
)

@Serializable
data class FoodItem(
    val id: String,
    val name: String,
    val category: String,
    val benefits: String,
    val imageUrl: String
)

@Serializable
data class VaccineItem(
    val vaccineId: String,
    val name: String,
    val dueDateString: String,
    val isCompleted: Boolean,
    val administeredDate: String? = null,
    val sideEffects: String
)

@Serializable
data class VaccineLogRequest(
    val vaccineId: String,
    val administeredDate: String
)

@Serializable
data class HealthLog(
    val logId: String,
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
