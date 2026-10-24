package com.xsparks.tukra.feature.mess

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.MenuItemDto
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.PlateAddItemRequest
import com.xsparks.tukra.core.network.PlateOpenRequest
import com.xsparks.tukra.core.network.PlateSessionDto
import com.xsparks.tukra.core.network.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MessCounterUiState(
    val activePlateSession: PlateSessionDto? = null,
    val activePlateCode: String = "",
    val messDishes: List<MenuItemDto> = emptyList(),
    val outletId: String? = null,
    val isLoading: Boolean = false,
    val isAddingItem: Boolean = false,
    val isFinishing: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class MessCounterViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(MessCounterUiState())
    val uiState: StateFlow<MessCounterUiState> = _uiState.asStateFlow()

    init {
        loadMessCatalog()
    }

    fun loadMessCatalog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Find mess outlet
                val outlets = api.getOutlets()
                val messOutlet = outlets.firstOrNull { it.category == "MESS" } ?: outlets.firstOrNull()
                val outletId = messOutlet?.id ?: ""

                if (outletId.isNotBlank()) {
                    val menuRes = api.getOutletMenu(outletId)
                    _uiState.update {
                        it.copy(
                            messDishes = menuRes.items,
                            outletId = outletId,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun openSession(plateCode: String) {
        val outletId = _uiState.value.outletId ?: return
        val cleanCode = plateCode.trim().uppercase()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val session = api.openPlateSession(PlateOpenRequest(plateCode = cleanCode, outletId = outletId))
                _uiState.update {
                    it.copy(
                        activePlateSession = session,
                        activePlateCode = cleanCode,
                        isLoading = false,
                        actionSuccessMessage = "Plate $cleanCode active on serving counter!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun addDishToPlate(dish: MenuItemDto) {
        val session = _uiState.value.activePlateSession ?: return
        if (session.status != "OPEN") return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingItem = true) }
            try {
                val updated = api.addPlateItem(session.id, PlateAddItemRequest(menuItemId = dish.id, qty = 1))
                _uiState.update {
                    it.copy(
                        activePlateSession = updated,
                        isAddingItem = false,
                        actionSuccessMessage = "Added ${dish.name} (+1)"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAddingItem = false, errorMessage = e.message) }
            }
        }
    }

    fun finishAndBill() {
        val session = _uiState.value.activePlateSession ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isFinishing = true) }
            try {
                val billed = api.finishPlateSession(session.id)
                _uiState.update {
                    it.copy(
                        activePlateSession = billed,
                        isFinishing = false,
                        actionSuccessMessage = "Plate billed! Student can now pay from app."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isFinishing = false, errorMessage = e.message) }
            }
        }
    }

    fun nextStudent() {
        _uiState.update {
            it.copy(
                activePlateSession = null,
                activePlateCode = "",
                actionSuccessMessage = null,
                errorMessage = null
            )
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}
