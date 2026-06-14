package com.tech.mamavoice.domain.model

data class FoodItem(
    val id: String,
    val name: String,
    val category: String,
    val benefits: String,
    val imageUrl: String
)

data class VaccineItem(
    val id: String,
    val name: String,
    val dueDateString: String,
    val isCompleted: Boolean
)

data class HealthLog(
    val id: String,
    val dateString: String,
    val weight: Double?,
    val bp: String?,
    val nutrition: String?,
    val symptoms: String?
)
