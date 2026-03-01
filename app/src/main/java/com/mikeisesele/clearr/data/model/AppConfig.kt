@file:JvmName("AppConfigEntityMappers")

package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.jvm.JvmName

@Entity(tableName = "app_config")
data class AppConfigEntity(
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

fun AppConfigEntity.toDomain(): AppConfig = AppConfig(
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

fun AppConfig.toEntity(): AppConfigEntity = AppConfigEntity(
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
