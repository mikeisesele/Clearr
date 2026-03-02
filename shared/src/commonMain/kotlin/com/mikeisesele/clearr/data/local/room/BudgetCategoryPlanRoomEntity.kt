package com.mikeisesele.clearr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan

@Entity(tableName = "budget_category_plans")
data class BudgetCategoryPlanRoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val categoryId: Long,
    val periodId: Long,
    val plannedAmountKobo: Long,
    val createdAt: Long
)

fun BudgetCategoryPlanRoomEntity.toDomain(): BudgetCategoryPlan = BudgetCategoryPlan(
    id = id,
    trackerId = trackerId,
    categoryId = categoryId,
    periodId = periodId,
    plannedAmountKobo = plannedAmountKobo,
    createdAt = createdAt
)

fun BudgetCategoryPlan.toRoomEntity(): BudgetCategoryPlanRoomEntity = BudgetCategoryPlanRoomEntity(
    id = id,
    trackerId = trackerId,
    categoryId = categoryId,
    periodId = periodId,
    plannedAmountKobo = plannedAmountKobo,
    createdAt = createdAt
)
