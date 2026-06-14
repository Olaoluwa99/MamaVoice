package com.tech.mamavoice.domain.repository

import com.tech.mamavoice.domain.model.FoodItem
import com.tech.mamavoice.domain.model.HealthLog
import com.tech.mamavoice.domain.model.VaccineItem
import com.tech.mamavoice.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface CoreFeaturesRepository {
    fun getFoodItems(): Flow<Resource<List<FoodItem>>>
    
    fun getImmunizationTimeline(): Flow<Resource<List<VaccineItem>>>
    suspend fun markVaccineCompleted(vaccineId: String): Resource<Unit>
    
    fun getHealthLogs(): Flow<Resource<List<HealthLog>>>
    suspend fun submitHealthLog(weight: Double?, bp: String?, nutrition: String?, symptoms: String?): Resource<Unit>
}
