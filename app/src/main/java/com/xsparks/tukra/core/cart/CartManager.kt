package com.xsparks.tukra.core.cart

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.xsparks.tukra.core.network.MenuItemDto
import com.xsparks.tukra.core.network.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CartItem(
    val menuItem: MenuItemDto,
    val quantity: Int,
    val notes: String? = null
)

@Serializable
data class CartState(
    val outletId: String? = null,
    val outletName: String? = null,
    val items: List<CartItem> = emptyList()
) {
    val totalItemsCount: Int get() = items.sumOf { it.quantity }
    val totalPaise: Int get() = items.sumOf { it.menuItem.pricePaise * it.quantity }
    val longestPrepSeconds: Int get() = items.maxOfOrNull { it.menuItem.prepTimeSeconds } ?: 180
    val totalPrepUnits: Int get() = items.sumOf { it.menuItem.prepUnits * it.quantity }.coerceAtMost(20)
    val isEmpty: Boolean get() = items.isEmpty()
}

sealed interface AddToCartResult {
    data object Success : AddToCartResult
    data class DifferentOutlet(val existingOutletName: String, val newOutletName: String) : AddToCartResult
    data class QuantityExceeded(val maxQty: Int = 10) : AddToCartResult
}

class CartManager(private val context: Context) {

    companion object {
        private val KEY_CART = stringPreferencesKey("tukra_cart_json")
        const val MAX_ITEM_QUANTITY = 10
        const val MAX_PREP_UNITS = 20

        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        @Volatile
        private var INSTANCE: CartManager? = null

        fun getInstance(context: Context): CartManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CartManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    init {
        scope.launch {
            loadCart()
        }
    }

    private suspend fun loadCart() {
        try {
            val prefs = context.dataStore.data.first()
            val raw = prefs[KEY_CART]
            if (!raw.isNullOrBlank()) {
                val state = json.decodeFromString<CartState>(raw)
                _cartState.value = state
            }
        } catch (_: Exception) {
            _cartState.value = CartState()
        }
    }

    private fun persistCart(state: CartState) {
        scope.launch {
            try {
                context.dataStore.edit { prefs ->
                    if (state.isEmpty) {
                        prefs.remove(KEY_CART)
                    } else {
                        prefs[KEY_CART] = json.encodeToString(state)
                    }
                }
            } catch (_: Exception) {
                // Silently handle persistence error
            }
        }
    }

    fun addItem(
        outletId: String,
        outletName: String,
        item: MenuItemDto,
        qty: Int = 1,
        notes: String? = null
    ): AddToCartResult {
        val current = _cartState.value

        // Check if cart has items from a different outlet
        if (current.outletId != null && current.outletId != outletId && current.items.isNotEmpty()) {
            return AddToCartResult.DifferentOutlet(
                existingOutletName = current.outletName ?: "Another outlet",
                newOutletName = outletName
            )
        }

        val existingIndex = current.items.indexOfFirst { it.menuItem.id == item.id }
        val newItems = current.items.toMutableList()

        if (existingIndex >= 0) {
            val existing = newItems[existingIndex]
            val newQty = existing.quantity + qty
            if (newQty > MAX_ITEM_QUANTITY) {
                return AddToCartResult.QuantityExceeded(MAX_ITEM_QUANTITY)
            }
            newItems[existingIndex] = existing.copy(
                quantity = newQty,
                notes = notes ?: existing.notes
            )
        } else {
            if (qty > MAX_ITEM_QUANTITY) {
                return AddToCartResult.QuantityExceeded(MAX_ITEM_QUANTITY)
            }
            newItems.add(CartItem(menuItem = item, quantity = qty, notes = notes))
        }

        val updated = CartState(
            outletId = outletId,
            outletName = outletName,
            items = newItems
        )
        _cartState.value = updated
        persistCart(updated)
        return AddToCartResult.Success
    }

    fun updateQuantity(itemId: String, newQty: Int) {
        val current = _cartState.value
        if (current.isEmpty) return

        val newItems = current.items.mapNotNull {
            if (it.menuItem.id == itemId) {
                if (newQty <= 0) null
                else it.copy(quantity = newQty.coerceAtMost(MAX_ITEM_QUANTITY))
            } else {
                it
            }
        }

        val updated = if (newItems.isEmpty()) {
            CartState()
        } else {
            current.copy(items = newItems)
        }

        _cartState.value = updated
        persistCart(updated)
    }

    fun removeItem(itemId: String) {
        updateQuantity(itemId, 0)
    }

    fun clearCart() {
        val updated = CartState()
        _cartState.value = updated
        persistCart(updated)
    }
}
