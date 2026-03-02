package com.mikeisesele.clearr.di

import android.content.Context
import androidx.room.Room
import com.mikeisesele.clearr.data.database.ClearrDatabase
import com.mikeisesele.clearr.data.local.room.ClearrSharedDatabase
import com.mikeisesele.clearr.data.local.room.createAndroidClearrSharedDatabase
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
    fun provideLegacyDatabase(@ApplicationContext context: Context): ClearrDatabase =
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
}
