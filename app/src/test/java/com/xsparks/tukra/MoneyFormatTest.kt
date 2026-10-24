package com.xsparks.tukra

import com.xsparks.tukra.core.util.formatIndianNumber
import com.xsparks.tukra.core.util.toRupees
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatTest {

    @Test
    fun testIndianNumberFormatting() {
        assertEquals("0", formatIndianNumber(0))
        assertEquals("70", formatIndianNumber(70))
        assertEquals("999", formatIndianNumber(999))
        assertEquals("1,000", formatIndianNumber(1000))
        assertEquals("10,000", formatIndianNumber(10000))
        assertEquals("1,00,000", formatIndianNumber(100000))
        assertEquals("1,24,000", formatIndianNumber(124000))
        assertEquals("1,00,00,000", formatIndianNumber(10000000))
    }

    @Test
    fun testPaiseToRupeesFormatting() {
        // Integer rupees
        assertEquals("₹70", 7000.toRupees())
        assertEquals("₹145", 14500.toRupees())
        assertEquals("₹1,240", 124000.toRupees())
        assertEquals("₹1,00,000", 10000000.toRupees())

        // Decimal paise
        assertEquals("₹145.50", 14550.toRupees())
        assertEquals("₹0.75", 75.toRupees())
    }
}
