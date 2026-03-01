package com.mikeisesele.clearr.data.local.room

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

fun buildClearrSharedDatabase(
    builder: RoomDatabase.Builder<ClearrSharedDatabase>
): ClearrSharedDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .build()
