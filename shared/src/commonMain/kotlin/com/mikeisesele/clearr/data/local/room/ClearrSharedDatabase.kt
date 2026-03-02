package com.mikeisesele.clearr.data.local.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.AutoMigration

@Database(
    entities = [
        AppConfigRoomEntity::class,
        TrackerRoomEntity::class,
        TodoRoomEntity::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
@ConstructedBy(ClearrSharedDatabaseConstructor::class)
abstract class ClearrSharedDatabase : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigRoomDao
    abstract fun trackerDao(): TrackerRoomDao
    abstract fun todoDao(): TodoRoomDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ClearrSharedDatabaseConstructor : RoomDatabaseConstructor<ClearrSharedDatabase> {
    override fun initialize(): ClearrSharedDatabase
}
