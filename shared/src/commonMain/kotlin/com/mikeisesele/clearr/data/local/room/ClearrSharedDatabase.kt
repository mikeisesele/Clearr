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
        TodoRoomEntity::class,
        GoalRoomEntity::class,
        GoalCompletionRoomEntity::class
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ],
    exportSchema = true
)
@ConstructedBy(ClearrSharedDatabaseConstructor::class)
abstract class ClearrSharedDatabase : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigRoomDao
    abstract fun trackerDao(): TrackerRoomDao
    abstract fun todoDao(): TodoRoomDao
    abstract fun goalDao(): GoalRoomDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ClearrSharedDatabaseConstructor : RoomDatabaseConstructor<ClearrSharedDatabase> {
    override fun initialize(): ClearrSharedDatabase
}
