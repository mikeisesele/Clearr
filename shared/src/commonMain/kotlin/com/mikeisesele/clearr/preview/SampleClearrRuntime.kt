package com.mikeisesele.clearr.preview

import com.mikeisesele.clearr.runtime.ClearrRuntime
import com.mikeisesele.clearr.runtime.InMemoryClearrRuntime

class SampleClearrRuntime(
    private val delegate: ClearrRuntime = InMemoryClearrRuntime()
) : ClearrRuntime by delegate
