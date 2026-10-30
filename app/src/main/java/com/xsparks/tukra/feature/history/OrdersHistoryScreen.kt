package com.xsparks.tukra.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.Success
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.EmptyState
import com.xsparks.tukra.core.designsystem.components.SkeletonShimmer
import com.xsparks.tukra.core.designsystem.components.StudentBottomBar
import com.xsparks.tukra.core.designsystem.components.StudentNavTab
import com.xsparks.tukra.core.network.OrderDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.TimeFormat
import com.xsparks.tukra.core.util.toRupees

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersHistoryScreen(
    tokenStore: TokenStore,
    viewModel: OrdersViewModel,
    onOrderClick: (orderId: String) -> Unit,
    onTabSelected: (StudentNavTab) -> Unit,
    onFoodRescueClick: (dish: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Orders",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Ink
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadOrders() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            StudentBottomBar(
                currentTab = StudentNavTab.ORDERS,
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
            // Month Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Butter)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "This Month's Spending",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkSoft
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${uiState.totalSpentThisMonthPaise.toRupees()} spent",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Ink
                        )
                        Text(
                            text = "${uiState.totalOrdersThisMonth} orders placed",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }

                    // Mini bar sparkline simulation
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(36.dp)
                    ) {
                        listOf(16.dp, 24.dp, 12.dp, 32.dp, 28.dp).forEach { height ->
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(height)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(TukraPink)
                            )
                        }
                    }
                }
            }

            // Tabs: Active / Past
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Ink,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = TukraPink
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "Active (${uiState.activeOrders.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            color = Ink
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "Past (${uiState.pastOrders.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            color = Ink
                        )
                    }
                )
            }

            // Orders List
            val displayedOrders = if (selectedTabIndex == 0) uiState.activeOrders else uiState.pastOrders

            if (uiState.isLoading && displayedOrders.isEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp)) {
                    items(3) {
                        SkeletonShimmer(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), height = 110.dp, shapeRadius = 16.dp)
                    }
                }
            } else if (displayedOrders.isEmpty()) {
                EmptyState(
                    title = if (selectedTabIndex == 0) "No active orders" else "No past orders",
                    message = if (selectedTabIndex == 0) "Your currently placed and preparing orders will appear here." else "Your past collected and refunded orders will be listed here.",
                    icon = Icons.Outlined.ReceiptLong
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedOrders, key = { it.id }) { order ->
                        OrderHistoryCard(
                            order = order,
                            onClick = { onOrderClick(order.id) },
                            onFoodRescueClick = onFoodRescueClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(
    order: OrderDto,
    onClick: () -> Unit,
    onFoodRescueClick: (dish: String) -> Unit = {}
) {
    val statusBg = when (order.status) {
        "READY" -> Success.copy(alpha = 0.15f)
        "COLLECTED" -> Color(0xFFF3F3F3)
        "PREPARING", "ACCEPTED" -> Butter
        "CANCELLED", "REJECTED", "NO_SHOW" -> Danger.copy(alpha = 0.12f)
        else -> Blush.copy(alpha = 0.4f)
    }

    val statusColor = when (order.status) {
        "READY" -> Success
        "CANCELLED", "REJECTED", "NO_SHOW" -> Danger
        else -> Ink
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.code,
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Ink
                    )
                    Text(
                        text = order.outletName ?: "Campus Canteen",
                        fontSize = 13.sp,
                        color = InkSoft
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items line
            val itemsSummary = order.items.joinToString(", ") { "${it.qty} × ${it.name}" }
            Text(
                text = itemsSummary,
                fontSize = 13.sp,
                color = Ink,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.slotStartsAt?.let { TimeFormat.formatSlotTime(it) } ?: "",
                    fontSize = 12.sp,
                    color = InkSoft
                )
                Text(
                    text = order.totalPaise.toRupees(),
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Ink
                )
            }

            if (order.status == "COLLECTED") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E101D))
                            .clickable {
                                val dish = order.items.firstOrNull()?.name ?: "Meal"
                                onFoodRescueClick(dish)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "🐟 Disliked? TinyFish Rescue",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD166)
                        )
                    }
                }
            }
        }
    }
}
