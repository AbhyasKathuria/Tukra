package com.xsparks.tukra.feature.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.LivePlateStatsDto
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.PlateLinkRequest
import com.xsparks.tukra.core.network.PlateSessionDto
import com.xsparks.tukra.core.network.ScanDishRequest
import com.xsparks.tukra.core.network.ScanDishResponse
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.safeApiCall
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlateScanUiState(
    val isLinking: Boolean = false,
    val isPolling: Boolean = false,
    val isPaying: Boolean = false,
    val isScanningDish: Boolean = false,
    val plateCode: String? = null,
    val studentEmail: String = "",
    val session: PlateSessionDto? = null,
    val liveStats: LivePlateStatsDto? = null,
    val lastScannedDish: ScanDishResponse? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isPaid: Boolean = false
)

class PlateScanViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(PlateScanUiState())
    val uiState: StateFlow<PlateScanUiState> = _uiState.asStateFlow()

    private var activePolling = false

    init {
        loadUserEmail()
        loadLivePlateStats()
    }

    private fun loadUserEmail() {
        viewModelScope.launch {
            tokenStore.userFlow.collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(studentEmail = user.email) }
                }
            }
        }
    }

    fun loadLivePlateStats() {
        viewModelScope.launch {
            val result = safeApiCall { api.getLivePlateStats() }
            if (result is Resource.Success) {
                _uiState.update { it.copy(liveStats = result.data) }
            }
        }
    }

    fun linkPlate(code: String, emailOverride: String? = null) {
        val trimmed = code.trim().uppercase()
        if (trimmed.isBlank()) return
        val targetEmail = emailOverride?.trim()?.ifBlank { null } ?: _uiState.value.studentEmail.ifBlank { null }

        viewModelScope.launch {
            _uiState.update { it.copy(isLinking = true, errorMessage = null, plateCode = trimmed) }
            val result = safeApiCall {
                api.linkPlateSession(PlateLinkRequest(plateCode = trimmed, studentEmail = targetEmail))
            }
            when (result) {
                is Resource.Success -> {
                    val session = result.data
                    _uiState.update {
                        it.copy(
                            isLinking = false,
                            session = session,
                            isPaid = session.status == "PAID",
                            successMessage = "Plate $trimmed linked to ${session.studentName ?: targetEmail ?: "your account"}"
                        )
                    }
                    startPollingBill(session.id)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLinking = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun scanDishAi(hint: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanningDish = true, errorMessage = null) }
            val plateCode = _uiState.value.plateCode ?: "PLT-0001"
            val result = safeApiCall {
                api.scanDishAi(ScanDishRequest(plateCode = plateCode, dishNameHint = hint))
            }
            when (result) {
                is Resource.Success -> {
                    val dishResult = result.data
                    _uiState.update {
                        it.copy(
                            isScanningDish = false,
                            lastScannedDish = dishResult,
                            successMessage = "TinyFish Vision detected: ${dishResult.dishName} (₹${dishResult.priceRupees})"
                        )
                    }
                    // Refresh session bill
                    val currentSession = _uiState.value.session
                    if (currentSession != null) {
                        val sessionRes = safeApiCall { api.getPlateSession(currentSession.id) }
                        if (sessionRes is Resource.Success) {
                            _uiState.update { it.copy(session = sessionRes.data) }
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isScanningDish = false, errorMessage = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun startPollingBill(sessionId: String) {
        activePolling = true
        _uiState.update { it.copy(isPolling = true) }

        viewModelScope.launch {
            while (activePolling) {
                val result = safeApiCall { api.getPlateSession(sessionId) }
                if (result is Resource.Success) {
                    val current = result.data
                    _uiState.update {
                        it.copy(
                            session = current,
                            isPaid = current.status == "PAID"
                        )
                    }
                    if (current.status == "PAID") {
                        activePolling = false
                        _uiState.update { it.copy(isPolling = false) }
                    }
                }
                if (activePolling) {
                    delay(3000) // Poll every 3 seconds
                }
            }
        }
    }

    fun payPlateBill() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isPaying = true, errorMessage = null) }
            val result = safeApiCall { api.payPlateSession(session.id) }
            when (result) {
                is Resource.Success -> {
                    activePolling = false
                    _uiState.update {
                        it.copy(
                            isPaying = false,
                            isPolling = false,
                            session = result.data,
                            isPaid = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isPaying = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun resetScanner() {
        activePolling = false
        _uiState.value = PlateScanUiState()
    }

    override fun onCleared() {
        super.onCleared()
        activePolling = false
    }
}
