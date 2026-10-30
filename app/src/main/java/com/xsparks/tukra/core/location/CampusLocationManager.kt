package com.xsparks.tukra.core.location

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class CampusLocation(
    val latitude: Double = 13.169,
    val longitude: Double = 77.536,
    val area: String = "Presidency University, Cauvery Block B",
    val city: String = "Bengaluru",
    val pincode: String = "560064",
    val isOnCampus: Boolean = true,
    val dropOffGate: String = "Main Gate 2 (Delivery Drop-off Hub)"
)

object CampusLocationManager {
    const val CAMPUS_LAT = 13.169
    const val CAMPUS_LNG = 77.536
    const val CAMPUS_RADIUS_METERS = 600.0

    /**
     * Resolves the current student location, rounded to 3 decimal places.
     * Never records or persists location history. Falls back to Presidency campus if denied or offline.
     */
    fun getResolvedLocation(context: Context): CampusLocation {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            var bestLocation: Location? = null

            if (locationManager != null) {
                val providers = locationManager.getProviders(true)
                for (provider in providers) {
                    try {
                        val l = locationManager.getLastKnownLocation(provider) ?: continue
                        if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                            bestLocation = l
                        }
                    } catch (_: SecurityException) {
                        // Coarse location permission not granted, fallback gracefully
                    }
                }
            }

            if (bestLocation != null) {
                val roundedLat = Math.round(bestLocation.latitude * 1000.0) / 1000.0
                val roundedLng = Math.round(bestLocation.longitude * 1000.0) / 1000.0
                val distMeters = calculateDistance(roundedLat, roundedLng, CAMPUS_LAT, CAMPUS_LNG)
                val onCampus = distMeters <= CAMPUS_RADIUS_METERS

                var resolvedArea = if (onCampus) "Presidency University (Cauvery Block B)" else "Rajanakunte / Yelahanka"
                var resolvedCity = "Bengaluru"
                var resolvedPincode = "560064"

                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(roundedLat, roundedLng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        resolvedArea = addr.subLocality ?: addr.locality ?: resolvedArea
                        resolvedCity = addr.locality ?: resolvedCity
                        resolvedPincode = addr.postalCode ?: resolvedPincode
                    }
                } catch (_: Exception) {
                    // Geocoder lookup failed or network offline
                }

                return CampusLocation(
                    latitude = roundedLat,
                    longitude = roundedLng,
                    area = resolvedArea,
                    city = resolvedCity,
                    pincode = resolvedPincode,
                    isOnCampus = onCampus,
                    dropOffGate = if (onCampus) "Main Gate 2 (Delivery Drop-off Hub)" else "Direct Doorstep Drop-off"
                )
            }
        } catch (_: Exception) {
            // Keep default Presidency University coordinates
        }

        return CampusLocation()
    }

    /**
     * Haversine distance formula between two lat/lng coordinates in meters.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // Earth radius in meters
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2) * sin(deltaLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }
}
