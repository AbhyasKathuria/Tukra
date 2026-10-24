package com.xsparks.tukra.feature.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.PlateLinkRequest
import com.xsparks.tukra.core.network.PlateSessionDto
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
    val plateCode: String? = null,
    val session: PlateSessionDto? = null,
    val errorMessage: String? = null,
    val isPaid: Boolean = false
)

class PlateScanViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(PlateScanUiState())
    val uiState: StateFlow<PlateScanUiState> = _uiState.asStateFlow()

    private var activePolling = false

    fun linkPlate(code: String) {
        val trimmed = code.trim().uppercase()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLinking = true, errorMessage = null, plateCode = trimmed) }
            val result = safeApiCall {
                api.linkPlateSession(PlateLinkRequest(plateCode = trimmed))
            }
            when (result) {
                is Resource.Success -> {
                    val session = result.data
                    _uiState.update {
                        it.copy(
                            isLinking = false,
                            session = session,
                            isPaid = session.status == "PAID"
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
