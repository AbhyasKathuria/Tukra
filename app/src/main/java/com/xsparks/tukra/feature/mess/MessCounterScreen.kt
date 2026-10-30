package com.xsparks.tukra.feature.mess

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
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
import com.xsparks.tukra.core.designsystem.components.MessBottomBar
import com.xsparks.tukra.core.designsystem.components.MessNavTab
import com.xsparks.tukra.core.designsystem.components.VegNonVegMark
import com.xsparks.tukra.core.network.MenuItemDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.toRupees

@Composable
fun MessCounterScreen(
    tokenStore: TokenStore,
    viewModel: MessCounterViewModel,
    onTabSelected: (MessNavTab) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var inputCode by remember { mutableStateOf("PLT-0001") }
    var showScannerDialog by remember { mutableStateOf(false) }

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
            MessBottomBar(
                currentTab = MessNavTab.COUNTER,
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
                                text = "Serving Counter",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Outlined.TouchApp, contentDescription = null, tint = PinkDeep, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "Tap dishes to simulate smart plate auto-billing",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showScannerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TukraPink, contentColor = Ink),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (uiState.activePlateSession != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.nextStudent() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Next Student", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = OutlineWarm, thickness = 1.dp)

            // Active Session or Open Plate Card
            val session = uiState.activePlateSession
            if (session == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Activate Smart Plate Session",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Point camera at student entry pass or plate barcode:",
                            fontSize = 12.sp,
                            color = InkSoft
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showScannerDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blush, contentColor = Ink),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, TukraPink)
                        ) {
                            Icon(
                                Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                tint = PinkDeep,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "📷 Open Camera QR Scanner",
                                    fontFamily = SoraFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Ink
                                )
                                Text(
                                    "Auto-verifies student pass or opens smart plate",
                                    fontSize = 11.sp,
                                    color = InkSoft
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Or tap demo plate number:",
                            fontSize = 11.sp,
                            color = InkSoft,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("PLT-0001", "PLT-0002", "PLT-0003").forEach { demoCode ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (inputCode == demoCode) Butter else Blush.copy(alpha = 0.3f))
                                        .clickable {
                                            inputCode = demoCode
                                            viewModel.openSession(demoCode)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = demoCode,
                                        fontWeight = if (inputCode == demoCode) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = Ink
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = inputCode,
                                onValueChange = { inputCode = it.uppercase() },
                                label = { Text("Plate Code") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = { viewModel.openSession(inputCode) },
                                colors = ButtonDefaults.buttonColors(containerColor = TukraPink, contentColor = Ink),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("Open Plate", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Active Plate Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .border(1.5.dp, if (session.status == "BILLED") Success else Butter, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (session.status == "BILLED") Success.copy(alpha = 0.08f) else Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                    Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Plate: ${session.plateCode}",
                                        fontFamily = SoraFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Ink
                                    )
                                    Text(
                                        text = session.studentName?.let { "Student: $it" } ?: "Anonymous / Not linked yet",
                                        fontSize = 12.sp,
                                        color = InkSoft
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = session.totalPaise.toRupees(),
                                    fontFamily = SoraFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Ink
                                )
                                Text(
                                    text = "${session.items.sumOf { it.qty }} items served",
                                    fontSize = 11.sp,
                                    color = InkSoft
                                )
                            }
                        }

                        if (session.status == "OPEN") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.finishAndBill() },
                                colors = ButtonDefaults.buttonColors(containerColor = TukraPink, contentColor = Ink),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("Finish & Bill Plate (बिल बनाएं)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { showScannerDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(38.dp)
                            ) {
                                Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = PinkDeep, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Student Pass or Another Plate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Success.copy(alpha = 0.2f))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "BILLED — Ready for student wallet debit",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Success
                                )
                            }
                        }
                    }
                }
            }

            // Big Touch Tiles Section
            Text(
                text = "Tap Dish to Add to Plate (टैप करें):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = InkSoft,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.messDishes, key = { it.id }) { dish ->
                    val sessionItems = session?.items ?: emptyList()
                    val countOnPlate = sessionItems.firstOrNull { it.name == dish.name }?.qty ?: 0

                    CounterDishTile(
                        dish = dish,
                        countOnPlate = countOnPlate,
                        enabled = session?.status == "OPEN",
                        onClick = { viewModel.addDishToPlate(dish) }
                    )
                }
            }

            // Honesty Disclaimer Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFBFBFB))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Simulated smart plate session — Counter taps simulate RFID tray weighing & cameras.",
                    fontSize = 11.sp,
                    color = InkSoft,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        if (showScannerDialog) {
            MessVerifyQrDialog(
                onDismiss = { showScannerDialog = false },
                onVerifyToken = { token ->
                    viewModel.verifyMessPass(token)
                },
                onPlateCodeDetected = { plateCode ->
                    inputCode = plateCode
                    viewModel.openSession(plateCode)
                    showScannerDialog = false
                }
            )
        }
    }
}

@Composable
fun CounterDishTile(
    dish: MenuItemDto,
    countOnPlate: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() }
            .border(
                width = if (countOnPlate > 0) 2.dp else 1.dp,
                color = if (countOnPlate > 0) TukraPink else OutlineWarm,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (countOnPlate > 0) Blush.copy(alpha = 0.25f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                VegNonVegMark(isVeg = dish.isVeg)
                if (countOnPlate > 0) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(TukraPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = countOnPlate.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Ink
                        )
                    }
                }
            }

            Column {
                Text(
                    text = dish.name,
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    color = Ink
                )
                Text(
                    text = dish.pricePaise.toRupees(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = PinkDeep
                )
            }
        }
    }
}
