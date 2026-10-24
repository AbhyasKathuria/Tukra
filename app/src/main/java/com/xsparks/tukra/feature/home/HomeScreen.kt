package com.xsparks.tukra.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Blush
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.SecondaryButton
import com.xsparks.tukra.core.network.TokenStore
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    tokenStore: TokenStore,
    onLogout: () -> Unit
) {
    val user by tokenStore.userFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top Bar: Greeting + Campus
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, ${user?.name?.split(" ")?.firstOrNull() ?: "Student"} 👋",
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Ink
                )
                Text(
                    text = "Presidency University",
                    fontSize = 13.sp,
                    color = InkSoft
                )
            }

            // Wallet pill placeholder
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Butter)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "₹0",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Ink
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Break Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Butter)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Break in 18 min",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Order ahead now to pick up without waiting in line.",
                    fontSize = 13.sp,
                    color = InkSoft
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Campus Canteens",
            fontFamily = SoraFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Ink
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Blush.copy(alpha = 0.3f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Restaurant,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Student Ordering Hub Ready",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Full menu browsing, cart & 5-min slot reservation loads in Stage 6.",
                    fontSize = 13.sp,
                    color = InkSoft
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        SecondaryButton(
            text = "Log Out",
            onClick = {
                scope.launch {
                    tokenStore.clearSession()
                    onLogout()
                }
            },
            isOutlined = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
