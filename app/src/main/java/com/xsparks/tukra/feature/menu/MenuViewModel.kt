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
    val items: List<MenuItemDto> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
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
                    val items = result.data.items
                    val categories = items.map { it.category }.distinct()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = items,
                            categories = categories,
                            selectedCategory = categories.firstOrNull()
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

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
