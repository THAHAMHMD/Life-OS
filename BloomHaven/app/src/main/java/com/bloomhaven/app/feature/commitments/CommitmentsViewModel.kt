package com.bloomhaven.app.feature.commitments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CommitmentsUiState(
    val commitments: List<CommitmentEntity> = emptyList(),
    val paymentHistory: List<CommitmentPaymentWithName> = emptyList(),
)

class CommitmentsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = CommitmentsRepository(
        application,
        CommitmentsDatabase.getInstance(application).commitmentsDao(),
    )

    val uiState: StateFlow<CommitmentsUiState> = combine(
        repo.activeCommitments(),
        repo.paymentHistory(),
    ) { commitments, history ->
        CommitmentsUiState(commitments = commitments, paymentHistory = history)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CommitmentsUiState())

    /** Used for both add (id == 0) and edit — the form hands back the same entity shape either way. */
    fun save(commitment: CommitmentEntity) {
        viewModelScope.launch { repo.save(commitment) }
    }

    fun deleteCommitment(commitment: CommitmentEntity) {
        viewModelScope.launch { repo.delete(commitment) }
    }

    fun markAsPaid(commitment: CommitmentEntity) {
        viewModelScope.launch { repo.markAsPaid(commitment) }
    }
}
