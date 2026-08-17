package com.bloomhaven.app.feature.commitments

import android.content.Context
import com.bloomhaven.app.core.notifications.ReminderScheduler
import com.bloomhaven.app.core.util.CurrencyUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Single source of truth for the user's recurring financial obligations — subscriptions and
 * bills. Used by (but does not depend on) Money Haven, so it stays fully self-contained.
 */
class CommitmentsRepository(
    private val context: Context,
    private val dao: CommitmentsDao,
) {
    fun activeCommitments(): Flow<List<CommitmentEntity>> = dao.activeCommitments()

    fun paymentHistory(): Flow<List<CommitmentPaymentWithName>> = dao.paymentHistory()

    /** Inserts a new commitment or updates an existing one (id == 0 means "new"), then
     *  (re)schedules or cancels its reminder to match [CommitmentEntity.remindersEnabled]. */
    suspend fun save(commitment: CommitmentEntity): Long {
        val id = if (commitment.id == 0L) {
            dao.insert(commitment)
        } else {
            dao.update(commitment)
            commitment.id
        }
        if (commitment.remindersEnabled) {
            scheduleReminder(id, commitment.name, commitment.amount, commitment.nextDueDate)
        } else {
            ReminderScheduler.cancel(context, uniqueName(id))
        }
        return id
    }

    /** Deletion is permanent: the commitment, its reminder, and its whole payment history all go. */
    suspend fun delete(commitment: CommitmentEntity) {
        dao.deletePaymentsForCommitment(commitment.id)
        dao.delete(commitment)
        ReminderScheduler.cancel(context, uniqueName(commitment.id))
    }

    /** Logs today's payment and advances nextDueDate by one recurrence period — the only way
     *  recurrence rolls forward in this simple v1 (no background auto-rollover). */
    suspend fun markAsPaid(commitment: CommitmentEntity) {
        dao.insertPayment(
            CommitmentPaymentEntity(
                commitmentId = commitment.id,
                paidDate = LocalDate.now(),
                amount = commitment.amount,
            ),
        )
        val nextDueDate = when (commitment.recurrence) {
            Recurrence.WEEKLY -> commitment.nextDueDate.plusDays(7)
            Recurrence.YEARLY -> commitment.nextDueDate.plusYears(1)
            else -> commitment.nextDueDate.plusMonths(1)
        }
        val updated = commitment.copy(nextDueDate = nextDueDate)
        dao.update(updated)
        if (updated.remindersEnabled) {
            scheduleReminder(updated.id, updated.name, updated.amount, nextDueDate)
        } else {
            ReminderScheduler.cancel(context, uniqueName(updated.id))
        }
    }

    private fun scheduleReminder(id: Long, name: String, amount: Double, dueDate: LocalDate) {
        ReminderScheduler.schedule(
            context = context,
            uniqueName = uniqueName(id),
            notificationId = (5_000_000L + id).toInt(),
            title = "Upcoming: $name",
            body = "$name is due tomorrow — ${CurrencyUtils.format(amount)}.",
            at = dueDate.minusDays(1).atTime(9, 0),
        )
    }

    private fun uniqueName(id: Long) = "commitment_$id"
}
