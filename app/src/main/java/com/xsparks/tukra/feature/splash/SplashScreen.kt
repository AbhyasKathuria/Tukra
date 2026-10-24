package com.xsparks.tukra.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.network.TokenStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    tokenStore: TokenStore,
    onNavigateToHome: () -> Unit,
    onNavigateToVendor: () -> Unit,
    onNavigateToMess: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 600)
        )
        delay(600) // session check < 1.2s total

        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            onNavigateToAuth()
        } else {
            val role = tokenStore.roleFlow.first() ?: "STUDENT"
            when (role.uppercase()) {
                "VENDOR" -> onNavigateToVendor()
                "MESS_OPERATOR" -> onNavigateToMess()
                "ADMIN" -> onNavigateToAdmin()
                else -> onNavigateToHome()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tukra wordmark with pink "bite" mark
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "T",
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    color = Ink
                )

                // The letter "u" with a pink bite accent
                Box(contentAlignment = Alignment.TopCenter) {
                    Text(
                        text = "u",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                        color = Ink
                    )
                    // Pink bite / crescent dot above the u
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(
                            color = TukraPink,
                            radius = size.width / 2
                        )
                        // Cut-out bite
                        drawCircle(
                            color = Color.White,
                            radius = size.width / 3.2f,
                            center = center.copy(x = center.x + size.width * 0.25f, y = center.y - size.height * 0.1f)
                        )
                    }
                }

                Text(
                    text = "kra",
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Take your tukra. Pay for your tukra.",
                fontFamily = SoraFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = InkSoft
            )
        }
    }
}
