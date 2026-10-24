package com.xsparks.tukra.feature.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.OrderDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.TimeFormat
import com.xsparks.tukra.core.util.safeApiCall
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackingUiState(
    val isLoading: Boolean = true,
    val order: OrderDto? = null,
    val isCancelling: Boolean = false,
    val canCancel: Boolean = false,
    val errorMessage: String? = null
)

class TrackingViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var isPolling = true

    fun startTracking(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            isPolling = true

            while (isPolling) {
                val result = safeApiCall { api.getOrderById(orderId) }
                when (result) {
                    is Resource.Success -> {
                        val order = result.data
                        val canCancel = evaluateCanCancel(order)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                order = order,
                                canCancel = canCancel
                            )
                        }

                        // Stop polling if terminal status reached
                        if (order.status in listOf("COLLECTED", "CANCELLED", "REJECTED", "NO_SHOW")) {
                            isPolling = false
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = result.message)
                        }
                    }
                    is Resource.Loading -> Unit
                }

                if (isPolling) {
                    delay(5000) // Poll every 5 s
                }
            }
        }
    }

    private fun evaluateCanCancel(order: OrderDto): Boolean {
        if (order.status !in listOf("PLACED", "ACCEPTED")) return false
        val slotStart = order.slotStartsAt ?: return false
        val minutesRemaining = TimeFormat.minutesUntil(slotStart)
        return minutesRemaining > 10
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, errorMessage = null) }
            val result = safeApiCall { api.cancelOrder(orderId) }
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            order = result.data,
                            canCancel = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isCancelling = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        isPolling = false
    }
}
