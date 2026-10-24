package com.xsparks.tukra.feature.vendor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.OrderDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.network.UpdateOrderStatusRequest
import com.xsparks.tukra.core.network.VerifyQrRequest
import com.xsparks.tukra.core.network.VerifyQrResponse
import com.xsparks.tukra.core.util.VendorSoundAlert
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class QueueFilterTab(val title: String, val hindiTitle: String) {
    ALL("All", "सब"),
    PLACED("New", "नए"),
    PREPARING("Cooking", "बन रहे हैं"),
    READY("Ready", "तैयार")
}

data class VendorQueueUiState(
    val orders: List<OrderDto> = emptyList(),
    val filteredOrders: List<OrderDto> = emptyList(),
    val selectedTab: QueueFilterTab = QueueFilterTab.ALL,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null,
    val pendingProposalsCount: Int = 0
)

class VendorQueueViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(VendorQueueUiState())
    val uiState: StateFlow<VendorQueueUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null
    private val knownOrderIds = mutableSetOf<String>()
    private var isFirstLoad = true

    fun startPolling(context: Context) {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                fetchOrders(context)
                fetchProposalsCount()
                delay(4000)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun selectTab(tab: QueueFilterTab) {
        _uiState.update { state ->
            state.copy(
                selectedTab = tab,
                filteredOrders = filterOrders(state.orders, tab)
            )
        }
    }

    private suspend fun fetchOrders(context: Context) {
        try {
            val orders: List<OrderDto> = api.getVendorOrders()

            // Check for new incoming orders to play loud alert for Ramesh bhaiya
            val newOrders: List<OrderDto> = orders.filter { order -> order.id !in knownOrderIds }
            if (!isFirstLoad && newOrders.any { order -> order.status == "PLACED" }) {
                VendorSoundAlert.playNewOrderAlert(context)
            }

            knownOrderIds.clear()
            knownOrderIds.addAll(orders.map { order -> order.id })
            isFirstLoad = false

            _uiState.update { state ->
                state.copy(
                    orders = orders,
                    filteredOrders = filterOrders(orders, state.selectedTab),
                    isLoading = false,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }

    private suspend fun fetchProposalsCount() {
        try {
            val proposals = api.getVendorProposals()
            _uiState.update { it.copy(pendingProposalsCount = proposals.size) }
        } catch (_: Exception) {}
    }

    private fun filterOrders(orders: List<OrderDto>, tab: QueueFilterTab): List<OrderDto> {
        return when (tab) {
            QueueFilterTab.ALL -> orders
            QueueFilterTab.PLACED -> orders.filter { it.status == "PLACED" }
            QueueFilterTab.PREPARING -> orders.filter { it.status in listOf("ACCEPTED", "PREPARING") }
            QueueFilterTab.READY -> orders.filter { it.status == "READY" }
        }
    }

    fun acceptOrder(orderId: String, context: Context) {
        updateStatus(orderId, "ACCEPTED", null, context, "Order Accepted (स्वीकार किया)")
    }

    fun startPreparing(orderId: String, context: Context) {
        updateStatus(orderId, "PREPARING", null, context, "Kitchen Started (बनाना शुरू)")
    }

    fun markReady(orderId: String, context: Context) {
        updateStatus(orderId, "READY", null, context, "Order Marked Ready (तैयार है)")
    }

    fun rejectOrder(orderId: String, reason: String, context: Context) {
        updateStatus(orderId, "REJECTED", reason, context, "Order Rejected (अस्वीकार किया)")
    }

    private fun updateStatus(
        orderId: String,
        newStatus: String,
        rejectReason: String?,
        context: Context,
        successMsg: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            try {
                api.updateOrderStatus(orderId, UpdateOrderStatusRequest(status = newStatus, rejectReason = rejectReason))
                VendorSoundAlert.playSuccessBeep(context)
                fetchOrders(context)
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        actionSuccessMessage = successMsg
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        errorMessage = e.message ?: "Failed to update order status"
                    )
                }
            }
        }
    }

    suspend fun verifyQrToken(token: String, context: Context): Result<VerifyQrResponse> {
        return try {
            val response = api.verifyOrderQr(VerifyQrRequest(token = token.trim()))
            VendorSoundAlert.playSuccessBeep(context)
            fetchOrders(context)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}
