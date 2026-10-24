package com.xsparks.tukra.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.DemoTopupRequest
import com.xsparks.tukra.core.network.HealthResponse
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.network.TopupVerifyRequest
import com.xsparks.tukra.core.network.WalletResponse
import com.xsparks.tukra.core.network.WalletTxnDto
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WalletUiState(
    val isLoading: Boolean = true,
    val isTopupLoading: Boolean = false,
    val balancePaise: Int = 0,
    val transactions: List<WalletTxnDto> = emptyList(),
    val isRazorpayAvailable: Boolean = false,
    val isDemoPayAvailable: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class WalletViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadWallet()
        checkHealth()
    }

    fun checkHealth() {
        viewModelScope.launch {
            val result = safeApiCall { api.getHealth() }
            if (result is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isRazorpayAvailable = result.data.features.razorpay,
                        isDemoPayAvailable = result.data.features.demoPay
                    )
                }
            }
        }
    }

    fun loadWallet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = safeApiCall { api.getWallet() }
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            balancePaise = result.data.balancePaise,
                            transactions = result.data.transactions
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

    fun topupDemo(amountRupees: Int) {
        val amountPaise = amountRupees * 100
        viewModelScope.launch {
            _uiState.update { it.copy(isTopupLoading = true, errorMessage = null, successMessage = null) }
            val result = safeApiCall {
                api.demoTopup(DemoTopupRequest(amountPaise = amountPaise))
            }
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isTopupLoading = false,
                            balancePaise = result.data.balancePaise,
                            transactions = result.data.transactions,
                            successMessage = "Successfully credited ₹$amountRupees to your Tukra wallet!"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isTopupLoading = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun verifyRazorpayPayment(paymentId: String, orderId: String, signature: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTopupLoading = true, errorMessage = null) }
            val result = safeApiCall {
                api.verifyTopup(
                    TopupVerifyRequest(
                        razorpayPaymentId = paymentId,
                        razorpayOrderId = orderId,
                        razorpaySignature = signature
                    )
                )
            }
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isTopupLoading = false,
                            balancePaise = result.data.balancePaise,
                            transactions = result.data.transactions,
                            successMessage = "Wallet top-up verified and credited!"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isTopupLoading = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
