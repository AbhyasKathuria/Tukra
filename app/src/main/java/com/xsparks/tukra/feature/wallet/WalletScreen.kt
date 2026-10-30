package com.xsparks.tukra.feature.wallet

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.xsparks.tukra.core.designsystem.components.PaymentGatewayDialog
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.StudentBottomBar
import com.xsparks.tukra.core.designsystem.components.StudentNavTab
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.network.WalletTxnDto
import com.xsparks.tukra.core.util.TimeFormat
import com.xsparks.tukra.core.util.toRupees

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    tokenStore: TokenStore,
    viewModel: WalletViewModel,
    onTabSelected: (StudentNavTab) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedAmount by remember { mutableIntStateOf(200) }
    var customAmountText by remember { mutableStateOf("") }
    var showPaymentGatewayDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        val err = uiState.errorMessage
        if (err != null) {
            snackbarHostState.showSnackbar(err)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        val msg = uiState.successMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tukra Wallet",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Ink
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadWallet() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            StudentBottomBar(
                currentTab = StudentNavTab.WALLET,
                onTabSelected = onTabSelected
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Balance Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Butter)
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Available Balance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkSoft
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Instant Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = uiState.balancePaise.toRupees(),
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = Ink
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Used for 1-tap checkout and counter pickup verification",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }
                }
            }

            // Top-up Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "Add Money to Wallet",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Ink
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(100, 200, 500).forEach { amount ->
                                val isSelected = selectedAmount == amount && customAmountText.isBlank()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) TukraPink else Blush.copy(alpha = 0.35f))
                                        .border(1.dp, if (isSelected) TukraPink else OutlineWarm, RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedAmount = amount
                                            customAmountText = ""
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+ ₹$amount",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Ink
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom input
                        OutlinedTextField(
                            value = customAmountText,
                            onValueChange = {
                                customAmountText = it.filter { char -> char.isDigit() }
                            },
                            placeholder = { Text("Or enter custom amount (₹10 – ₹5,000)", color = InkSoft.copy(alpha = 0.5f), fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TukraPink,
                                unfocusedBorderColor = OutlineWarm,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val finalAmount = customAmountText.toIntOrNull() ?: selectedAmount
                        PrimaryButton(
                            text = "Add ₹$finalAmount via Payment Gateway (UPI / Card / Bank)",
                            onClick = {
                                showPaymentGatewayDialog = true
                            },
                            isLoading = uiState.isTopupLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Transaction History Header
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Ink
                    )
                    Text(
                        text = "${uiState.transactions.size} records",
                        fontSize = 12.sp,
                        color = InkSoft
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Transactions List
            if (uiState.transactions.isEmpty()) {
                item {
                    EmptyState(
                        title = "No transactions yet",
                        message = "Your wallet top-ups, order debits, and refunds will appear here."
                    )
                }
            } else {
                items(uiState.transactions, key = { it.id }) { txn ->
                    TransactionRow(txn = txn)
                    HorizontalDivider(
                        color = OutlineWarm.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }

        if (showPaymentGatewayDialog) {
            val finalAmount = customAmountText.toIntOrNull() ?: selectedAmount
            PaymentGatewayDialog(
                amountRupees = "₹$finalAmount",
                onDismiss = { showPaymentGatewayDialog = false },
                onPaymentSuccess = { method, detail ->
                    showPaymentGatewayDialog = false
                    viewModel.topupDemo(finalAmount)
                }
            )
        }
    }
}

@Composable
fun TransactionRow(txn: WalletTxnDto) {
    val isCredit = txn.type in listOf("TOPUP", "REFUND")
    val title = when (txn.type) {
        "TOPUP" -> "Wallet Top-up"
        "ORDER" -> "Canteen Order"
        "REFUND" -> "Order Refund"
        "PLATE" -> "Mess Plate Session"
        else -> txn.type
    }

    val icon = if (isCredit) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward
    val iconBg = if (isCredit) Success.copy(alpha = 0.12f) else Danger.copy(alpha = 0.1f)
    val iconColor = if (isCredit) Success else Danger

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink
            )
            Text(
                text = TimeFormat.formatDateTime(txn.createdAt),
                fontSize = 12.sp,
                color = InkSoft
            )
        }

        val amountPrefix = if (isCredit) "+" else "-"
        val amountColor = if (isCredit) Success else Ink
        Text(
            text = "$amountPrefix${txn.amountPaise.toRupees()}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}
