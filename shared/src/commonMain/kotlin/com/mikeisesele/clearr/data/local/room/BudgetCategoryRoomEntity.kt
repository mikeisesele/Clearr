package com.mikeisesele.clearr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetFrequency

@Entity(tableName = "budget_categories")
data class BudgetCategoryRoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val frequency: BudgetFrequency,
    val name: String,
    val icon: String,
    val colorToken: String,
    val plannedAmountKobo: Long,
    val sortOrder: Int,
    val createdAt: Long
)

fun BudgetCategoryRoomEntity.toDomain(): BudgetCategory = BudgetCategory(
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

fun BudgetCategory.toRoomEntity(): BudgetCategoryRoomEntity = BudgetCategoryRoomEntity(
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
