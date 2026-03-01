package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "budget_periods")
data class BudgetPeriodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val frequency: BudgetFrequency,
    val label: String,
    val startDate: Long,
    val endDate: Long
)

@Entity(tableName = "budget_categories")
data class BudgetCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val frequency: BudgetFrequency,
    val name: String,
    val icon: String,
    val colorToken: String,
    val plannedAmountKobo: Long,
    val sortOrder: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budget_entries")
data class BudgetEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val categoryId: Long,
    val periodId: Long,
    val amountKobo: Long,
    val note: String? = null,
    val loggedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "budget_category_plans",
    indices = [Index(value = ["categoryId", "periodId"], unique = true)]
)
data class BudgetCategoryPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val categoryId: Long,
    val periodId: Long,
    val plannedAmountKobo: Long,
    val createdAt: Long = System.currentTimeMillis()
)

fun BudgetPeriodEntity.toDomain(): BudgetPeriod = BudgetPeriod(
    id = id,
    trackerId = trackerId,
    frequency = frequency,
    label = label,
    startDate = startDate,
    endDate = endDate
)

fun BudgetPeriod.toEntity(): BudgetPeriodEntity = BudgetPeriodEntity(
    id = id,
    trackerId = trackerId,
    frequency = frequency,
    label = label,
    startDate = startDate,
    endDate = endDate
)

fun BudgetCategoryEntity.toDomain(): BudgetCategory = BudgetCategory(
    id = id,
    trackerId = trackerId,
    frequency = frequency,
    name = name,
    icon = icon,
    colorToken = colorToken,
    plannedAmountKobo = plannedAmountKobo,
    sortOrder = sortOrder,
    createdAt = createdAt
)

fun BudgetCategory.toEntity(): BudgetCategoryEntity = BudgetCategoryEntity(
    id = id,
    trackerId = trackerId,
    frequency = frequency,
    name = name,
    icon = icon,
    colorToken = colorToken,
    plannedAmountKobo = plannedAmountKobo,
    sortOrder = sortOrder,
    createdAt = createdAt
)

fun BudgetEntryEntity.toDomain(): BudgetEntry = BudgetEntry(
    id = id,
    trackerId = trackerId,
    categoryId = categoryId,
    periodId = periodId,
    amountKobo = amountKobo,
    note = note,
    loggedAt = loggedAt
)

fun BudgetEntry.toEntity(): BudgetEntryEntity = BudgetEntryEntity(
    id = id,
    trackerId = trackerId,
    categoryId = categoryId,
    periodId = periodId,
    amountKobo = amountKobo,
    note = note,
    loggedAt = loggedAt
)

fun BudgetCategoryPlanEntity.toDomain(): BudgetCategoryPlan = BudgetCategoryPlan(
    id = id,
    trackerId = trackerId,
    categoryId = categoryId,
    periodId = periodId,
    plannedAmountKobo = plannedAmountKobo,
    createdAt = createdAt
)

fun BudgetCategoryPlan.toEntity(): BudgetCategoryPlanEntity = BudgetCategoryPlanEntity(
    id = id,
    trackerId = trackerId,
    categoryId = categoryId,
    periodId = periodId,
    plannedAmountKobo = plannedAmountKobo,
    createdAt = createdAt
)
