package com.xsparks.tukra.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.PinkDeep
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.Success
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.AdminBottomBar
import com.xsparks.tukra.core.designsystem.components.AdminNavTab
import com.xsparks.tukra.core.network.AdminOutletItem
import com.xsparks.tukra.core.network.TokenStore

@Composable
fun AdminOutletsScreen(
    tokenStore: TokenStore,
    viewModel: AdminViewModel,
    onTabSelected: (AdminNavTab) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadOutlets()
    }

    LaunchedEffect(uiState.actionSuccessMessage) {
        uiState.actionSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AdminBottomBar(
                currentTab = AdminNavTab.OUTLETS,
                onTabSelected = onTabSelected
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Campus Outlets",
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Ink
                )
                Text(
                    text = "Operational status, slot capacities and TinyFish sync health",
                    fontSize = 12.sp,
                    color = InkSoft
                )
            }

            HorizontalDivider(color = OutlineWarm, thickness = 1.dp)

            if (uiState.outlets.isEmpty() && uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TukraPink)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.outlets, key = { it.id }) { outlet ->
                        AdminOutletCard(
                            outlet = outlet,
                            onSyncClick = { viewModel.triggerTinyFishSync() }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOutletCard(
    outlet: AdminOutletItem,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Butter),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Storefront, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = outlet.name,
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Ink
                        )
                        Text(
                            text = "Operator: ${outlet.ownerName ?: "Stall Manager"} • Type: ${outlet.type}",
                            fontSize = 11.sp,
                            color = InkSoft
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (outlet.isOpen) Success.copy(alpha = 0.15f) else Danger.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (outlet.isOpen) "OPEN" else "CLOSED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (outlet.isOpen) Success else Danger
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = OutlineWarm, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Slot Capacity", fontSize = 10.sp, color = InkSoft)
                    Text(
                        text = "${outlet.slotCapacity ?: 10} units / 5 min",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Ink
                    )
                }

                Column {
                    Text(text = "Orders Today", fontSize = 10.sp, color = InkSoft)
                    Text(
                        text = "${outlet.totalOrders} orders",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Ink
                    )
                }

                Column {
                    Text(text = "TinyFish Sync", fontSize = 10.sp, color = InkSoft)
                    Text(
                        text = outlet.lastSyncStatus ?: "Never",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (outlet.lastSyncStatus == "COMPLETED") Success else PinkDeep
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSyncClick,
                colors = ButtonDefaults.buttonColors(containerColor = Butter, contentColor = Ink),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Ink, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Menu with TinyFish AI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
