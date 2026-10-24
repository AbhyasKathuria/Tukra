package com.xsparks.tukra.core.util

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeFormat {
    val IST_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.ENGLISH)
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    fun formatSlotTime(utcIsoString: String): String {
        return try {
            val instant = Instant.parse(utcIsoString)
            val istZoned = instant.atZone(IST_ZONE)
            TIME_FORMATTER.format(istZoned)
        } catch (_: Exception) {
            utcIsoString
        }
    }

    fun formatSlotRange(startIso: String, endIso: String): String {
        return try {
            val startZoned = Instant.parse(startIso).atZone(IST_ZONE)
            val endZoned = Instant.parse(endIso).atZone(IST_ZONE)
            "${TIME_FORMATTER.format(startZoned)} - ${TIME_FORMATTER.format(endZoned)}"
        } catch (_: Exception) {
            formatSlotTime(startIso)
        }
    }

    fun formatDateTime(utcIsoString: String): String {
        return try {
            val instant = Instant.parse(utcIsoString)
            val istZoned = instant.atZone(IST_ZONE)
            DATE_TIME_FORMATTER.format(istZoned)
        } catch (_: Exception) {
            utcIsoString
        }
    }

    fun minutesUntil(utcIsoString: String): Long {
        return try {
            val target = Instant.parse(utcIsoString)
            val now = Instant.now()
            Duration.between(now, target).toMinutes()
        } catch (_: Exception) {
            0
        }
    }
}
