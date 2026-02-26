package com.mikeisesele.clearr.core.ai

import android.content.Context
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object GeminiNanoEngine {

    private val initMutex = Mutex()
    @Volatile
    private var initialized = false

    suspend fun isReady(context: Context): Boolean {
        return initMutex.withLock {
            runCatching {
                val client = Generation.getClient()
                val status = client.checkStatus()
                when (status) {
                    FeatureStatus.AVAILABLE -> {
                        initialized = true
                        true
                    }
                    FeatureStatus.DOWNLOADABLE,
                    FeatureStatus.DOWNLOADING -> {
                        var completed = false
                        client.download().collectLatest { dl: DownloadStatus ->
                            when (dl) {
                                is DownloadStatus.DownloadCompleted -> completed = true
                                is DownloadStatus.DownloadFailed -> completed = false
                                else -> Unit
                            }
                        }
                        initialized = completed
                        completed
                    }
                    else -> {
                        initialized = false
                        false
                    }
                }
            }.getOrElse {
                initialized = false
                false
            }
        }
    }

    suspend fun generateText(
        context: Context,
        prompt: String
    ): String? {
        if (!initialized && !isReady(context)) return null
        return runCatching {
            val client = Generation.getClient()
            val response = client.generateContent(prompt)
            val candidate = response.candidates.firstOrNull()
            val text = runCatching {
                val method = candidate?.javaClass?.methods?.firstOrNull { it.name == "getText" && it.parameterCount == 0 }
                method?.invoke(candidate) as? String
            }.getOrNull()?.trim()
            text?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
