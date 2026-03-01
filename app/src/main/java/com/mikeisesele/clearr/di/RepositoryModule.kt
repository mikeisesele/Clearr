package com.mikeisesele.clearr.di

import com.mikeisesele.clearr.data.repository.ClearrRepositoryImpl
import com.mikeisesele.clearr.domain.repository.ClearrRepository
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
    abstract fun bindClearrRepository(impl: ClearrRepositoryImpl): ClearrRepository
}
