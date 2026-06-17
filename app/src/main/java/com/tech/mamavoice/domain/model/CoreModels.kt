package com.tech.mamavoice.domain.model

data class NutritionalValues(
    val calories: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?,
    val fiber: Double?,
    val sodium: Double?,
    val iron: Double?,
    val calcium: Double?,
    val vitaminC: Double?,
    val folate: Double?,
    val vitaminA: Double?,
    val zinc: Double?
)

data class FoodItem(
    val id: String,
    val name: String,
    val category: String,
    val benefits: String,
    val mamaVoiceTip: String?,
    val dangerWarning: String?,
    val preparationTips: String?,
    val affordabilityRating: Int?,
    val availabilityRating: Int?,
    val imageUrls: List<String>,
    val nutritionalValues: NutritionalValues?,
    val suitableFor: List<String>,
    val trimesterRecommendation: List<String>,
    val keyNutrients: List<String>,
    val servingSuggestion: String?,
    val pairsWellWith: List<String>,
    val avoidWith: List<String>,
    val isHighIron: Boolean,
    val isHighFolate: Boolean,
    val isHighCalcium: Boolean,
    val isHighProtein: Boolean,
    val isHighVitaminC: Boolean
)

data class VaccineItem(
    val id: String,
    val name: String,
    val dueDateString: String,
    val dueDate: String? = null,
    val isCompleted: Boolean,
    val administeredDate: String? = null,
    val sideEffects: String? = null
)

data class HealthLog(
    val id: String,
    val dateString: String,
    val weight: Double?,
    val bp: String?,
    val nutrition: String?,
    val symptoms: String?
)
