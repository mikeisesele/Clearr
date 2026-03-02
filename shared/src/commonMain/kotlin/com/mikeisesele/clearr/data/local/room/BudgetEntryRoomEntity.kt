package com.mikeisesele.clearr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikeisesele.clearr.data.model.BudgetEntry

@Entity(tableName = "budget_entries")
data class BudgetEntryRoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val categoryId: Long,
    val periodId: Long,
    val amountKobo: Long,
    val note: String? = null,
    val loggedAt: Long
)

fun BudgetEntryRoomEntity.toDomain(): BudgetEntry = BudgetEntry(
    id = id,
    trackerId = trackerId,
    categoryId = categoryId,
    periodId = periodId,
    amountKobo = amountKobo,
    note = note,
    loggedAt = loggedAt
)

fun BudgetEntry.toRoomEntity(): BudgetEntryRoomEntity = BudgetEntryRoomEntity(
    id = id,
    trackerId = trackerId,
    categoryId = categoryId,
    periodId = periodId,
    amountKobo = amountKobo,
    note = note,
    loggedAt = loggedAt
)
