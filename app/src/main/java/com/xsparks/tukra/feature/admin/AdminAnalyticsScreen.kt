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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Blush
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
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.network.TopDishStat
import com.xsparks.tukra.core.network.WasteAuditItem
import com.xsparks.tukra.core.util.toRupees

@Composable
fun AdminAnalyticsScreen(
    tokenStore: TokenStore,
    viewModel: AdminViewModel,
    onTabSelected: (AdminNavTab) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDemand()
    }

    Scaffold(
        bottomBar = {
            AdminBottomBar(
                currentTab = AdminNavTab.ANALYTICS,
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
                    text = "Demand & Waste Analytics",
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Ink
                )
                Text(
                    text = "Campus consumption trends and mess food surplus auditing",
                    fontSize = 12.sp,
                    color = InkSoft
                )
            }

            HorizontalDivider(color = OutlineWarm, thickness = 1.dp)

            if (uiState.demand == null && uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TukraPink)
                }
            } else {
                val demand = uiState.demand
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Dishes Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Leaderboard, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Top Consumed Dishes Today",
                                        fontFamily = SoraFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Ink
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val dishes = demand?.topDishes ?: emptyList()
                                if (dishes.isEmpty()) {
                                    Text("No orders placed yet today.", fontSize = 12.sp, color = InkSoft)
                                } else {
                                    val maxQty = dishes.maxOfOrNull { it.totalQty }?.coerceAtLeast(1) ?: 1
                                    dishes.forEachIndexed { index, dish ->
                                        TopDishRow(index = index + 1, dish = dish, maxQty = maxQty)
                                        if (index < dishes.lastIndex) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Food Waste Estimate Card
                    item {
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
                                        Icon(Icons.Outlined.Eco, contentDescription = null, tint = Success, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Mess Food Surplus Audit",
                                            fontFamily = SoraFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Ink
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Butter)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Waste Target < 10%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Compares planned kitchen cooking batches against actual reservations and smart plate consumption.",
                                    fontSize = 11.sp,
                                    color = InkSoft
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                val wasteItems = demand?.wasteAudit ?: emptyList()
                                if (wasteItems.isEmpty()) {
                                    Text("No mess cooking batches recorded for today.", fontSize = 12.sp, color = InkSoft)
                                } else {
                                    wasteItems.forEach { item ->
                                        WasteAuditRow(item = item)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
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
fun TopDishRow(index: Int, dish: TopDishStat, maxQty: Int) {
    val fraction = (dish.totalQty.toFloat() / maxQty.toFloat()).coerceIn(0.1f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index. ${dish.name}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Ink
            )
            Text(
                text = "${dish.totalQty} sold (${dish.totalRevenuePaise.toRupees()})",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = PinkDeep
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFEEEEEE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TukraPink)
            )
        }
    }
}

@Composable
fun WasteAuditRow(item: WasteAuditItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, OutlineWarm, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${item.dishName} (${item.meal})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Ink
                )
                Text(
                    text = "Planned: ${item.plannedQty} • Consumed: ${item.consumedQty} • Surplus: ${item.estimatedSurplus}",
                    fontSize = 11.sp,
                    color = InkSoft
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (item.status == "BALANCED") Success.copy(alpha = 0.15f) else Danger.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${item.wastePercent}% surplus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (item.status == "BALANCED") Success else Danger
                )
            }
        }
    }
}
