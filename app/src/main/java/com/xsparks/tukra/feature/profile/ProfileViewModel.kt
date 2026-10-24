package com.xsparks.tukra.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.network.UpdateUserRequest
import com.xsparks.tukra.core.network.UserDto
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val isDeletingAccount: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = safeApiCall { api.getCurrentUser() }
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, user = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateProfile(name: String, hostel: String?, dietPreference: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = safeApiCall {
                api.updateCurrentUser(
                    UpdateUserRequest(
                        name = name.trim(),
                        hostel = hostel?.trim()?.ifBlank { null },
                        dietPreference = dietPreference
                    )
                )
            }
            when (result) {
                is Resource.Success -> {
                    val updatedUser = result.data
                    val token = tokenStore.getToken() ?: ""
                    tokenStore.saveSession(token, updatedUser)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = updatedUser,
                            successMessage = "Profile updated successfully!"
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

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, errorMessage = null) }
            val result = safeApiCall { api.deleteAccount() }
            when (result) {
                is Resource.Success -> {
                    tokenStore.clearSession()
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            isAccountDeleted = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isDeletingAccount = false, errorMessage = result.message)
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
