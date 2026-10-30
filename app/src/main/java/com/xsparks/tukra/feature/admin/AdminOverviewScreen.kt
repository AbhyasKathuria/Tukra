package com.xsparks.tukra.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Blush
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.PinkDeep
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.Success
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.AdminBottomBar
import com.xsparks.tukra.core.designsystem.components.AdminNavTab
import com.xsparks.tukra.core.network.HourlyOrderStat
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.toRupees
import kotlinx.coroutines.launch

@Composable
fun AdminOverviewScreen(
    tokenStore: TokenStore,
    viewModel: AdminViewModel,
    onTabSelected: (AdminNavTab) -> Unit,
    onLogout: () -> Unit,
    onViewAgentConsole: () -> Unit = {},
    onViewAboutTinyFish: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
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
                currentTab = AdminNavTab.OVERVIEW,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Campus Command",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Ink
                        )
                        Text(
                            text = "Executive Campus Dining & Intelligence",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.loadAll() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = Ink)
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    tokenStore.clearSession()
                                    onLogout()
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Log Out", tint = InkSoft)
                        }
                    }
                }
            }

            HorizontalDivider(color = OutlineWarm, thickness = 1.dp)

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TukraPink)
                }
            } else {
                val overview = uiState.overview
                val kpis = overview?.kpis

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TinyFish Agent Console Banner for Admin
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E101D))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🐟", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "TINYFISH AGENT LAYER",
                                        fontFamily = SoraFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFD166)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF06D6A0))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("LIVE AGENT MONITOR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Autonomous Menu Sync & Food Rescue Agent Runs",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Tracks live stream runs, duration, 30-min credit caching & error handling",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFD166))
                                        .clickable { onViewAgentConsole() }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⚡ Open Console", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .clickable { onViewAboutTinyFish() }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("ℹ️ Usage Map", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // 4 KPI Cards Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiTile(
                            title = "Orders Today",
                            value = "${kpis?.ordersToday ?: 0}",
                            subtitle = "Across all campus outlets",
                            icon = Icons.Outlined.ReceiptLong,
                            iconBg = Butter,
                            modifier = Modifier.weight(1f)
                        )
                        KpiTile(
                            title = "Revenue Today",
                            value = "₹${kpis?.revenueTodayRupees ?: 0}",
                            subtitle = "Itemized GST inclusive",
                            icon = Icons.Outlined.AccountBalanceWallet,
                            iconBg = Blush,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiTile(
                            title = "Active Outlets",
                            value = "${kpis?.activeOutlets ?: 0} / ${kpis?.totalOutlets ?: 0}",
                            subtitle = "Accepting orders now",
                            icon = Icons.Outlined.Storefront,
                            iconBg = Success.copy(alpha = 0.2f),
                            modifier = Modifier.weight(1f)
                        )
                        KpiTile(
                            title = "Average Wait",
                            value = "${kpis?.avgWaitMinutes ?: 8} min",
                            subtitle = "QueueLess pickup delay",
                            icon = Icons.Outlined.Schedule,
                            iconBg = Butter,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Orders by Hour Chart Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Orders by Hour (Today)",
                                    fontFamily = SoraFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Ink
                                )
                                Text(
                                    text = "Campus Break Spikes",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PinkDeep
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OrdersByHourChart(
                                hours = overview?.ordersByHour ?: emptyList()
                            )
                        }
                    }

                    // Autonomous TinyFish Agent Trigger Button
                    Button(
                        onClick = { viewModel.triggerTinyFishSync() },
                        enabled = !uiState.isActionLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TukraPink,
                            contentColor = Ink
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isActionLoading) "Triggering Agent..." else "Run TinyFish Campus Intelligence Agent",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun KpiTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = InkSoft, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontFamily = SoraFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Ink
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = InkSoft)
        }
    }
}

@Composable
fun OrdersByHourChart(hours: List<HourlyOrderStat>) {
    val maxCount = hours.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            hours.forEach { stat ->
                val barFraction = (stat.count.toFloat() / maxCount.toFloat()).coerceIn(0.08f, 1f)
                val isPeak = stat.count == maxCount && stat.count > 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    if (stat.count > 0) {
                        Text(
                            text = stat.count.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPeak) PinkDeep else InkSoft
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height((100 * barFraction).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (isPeak) TukraPink else Blush.copy(alpha = 0.5f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = OutlineWarm, thickness = 1.dp)
        Spacer(modifier = Modifier.height(4.dp))

        // Hour labels (sampling every second hour for legibility)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            hours.filterIndexed { index, _ -> index % 2 == 0 }.forEach { stat ->
                Text(
                    text = stat.label.replace(" ", ""),
                    fontSize = 9.sp,
                    color = InkSoft
                )
            }
        }
    }
}
