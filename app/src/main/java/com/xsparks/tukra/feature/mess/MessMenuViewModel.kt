package com.xsparks.tukra.feature.mess

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.MenuItemDto
import com.xsparks.tukra.core.network.MessMenuItemDto
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.PostMessMenuRequest
import com.xsparks.tukra.core.network.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class MessMeal(val apiName: String, val title: String, val hindiTitle: String) {
    BREAKFAST("BREAKFAST", "Breakfast", "नाश्ता"),
    LUNCH("LUNCH", "Lunch", "दोपहर भोजन"),
    SNACKS("SNACKS", "Snacks", "शाम नाश्ता"),
    DINNER("DINNER", "Dinner", "रात का खाना")
}

data class MessMenuUiState(
    val selectedMeal: MessMeal = MessMeal.LUNCH,
    val serviceDate: String = LocalDate.now().toString(),
    val items: List<MessMenuItemDto> = emptyList(),
    val availableOutletItems: List<MenuItemDto> = emptyList(),
    val outletId: String? = null,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class MessMenuViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(MessMenuUiState())
    val uiState: StateFlow<MessMenuUiState> = _uiState.asStateFlow()

    init {
        loadMenu()
    }

    fun selectMeal(meal: MessMeal) {
        _uiState.update { it.copy(selectedMeal = meal) }
        loadMenu()
    }

    fun loadMenu() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = _uiState.value
                val res = api.getMessMenu(
                    date = state.serviceDate,
                    meal = state.selectedMeal.apiName
                )
                val resolvedOutletId = res.items.firstOrNull()?.outletId

                _uiState.update {
                    it.copy(
                        items = res.items,
                        outletId = resolvedOutletId ?: it.outletId,
                        isLoading = false
                    )
                }

                // Also load available items for publishing if outletId is known
                if (resolvedOutletId != null) {
                    val menuRes = api.getOutletMenu(resolvedOutletId)
                    _uiState.update { it.copy(availableOutletItems = menuRes.items) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updatePlannedQty(item: MessMenuItemDto, newQty: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            try {
                api.postMessMenu(
                    PostMessMenuRequest(
                        outletId = item.outletId,
                        serviceDate = _uiState.value.serviceDate,
                        meal = item.meal,
                        menuItemId = item.menuItemId,
                        plannedQty = newQty
                    )
                )
                loadMenu()
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        actionSuccessMessage = "Planned quantity updated to $newQty"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isActionLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun publishDishToMeal(menuItemId: String, plannedQty: Int) {
        val outletId = _uiState.value.outletId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            try {
                api.postMessMenu(
                    PostMessMenuRequest(
                        outletId = outletId,
                        serviceDate = _uiState.value.serviceDate,
                        meal = _uiState.value.selectedMeal.apiName,
                        menuItemId = menuItemId,
                        plannedQty = plannedQty
                    )
                )
                loadMenu()
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        actionSuccessMessage = "Dish published to ${_uiState.value.selectedMeal.title}!"
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
