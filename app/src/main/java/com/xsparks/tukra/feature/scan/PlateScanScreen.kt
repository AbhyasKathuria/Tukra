package com.xsparks.tukra.feature.scan

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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.SecondaryButton
import com.xsparks.tukra.core.designsystem.components.StudentBottomBar
import com.xsparks.tukra.core.designsystem.components.StudentNavTab
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.toRupees

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateScanScreen(
    tokenStore: TokenStore,
    viewModel: PlateScanViewModel,
    onTabSelected: (StudentNavTab) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var inputCode by remember { mutableStateOf("") }
    var collegeEmail by remember { mutableStateOf("") }

    LaunchedEffect(uiState.studentEmail) {
        if (collegeEmail.isBlank() && uiState.studentEmail.isNotBlank()) {
            collegeEmail = uiState.studentEmail
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val err = uiState.errorMessage
        if (err != null) {
            snackbarHostState.showSnackbar(err)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        val msg = uiState.successMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Plate & Vision Scan",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Ink
                        )
                        Text(
                            text = "Presidency University Smart Dining",
                            fontSize = 11.sp,
                            color = InkSoft
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            StudentBottomBar(
                currentTab = StudentNavTab.SCAN,
                onTabSelected = onTabSelected
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Plate Count using TinyFish (Point 8)
            val stats = uiState.liveStats
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF2A1525), Color(0xFF1B0C17))
                        )
                    )
                    .border(1.dp, Color(0xFFD4A373).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF06D6A0))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TINYFISH LIVE PLATE MONITOR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF06D6A0),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Hall Sensor: Active",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD166)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Active Plates", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("${stats?.activePlatesInHall ?: 42}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Clean Plates", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("${stats?.clearedPlatesToday ?: 318}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06D6A0))
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Zero Waste", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("${stats?.zeroWasteRatePercent ?: 94}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD166))
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Food Saved", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("${stats?.foodWasteSavedKg ?: 18.5}kg", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4EA8DE))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val session = uiState.session

            if (session == null) {
                // Viewfinder placeholder
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(2.dp, TukraPink, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                            tint = Ink,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Scan Counter Plate QR",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scan code on plate rim or tray edge",
                            fontSize = 11.sp,
                            color = InkSoft,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Point 12: Student College Email Input
                Text(
                    text = "Link to Student College Email (Auto-Calculate)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = collegeEmail,
                    onValueChange = { collegeEmail = it },
                    placeholder = { Text("e.g. aarav@presidency.ac.in", color = InkSoft.copy(alpha = 0.5f), fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = InkSoft, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TukraPink,
                        unfocusedBorderColor = OutlineWarm
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Manual plate code input
                Text(
                    text = "Plate Code",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it.uppercase() },
                    placeholder = { Text("e.g. PLT-0001", color = InkSoft.copy(alpha = 0.5f), fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TukraPink,
                        unfocusedBorderColor = OutlineWarm
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Demo chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PLT-0001", "PLT-0002", "PLT-0003").forEach { code ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Butter)
                                .clickable {
                                    inputCode = code
                                    viewModel.linkPlate(code, collegeEmail)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(code, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    text = "Link My Plate & Calculate In My Name",
                    onClick = { viewModel.linkPlate(inputCode, collegeEmail) },
                    isLoading = uiState.isLinking,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Live Growing Bill view with Student Linked Info (Point 12)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (uiState.isPaid) Success.copy(alpha = 0.12f) else Butter)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Plate Session: ${session.plateCode}",
                                    fontFamily = SoraFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Ink
                                )
                                Text(
                                    text = "👤 Linked: ${session.studentName ?: "Aarav Sharma"} (${session.studentEmail ?: collegeEmail.ifBlank { "aarav@presidency.ac.in" }})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink
                                )
                                Text(
                                    text = "Roll: ${session.rollNumber ?: "20231CSE0491"} • ${session.hostel ?: "Cauvery Boys Hostel"}",
                                    fontSize = 10.sp,
                                    color = InkSoft
                                )
                            }

                            if (uiState.isPaid) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(24.dp))
                            } else if (session.status == "OPEN") {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Ink, strokeWidth = 2.dp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Point 10: Live Dishes Scanning using TinyFish Vision AI Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFF06D6A0).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFF1B8754),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "TinyFish Live Dish Scanner",
                                        fontFamily = SoraFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Ink
                                    )
                                    Text(
                                        text = "Autonomous multi-modal vision dish recognition",
                                        fontSize = 10.sp,
                                        color = InkSoft
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF06D6A0).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("v2.1 AI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B8754))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val lastScanned = uiState.lastScannedDish
                        if (lastScanned != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Blush.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "✅ Detected: ${lastScanned.dishName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Ink
                                        )
                                        Text(
                                            text = "₹${lastScanned.priceRupees}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = Ink
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Confidence: ${Math.round(lastScanned.confidenceScore * 100)}% • ${lastScanned.estimatedCalories} kcal • Added to Tray",
                                        fontSize = 10.sp,
                                        color = Color(0xFF1B8754),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Trigger dish scan button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2A1525))
                                    .clickable(enabled = !uiState.isScanningDish) {
                                        viewModel.scanDishAi(null)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isScanningDish) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("📸 Scan Tray Dish", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Butter)
                                    .clickable { viewModel.scanDishAi("Paneer") }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Scan Paneer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Itemized Dishes Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Counter Dishes (${session.items.sumOf { it.qty }})",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (session.items.isEmpty()) {
                            Text(
                                text = "No dishes recorded yet. As the operator taps items or TinyFish Vision detects dishes on the tray, they appear here live.",
                                fontSize = 12.sp,
                                color = InkSoft
                            )
                        } else {
                            session.items.forEach { dish ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${dish.qty} × ${dish.name}",
                                        fontSize = 13.sp,
                                        color = Ink
                                    )
                                    Text(
                                        text = (dish.unitPricePaise * dish.qty).toRupees(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = OutlineWarm)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text(session.totalPaise.toRupees(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (session.status == "BILLED" || (session.status == "OPEN" && session.totalPaise > 0)) {
                    PrimaryButton(
                        text = "Pay ${session.totalPaise.toRupees()} & Award Eco-Credits",
                        onClick = { viewModel.payPlateBill() },
                        isLoading = uiState.isPaying,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.isPaid) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFD4EDDA))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌱 +15 Green Credits awarded to ${session.studentName ?: "student account"}! Zero-waste streak extended.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF155724),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    SecondaryButton(
                        text = "Scan Another Plate",
                        onClick = { viewModel.resetScanner() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Honesty rule footer note
            Text(
                text = "Presidency University Smart Plate System • TinyFish Multi-Modal Vision & Auto-Calculation",
                fontSize = 10.sp,
                color = InkSoft,
                textAlign = TextAlign.Center
            )
        }
    }
}
