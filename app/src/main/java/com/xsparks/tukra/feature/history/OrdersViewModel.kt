package com.xsparks.tukra.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.OrderDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdersHistoryUiState(
    val isLoading: Boolean = true,
    val activeOrders: List<OrderDto> = emptyList(),
    val pastOrders: List<OrderDto> = emptyList(),
    val totalSpentThisMonthPaise: Int = 0,
    val totalOrdersThisMonth: Int = 0,
    val errorMessage: String? = null
)

class OrdersViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(OrdersHistoryUiState())
    val uiState: StateFlow<OrdersHistoryUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = safeApiCall { api.getOrders() }
            when (result) {
                is Resource.Success -> {
                    val all = result.data
                    val active = all.filter { it.status in listOf("PLACED", "ACCEPTED", "PREPARING", "READY") }
                    val past = all.filter { it.status !in listOf("PLACED", "ACCEPTED", "PREPARING", "READY") }

                    val monthSpent = all.filter { it.status == "COLLECTED" }.sumOf { it.totalPaise }
                    val monthCount = all.size

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeOrders = active,
                            pastOrders = past,
                            totalSpentThisMonthPaise = monthSpent,
                            totalOrdersThisMonth = monthCount
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
