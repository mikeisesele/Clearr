package com.mikeisesele.clearr.runtime

actual fun createPlatformRuntime(): ClearrRuntime = AndroidClearrRuntime()
