package com.mikeisesele.clearr.data.local.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [
        AppConfigRoomEntity::class,
        TrackerRoomEntity::class
    ],
    version = 1,
    exportSchema = true
)
@ConstructedBy(ClearrSharedDatabaseConstructor::class)
abstract class ClearrSharedDatabase : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigRoomDao
    abstract fun trackerDao(): TrackerRoomDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ClearrSharedDatabaseConstructor : RoomDatabaseConstructor<ClearrSharedDatabase> {
    override fun initialize(): ClearrSharedDatabase
}
