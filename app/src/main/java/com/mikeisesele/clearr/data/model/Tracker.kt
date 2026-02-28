package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A Tracker is an independent tracking unit.
 * Each tracker owns its own member list (TrackerMember) and period records (TrackerPeriod + TrackerRecord).
 * It is completely isolated from other trackers.
 */
@Entity(tableName = "trackers")
data class Tracker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TrackerType = TrackerType.DUES,
    val frequency: Frequency = Frequency.MONTHLY,
    /** Per-tracker layout style (independent from other trackers). */
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    /** Default amount per period (only relevant for DUES type) */
    val defaultAmount: Double = 5000.0,
    /** True until the tracker is first opened after creation */
    val isNew: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A member belonging to exactly one tracker.
 * Member lists are per-tracker and do not share with other trackers.
 */
@Entity(tableName = "tracker_members")
data class TrackerMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val name: String,
    val phone: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A Period represents one cycle of tracking for a given tracker.
 * e.g. "February 2026", "Week 8, 2026", "Q1 2026", "Term 1 2026"
 * Periods are generated automatically based on tracker frequency.
 */
@Entity(tableName = "tracker_periods")
data class TrackerPeriod(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    /** Human-readable label: "February 2026", "Week 8, 2026", etc. */
    val label: String,
    val startDate: Long,
    val endDate: Long,
    /** True = this is the period currently active based on the date */
    val isCurrent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A Record is one member's status for one period.
 * status depends on tracker type:
 *  DUES       → PAID / PARTIAL / UNPAID
 *  ATTENDANCE → PRESENT / ABSENT
 *  TASKS      → DONE / PENDING
 *  EVENTS     → PRESENT / ABSENT
 */
@Entity(tableName = "tracker_records")
data class TrackerRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackerId: Long,
    val periodId: Long,
    val memberId: Long,
    val status: RecordStatus = RecordStatus.UNPAID,
    /** Amount paid — only meaningful for DUES type */
    val amountPaid: Double = 0.0,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RecordStatus {
    /** DUES */
    PAID, PARTIAL, UNPAID,
    /** ATTENDANCE / EVENTS */
    PRESENT, ABSENT,
    /** TASKS */
    DONE, PENDING
}

/** Aggregated summary emitted by the DAO for the tracker list card */
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
