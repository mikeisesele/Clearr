package com.mikeisesele.clearr.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetRoomDao {
    @Query("SELECT * FROM budget_periods WHERE trackerId = :trackerId AND frequency = :frequency ORDER BY startDate ASC")
    fun getBudgetPeriods(trackerId: Long, frequency: com.mikeisesele.clearr.data.model.BudgetFrequency): Flow<List<BudgetPeriodRoomEntity>>

    @Query("SELECT * FROM budget_periods WHERE trackerId = :trackerId AND frequency = :frequency ORDER BY startDate ASC")
    suspend fun getBudgetPeriodsOnce(trackerId: Long, frequency: com.mikeisesele.clearr.data.model.BudgetFrequency): List<BudgetPeriodRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetPeriods(periods: List<BudgetPeriodRoomEntity>)

    @Query("DELETE FROM budget_periods WHERE trackerId = :trackerId")
    suspend fun deleteBudgetPeriodsForTracker(trackerId: Long)

    @Query("SELECT * FROM budget_categories WHERE trackerId = :trackerId AND frequency = :frequency ORDER BY sortOrder ASC")
    fun getBudgetCategories(trackerId: Long, frequency: com.mikeisesele.clearr.data.model.BudgetFrequency): Flow<List<BudgetCategoryRoomEntity>>

    @Query("SELECT MAX(sortOrder) FROM budget_categories WHERE trackerId = :trackerId AND frequency = :frequency")
    suspend fun getBudgetMaxSortOrder(trackerId: Long, frequency: com.mikeisesele.clearr.data.model.BudgetFrequency): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCategory(category: BudgetCategoryRoomEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCategories(categories: List<BudgetCategoryRoomEntity>)

    @Update
    suspend fun updateBudgetCategory(category: BudgetCategoryRoomEntity)

    @Query("DELETE FROM budget_categories WHERE id = :categoryId")
    suspend fun deleteBudgetCategory(categoryId: Long)

    @Query("DELETE FROM budget_categories WHERE trackerId = :trackerId")
    suspend fun deleteBudgetCategoriesForTracker(trackerId: Long)

    @Query("UPDATE budget_categories SET sortOrder = :sortOrder WHERE id = :categoryId")
    suspend fun updateBudgetCategorySortOrder(categoryId: Long, sortOrder: Int)

    @Query("SELECT * FROM budget_category_plans WHERE trackerId = :trackerId")
    fun getBudgetCategoryPlansForTracker(trackerId: Long): Flow<List<BudgetCategoryPlanRoomEntity>>

    @Query("SELECT * FROM budget_category_plans WHERE periodId = :periodId")
    suspend fun getBudgetCategoryPlansForPeriod(periodId: Long): List<BudgetCategoryPlanRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCategoryPlans(plans: List<BudgetCategoryPlanRoomEntity>)

    @Query("DELETE FROM budget_category_plans WHERE periodId = :periodId")
    suspend fun deleteBudgetCategoryPlansForPeriod(periodId: Long)

    @Query("DELETE FROM budget_category_plans WHERE categoryId = :categoryId")
    suspend fun deleteBudgetCategoryPlansForCategory(categoryId: Long)

    @Query("DELETE FROM budget_category_plans WHERE trackerId = :trackerId")
    suspend fun deleteBudgetCategoryPlansForTracker(trackerId: Long)

    @Query("SELECT * FROM budget_entries WHERE trackerId = :trackerId")
    fun getBudgetEntriesForTracker(trackerId: Long): Flow<List<BudgetEntryRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetEntry(entry: BudgetEntryRoomEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetEntries(entries: List<BudgetEntryRoomEntity>)

    @Query("DELETE FROM budget_entries WHERE categoryId = :categoryId")
    suspend fun deleteBudgetEntriesForCategory(categoryId: Long)

    @Query("DELETE FROM budget_entries WHERE trackerId = :trackerId")
    suspend fun deleteBudgetEntriesForTracker(trackerId: Long)

    @Query("SELECT COUNT(*) FROM budget_periods")
    suspend fun countBudgetPeriods(): Int
}
