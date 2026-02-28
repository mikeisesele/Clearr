package com.mikeisesele.clearr.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budget_periods WHERE trackerId = :trackerId AND frequency = :frequency ORDER BY startDate ASC")
    fun getPeriods(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetPeriod>>

    @Query("SELECT * FROM budget_periods WHERE trackerId = :trackerId AND frequency = :frequency ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestPeriod(trackerId: Long, frequency: BudgetFrequency): BudgetPeriod?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriods(periods: List<BudgetPeriod>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: BudgetPeriod): Long

    @Query("SELECT * FROM budget_categories WHERE trackerId = :trackerId AND frequency = :frequency ORDER BY sortOrder ASC, createdAt ASC")
    fun getCategories(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetCategory>>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM budget_categories WHERE trackerId = :trackerId AND frequency = :frequency")
    suspend fun getMaxSortOrder(trackerId: Long, frequency: BudgetFrequency): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: BudgetCategory): Long

    @Update
    suspend fun updateCategory(category: BudgetCategory)

    @Query("DELETE FROM budget_categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Long)

    @Query("UPDATE budget_categories SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateCategorySortOrder(id: Long, sortOrder: Int)

    @Query("SELECT * FROM budget_entries WHERE trackerId = :trackerId ORDER BY loggedAt ASC")
    fun getEntriesForTracker(trackerId: Long): Flow<List<BudgetEntry>>

    @Query("SELECT * FROM budget_category_plans WHERE trackerId = :trackerId ORDER BY createdAt ASC")
    fun getCategoryPlansForTracker(trackerId: Long): Flow<List<BudgetCategoryPlan>>

    @Query("SELECT * FROM budget_category_plans WHERE periodId = :periodId ORDER BY createdAt ASC")
    suspend fun getCategoryPlansForPeriod(periodId: Long): List<BudgetCategoryPlan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryPlan(plan: BudgetCategoryPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryPlans(plans: List<BudgetCategoryPlan>)

    @Query("DELETE FROM budget_category_plans WHERE periodId = :periodId")
    suspend fun deleteCategoryPlansForPeriod(periodId: Long)

    @Query("DELETE FROM budget_category_plans WHERE categoryId = :categoryId")
    suspend fun deleteCategoryPlansByCategory(categoryId: Long)

    @Query("DELETE FROM budget_category_plans WHERE trackerId = :trackerId")
    suspend fun deleteCategoryPlansByTracker(trackerId: Long)

    @Query("SELECT * FROM budget_entries WHERE periodId = :periodId ORDER BY loggedAt ASC")
    fun getEntriesForPeriod(periodId: Long): Flow<List<BudgetEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: BudgetEntry): Long

    @Query("DELETE FROM budget_entries WHERE categoryId = :categoryId")
    suspend fun deleteEntriesByCategory(categoryId: Long)

    @Query("DELETE FROM budget_entries WHERE trackerId = :trackerId")
    suspend fun deleteEntriesByTracker(trackerId: Long)

    @Query("DELETE FROM budget_categories WHERE trackerId = :trackerId")
    suspend fun deleteCategoriesByTracker(trackerId: Long)

    @Query("DELETE FROM budget_periods WHERE trackerId = :trackerId")
    suspend fun deletePeriodsByTracker(trackerId: Long)
}
