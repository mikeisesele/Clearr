package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton configuration table – always id = 1.
 * Stores all settings from the Setup Wizard and can be edited later via Settings.
 */
@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,

    // Group identity
    val groupName: String = "JSS Durumi Brothers",
    val adminName: String = "",
    val adminPhone: String = "",

    // Tracker behavior
    val trackerType: TrackerType = TrackerType.DUES,
    val frequency: Frequency = Frequency.MONTHLY,

    // Amount
    val defaultAmount: Double = 5000.0,

    // Custom frequency config (JSON arrays stored as strings)
    val customPeriodLabels: String = "[]",  // e.g. ["Term 1","Term 2","Term 3"]
    val variableAmounts: String = "[]",     // e.g. [5000,7000,6000] per period

    // UI Layout style
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,

    // Reminder notifications
    val remindersEnabled: Boolean = true,
    val reminderDayOfPeriod: Int = 5,  // day number within period to send reminder

    // Wizard
    val setupComplete: Boolean = false
)

enum class TrackerType {
    DUES,       // Group financial obligations
    GOALS,      // Personal goals / recurring habits
    TODO,       // Personal to-do / task list
    BUDGET      // Planned vs actual spending
}

enum class Frequency {
    MONTHLY,
    WEEKLY,
    QUARTERLY,   // 4 periods/year (Jan, Apr, Jul, Oct)
    TERMLY,      // 3 periods/year (school terms)
    BIANNUAL,    // 2 periods/year
    ANNUAL,      // 1 period/year
    CUSTOM       // user-defined period labels
}

enum class LayoutStyle {
    GRID,     // default horizontal scrolling grid
    KANBAN,   // columns per period, members as cards
    CARDS,    // member cards with period chips
    RECEIPT   // ledger / receipt style
}
