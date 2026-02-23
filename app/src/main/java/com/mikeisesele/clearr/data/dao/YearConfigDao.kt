package com.mikeisesele.clearr.data.dao

import androidx.room.*
import com.mikeisesele.clearr.data.model.YearConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface YearConfigDao {

    @Query("SELECT * FROM year_configs ORDER BY year DESC")
    fun getAllYearConfigs(): Flow<List<YearConfig>>

    @Query("SELECT * FROM year_configs WHERE year = :year")
    suspend fun getYearConfig(year: Int): YearConfig?

    @Query("SELECT * FROM year_configs WHERE year = :year")
    fun getYearConfigFlow(year: Int): Flow<YearConfig?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertYearConfig(config: YearConfig)

    @Update
    suspend fun updateYearConfig(config: YearConfig)

    @Query("UPDATE year_configs SET dueAmountPerMonth = :amount WHERE year = :year")
    suspend fun updateDueAmount(year: Int, amount: Double)
}
