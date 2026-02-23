package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_records",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val year: Int,
    val monthIndex: Int,
    val amountPaid: Double,
    val expectedAmount: Double,
    val paidAt: Long = System.currentTimeMillis(),
    val note: String? = null,
    val isUndone: Boolean = false
)
