package com.tech.mamavoice.domain.repository

import com.tech.mamavoice.data.remote.dto.DashboardResponse
import com.tech.mamavoice.domain.util.Resource

interface DashboardRepository {
    suspend fun getDashboard(): Resource<DashboardResponse>
}
