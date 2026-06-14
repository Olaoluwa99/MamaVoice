package com.tech.mamavoice.domain.repository

import com.tech.mamavoice.domain.util.Resource

interface AuthRepository {
    suspend fun register(email: String, password: String): Resource<Boolean>
    suspend fun login(email: String, password: String): Resource<Boolean>
    suspend fun updateProfile(firstName: String, type: String, targetDate: String): Resource<Unit>
}
