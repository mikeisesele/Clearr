package com.mikeisesele.clearr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod

@Entity(tableName = "budget_periods")
data class BudgetPeriodRoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val frequency: BudgetFrequency,
    val label: String,
    val startDate: Long,
    val endDate: Long
)

fun BudgetPeriodRoomEntity.toDomain(): BudgetPeriod = BudgetPeriod(
    id = id,
    trackerId = trackerId,
    frequency = frequency,
    label = label,
    startDate = startDate,
    endDate = endDate
)

fun BudgetPeriod.toRoomEntity(): BudgetPeriodRoomEntity = BudgetPeriodRoomEntity(
    id = id,
    trackerId = trackerId,
    frequency = frequency,
    label = label,
    startDate = startDate,
    endDate = endDate
)
