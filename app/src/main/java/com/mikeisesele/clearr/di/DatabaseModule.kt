package com.mikeisesele.clearr.di

import android.content.Context
import androidx.room.Room
import com.mikeisesele.clearr.data.local.room.ClearrSharedDatabase
import com.mikeisesele.clearr.data.local.room.createAndroidClearrSharedDatabase
import com.mikeisesele.clearr.data.dao.AppConfigDao
import com.mikeisesele.clearr.data.dao.BudgetDao
import com.mikeisesele.clearr.data.dao.GoalsDao
import com.mikeisesele.clearr.data.dao.TrackerDao
import com.mikeisesele.clearr.data.dao.TodoDao
import com.mikeisesele.clearr.data.database.ClearrDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ClearrDatabase =
        Room.databaseBuilder(
            context,
            ClearrDatabase::class.java,
            "clearr_database"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideSharedDatabase(@ApplicationContext context: Context): ClearrSharedDatabase =
        createAndroidClearrSharedDatabase(context)

    @Provides
    fun provideAppConfigDao(db: ClearrDatabase): AppConfigDao = db.appConfigDao()

    @Provides
    fun provideTrackerDao(db: ClearrDatabase): TrackerDao = db.trackerDao()

    @Provides
    fun provideBudgetDao(db: ClearrDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideTodoDao(db: ClearrDatabase): TodoDao = db.todoDao()

    @Provides
    fun provideGoalsDao(db: ClearrDatabase): GoalsDao = db.goalsDao()
}
