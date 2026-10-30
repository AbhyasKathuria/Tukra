package com.xsparks.tukra.feature.mess

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Blush
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.StudentBottomBar
import com.xsparks.tukra.core.designsystem.components.StudentNavTab
import com.xsparks.tukra.core.designsystem.components.VegNonVegMark
import com.xsparks.tukra.core.network.HostelDashboardDto
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.QrGenerator
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.safeApiCall
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelMessScreen(
    tokenStore: TokenStore,
    onBackClick: () -> Unit,
    onTabSelected: (StudentNavTab) -> Unit
) {
    val api = remember { NetworkClient.getApi(tokenStore) }
    var dashboardData by remember { mutableStateOf<HostelDashboardDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTimer by remember { mutableIntStateOf(30) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        val res = safeApiCall { api.getHostelDashboard() }
        if (res is Resource.Success) {
            dashboardData = res.data
        } else {
            // Fallback default demo data
            dashboardData = HostelDashboardDto()
        }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (refreshTimer > 1) {
                refreshTimer -= 1
            } else {
                refreshTimer = 30
            }
        }
    }

    val data = dashboardData ?: HostelDashboardDto()
    val qrContent = "${data.mealPassQr}:$refreshTimer"
    val qrBitmap = remember(qrContent) {
        QrGenerator.generateQrBitmap(qrContent, sizePx = 480)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hostel Mess Pass & Attendance",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Ink
                        )
                        Text(
                            text = data.hostelName,
                            fontSize = 11.sp,
                            color = InkSoft
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
                    }
                },
                actions = {
                    IconButton(onClick = { refreshTimer = 30 }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh Pass", tint = Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            StudentBottomBar(
                currentTab = StudentNavTab.HOME,
                onTabSelected = onTabSelected
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Ink)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Meal Window Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF2A1525), Color(0xFF4A1E35))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF06D6A0))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${data.currentMeal} SERVICE ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF06D6A0)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Timing: ${data.mealTiming}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Room: ${data.roomNumber} • ${data.studentName}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF06D6A0).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF06D6A0), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ENTRY PERMITTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF06D6A0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // High-Resolution QR Mess Pass Card (Point 9)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.5.dp, OutlineWarm, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OFFICIAL MESS ENTRY PASS",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Ink,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Presidency University Mess Management System",
                            fontSize = 11.sp,
                            color = InkSoft
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code Image
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, OutlineWarm.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Dynamic Mess Pass QR",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, tint = InkSoft, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dynamic QR refreshes in ${refreshTimer}s",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = InkSoft
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Blush)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Pass ID: PU-MESS-${data.rollNumber}-${data.currentMeal}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Attendance & Eco-Impact Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, OutlineWarm, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Monthly Attendance", fontSize = 11.sp, color = InkSoft)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${data.monthlyAttendancePercent}%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                            Text("Streak: ${data.attendanceStreakDays} days 🔥", fontSize = 10.sp, color = Color(0xFFB45309))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, OutlineWarm, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Green Credits", fontSize = 11.sp, color = InkSoft)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${data.greenCredits} pts", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B8754))
                            Text("Saved: ${data.foodSavedKg} kg 🍲", fontSize = 10.sp, color = InkSoft)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Today's Mess Hall Menu Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Today's ${data.currentMeal.lowercase().replaceFirstChar { it.uppercase() }} Menu",
                                    fontFamily = SoraFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Ink
                                )
                            }
                            Text(
                                text = "Unlimited Buffet",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B8754)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val sampleDishes = if (data.todayMenu.isNotEmpty()) {
                            data.todayMenu
                        } else {
                            listOf(
                                com.xsparks.tukra.core.network.MessMealItemDto("1", "Paneer Makhani & Jeera Rice", true, "Rich cottage cheese in creamy tomato gravy with aged basmati"),
                                com.xsparks.tukra.core.network.MessMealItemDto("2", "Dal Tadka & Phulkas", true, "Yellow lentils tempered with cumin, garlic and ghee"),
                                com.xsparks.tukra.core.network.MessMealItemDto("3", "Aloo Gobi Adraki", true, "Fresh cauliflower and potatoes tossed in ginger and spices"),
                                com.xsparks.tukra.core.network.MessMealItemDto("4", "Boondi Raita & Mixed Salad", true, "Chilled spiced yogurt with cucumber and carrot crudites"),
                                com.xsparks.tukra.core.network.MessMealItemDto("5", "Gulab Jamun (2 pcs)", true, "Warm milk dumplings soaked in cardamom rose syrup")
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            sampleDishes.forEach { dish ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Blush.copy(alpha = 0.4f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    VegNonVegMark(isVeg = dish.isVeg, size = 12)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(dish.dishName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                                        if (!dish.description.isNullOrBlank()) {
                                            Text(dish.description, fontSize = 10.sp, color = InkSoft)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
