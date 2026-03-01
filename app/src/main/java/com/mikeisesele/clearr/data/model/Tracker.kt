package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trackers")
data class TrackerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TrackerType = TrackerType.BUDGET,
    val frequency: Frequency = Frequency.MONTHLY,
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val defaultAmount: Double = 0.0,
    val isNew: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun TrackerEntity.toDomain(): Tracker = Tracker(
    id = id,
    name = name,
    type = type,
    frequency = frequency,
    layoutStyle = layoutStyle,
    defaultAmount = defaultAmount,
    isNew = isNew,
    createdAt = createdAt
)

fun Tracker.toEntity(): TrackerEntity = TrackerEntity(
    id = id,
    name = name,
    type = type,
    frequency = frequency,
    layoutStyle = layoutStyle,
    defaultAmount = defaultAmount,
    isNew = isNew,
    createdAt = createdAt
)
