package com.mikeisesele.clearr.domain.repository

import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.Tracker
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getTrackerByIdFlow(id: Long): Flow<Tracker?>
    fun getBudgetPeriods(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetPeriod>>
    suspend fun ensureBudgetPeriods(trackerId: Long, frequency: BudgetFrequency)
    fun getBudgetCategories(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetCategory>>
    suspend fun getBudgetMaxSortOrder(trackerId: Long, frequency: BudgetFrequency): Int
    suspend fun addBudgetCategory(category: BudgetCategory): Long
    suspend fun updateBudgetCategory(category: BudgetCategory)
    suspend fun deleteBudgetCategory(categoryId: Long)
    suspend fun reorderBudgetCategories(trackerId: Long, frequency: BudgetFrequency, orderedIds: List<Long>)
    fun getBudgetCategoryPlansForTracker(trackerId: Long): Flow<List<BudgetCategoryPlan>>
    suspend fun getBudgetCategoryPlansForPeriod(periodId: Long): List<BudgetCategoryPlan>
    suspend fun saveBudgetCategoryPlans(periodId: Long, plans: List<BudgetCategoryPlan>)
    fun getBudgetEntriesForTracker(trackerId: Long): Flow<List<BudgetEntry>>
    suspend fun addBudgetEntry(entry: BudgetEntry): Long
}
