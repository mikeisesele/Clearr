package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "year_configs")
data class YearConfig(
    @PrimaryKey
    val year: Int,
    val dueAmountPerMonth: Double = 5000.0,
    val startedAt: Long = System.currentTimeMillis()
)
