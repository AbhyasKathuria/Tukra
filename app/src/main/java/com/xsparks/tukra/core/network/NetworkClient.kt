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

    fun getAuthInterceptor(tokenStore: TokenStore): AuthInterceptor {
        if (authInterceptorInstance == null) {
            authInterceptorInstance = AuthInterceptor(tokenStore)
        }
        return authInterceptorInstance!!
    }

    fun getApi(tokenStore: TokenStore): TukraApi {
        if (apiInstance == null) {
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

            var baseUrl = BuildConfig.API_BASE_URL
            if (!baseUrl.endsWith("/")) {
                baseUrl = "$baseUrl/"
            }

            val contentType = "application/json".toMediaType()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()

            apiInstance = retrofit.create(TukraApi::class.java)
        }
        return apiInstance!!
    }
}
