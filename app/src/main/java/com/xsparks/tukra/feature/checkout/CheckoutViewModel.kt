package com.xsparks.tukra.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.cart.CartManager
import com.xsparks.tukra.core.network.CreateOrderRequest
import com.xsparks.tukra.core.network.DemoTopupRequest
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.OrderItemInput
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val isPlacingOrder: Boolean = false,
    val walletBalancePaise: Int = 0,
    val isDemoTopupLoading: Boolean = false,
    val errorMessage: String? = null,
    val errorCode: String? = null,
    val createdOrderId: String? = null
)

class CheckoutViewModel(
    private val tokenStore: TokenStore,
    private val cartManager: CartManager
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        refreshWallet()
    }

    fun refreshWallet() {
        viewModelScope.launch {
            val result = safeApiCall { api.getWallet() }
            if (result is Resource.Success) {
                _uiState.update { it.copy(walletBalancePaise = result.data.balancePaise) }
            }
        }
    }

    fun addDemoMoneyAndPay(outletId: String, slotId: String, requiredShortfallPaise: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDemoTopupLoading = true, errorMessage = null) }
            // Top up shortfall
            val topupRes = safeApiCall {
                api.demoTopup(DemoTopupRequest(amountPaise = requiredShortfallPaise))
            }
            when (topupRes) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isDemoTopupLoading = false,
                            walletBalancePaise = topupRes.data.balancePaise
                        )
                    }
                    // Automatically proceed to pay
                    placeOrder(outletId, slotId, paymentMethod = "WALLET")
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isDemoTopupLoading = false,
                            errorMessage = topupRes.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun placeOrder(
        outletId: String,
        slotId: String,
        paymentMethod: String = "WALLET"
    ) {
        val cart = cartManager.cartState.value
        if (cart.isEmpty) {
            _uiState.update { it.copy(errorMessage = "Your cart is empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true, errorMessage = null, errorCode = null) }

            val items = cart.items.map {
                OrderItemInput(
                    menuItemId = it.menuItem.id,
                    qty = it.quantity,
                    notes = it.notes
                )
            }

            val request = CreateOrderRequest(
                outletId = outletId,
                slotId = slotId,
                items = items,
                paymentMethod = paymentMethod
            )

            val result = safeApiCall { api.createOrder(request) }
            when (result) {
                is Resource.Success -> {
                    cartManager.clearCart()
                    _uiState.update {
                        it.copy(
                            isPlacingOrder = false,
                            createdOrderId = result.data.id
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isPlacingOrder = false,
                            errorMessage = result.message,
                            errorCode = result.code
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, errorCode = null) }
    }
}
