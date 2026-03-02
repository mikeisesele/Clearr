package com.mikeisesele.clearr.data.local.room

import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.domain.budget.BudgetPeriodPlanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomBudgetRepository(
    private val budgetDao: BudgetRoomDao,
    private val periodPlanner: BudgetPeriodPlanner = BudgetPeriodPlanner()
) {
    fun getBudgetPeriods(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetPeriod>> =
        budgetDao.getBudgetPeriods(trackerId, frequency).map { list -> list.map(BudgetPeriodRoomEntity::toDomain) }

    suspend fun ensureBudgetPeriods(trackerId: Long, frequency: BudgetFrequency) {
        val existing = budgetDao.getBudgetPeriodsOnce(trackerId, frequency).map(BudgetPeriodRoomEntity::toDomain)
        val periods = when {
            existing.isEmpty() -> periodPlanner.initialPeriods(trackerId, frequency)
            else -> periodPlanner.missingPeriods(trackerId, frequency, existing.maxBy { it.startDate })
        }
        if (periods.isNotEmpty()) {
            budgetDao.insertBudgetPeriods(periods.map(BudgetPeriod::toRoomEntity))
        }
    }

    fun getBudgetCategories(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetCategory>> =
        budgetDao.getBudgetCategories(trackerId, frequency).map { list -> list.map(BudgetCategoryRoomEntity::toDomain) }

    suspend fun getBudgetMaxSortOrder(trackerId: Long, frequency: BudgetFrequency): Int =
        budgetDao.getBudgetMaxSortOrder(trackerId, frequency) ?: -1

    suspend fun addBudgetCategory(category: BudgetCategory): Long =
        budgetDao.insertBudgetCategory(category.toRoomEntity())

    suspend fun updateBudgetCategory(category: BudgetCategory) {
        budgetDao.updateBudgetCategory(category.toRoomEntity())
    }

    suspend fun deleteBudgetCategory(categoryId: Long) {
        budgetDao.deleteBudgetCategoryPlansForCategory(categoryId)
        budgetDao.deleteBudgetEntriesForCategory(categoryId)
        budgetDao.deleteBudgetCategory(categoryId)
    }

    suspend fun reorderBudgetCategories(trackerId: Long, frequency: BudgetFrequency, orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, categoryId ->
            budgetDao.updateBudgetCategorySortOrder(categoryId, index)
        }
    }

    fun getBudgetCategoryPlansForTracker(trackerId: Long): Flow<List<BudgetCategoryPlan>> =
        budgetDao.getBudgetCategoryPlansForTracker(trackerId).map { list -> list.map(BudgetCategoryPlanRoomEntity::toDomain) }

    suspend fun getBudgetCategoryPlansForPeriod(periodId: Long): List<BudgetCategoryPlan> =
        budgetDao.getBudgetCategoryPlansForPeriod(periodId).map(BudgetCategoryPlanRoomEntity::toDomain)

    suspend fun saveBudgetCategoryPlans(periodId: Long, plans: List<BudgetCategoryPlan>) {
        budgetDao.deleteBudgetCategoryPlansForPeriod(periodId)
        if (plans.isNotEmpty()) {
            budgetDao.insertBudgetCategoryPlans(plans.map(BudgetCategoryPlan::toRoomEntity))
        }
    }

    fun getBudgetEntriesForTracker(trackerId: Long): Flow<List<BudgetEntry>> =
        budgetDao.getBudgetEntriesForTracker(trackerId).map { list -> list.map(BudgetEntryRoomEntity::toDomain) }

    suspend fun addBudgetEntry(entry: BudgetEntry): Long =
        budgetDao.insertBudgetEntry(entry.toRoomEntity())

    suspend fun deleteBudgetDataForTracker(trackerId: Long) {
        budgetDao.deleteBudgetEntriesForTracker(trackerId)
        budgetDao.deleteBudgetCategoryPlansForTracker(trackerId)
        budgetDao.deleteBudgetCategoriesForTracker(trackerId)
        budgetDao.deleteBudgetPeriodsForTracker(trackerId)
    }

    suspend fun isEmpty(): Boolean = budgetDao.countBudgetPeriods() == 0

    suspend fun seedBudgetData(
        periods: List<BudgetPeriod>,
        categories: List<BudgetCategory>,
        plans: List<BudgetCategoryPlan>,
        entries: List<BudgetEntry>
    ) {
        if (periods.isNotEmpty()) budgetDao.insertBudgetPeriods(periods.map(BudgetPeriod::toRoomEntity))
        if (categories.isNotEmpty()) budgetDao.insertBudgetCategories(categories.map(BudgetCategory::toRoomEntity))
        if (plans.isNotEmpty()) budgetDao.insertBudgetCategoryPlans(plans.map(BudgetCategoryPlan::toRoomEntity))
        if (entries.isNotEmpty()) budgetDao.insertBudgetEntries(entries.map(BudgetEntry::toRoomEntity))
    }
}
