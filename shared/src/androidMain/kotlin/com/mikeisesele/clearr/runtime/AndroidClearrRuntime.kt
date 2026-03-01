package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.preview.SampleClearrRuntime

class AndroidClearrRuntime(
    private val delegate: ClearrRuntime = SampleClearrRuntime()
) : ClearrRuntime by delegate
