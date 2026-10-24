package com.xsparks.tukra

import com.xsparks.tukra.core.network.HourlyOrderStat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MessAndAdminLogicTest {

    @Test
    fun testMessDemandForecastCalculation() {
        // Suggested quantity is max of baseline (15) and (4-week avg + current reservations)
        fun calculateSuggestedQty(fourWeekAvg: Int, currentReservations: Int): Int {
            return maxOf(15, fourWeekAvg + currentReservations)
        }

        // Case 1: Brand new dish with 0 history and 0 reservations -> baseline 15
        assertEquals(15, calculateSuggestedQty(0, 0))

        // Case 2: 25 avg history + 12 current pre-reservations -> 37 portions
        assertEquals(37, calculateSuggestedQty(25, 12))

        // Case 3: 5 avg history + 3 reservations -> 8 < 15 -> baseline 15
        assertEquals(15, calculateSuggestedQty(5, 3))
    }

    @Test
    fun testFoodWasteAndSurplusCalculation() {
        fun calculateWaste(planned: Int, consumed: Int): Pair<Int, String> {
            val estimatedSurplus = maxOf(0, planned - consumed)
            val wastePercent = if (planned > 0) Math.round((estimatedSurplus.toFloat() / planned.toFloat()) * 100) else 0
            val status = if (wastePercent > 20) "HIGH_SURPLUS" else "BALANCED"
            return Pair(wastePercent, status)
        }

        // Balanced: 50 planned, 45 consumed (10% surplus)
        val (waste1, status1) = calculateWaste(50, 45)
        assertEquals(10, waste1)
        assertEquals("BALANCED", status1)

        // High surplus: 50 planned, 25 consumed (50% surplus)
        val (waste2, status2) = calculateWaste(50, 25)
        assertEquals(50, waste2)
        assertEquals("HIGH_SURPLUS", status2)

        // Perfect match: 40 planned, 40 consumed (0% surplus)
        val (waste3, status3) = calculateWaste(40, 40)
        assertEquals(0, waste3)
        assertEquals("BALANCED", status3)
    }

    @Test
    fun testHourlyChartPeakDetection() {
        val stats = listOf(
            HourlyOrderStat(8, "8 AM", 2, 19000),
            HourlyOrderStat(12, "12 PM", 18, 142000), // Lunch peak
            HourlyOrderStat(15, "3 PM", 5, 35000),
            HourlyOrderStat(18, "6 PM", 12, 95000)
        )

        val maxCount = stats.maxOfOrNull { it.count } ?: 0
        assertEquals(18, maxCount)

        val peakStat = stats.first { it.count == maxCount }
        assertEquals("12 PM", peakStat.label)
        assertTrue(peakStat.revenuePaise > 100000)
    }
}
