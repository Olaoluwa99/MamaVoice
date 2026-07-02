package com.tech.mamavoice.di

import com.tech.mamavoice.data.repository.AuthRepositoryImpl
import com.tech.mamavoice.data.repository.CoreFeaturesRepositoryImpl
import com.tech.mamavoice.data.repository.DashboardRepositoryImpl
import com.tech.mamavoice.domain.repository.AuthRepository
import com.tech.mamavoice.domain.repository.CoreFeaturesRepository
import com.tech.mamavoice.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindVoiceRepository(
        voiceRepositoryImpl: com.tech.mamavoice.data.repository.VoiceRepositoryImpl
    ): com.tech.mamavoice.domain.repository.VoiceRepository

    @Binds
    @Singleton
    abstract fun bindCoreFeaturesRepository(
        coreFeaturesRepositoryImpl: CoreFeaturesRepositoryImpl
    ): CoreFeaturesRepository

    @Binds
    @Singleton
    abstract fun bindMamaVoiceRepository(
        mamaVoiceRepositoryImpl: com.tech.mamavoice.data.repository.MamaVoiceRepositoryImpl
    ): com.tech.mamavoice.domain.repository.MamaVoiceRepository
}
