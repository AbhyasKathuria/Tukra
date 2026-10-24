package com.xsparks.tukra.feature.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.AddItemRequest
import com.xsparks.tukra.core.network.MenuItemDto
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.network.UpdateItemRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VendorMenuUiState(
    val items: List<MenuItemDto> = emptyList(),
    val filteredItems: List<MenuItemDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class VendorMenuViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(VendorMenuUiState())
    val uiState: StateFlow<VendorMenuUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val items = api.getVendorItems()
                _uiState.update { state ->
                    state.copy(
                        items = items,
                        filteredItems = filterItems(items, state.searchQuery),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredItems = filterItems(state.items, query)
            )
        }
    }

    private fun filterItems(items: List<MenuItemDto>, query: String): List<MenuItemDto> {
        if (query.isBlank()) return items
        return items.filter {
            it.name.contains(query, ignoreCase = true) ||
            (it.category?.contains(query, ignoreCase = true) == true) ||
            (it.categoryName?.contains(query, ignoreCase = true) == true) ||
            (it.description?.contains(query, ignoreCase = true) == true)
        }
    }

    fun toggleAvailability(itemId: String, currentAvailable: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            try {
                api.updateVendorItem(itemId, UpdateItemRequest(isAvailable = !currentAvailable))
                val updatedItems = _uiState.value.items.map {
                    if (it.id == itemId) it.copy(isAvailable = !currentAvailable) else it
                }
                _uiState.update { state ->
                    state.copy(
                        items = updatedItems,
                        filteredItems = filterItems(updatedItems, state.searchQuery),
                        isActionLoading = false,
                        actionSuccessMessage = if (!currentAvailable) "Item marked Available" else "Item marked Sold Out"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isActionLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updatePrice(itemId: String, newPriceRupees: Int) {
        if (newPriceRupees <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            try {
                val pricePaise = newPriceRupees * 100
                api.updateVendorItem(itemId, UpdateItemRequest(pricePaise = pricePaise))
                val updatedItems = _uiState.value.items.map {
                    if (it.id == itemId) it.copy(pricePaise = pricePaise) else it
                }
                _uiState.update { state ->
                    state.copy(
                        items = updatedItems,
                        filteredItems = filterItems(updatedItems, state.searchQuery),
                        isActionLoading = false,
                        actionSuccessMessage = "Price updated to ₹$newPriceRupees"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isActionLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun addNewDish(
        name: String,
        priceRupees: Int,
        isVeg: Boolean,
        prepMinutes: Int,
        prepUnits: Int,
        description: String?
    ) {
        if (name.isBlank() || priceRupees <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            try {
                api.addVendorItem(
                    AddItemRequest(
                        name = name.trim(),
                        pricePaise = priceRupees * 100,
                        isVeg = isVeg,
                        prepSeconds = prepMinutes * 60,
                        prepUnits = prepUnits,
                        description = description?.takeIf { it.isNotBlank() }
                    )
                )
                loadItems()
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        actionSuccessMessage = "$name added to menu!"
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
