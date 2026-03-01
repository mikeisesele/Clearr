package com.mikeisesele.clearr.di

import com.mikeisesele.clearr.domain.repository.ClearrRepository
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideTrackerBootstrapper(repository: ClearrRepository): TrackerBootstrapper =
        TrackerBootstrapper(repository)

    @Provides
    @Singleton
    fun provideObserveTrackerSummariesUseCase(repository: ClearrRepository): ObserveTrackerSummariesUseCase =
        ObserveTrackerSummariesUseCase(repository)
}
