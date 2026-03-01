package com.mikeisesele.clearr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TrackerType

@Entity(tableName = "app_config")
data class AppConfigRoomEntity(
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

fun AppConfigRoomEntity.toDomain(): AppConfig = AppConfig(
    id = id,
    groupName = groupName,
    adminName = adminName,
    adminPhone = adminPhone,
    trackerType = trackerType,
    frequency = frequency,
    defaultAmount = defaultAmount,
    customPeriodLabels = customPeriodLabels,
    variableAmounts = variableAmounts,
    layoutStyle = layoutStyle,
    remindersEnabled = remindersEnabled,
    reminderDayOfPeriod = reminderDayOfPeriod,
    setupComplete = setupComplete
)

fun AppConfig.toRoomEntity(): AppConfigRoomEntity = AppConfigRoomEntity(
    id = id,
    groupName = groupName,
    adminName = adminName,
    adminPhone = adminPhone,
    trackerType = trackerType,
    frequency = frequency,
    defaultAmount = defaultAmount,
    customPeriodLabels = customPeriodLabels,
    variableAmounts = variableAmounts,
    layoutStyle = layoutStyle,
    remindersEnabled = remindersEnabled,
    reminderDayOfPeriod = reminderDayOfPeriod,
    setupComplete = setupComplete
)
