package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.preview.SampleClearrRuntime

actual fun createPlatformRuntime(): ClearrRuntime = SampleClearrRuntime()
