package com.xsparks.tukra.feature.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.ProposalDecisionRequest
import com.xsparks.tukra.core.network.ProposalDto
import com.xsparks.tukra.core.network.SyncStatusResponse
import com.xsparks.tukra.core.network.TokenStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class VendorProposalsUiState(
    val proposals: List<ProposalDto> = emptyList(),
    val activeSyncJob: SyncStatusResponse? = null,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = false,
    val isDeciding: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class VendorProposalsViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(VendorProposalsUiState())
    val uiState: StateFlow<VendorProposalsUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    init {
        loadProposals()
    }

    fun loadProposals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val proposals = api.getVendorProposals()
                _uiState.update {
                    it.copy(
                        proposals = proposals,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun startSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, errorMessage = null, actionSuccessMessage = null) }
            try {
                val res = api.startMenuSync()
                val jobId = res.jobId

                pollJob?.cancel()
                pollJob = viewModelScope.launch {
                    while (isActive) {
                        try {
                            val statusRes = api.getSyncStatus(jobId)
                            _uiState.update { it.copy(activeSyncJob = statusRes) }

                            if (statusRes.status == "COMPLETED") {
                                _uiState.update {
                                    it.copy(
                                        isSyncing = false,
                                        actionSuccessMessage = "TinyFish Sync Complete! Extracted ${statusRes.proposalCount} proposals."
                                    )
                                }
                                loadProposals()
                                break
                            } else if (statusRes.status == "FAILED") {
                                _uiState.update {
                                    it.copy(
                                        isSyncing = false,
                                        errorMessage = statusRes.errorMessage ?: "TinyFish sync failed"
                                    )
                                }
                                break
                            }
                        } catch (pollErr: Exception) {
                            _uiState.update { it.copy(isSyncing = false, errorMessage = pollErr.message) }
                            break
                        }
                        delay(1500)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncing = false, errorMessage = e.message) }
            }
        }
    }

    fun decideProposal(proposalId: String, approve: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeciding = true) }
            try {
                api.decideProposal(proposalId, ProposalDecisionRequest(approve = approve))
                val updated = _uiState.value.proposals.filterNot { it.id == proposalId }
                _uiState.update {
                    it.copy(
                        proposals = updated,
                        isDeciding = false,
                        actionSuccessMessage = if (approve) "Proposal approved & menu updated" else "Proposal rejected"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDeciding = false, errorMessage = e.message) }
            }
        }
    }

    fun approveAllProposals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeciding = true) }
            try {
                val res = api.approveAllProposals()
                _uiState.update {
                    it.copy(
                        proposals = emptyList(),
                        isDeciding = false,
                        actionSuccessMessage = "Approved all ${res.approvedCount} menu changes!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDeciding = false, errorMessage = e.message) }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}
