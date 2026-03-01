package com.mikeisesele.clearr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikeisesele.clearr.core.time.nowEpochMillis
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType

@Entity(tableName = "trackers")
data class TrackerRoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TrackerType = TrackerType.BUDGET,
    val frequency: Frequency = Frequency.MONTHLY,
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val defaultAmount: Double = 0.0,
    val isNew: Boolean = false,
    val createdAt: Long = nowEpochMillis()
)

fun TrackerRoomEntity.toDomain(): Tracker = Tracker(
    id = id,
    name = name,
    type = type,
    frequency = frequency,
    layoutStyle = layoutStyle,
    defaultAmount = defaultAmount,
    isNew = isNew,
    createdAt = createdAt
)

fun Tracker.toRoomEntity(): TrackerRoomEntity = TrackerRoomEntity(
    id = id,
    name = name,
    type = type,
    frequency = frequency,
    layoutStyle = layoutStyle,
    defaultAmount = defaultAmount,
    isNew = isNew,
    createdAt = createdAt
)
