package com.xsparks.tukra.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.xsparks.tukra.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkClient {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }

    private var apiInstance: TukraApi? = null
    private var authInterceptorInstance: AuthInterceptor? = null
    private var activeBaseUrl: String? = null
    var customBaseUrlOverride: String? = null

    fun getEffectiveBaseUrl(): String {
        val override = customBaseUrlOverride
        if (!override.isNullOrBlank()) {
            return if (override.endsWith("/")) override else "$override/"
        }
        var baseUrl = BuildConfig.API_BASE_URL
        return if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    }

    fun setCustomBaseUrl(url: String?) {
        if (url.isNullOrBlank()) {
            customBaseUrlOverride = null
        } else {
            var formatted = url.trim()
            if (!formatted.endsWith("/")) {
                formatted = "$formatted/"
            }
            customBaseUrlOverride = formatted
        }
        apiInstance = null // Invalidate to rebuild on next call
    }

    fun getAuthInterceptor(tokenStore: TokenStore): AuthInterceptor {
        if (authInterceptorInstance == null) {
            authInterceptorInstance = AuthInterceptor(tokenStore)
        }
        return authInterceptorInstance!!
    }

    fun getApi(tokenStore: TokenStore): TukraApi {
        val targetUrl = getEffectiveBaseUrl()
        if (apiInstance == null || activeBaseUrl != targetUrl) {
            activeBaseUrl = targetUrl
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }

            val authInterceptor = getAuthInterceptor(tokenStore)

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()

            val contentType = "application/json".toMediaType()
            val retrofit = Retrofit.Builder()
                .baseUrl(targetUrl)
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()

            apiInstance = retrofit.create(TukraApi::class.java)
        }
        return apiInstance!!
    }
}
