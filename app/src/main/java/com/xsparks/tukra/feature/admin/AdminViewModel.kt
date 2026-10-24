package com.xsparks.tukra.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.AdminDemandResponse
import com.xsparks.tukra.core.network.AdminOutletItem
import com.xsparks.tukra.core.network.AdminOverviewResponse
import com.xsparks.tukra.core.network.AdminSyncJobItem
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.network.UpdateOutletRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val overview: AdminOverviewResponse? = null,
    val outlets: List<AdminOutletItem> = emptyList(),
    val demand: AdminDemandResponse? = null,
    val syncJobs: List<AdminSyncJobItem> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class AdminViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        loadOverview()
        loadOutlets()
        loadDemand()
        loadSyncJobs()
    }

    fun loadOverview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val res = api.getAdminOverview()
                _uiState.update { it.copy(overview = res, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadOutlets() {
        viewModelScope.launch {
            try {
                val list = api.getAdminOutlets()
                _uiState.update { it.copy(outlets = list) }
            } catch (_: Exception) {}
        }
    }

    fun loadDemand() {
        viewModelScope.launch {
            try {
                val res = api.getAdminDemand()
                _uiState.update { it.copy(demand = res) }
            } catch (_: Exception) {}
        }
    }

    fun loadSyncJobs() {
        viewModelScope.launch {
            try {
                val jobs = api.getAdminSyncJobs()
                _uiState.update { it.copy(syncJobs = jobs) }
            } catch (_: Exception) {}
        }
    }

    fun triggerTinyFishSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            try {
                api.startMenuSync()
                loadSyncJobs()
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        actionSuccessMessage = "TinyFish autonomous sync agent triggered!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isActionLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}
