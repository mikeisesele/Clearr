package com.mikeisesele.clearr.domain.repository

import com.mikeisesele.clearr.data.model.AppConfig
import kotlinx.coroutines.flow.Flow

interface AppConfigRepository {
    fun getAppConfigFlow(): Flow<AppConfig?>
    suspend fun getAppConfig(): AppConfig?
    suspend fun upsertAppConfig(config: AppConfig)
}
