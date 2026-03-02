package com.mikeisesele.clearr.di

import com.mikeisesele.clearr.data.repository.BudgetPreferencesRepository
import com.mikeisesele.clearr.data.repository.AndroidSharedCoreRepository
import com.mikeisesele.clearr.data.repository.ClearrRepositoryImpl
import com.mikeisesele.clearr.data.repository.TodoPreferencesRepository
import com.mikeisesele.clearr.domain.budget.BudgetPeriodPlanner
import com.mikeisesele.clearr.domain.repository.AppConfigRepository
import com.mikeisesele.clearr.domain.repository.BudgetRepository
import com.mikeisesele.clearr.domain.repository.BudgetPreferencesRepository as BudgetPreferencesContract
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import com.mikeisesele.clearr.domain.repository.GoalsRepository
import com.mikeisesele.clearr.domain.repository.TodoRepository
import com.mikeisesele.clearr.ui.feature.budget.AndroidBudgetAiService
import com.mikeisesele.clearr.ui.feature.budget.BudgetAiService
import com.mikeisesele.clearr.ui.feature.goals.AndroidGoalsAiService
import com.mikeisesele.clearr.ui.feature.goals.GoalsAiService
import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository as TodoPreferencesContract
import com.mikeisesele.clearr.ui.feature.todo.AndroidTodoAiService
import com.mikeisesele.clearr.ui.feature.todo.TodoAiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindClearrRepository(impl: AndroidSharedCoreRepository): ClearrRepository

    @Binds
    @Singleton
    abstract fun bindAppConfigRepository(impl: AndroidSharedCoreRepository): AppConfigRepository

    @Binds
    @Singleton
    abstract fun bindGoalsRepository(impl: AndroidSharedCoreRepository): GoalsRepository

    @Binds
    @Singleton
    abstract fun bindTodoRepository(impl: AndroidSharedCoreRepository): TodoRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: AndroidSharedCoreRepository): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindTodoPreferencesRepository(impl: TodoPreferencesRepository): TodoPreferencesContract

    @Binds
    @Singleton
    abstract fun bindBudgetPreferencesRepository(impl: BudgetPreferencesRepository): BudgetPreferencesContract

    @Binds
    @Singleton
    abstract fun bindTodoAiService(impl: AndroidTodoAiService): TodoAiService

    @Binds
    @Singleton
    abstract fun bindBudgetAiService(impl: AndroidBudgetAiService): BudgetAiService

    @Binds
    @Singleton
    abstract fun bindGoalsAiService(impl: AndroidGoalsAiService): GoalsAiService

    companion object {
        @Provides
        @Singleton
        fun provideBudgetPeriodPlanner(): BudgetPeriodPlanner = BudgetPeriodPlanner()
    }
}
