package com.mikeisesele.clearr.runtime

class IosClearrRuntime(
    private val delegate: ClearrRuntime = InMemoryClearrRuntime()
) : ClearrRuntime by delegate
