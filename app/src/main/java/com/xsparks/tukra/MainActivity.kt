package com.xsparks.tukra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.xsparks.tukra.core.designsystem.TukraTheme
import com.xsparks.tukra.core.navigation.TukraNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tokenStore = (application as TukraApp).tokenStore

        setContent {
            TukraTheme {
                val navController = rememberNavController()
                TukraNavHost(
                    navController = navController,
                    tokenStore = tokenStore
                )
            }
        }
    }
}
