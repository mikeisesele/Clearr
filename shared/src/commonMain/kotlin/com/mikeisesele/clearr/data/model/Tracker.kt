package com.mikeisesele.clearr.data.model

data class Tracker(
    val id: Long = 0,
    val name: String,
    val type: TrackerType = TrackerType.BUDGET,
    val frequency: Frequency = Frequency.MONTHLY,
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val defaultAmount: Double = 0.0,
    val isNew: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class TrackerSummary(
    val trackerId: Long,
    val name: String,
    val type: TrackerType,
    val frequency: Frequency,
    val currentPeriodLabel: String,
    val totalMembers: Int,
    val completedCount: Int,
    val completionPercent: Int,
    val amountCompletedKobo: Long = 0L,
    val amountTargetKobo: Long = 0L,
    val isNew: Boolean,
    val createdAt: Long
)
