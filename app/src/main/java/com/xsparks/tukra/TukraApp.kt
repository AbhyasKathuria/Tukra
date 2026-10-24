package com.xsparks.tukra

import android.app.Application
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TukraApp : Application() {

    lateinit var tokenStore: TokenStore
        private set

    override fun onCreate() {
        super.onCreate()
        tokenStore = TokenStore(applicationContext)

        // Restore custom backend base URL if configured
        CoroutineScope(Dispatchers.IO).launch {
            tokenStore.getBaseUrl()?.let { savedUrl ->
                if (savedUrl.isNotBlank()) {
                    NetworkClient.setCustomBaseUrl(savedUrl)
                }
            }
        }
    }
}
