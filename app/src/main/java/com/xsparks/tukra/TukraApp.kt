package com.xsparks.tukra

import android.app.Application
import com.xsparks.tukra.core.network.TokenStore

class TukraApp : Application() {

    lateinit var tokenStore: TokenStore
        private set

    override fun onCreate() {
        super.onCreate()
        tokenStore = TokenStore(applicationContext)
    }
}
