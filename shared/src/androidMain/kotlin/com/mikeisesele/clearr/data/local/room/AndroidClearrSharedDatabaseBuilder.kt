package com.mikeisesele.clearr.data.local.room

import android.content.Context
import androidx.room.Room

private const val CLEAR_SHARED_DB_NAME = "clearr_shared.db"

fun createAndroidClearrSharedDatabase(context: Context): ClearrSharedDatabase =
    buildClearrSharedDatabase(
        Room.databaseBuilder(
            context.applicationContext,
            ClearrSharedDatabase::class.java,
            CLEAR_SHARED_DB_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
    )
