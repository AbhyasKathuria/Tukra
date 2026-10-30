package com.xsparks.tukra.feature.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.MenuItemDto
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MenuUiState(
    val isLoading: Boolean = true,
    val outletId: String = "",
    val outletName: String = "",
    val allItems: List<MenuItemDto> = emptyList(),
    val items: List<MenuItemDto> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val dietFilter: String = "ALL", // "ALL", "VEG", "NON_VEG"
    val quickBiteFilter: Boolean = false,
    val liveOptionsCount: Int = 0,
    val errorMessage: String? = null
)

class MenuViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    fun loadMenu(outletId: String, outletName: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                outletId = outletId,
                outletName = outletName,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = safeApiCall { api.getOutletMenu(outletId) }
            when (result) {
                is Resource.Success -> {
                    val rawItems = result.data.items
                    val filtered = applyFilters(rawItems, _uiState.value.dietFilter, _uiState.value.quickBiteFilter)
                    val categories = filtered.map { it.category }.distinct()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allItems = rawItems,
                            items = filtered,
                            categories = categories,
                            selectedCategory = categories.firstOrNull(),
                            liveOptionsCount = result.data.liveOptionsCount.takeIf { c -> c > 0 } ?: rawItems.count { i -> i.isAvailable }
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

    fun setDietFilter(filter: String) {
        _uiState.update { current ->
            val newDiet = filter
            val filtered = applyFilters(current.allItems, newDiet, current.quickBiteFilter)
            val categories = filtered.map { it.category }.distinct()
            current.copy(
                dietFilter = newDiet,
                items = filtered,
                categories = categories,
                selectedCategory = if (current.selectedCategory in categories) current.selectedCategory else categories.firstOrNull()
            )
        }
    }

    fun toggleQuickBites() {
        _uiState.update { current ->
            val newQb = !current.quickBiteFilter
            val filtered = applyFilters(current.allItems, current.dietFilter, newQb)
            val categories = filtered.map { it.category }.distinct()
            current.copy(
                quickBiteFilter = newQb,
                items = filtered,
                categories = categories,
                selectedCategory = if (current.selectedCategory in categories) current.selectedCategory else categories.firstOrNull()
            )
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    private fun applyFilters(
        items: List<MenuItemDto>,
        diet: String,
        quickBite: Boolean
    ): List<MenuItemDto> {
        return items.filter { item ->
            val matchDiet = when (diet) {
                "VEG" -> item.isVeg
                "NON_VEG" -> !item.isVeg
                else -> true
            }
            val matchQuickBite = !quickBite || item.isQuickBite || item.prepTimeSeconds <= 180
            matchDiet && matchQuickBite
        }
    }
}
