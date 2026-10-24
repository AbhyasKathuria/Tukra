package com.xsparks.tukra.feature.mess

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
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TrendingUp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.PinkDeep
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.MessBottomBar
import com.xsparks.tukra.core.designsystem.components.MessNavTab
import com.xsparks.tukra.core.designsystem.components.VegNonVegMark
import com.xsparks.tukra.core.network.DemandForecastItemDto
import com.xsparks.tukra.core.network.TokenStore

@Composable
fun MessReservationsScreen(
    tokenStore: TokenStore,
    viewModel: MessReservationsViewModel,
    onTabSelected: (MessNavTab) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.notice) {
        uiState.notice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotice()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MessBottomBar(
                currentTab = MessNavTab.RESERVATIONS,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mess Demand Forecast",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Outlined.TrendingUp, contentDescription = null, tint = PinkDeep, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "Historical weekday average + pre-booked student reservations",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }

                    Button(
                        onClick = { viewModel.exportReservations(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Butter, contentColor = Ink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            HorizontalDivider(color = OutlineWarm, thickness = 1.dp)

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TukraPink)
                }
            } else {
                val forecast = uiState.forecast
                if (forecast == null || forecast.items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No forecast data available for this date.", color = InkSoft, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Butter.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Target Date: ${forecast.targetDate} (${forecast.dayOfWeek}) • Baseline 4-week weekday history",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Ink
                                )
                            }
                        }

                        items(forecast.items, key = { it.menuItemId }) { item ->
                            ForecastItemCard(item = item)
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastItemCard(item: DemandForecastItemDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VegNonVegMark(isVeg = item.isVeg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.name,
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Ink
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF4F4F4))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = item.category, fontSize = 11.sp, color = InkSoft)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Metric Row: Booked, Historical Avg, Suggested Quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Reservations", fontSize = 11.sp, color = InkSoft)
                    Text(
                        text = "${item.currentReservations} booked",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Ink
                    )
                }

                Column {
                    Text(text = "4-Wk History", fontSize = 11.sp, color = InkSoft)
                    Text(
                        text = "${item.fourWeekWeekdayAvg} avg",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Ink
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Butter)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${item.suggestedQty} portions",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Ink
                        )
                        Text(
                            text = "Suggested (Estimate)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinkDeep
                        )
                    }
                }
            }
        }
    }
}
