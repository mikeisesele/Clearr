package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val groupName: String = "Clearr",
    val adminName: String = "",
    val adminPhone: String = "",
    val trackerType: TrackerType = TrackerType.BUDGET,
    val frequency: Frequency = Frequency.MONTHLY,
    val defaultAmount: Double = 0.0,
    val customPeriodLabels: String = "[]",
    val variableAmounts: String = "[]",
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val remindersEnabled: Boolean = false,
    val reminderDayOfPeriod: Int = 5,
    val setupComplete: Boolean = true
)

enum class TrackerType {
    GOALS,
    TODO,
    BUDGET
}

enum class Frequency {
    MONTHLY,
    WEEKLY,
    QUARTERLY,
    TERMLY,
    BIANNUAL,
    ANNUAL,
    CUSTOM
}

enum class LayoutStyle {
    GRID,
    KANBAN,
    CARDS,
    RECEIPT
}
