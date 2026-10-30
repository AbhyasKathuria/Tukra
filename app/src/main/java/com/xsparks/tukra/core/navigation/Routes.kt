package com.xsparks.tukra.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Splash : Route
    @Serializable data object Onboarding : Route
    @Serializable data object Login : Route
    @Serializable data object Register : Route

    // Student Flow
    @Serializable data object StudentHome : Route
    @Serializable data class OutletMenu(val outletId: String, val outletName: String) : Route
    @Serializable data object Cart : Route
    @Serializable data class Checkout(
        val outletId: String,
        val outletName: String,
        val slotId: String,
        val slotStartsAt: String
    ) : Route
    @Serializable data class Tracking(val orderId: String) : Route
    @Serializable data object OrdersHistory : Route
    @Serializable data object Wallet : Route
    @Serializable data object ScanPlate : Route
    @Serializable data object Profile : Route
    @Serializable data object HostelMess : Route
    @Serializable data class TinyFishFoodRescue(val initialDish: String = "Paneer Butter Masala", val reason: String? = null) : Route

    // Roles & Vendor Flow
    @Serializable data object VendorQueue : Route
    @Serializable data object VendorMenu : Route
    @Serializable data object VendorCapacity : Route
    @Serializable data object VendorProposals : Route
    @Serializable data class VendorOrderDetail(val orderId: String) : Route
    // Mess Flow
    @Serializable data object MessMenu : Route
    @Serializable data object MessReservations : Route
    @Serializable data object MessCounter : Route

    // Admin Flow
    @Serializable data object AdminOverview : Route
    @Serializable data object AdminOutlets : Route
    @Serializable data object AdminAnalytics : Route
    @Serializable data object AdminSync : Route
}
