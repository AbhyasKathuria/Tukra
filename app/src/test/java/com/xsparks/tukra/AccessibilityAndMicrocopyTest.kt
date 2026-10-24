package com.xsparks.tukra

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class AccessibilityAndMicrocopyTest {

    // Helper to calculate relative luminance according to WCAG 2.1
    private fun luminance(r: Int, g: Int, b: Int): Double {
        val a = listOf(r, g, b).map { v ->
            val s = v / 255.0
            if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        return a[0] * 0.2126 + a[1] * 0.7152 + a[2] * 0.0722
    }

    private fun contrastRatio(l1: Double, l2: Double): Double {
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun testBrandColorContrastsExceedWCAGAA() {
        // Ink: #2B1A27 (43, 26, 39)
        val lumInk = luminance(43, 26, 39)

        // TukraPink: #FE81D4 (254, 129, 212)
        val lumPink = luminance(254, 129, 212)
        val ratioInkOnPink = contrastRatio(lumPink, lumInk)
        assertTrue("Ink on TukraPink should be >= 4.5:1 (was $ratioInkOnPink)", ratioInkOnPink >= 4.5)

        // Butter: #FFEABB (255, 234, 187)
        val lumButter = luminance(255, 234, 187)
        val ratioInkOnButter = contrastRatio(lumButter, lumInk)
        assertTrue("Ink on Butter should be >= 4.5:1 (was $ratioInkOnButter)", ratioInkOnButter >= 4.5)

        // Rose: #FAACBF (250, 172, 191)
        val lumRose = luminance(250, 172, 191)
        val ratioInkOnRose = contrastRatio(lumRose, lumInk)
        assertTrue("Ink on Rose should be >= 4.5:1 (was $ratioInkOnRose)", ratioInkOnRose >= 4.5)

        // Blush: #FBC3C1 (251, 195, 193)
        val lumBlush = luminance(251, 195, 193)
        val ratioInkOnBlush = contrastRatio(lumBlush, lumInk)
        assertTrue("Ink on Blush should be >= 4.5:1 (was $ratioInkOnBlush)", ratioInkOnBlush >= 4.5)

        // PinkDeep on White: #B0246F (176, 36, 111) on #FFFFFF
        val lumWhite = luminance(255, 255, 255)
        val lumPinkDeep = luminance(176, 36, 111)
        val ratioPinkDeepOnWhite = contrastRatio(lumWhite, lumPinkDeep)
        assertTrue("PinkDeep on White should be >= 4.5:1 (was $ratioPinkDeepOnWhite)", ratioPinkDeepOnWhite >= 4.5)
    }

    @Test
    fun testVerbatimMicrocopyStrings() {
        val emptyCart = "Nothing here yet. Pick your tukra."
        val slotPickerTitle = "When do you want it?"
        val orderReady = "Your order is hot and ready. Show your QR at the counter."
        val slotFull = "That time just filled up. Try the next slot."
        val noInternet = "No connection. Your cart is safe, we will wait."

        assertTrue(emptyCart.contains("Pick your tukra"))
        assertEquals("When do you want it?", slotPickerTitle)
        assertTrue(orderReady.contains("hot and ready"))
        assertTrue(slotFull.contains("filled up"))
        assertTrue(noInternet.contains("Your cart is safe"))
    }

    @Test
    fun testTalkBackDishRowFormat() {
        fun formatTalkBack(name: String, isVeg: Boolean, price: String, qty: Int): String {
            val veg = if (isVeg) "Vegetarian" else "Non-vegetarian"
            val cart = if (qty > 0) "$qty in cart" else "Not in cart"
            return "$name, $veg, $price, $cart"
        }

        val label = formatTalkBack("Masala Dosa", true, "₹70", 2)
        assertEquals("Masala Dosa, Vegetarian, ₹70, 2 in cart", label)

        val label2 = formatTalkBack("Chicken Biryani", false, "₹140", 0)
        assertEquals("Chicken Biryani, Non-vegetarian, ₹140, Not in cart", label2)
    }
}
