package com.xsparks.tukra.feature.checkout

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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.BuildConfig
import com.xsparks.tukra.core.cart.CartManager
import com.xsparks.tukra.core.designsystem.Blush
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.Success
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.PaymentGatewayDialog
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.SecondaryButton
import com.xsparks.tukra.core.designsystem.components.VegNonVegMark
import com.xsparks.tukra.core.util.TimeFormat
import com.xsparks.tukra.core.util.toRupees

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    outletId: String,
    outletName: String,
    slotId: String,
    slotStartsAt: String,
    viewModel: CheckoutViewModel,
    onBackClick: () -> Unit,
    onOrderPlaced: (orderId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cartManager = remember { CartManager.getInstance(context) }
    val cartState by cartManager.cartState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPaymentGatewayDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.createdOrderId) {
        val orderId = uiState.createdOrderId
        if (orderId != null) {
            onOrderPlaced(orderId)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val err = uiState.errorMessage
        if (err != null) {
            snackbarHostState.showSnackbar(err)
            viewModel.clearError()
        }
    }

    val totalPaise = cartState.totalPaise
    val walletPaise = uiState.walletBalancePaise
    val isShort = walletPaise < totalPaise
    val shortfallPaise = if (isShort) totalPaise - walletPaise else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Checkout",
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                // Point 13: Dedicated Payment Gateway Dashboard Trigger
                PrimaryButton(
                    text = "Pay ${totalPaise.toRupees()} (UPI / Card / NetBanking)",
                    onClick = { showPaymentGatewayDialog = true },
                    isLoading = uiState.isPlacingOrder || uiState.isDemoTopupLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isShort) {
                    SecondaryButton(
                        text = "Top-up & Pay from Wallet (${shortfallPaise.toRupees()} short)",
                        onClick = {
                            viewModel.addDemoMoneyAndPay(outletId, slotId, shortfallPaise)
                        },
                        isOutlined = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    SecondaryButton(
                        text = "Instant 1-Tap Wallet Pay (${walletPaise.toRupees()} available)",
                        onClick = {
                            viewModel.placeOrder(outletId, slotId, paymentMethod = "WALLET")
                        },
                        isOutlined = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Pickup details card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Butter),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pickup Slot: ${TimeFormat.formatSlotTime(slotStartsAt)}",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Ink
                            )
                            Text(
                                text = outletName,
                                fontSize = 13.sp,
                                color = InkSoft
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = OutlineWarm)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = InkSoft, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Free cancellation until 10 minutes before pickup",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Blush.copy(alpha = 0.35f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Ink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tukra Wallet",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Ink
                            )
                            Text(
                                text = "Available balance: ${walletPaise.toRupees()}",
                                fontSize = 12.sp,
                                color = InkSoft
                            )
                        }
                    }

                    if (isShort) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Butter)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Short by ${shortfallPaise.toRupees()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }
                    } else {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = "Sufficient", tint = Success, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order items review
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
                        text = "Order Summary (${cartState.totalItemsCount} items)",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    cartState.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VegNonVegMark(isVeg = item.menuItem.isVeg, size = 12)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${item.quantity} × ${item.menuItem.name}",
                                fontSize = 13.sp,
                                color = Ink,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = (item.menuItem.pricePaise * item.quantity).toRupees(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = OutlineWarm)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(totalPaise.toRupees(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showPaymentGatewayDialog) {
            PaymentGatewayDialog(
                amountRupees = totalPaise.toRupees(),
                onDismiss = { showPaymentGatewayDialog = false },
                onPaymentSuccess = { method, detail ->
                    showPaymentGatewayDialog = false
                    if (isShort) {
                        viewModel.addDemoMoneyAndPay(outletId, slotId, shortfallPaise)
                    } else {
                        viewModel.placeOrder(outletId, slotId, paymentMethod = "WALLET")
                    }
                }
            )
        }
    }
}
