package com.xsparks.tukra.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.AuthResponse
import com.xsparks.tukra.core.network.LoginRequest
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.RegisterRequest
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successRole: String? = null
)

class AuthViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private fun getApi() = NetworkClient.getApi(tokenStore)
    private val authInterceptor = NetworkClient.getAuthInterceptor(tokenStore)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            tokenStore.saveBaseUrl(url)
            NetworkClient.setCustomBaseUrl(url)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(error = "Please enter both email and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = safeApiCall {
                getApi().login(LoginRequest(email = email.trim(), password = pass))
            }

            when (result) {
                is Resource.Success -> {
                    handleAuthSuccess(result.data)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun register(
        email: String,
        pass: String,
        name: String,
        hostel: String? = null,
        dietPreference: String? = null
    ) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all required fields") }
            return
        }

        if (pass.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = safeApiCall {
                getApi().register(
                    RegisterRequest(
                        email = email.trim(),
                        password = pass,
                        name = name.trim(),
                        hostel = hostel?.trim()?.ifBlank { null },
                        dietPreference = dietPreference
                    )
                )
            }

            when (result) {
                is Resource.Success -> {
                    handleAuthSuccess(result.data)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private suspend fun handleAuthSuccess(authResponse: AuthResponse) {
        tokenStore.saveSession(authResponse.token, authResponse.user)
        authInterceptor.setToken(authResponse.token)
        _uiState.update {
            it.copy(
                isLoading = false,
                error = null,
                successRole = authResponse.user.role
            )
        }
    }

    fun resetSuccessState() {
        _uiState.update { it.copy(successRole = null) }
    }
}
