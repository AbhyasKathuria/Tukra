package com.xsparks.tukra.feature.tracking

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.xsparks.tukra.core.designsystem.PinkDeep
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.Success
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.SecondaryButton
import com.xsparks.tukra.core.designsystem.components.StatusStepper
import com.xsparks.tukra.core.util.QrGenerator
import com.xsparks.tukra.core.util.TimeFormat
import com.xsparks.tukra.core.util.toRupees

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    orderId: String,
    viewModel: TrackingViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(orderId) {
        viewModel.startTracking(orderId)
    }

    LaunchedEffect(uiState.order?.qrToken) {
        val token = uiState.order?.qrToken
        if (!token.isNullOrBlank()) {
            qrBitmap = QrGenerator.generateQrBitmap(token, sizePx = 400)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Live Order Tracking",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (uiState.isLoading && uiState.order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Ink)
            }
        } else {
            val order = uiState.order
            if (order == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Order not found",
                        color = InkSoft,
                        fontSize = 15.sp
                    )
                }
            } else {
                val isReady = order.status == "READY"
                val isCollected = order.status == "COLLECTED"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Order Code
                    Text(
                        text = order.code,
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        color = Ink
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = order.outletName ?: "Campus Canteen",
                        fontSize = 14.sp,
                        color = InkSoft
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Status Stepper
                    StatusStepper(
                        currentStatus = order.status,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Slot timing / countdown banner
                    order.slotStartsAt?.let { slotStartsAt ->
                        val minutes = TimeFormat.minutesUntil(slotStartsAt)
                        val timingText = if (minutes > 0) {
                            "Pickup in $minutes min (${TimeFormat.formatSlotTime(slotStartsAt)})"
                        } else {
                            "Pickup time: ${TimeFormat.formatSlotTime(slotStartsAt)}"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isReady) Success.copy(alpha = 0.15f) else Butter)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isReady) Icons.Outlined.Check else Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = if (isReady) Success else Ink,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isReady) "Hot & Ready for Pickup at the counter!" else timingText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isReady) Success else Ink
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // QR Code Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(
                                width = if (isReady) 2.5.dp else 1.dp,
                                color = if (isReady) TukraPink else OutlineWarm,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap!!.asImageBitmap(),
                                    contentDescription = "Pickup QR Code",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Blush.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.QrCode,
                                        contentDescription = null,
                                        tint = Ink,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Show this QR at the counter to collect",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Single-use digital verification",
                                fontSize = 11.sp,
                                color = InkSoft,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Order Items Details
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
                                text = "Items (${order.items.sumOf { it.qty }})",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            order.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.qty} × ${item.name}",
                                        fontSize = 13.sp,
                                        color = Ink
                                    )
                                    Text(
                                        text = (item.unitPricePaise * item.qty).toRupees(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = OutlineWarm)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Paid", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                                Text(order.totalPaise.toRupees(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Cancellation button
                    if (uiState.canCancel) {
                        SecondaryButton(
                            text = "Cancel Order & Refund",
                            onClick = { viewModel.cancelOrder(order.id) },
                            isOutlined = true,
                            enabled = !uiState.isCancelling,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Return Home
                    PrimaryButton(
                        text = if (isCollected) "Order Again" else "Back to Canteens",
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
