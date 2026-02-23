package com.mikeisesele.clearr.di

import com.mikeisesele.clearr.data.repository.DuesRepositoryImpl
import com.mikeisesele.clearr.domain.repository.DuesRepository
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
    abstract fun bindDuesRepository(impl: DuesRepositoryImpl): DuesRepository
}
