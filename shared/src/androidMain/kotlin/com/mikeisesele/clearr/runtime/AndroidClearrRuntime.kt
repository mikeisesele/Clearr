package com.mikeisesele.clearr.runtime

class AndroidClearrRuntime(
    private val delegate: ClearrRuntime = InMemoryClearrRuntime()
) : ClearrRuntime by delegate
