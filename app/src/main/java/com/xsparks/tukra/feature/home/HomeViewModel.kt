package com.xsparks.tukra.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.CampusDto
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.OutletDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.network.UserDto
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val user: UserDto? = null,
    val walletBalancePaise: Int = 0,
    val campus: CampusDto? = null,
    val breakNotice: String? = "Break in 18 min. Order now and skip the line.",
    val outlets: List<OutletDto> = emptyList(),
    val filteredOutlets: List<OutletDto> = emptyList(),
    val searchQuery: String = "",
    val filterVegOnly: Boolean = false,
    val filterUnder100: Boolean = false,
    val filterReadyFast: Boolean = false,
    val filterOpenNow: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeUser()
        refreshHome()
    }

    private fun observeUser() {
        viewModelScope.launch {
            tokenStore.userFlow.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun refreshHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Fetch wallet balance
            val walletRes = safeApiCall { api.getWallet() }
            if (walletRes is Resource.Success) {
                _uiState.update { it.copy(walletBalancePaise = walletRes.data.balancePaise) }
            }

            // 2. Fetch campus
            val campusRes = safeApiCall { api.getCampus() }
            if (campusRes is Resource.Success) {
                _uiState.update { it.copy(campus = campusRes.data) }
            }

            // 3. Fetch outlets
            val outletsRes = safeApiCall { api.getOutlets() }
            when (outletsRes) {
                is Resource.Success -> {
                    val sorted = sortOutlets(outletsRes.data)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = false,
                            outlets = sorted,
                            filteredOutlets = applyFilters(sorted, it.searchQuery, it.filterVegOnly, it.filterReadyFast, it.filterOpenNow)
                        )
                    }
                }
                is Resource.Error -> {
                    val isOff = outletsRes.code == "OFFLINE"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = isOff,
                            errorMessage = if (it.outlets.isEmpty()) outletsRes.message else null
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredOutlets = applyFilters(it.outlets, query, it.filterVegOnly, it.filterReadyFast, it.filterOpenNow)
            )
        }
    }

    fun toggleVegOnly() {
        _uiState.update {
            val newVal = !it.filterVegOnly
            it.copy(
                filterVegOnly = newVal,
                filteredOutlets = applyFilters(it.outlets, it.searchQuery, newVal, it.filterReadyFast, it.filterOpenNow)
            )
        }
    }

    fun toggleReadyFast() {
        _uiState.update {
            val newVal = !it.filterReadyFast
            it.copy(
                filterReadyFast = newVal,
                filteredOutlets = applyFilters(it.outlets, it.searchQuery, it.filterVegOnly, newVal, it.filterOpenNow)
            )
        }
    }

    fun toggleOpenNow() {
        _uiState.update {
            val newVal = !it.filterOpenNow
            it.copy(
                filterOpenNow = newVal,
                filteredOutlets = applyFilters(it.outlets, it.searchQuery, it.filterVegOnly, it.filterReadyFast, newVal)
            )
        }
    }

    private fun sortOutlets(list: List<OutletDto>): List<OutletDto> {
        // Open outlets first, then closed outlets
        return list.sortedWith(compareByDescending<OutletDto> { it.isOpen }.thenBy { it.name })
    }

    private fun applyFilters(
        list: List<OutletDto>,
        query: String,
        vegOnly: Boolean,
        readyFast: Boolean,
        openNow: Boolean
    ): List<OutletDto> {
        return list.filter { outlet ->
            val matchesQuery = query.isBlank() ||
                outlet.name.contains(query, ignoreCase = true) ||
                outlet.category.contains(query, ignoreCase = true)

            val matchesOpen = !openNow || outlet.isOpen
            val matchesFast = !readyFast || (outlet.waitMinutes != null && outlet.waitMinutes <= 15)

            matchesQuery && matchesOpen && matchesFast
        }
    }
}
