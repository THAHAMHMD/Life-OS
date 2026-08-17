package com.bloomhaven.app.feature.commitments

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CommitmentsDao {
    @Insert
    suspend fun insert(commitment: CommitmentEntity): Long

    @Update
    suspend fun update(commitment: CommitmentEntity)

    @Delete
    suspend fun delete(commitment: CommitmentEntity)

    @Query("SELECT * FROM commitments WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun activeCommitments(): Flow<List<CommitmentEntity>>

    @Insert
    suspend fun insertPayment(payment: CommitmentPaymentEntity): Long

    @Query("DELETE FROM commitment_payments WHERE commitmentId = :commitmentId")
    suspend fun deletePaymentsForCommitment(commitmentId: Long)

    @Query(
        """
        SELECT p.id AS id, p.commitmentId AS commitmentId, p.paidDate AS paidDate, p.amount AS amount, c.name AS commitmentName
        FROM commitment_payments p
        INNER JOIN commitments c ON c.id = p.commitmentId
        ORDER BY p.paidDate DESC, p.id DESC
        """,
    )
    fun paymentHistory(): Flow<List<CommitmentPaymentWithName>>
}

/** Payment history row joined with its commitment's name — backs the collapsible History section. */
data class CommitmentPaymentWithName(
    val id: Long,
    val commitmentId: Long,
    val paidDate: LocalDate,
    val amount: Double,
    val commitmentName: String,
)
