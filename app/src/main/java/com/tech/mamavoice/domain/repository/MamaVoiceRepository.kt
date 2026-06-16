package com.tech.mamavoice.domain.repository

import com.tech.mamavoice.data.remote.dto.AppEnumsResponse
import com.tech.mamavoice.domain.util.Resource

interface MamaVoiceRepository {
    suspend fun getAppEnums(): Resource<AppEnumsResponse>
}
