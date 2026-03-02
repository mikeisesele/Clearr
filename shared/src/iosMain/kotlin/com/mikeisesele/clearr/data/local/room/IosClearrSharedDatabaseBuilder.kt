package com.mikeisesele.clearr.data.local.room

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

private const val CLEAR_SHARED_DB_NAME = "clearr_shared.db"

@OptIn(ExperimentalForeignApi::class)
fun createIosClearrSharedDatabase(): ClearrSharedDatabase {
    val supportDir = checkNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
    )
    val dbUrl = checkNotNull(supportDir.URLByAppendingPathComponent(CLEAR_SHARED_DB_NAME))
    return buildClearrSharedDatabase(
        Room.databaseBuilder<ClearrSharedDatabase>(name = dbUrl.path!!)
    )
}
