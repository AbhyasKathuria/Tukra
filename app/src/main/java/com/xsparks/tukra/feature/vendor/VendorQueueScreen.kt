package com.xsparks.tukra.feature.vendor

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.OutdoorGrill
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.xsparks.tukra.core.designsystem.components.VendorBottomBar
import com.xsparks.tukra.core.designsystem.components.VendorNavTab
import com.xsparks.tukra.core.network.OrderDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.toRupees
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun VendorQueueScreen(
    tokenStore: TokenStore,
    viewModel: VendorQueueViewModel,
    onTabSelected: (VendorNavTab) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val user by tokenStore.userFlow.collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }

    var showVerifyQrDialog by remember { mutableStateOf(false) }
    var orderToReject by remember { mutableStateOf<OrderDto?>(null) }
    var rejectReasonText by remember { mutableStateOf("Item out of stock") }

    // Start 4-second polling on mount, cancel on unmount
    DisposableEffect(Unit) {
        viewModel.startPolling(context)
        onDispose {
            viewModel.stopPolling()
        }
    }

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
            VendorBottomBar(
                currentTab = VendorNavTab.QUEUE,
                onTabSelected = onTabSelected,
                pendingProposalsCount = uiState.pendingProposalsCount,
                activeOrdersCount = uiState.orders.size
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Bar
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
                            text = user?.name ?: "Vendor Stall",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Ink
                        )
                        Text(
                            text = "Live Order Queue (लाइव ऑर्डर्स)",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showVerifyQrDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TukraPink,
                                contentColor = Ink
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Verify QR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                scope.launch {
                                    tokenStore.clearSession()
                                    onLogout()
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Logout, contentDescription = "Log Out", tint = InkSoft)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Queue Filter Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(QueueFilterTab.values()) { tab ->
                        val isSelected = uiState.selectedTab == tab
                        val count = when (tab) {
                            QueueFilterTab.ALL -> uiState.orders.size
                            QueueFilterTab.PLACED -> uiState.orders.count { it.status == "PLACED" }
                            QueueFilterTab.PREPARING -> uiState.orders.count { it.status in listOf("ACCEPTED", "PREPARING") }
                            QueueFilterTab.READY -> uiState.orders.count { it.status == "READY" }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) TukraPink else Blush.copy(alpha = 0.35f))
                                .clickable { viewModel.selectTab(tab) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${tab.title} (${tab.hindiTitle})",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Ink
                                )
                                if (count > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Butter else Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Divider(color = OutlineWarm, thickness = 1.dp)

            if (uiState.isAuthError) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Danger.copy(alpha = 0.1f))
                        .border(1.dp, Danger.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Vendor Session Expired",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Danger
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please log in again as Vendor to manage orders.",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(TukraPink)
                                .clickable {
                                    scope.launch {
                                        tokenStore.clearSession()
                                        onLogout()
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Log In Again",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Orders list
            if (uiState.filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Butter),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Restaurant,
                                contentDescription = null,
                                tint = Ink,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No orders in this queue",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New campus orders will appear here automatically with a sound alert.",
                            fontSize = 13.sp,
                            color = InkSoft,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.filteredOrders, key = { it.id }) { order ->
                        VendorOrderCard(
                            order = order,
                            onAccept = { viewModel.acceptOrder(order.id, context) },
                            onStartPreparing = { viewModel.startPreparing(order.id, context) },
                            onMarkReady = { viewModel.markReady(order.id, context) },
                            onRejectClick = {
                                orderToReject = order
                                rejectReasonText = "Item out of stock"
                            },
                            onVerifyQr = { showVerifyQrDialog = true }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Verify QR Dialog
    if (showVerifyQrDialog) {
        VendorVerifyQrDialog(
            onDismiss = { showVerifyQrDialog = false },
            onVerifyToken = { token ->
                viewModel.verifyQrToken(token, context)
            }
        )
    }

    // Reject Reason Dialog
    if (orderToReject != null) {
        AlertDialog(
            onDismissRequest = { orderToReject = null },
            title = {
                Text(
                    text = "Reject Order ${orderToReject?.code}?",
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Danger
                )
            },
            text = {
                Column {
                    Text(
                        text = "Specify why this order cannot be prepared:",
                        fontSize = 13.sp,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(
                        "Item out of stock",
                        "Counter too busy",
                        "Kitchen closing soon",
                        "Ingredients not available"
                    ).forEach { preset ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (rejectReasonText == preset) Blush else Color(0xFFF9F9F9))
                                .clickable { rejectReasonText = preset }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 13.sp,
                                fontWeight = if (rejectReasonText == preset) FontWeight.Bold else FontWeight.Normal,
                                color = Ink
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReasonText,
                        onValueChange = { rejectReasonText = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Danger,
                            focusedLabelColor = Ink
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ord = orderToReject
                        if (ord != null) {
                            viewModel.rejectOrder(ord.id, rejectReasonText, context)
                        }
                        orderToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = Color.White)
                ) {
                    Text("Reject & Refund")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToReject = null }) {
                    Text("Cancel", color = Ink)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun VendorOrderCard(
    order: OrderDto,
    onAccept: () -> Unit,
    onStartPreparing: () -> Unit,
    onMarkReady: () -> Unit,
    onRejectClick: () -> Unit,
    onVerifyQr: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Code + Student Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.code,
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Butter)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.studentFirstName ?: "Student",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Ink
                        )
                    }
                }

                // Status Tag
                val (statusBg, statusFg, statusLabel) = when (order.status) {
                    "PLACED" -> Triple(Blush, Ink, "NEW (नया)")
                    "ACCEPTED" -> Triple(Butter, Ink, "ACCEPTED (स्वीकृत)")
                    "PREPARING" -> Triple(Butter, Ink, "COOKING (तैयार हो रहा है)")
                    "READY" -> Triple(Success.copy(alpha = 0.2f), Success, "READY (तैयार)")
                    "COLLECTED" -> Triple(Color(0xFFEEEEEE), InkSoft, "COLLECTED")
                    else -> Triple(Color(0xFFEEEEEE), InkSoft, order.status)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = statusFg
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Slot time countdown
            if (!order.slotStartsAt.isNullOrBlank()) {
                val formattedSlot = try {
                    val inst = Instant.parse(order.slotStartsAt)
                    val minutesUntil = ChronoUnit.MINUTES.between(Instant.now(), inst)
                    if (minutesUntil > 0) "Pickup in ~$minutesUntil mins" else "Pickup due now"
                } catch (_: Exception) {
                    "Slot: ${order.slotStartsAt}"
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = PinkDeep,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedSlot,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PinkDeep
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Total: ${order.totalPaise.toRupees()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = OutlineWarm, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Items List (Large readable font for kitchen)
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Butter),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${item.qty}x",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Ink
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Ink
                        )
                    }
                    Text(
                        text = (item.unitPricePaise * item.qty).toRupees(),
                        fontSize = 13.sp,
                        color = InkSoft
                    )
                }
            }

            // Customer notes (if any)
            if (!order.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Butter.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Note: ${order.note}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons based on status
            when (order.status) {
                "PLACED" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRejectClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Danger)
                        ) {
                            Text("Reject", color = Danger, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TukraPink,
                                contentColor = Ink
                            )
                        ) {
                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Accept (स्वीकार करें)", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "ACCEPTED" -> {
                    Button(
                        onClick = onStartPreparing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Butter,
                            contentColor = Ink
                        )
                    ) {
                        Icon(Icons.Outlined.OutdoorGrill, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Preparing (बनाना शुरू करें)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                "PREPARING" -> {
                    Button(
                        onClick = onMarkReady,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Success,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Outlined.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Ready (तैयार है)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                "READY" -> {
                    Button(
                        onClick = onVerifyQr,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TukraPink,
                            contentColor = Ink
                        )
                    ) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify Pickup QR (क्यूआर चेक करें)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
