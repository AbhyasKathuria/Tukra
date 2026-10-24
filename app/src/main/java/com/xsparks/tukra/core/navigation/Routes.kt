package com.xsparks.tukra.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Splash : Route
    @Serializable data object Onboarding : Route
    @Serializable data object Login : Route
    @Serializable data object Register : Route
    @Serializable data object StudentHome : Route
    @Serializable data object VendorQueue : Route
    @Serializable data object MessMenu : Route
    @Serializable data object AdminOverview : Route
}
