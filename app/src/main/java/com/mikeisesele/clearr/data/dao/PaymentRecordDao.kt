package com.mikeisesele.clearr.data.dao

import androidx.room.*
import com.mikeisesele.clearr.data.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRecordDao {

    @Query("SELECT * FROM payment_records WHERE memberId = :memberId AND year = :year AND monthIndex = :monthIndex AND isUndone = 0")
    fun getPaymentsForMonth(memberId: Long, year: Int, monthIndex: Int): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE memberId = :memberId AND year = :year AND isUndone = 0 ORDER BY monthIndex ASC, paidAt ASC")
    fun getPaymentsForMemberYear(memberId: Long, year: Int): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE year = :year AND isUndone = 0")
    fun getPaymentsForYear(year: Int): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE memberId = :memberId AND year = :year AND monthIndex = :monthIndex AND isUndone = 0 ORDER BY paidAt DESC LIMIT 1")
    suspend fun getLatestPayment(memberId: Long, year: Int, monthIndex: Int): PaymentRecord?

    @Query("SELECT COALESCE(SUM(amountPaid), 0.0) FROM payment_records WHERE memberId = :memberId AND year = :year AND monthIndex = :monthIndex AND isUndone = 0")
    suspend fun getTotalPaidForMonth(memberId: Long, year: Int, monthIndex: Int): Double

    @Query("SELECT COALESCE(SUM(amountPaid), 0.0) FROM payment_records WHERE year = :year AND isUndone = 0")
    fun getTotalCollectedForYear(year: Int): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(record: PaymentRecord): Long

    @Update
    suspend fun updatePayment(record: PaymentRecord)

    @Query("UPDATE payment_records SET isUndone = 1 WHERE id = :id")
    suspend fun undoPayment(id: Long)

    @Query("DELETE FROM payment_records WHERE memberId = :memberId AND year = :year AND monthIndex = :monthIndex")
    suspend fun deletePaymentsForMonth(memberId: Long, year: Int, monthIndex: Int)

    @Query("DELETE FROM payment_records WHERE memberId = :memberId")
    suspend fun deletePaymentsForMember(memberId: Long)

    @Query("SELECT * FROM payment_records WHERE isUndone = 0 ORDER BY paidAt DESC")
    fun getAllPayments(): Flow<List<PaymentRecord>>
}
