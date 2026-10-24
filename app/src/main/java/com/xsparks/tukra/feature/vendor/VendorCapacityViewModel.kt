package com.xsparks.tukra.feature.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.OutletDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.network.UpdateOutletRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VendorCapacityUiState(
    val outlet: OutletDto? = null,
    val isOpen: Boolean = true,
    val slotCapacity: Int = 10,
    val acceptsOrdersUntil: String = "22:00",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class VendorCapacityViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(VendorCapacityUiState())
    val uiState: StateFlow<VendorCapacityUiState> = _uiState.asStateFlow()

    init {
        loadOutlet()
    }

    fun loadOutlet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val outlet = api.getVendorOutlet()
                _uiState.update {
                    it.copy(
                        outlet = outlet,
                        isOpen = outlet.isOpen,
                        slotCapacity = outlet.slotCapacity ?: 10,
                        acceptsOrdersUntil = outlet.acceptsOrdersUntil ?: "22:00",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun setOpen(open: Boolean) {
        _uiState.update { it.copy(isOpen = open) }
    }

    fun setSlotCapacity(capacity: Int) {
        _uiState.update { it.copy(slotCapacity = capacity.coerceIn(1, 100)) }
    }

    fun setAcceptsOrdersUntil(time: String) {
        _uiState.update { it.copy(acceptsOrdersUntil = time) }
    }

    fun saveChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val updated = api.updateVendorOutlet(
                    UpdateOutletRequest(
                        isOpen = _uiState.value.isOpen,
                        slotCapacity = _uiState.value.slotCapacity,
                        acceptsOrdersUntil = _uiState.value.acceptsOrdersUntil
                    )
                )
                _uiState.update {
                    it.copy(
                        outlet = updated,
                        isSaving = false,
                        actionSuccessMessage = "Outlet settings saved successfully!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}
