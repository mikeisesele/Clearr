package com.mikeisesele.clearr.di

import com.mikeisesele.clearr.data.repository.ClearrRepositoryImpl
import com.mikeisesele.clearr.data.repository.TodoPreferencesRepository
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository as TodoPreferencesContract
import com.mikeisesele.clearr.ui.feature.todo.AndroidTodoAiService
import com.mikeisesele.clearr.ui.feature.todo.TodoAiService
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

    @Binds
    @Singleton
    abstract fun bindTodoPreferencesRepository(impl: TodoPreferencesRepository): TodoPreferencesContract

    @Binds
    @Singleton
    abstract fun bindTodoAiService(impl: AndroidTodoAiService): TodoAiService
}
