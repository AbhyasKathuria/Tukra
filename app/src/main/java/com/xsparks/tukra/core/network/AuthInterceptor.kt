package com.xsparks.tukra.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {
    @Volatile
    private var cachedToken: String? = null

    fun setToken(token: String?) {
        cachedToken = token
    }

    fun clearToken() {
        cachedToken = null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = cachedToken ?: runBlocking {
            tokenStore.getToken()
        }?.also { cachedToken = it }

        val request = if (!token.isNullOrBlank() && original.header("Authorization") == null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)
        if (response.code == 401) {
            // Invalidate stale or expired token
            clearToken()
        }
        return response
    }
}
