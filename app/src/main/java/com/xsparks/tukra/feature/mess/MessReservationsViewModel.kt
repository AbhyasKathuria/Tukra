package com.xsparks.tukra.feature.mess

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsparks.tukra.core.network.DemandForecastResponse
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MessReservationsUiState(
    val forecast: DemandForecastResponse? = null,
    val targetDate: String = LocalDate.now().toString(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val notice: String? = null
)

class MessReservationsViewModel(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val api = NetworkClient.getApi(tokenStore)

    private val _uiState = MutableStateFlow(MessReservationsUiState())
    val uiState: StateFlow<MessReservationsUiState> = _uiState.asStateFlow()

    init {
        loadForecast()
    }

    fun loadForecast() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val forecast = api.getMessForecast(date = _uiState.value.targetDate)
                _uiState.update {
                    it.copy(
                        forecast = forecast,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun exportReservations(context: Context) {
        val fc = _uiState.value.forecast ?: return
        val builder = java.lang.StringBuilder()
        builder.append("TUKRA MESS RESERVATIONS & DEMAND FORECAST\n")
        builder.append("Date: ${fc.targetDate} (${fc.dayOfWeek})\n\n")

        fc.items.forEach { item ->
            builder.append("• ${item.name} [${item.category}]\n")
            builder.append("  - Current Reservations: ${item.currentReservations} portions\n")
            builder.append("  - 4-Week Average: ${item.fourWeekWeekdayAvg}\n")
            builder.append("  - Suggested Prep (Estimate): ${item.suggestedQty} portions\n\n")
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Mess Demand Forecast", builder.toString())
        clipboard?.setPrimaryClip(clip)

        _uiState.update { it.copy(notice = "Forecast & reservation list copied to clipboard!") }
    }

    fun clearNotice() {
        _uiState.update { it.copy(notice = null, errorMessage = null) }
    }
}
