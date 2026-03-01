package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.preview.SampleClearrRuntime

class IosClearrRuntime(
    private val delegate: ClearrRuntime = SampleClearrRuntime()
) : ClearrRuntime by delegate
