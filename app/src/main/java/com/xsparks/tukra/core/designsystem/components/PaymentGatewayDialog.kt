package com.xsparks.tukra.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.delay

enum class PaymentTab(val label: String) {
    UPI("UPI"),
    CARD("Card"),
    NETBANKING("NetBanking"),
    WALLET("Campus Wallet")
}

@Composable
fun PaymentGatewayDialog(
    amountRupees: String,
    onDismiss: () -> Unit,
    onPaymentSuccess: (method: String, detail: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(PaymentTab.UPI) }

    // UPI state
    var selectedUpiApp by remember { mutableStateOf("Google Pay") }
    var customUpiId by remember { mutableStateOf("aarav@okhdfcbank") }

    // Card state
    var cardNumber by remember { mutableStateOf("4532 8921 7734 1092") }
    var cardExpiry by remember { mutableStateOf("12/28") }
    var cardCvv by remember { mutableStateOf("382") }
    var cardHolderName by remember { mutableStateOf("AARAV SHARMA") }

    // Bank state
    var selectedBank by remember { mutableStateOf("HDFC Bank") }

    // Processing animation state
    var isProcessing by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    fun triggerPayment(method: String, detail: String) {
        isProcessing = true
        processingStep = "Connecting to Secure Payment Switch..."
    }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(800)
            processingStep = "Authorizing transaction for $amountRupees..."
            delay(1000)
            processingStep = "Verifying 256-bit Token..."
            delay(700)
            isSuccess = true
            delay(600)
            isProcessing = false
            val detail = when (selectedTab) {
                PaymentTab.UPI -> if (customUpiId.isNotBlank()) customUpiId else selectedUpiApp
                PaymentTab.CARD -> "Card ending in ${cardNumber.takeLast(4)}"
                PaymentTab.NETBANKING -> selectedBank
                PaymentTab.WALLET -> "Tukra Student Wallet"
            }
            onPaymentSuccess(selectedTab.name, detail)
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Payment Gateway",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Ink
                    )
                    Text(
                        text = "Total Payable: $amountRupees",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B8754)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Shield, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("256-bit SSL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }
        },
        text = {
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isSuccess) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Payment Approved!",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Ink
                            )
                            Text(
                                text = "Transaction verified successfully",
                                fontSize = 12.sp,
                                color = InkSoft
                            )
                        } else {
                            CircularProgressIndicator(color = Ink, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = processingStep,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )
                            Text(
                                text = "Please do not press back or close the app",
                                fontSize = 11.sp,
                                color = InkSoft
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Navigation Tabs Row
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = Color.White,
                        contentColor = Ink,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                color = TukraPink
                            )
                        }
                    ) {
                        PaymentTab.values().forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = {
                                    Text(
                                        text = tab.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == tab) Ink else InkSoft
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedTab) {
                        PaymentTab.UPI -> {
                            Text("Fast Pay with UPI App", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Google Pay", "PhonePe", "Paytm", "BHIM").forEach { app ->
                                    val isSel = selectedUpiApp == app
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) TukraPink else Blush.copy(alpha = 0.4f))
                                            .border(1.dp, if (isSel) TukraPink else OutlineWarm, RoundedCornerShape(8.dp))
                                            .clickable { selectedUpiApp = app }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(app, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Or Enter UPI ID / VPA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customUpiId,
                                onValueChange = { customUpiId = it },
                                placeholder = { Text("username@bank", color = InkSoft.copy(alpha = 0.5f), fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(
                                text = "Pay $amountRupees via UPI",
                                onClick = { triggerPayment("UPI", customUpiId) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        PaymentTab.CARD -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Card Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Butter)
                                        .clickable {
                                            cardNumber = "4532 8921 7734 1092"
                                            cardExpiry = "12/28"
                                            cardCvv = "382"
                                            cardHolderName = "AARAV SHARMA"
                                        }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("⚡ Use Demo Card", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { cardNumber = it },
                                label = { Text("Card Number") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { cardExpiry = it },
                                    label = { Text("Expiry (MM/YY)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { cardCvv = it },
                                    label = { Text("CVV") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cardHolderName,
                                onValueChange = { cardHolderName = it },
                                label = { Text("Cardholder Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(
                                text = "Pay $amountRupees with Card",
                                onClick = { triggerPayment("CARD", cardNumber) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        PaymentTab.NETBANKING -> {
                            Text("Select Your Bank", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Spacer(modifier = Modifier.height(8.dp))

                            val banks = listOf("HDFC Bank", "State Bank of India", "ICICI Bank", "Axis Bank", "Punjab National Bank", "Kotak Mahindra")
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                banks.forEach { bank ->
                                    val isSel = selectedBank == bank
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) Blush else Color.White)
                                            .border(1.dp, if (isSel) TukraPink else OutlineWarm, RoundedCornerShape(8.dp))
                                            .clickable { selectedBank = bank }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(bank, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = Ink)
                                        if (isSel) {
                                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(
                                text = "Pay $amountRupees via $selectedBank",
                                onClick = { triggerPayment("NETBANKING", selectedBank) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        PaymentTab.WALLET -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Butter)
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text("Tukra Student Campus Wallet", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Deduct directly from your prepaid dining wallet.", fontSize = 11.sp, color = InkSoft)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(
                                text = "Instant 1-Tap Pay $amountRupees",
                                onClick = { triggerPayment("WALLET", "Student Wallet") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (!isProcessing) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = InkSoft)
                }
            }
        },
        containerColor = Color.White
    )
}
