package com.mikeisesele.clearr.data.dao

import androidx.room.*
import com.mikeisesele.clearr.data.model.AppConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfig)

    @Update
    suspend fun updateConfig(config: AppConfig)

    /** Convenience: upsert (insert or replace) */
    suspend fun upsertConfig(config: AppConfig) = insertConfig(config)
}
