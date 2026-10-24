package com.xsparks.tukra

import com.xsparks.tukra.core.cart.CartItem
import com.xsparks.tukra.core.cart.CartState
import com.xsparks.tukra.core.network.MenuItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CartLogicTest {

    private val item1 = MenuItemDto(
        id = "item-1",
        outletId = "outlet-south-tiffin",
        name = "Masala Dosa",
        pricePaise = 7000,
        isVeg = true,
        category = "Mains",
        prepTimeSeconds = 300,
        prepUnits = 2
    )

    private val item2 = MenuItemDto(
        id = "item-2",
        outletId = "outlet-south-tiffin",
        name = "Filter Coffee",
        pricePaise = 2500,
        isVeg = true,
        category = "Beverages",
        prepTimeSeconds = 120,
        prepUnits = 1
    )

    private val differentOutletItem = MenuItemDto(
        id = "item-3",
        outletId = "outlet-night-canteen",
        name = "Egg Maggi",
        pricePaise = 5000,
        isVeg = false,
        category = "Snacks",
        prepTimeSeconds = 240,
        prepUnits = 1
    )

    @Test
    fun testEmptyCartState() {
        val emptyCart = CartState()
        assertTrue(emptyCart.isEmpty)
        assertEquals(0, emptyCart.totalItemsCount)
        assertEquals(0, emptyCart.totalPaise)
        assertEquals(180, emptyCart.longestPrepSeconds)
        assertEquals(0, emptyCart.totalPrepUnits)
    }

    @Test
    fun testCartCalculationsWithItems() {
        val cart = CartState(
            outletId = "outlet-south-tiffin",
            outletName = "South Tiffin",
            items = listOf(
                CartItem(menuItem = item1, quantity = 2), // 2 * 7000 = 14000 paise, 4 prep units, 300s
                CartItem(menuItem = item2, quantity = 3)  // 3 * 2500 = 7500 paise, 3 prep units, 120s
            )
        )

        assertFalse(cart.isEmpty)
        assertEquals(5, cart.totalItemsCount)
        assertEquals(21500, cart.totalPaise) // ₹215
        assertEquals(300, cart.longestPrepSeconds) // Max prep time is 300s
        assertEquals(7, cart.totalPrepUnits) // 4 + 3 = 7 prep units
    }

    @Test
    fun testPrepUnitsCappedAt20() {
        val heavyItem = item1.copy(prepUnits = 5)
        val cart = CartState(
            outletId = "outlet-south-tiffin",
            outletName = "South Tiffin",
            items = listOf(
                CartItem(menuItem = heavyItem, quantity = 10) // 10 * 5 = 50 units -> capped at 20
            )
        )

        assertEquals(20, cart.totalPrepUnits)
    }
}
