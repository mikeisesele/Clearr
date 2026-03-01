package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.preview.InMemoryClearrRepository
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.gettimeofday
import platform.posix.timeval

@OptIn(ExperimentalForeignApi::class)
class IosClearrRuntime(
    private val delegate: ClearrRuntime = InMemoryClearrRuntime(
        repository = InMemoryClearrRepository.empty(),
        nowMillis = {
            memScoped {
                val tv = alloc<timeval>()
                gettimeofday(tv.ptr, null)
                (tv.tv_sec * 1000L) + (tv.tv_usec / 1000L)
            }
        }
    )
) : ClearrRuntime by delegate
