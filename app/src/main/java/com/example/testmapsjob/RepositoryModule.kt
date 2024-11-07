package com.example.testmapsjob

import com.example.data.PointsRepositoryImpl
import com.example.domain.LoadPointResult
import com.example.domain.PointsRepository
import com.example.presentation.UIMapper
import com.example.presentation.UIState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideRepository(pointsRepositoryImpl: PointsRepositoryImpl): PointsRepository

    @Binds
    abstract fun provideMapper(mapper: UIMapper): LoadPointResult.Mapper<UIState>
}