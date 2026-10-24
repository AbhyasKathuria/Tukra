package com.xsparks.tukra

import com.xsparks.tukra.core.network.OrderDto
import com.xsparks.tukra.core.network.ProposalDto
import com.xsparks.tukra.feature.vendor.QueueFilterTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VendorQueueFilterTest {

    private val sampleOrders = listOf(
        OrderDto(id = "o1", code = "TK-1001", status = "PLACED", totalPaise = 5000),
        OrderDto(id = "o2", code = "TK-1002", status = "ACCEPTED", totalPaise = 8500),
        OrderDto(id = "o3", code = "TK-1003", status = "PREPARING", totalPaise = 12000),
        OrderDto(id = "o4", code = "TK-1004", status = "READY", totalPaise = 4500),
        OrderDto(id = "o5", code = "TK-1005", status = "COLLECTED", totalPaise = 7000)
    )

    private fun filterOrders(orders: List<OrderDto>, tab: QueueFilterTab): List<OrderDto> {
        return when (tab) {
            QueueFilterTab.ALL -> orders
            QueueFilterTab.PLACED -> orders.filter { it.status == "PLACED" }
            QueueFilterTab.PREPARING -> orders.filter { it.status in listOf("ACCEPTED", "PREPARING") }
            QueueFilterTab.READY -> orders.filter { it.status == "READY" }
        }
    }

    @Test
    fun testQueueFilters() {
        // Tab ALL returns all active orders
        val all = filterOrders(sampleOrders, QueueFilterTab.ALL)
        assertEquals(5, all.size)

        // Tab PLACED returns only new placed orders
        val placed = filterOrders(sampleOrders, QueueFilterTab.PLACED)
        assertEquals(1, placed.size)
        assertEquals("TK-1001", placed[0].code)

        // Tab PREPARING returns both ACCEPTED and PREPARING kitchen orders
        val cooking = filterOrders(sampleOrders, QueueFilterTab.PREPARING)
        assertEquals(2, cooking.size)
        assertTrue(cooking.any { it.code == "TK-1002" })
        assertTrue(cooking.any { it.code == "TK-1003" })

        // Tab READY returns ready orders waiting for student QR scan
        val ready = filterOrders(sampleOrders, QueueFilterTab.READY)
        assertEquals(1, ready.size)
        assertEquals("TK-1004", ready[0].code)
    }

    @Test
    fun testProposalFlagging() {
        val normalProposal = ProposalDto(
            id = "p1",
            changeType = "PRICE_CHANGE",
            itemName = "Masala Dosa",
            oldPricePaise = 7000,
            newPricePaise = 7500,
            flagged = false
        )
        assertFalse(normalProposal.flagged)

        val extremeProposal = ProposalDto(
            id = "p2",
            changeType = "PRICE_CHANGE",
            itemName = "Cold Coffee",
            oldPricePaise = 4000,
            newPricePaise = 15000,
            flagged = true
        )
        assertTrue(extremeProposal.flagged)
    }

    @Test
    fun testVendorStatusProgression() {
        val allowedTransitions = mapOf(
            "PLACED" to listOf("ACCEPTED", "REJECTED"),
            "ACCEPTED" to listOf("PREPARING", "REJECTED"),
            "PREPARING" to listOf("READY"),
            "READY" to listOf("COLLECTED")
        )

        assertTrue(allowedTransitions["PLACED"]!!.contains("ACCEPTED"))
        assertTrue(allowedTransitions["ACCEPTED"]!!.contains("PREPARING"))
        assertTrue(allowedTransitions["PREPARING"]!!.contains("READY"))
        assertTrue(allowedTransitions["READY"]!!.contains("COLLECTED"))

        assertFalse(allowedTransitions["PLACED"]!!.contains("READY")) // Cannot jump straight to READY
        assertFalse(allowedTransitions["PREPARING"]!!.contains("COLLECTED")) // Cannot collect before ready
    }
}
