package com.xsparks.tukra.core.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

sealed interface Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>
    data class Error(val message: String, val code: String? = null) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}

suspend fun <T> safeApiCall(call: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(call())
    } catch (e: IOException) {
        Resource.Error(
            message = "You appear to be offline. Please check your internet connection.",
            code = "OFFLINE"
        )
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        var message = "An unexpected error occurred (${e.code()})"
        var code: String? = null

        if (!errorBody.isNullOrBlank()) {
            try {
                val json = Json { ignoreUnknownKeys = true }
                val root = json.parseToJsonElement(errorBody).jsonObject
                val errorObj = root["error"]?.jsonObject
                if (errorObj != null) {
                    message = errorObj["message"]?.jsonPrimitive?.content ?: message
                    code = errorObj["code"]?.jsonPrimitive?.content
                }
            } catch (_: Exception) {
                // Keep default error message
            }
        }

        // Map friendly error message based on code
        val friendlyMessage = when (code) {
            "SLOT_FULL" -> "This pickup slot is now full. Please select an alternate slot."
            "ITEM_UNAVAILABLE" -> "One or more items in your cart are currently sold out."
            "OUTLET_CLOSED" -> "This outlet is currently closed or past last-order time."
            "INSUFFICIENT_BALANCE" -> "Your wallet balance is insufficient. Please top up your wallet."
            "UNAUTHENTICATED" -> "Your session has expired. Please log in again."
            "UNAUTHORIZED" -> "You do not have permission to perform this action."
            "ALREADY_COLLECTED" -> "This order has already been collected."
            "WRONG_OUTLET" -> "This order does not belong to your outlet."
            else -> message
        }

        Resource.Error(message = friendlyMessage, code = code)
    } catch (e: Exception) {
        Resource.Error(
            message = e.localizedMessage ?: "Something went wrong. Please try again.",
            code = "UNKNOWN"
        )
    }
}
