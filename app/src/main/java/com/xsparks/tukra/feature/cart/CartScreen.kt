package com.xsparks.tukra.feature.cart

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.xsparks.tukra.core.cart.CartManager
import com.xsparks.tukra.core.designsystem.Blush
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.EmptyState
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.VegNonVegMark
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.toRupees
import com.xsparks.tukra.feature.slot.SlotPickerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    tokenStore: TokenStore,
    onBackClick: () -> Unit,
    onProceedToCheckout: (outletId: String, outletName: String, slotId: String, slotStartsAt: String) -> Unit
) {
    val context = LocalContext.current
    val cartManager = remember { CartManager.getInstance(context) }
    val cartState by cartManager.cartState.collectAsState()

    var showSlotPicker by remember { mutableStateOf(false) }
    val slotSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Your Plate",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Ink
                        )
                        if (!cartState.isEmpty) {
                            Text(
                                text = cartState.outletName ?: "",
                                fontSize = 12.sp,
                                color = InkSoft
                            )
                        }
                    }
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
                actions = {
                    if (!cartState.isEmpty) {
                        TextButton(onClick = { cartManager.clearCart() }) {
                            Text(
                                text = "Clear",
                                color = Danger,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (!cartState.isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(20.dp)
                ) {
                    PrimaryButton(
                        text = "Choose pickup time • ${cartState.totalPaise.toRupees()}",
                        onClick = { showSlotPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (cartState.isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "Your plate is empty",
                    message = "Browse campus canteens and pick your favourite dishes to pay only for what you eat.",
                    icon = Icons.Outlined.ShoppingBag,
                    actionButtonText = "Browse canteens",
                    onActionClick = onBackClick
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Prep time banner
                item {
                    val prepMinutes = (cartState.longestPrepSeconds / 60).coerceAtLeast(1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Butter)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Timer, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Estimated prep time ~$prepMinutes min (${cartState.totalPrepUnits} prep units)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Item Rows
                items(cartState.items, key = { it.menuItem.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VegNonVegMark(isVeg = item.menuItem.isVeg, size = 14)
                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.menuItem.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )
                            if (!item.notes.isNullOrBlank()) {
                                Text(
                                    text = "Note: ${item.notes}",
                                    fontSize = 12.sp,
                                    color = InkSoft
                                )
                            }
                            Text(
                                text = (item.menuItem.pricePaise * item.quantity).toRupees(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }

                        // Stepper
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Blush.copy(alpha = 0.35f))
                                .border(1.dp, TukraPink, RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        cartManager.updateQuantity(item.menuItem.id, item.quantity - 1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.quantity == 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = if (item.quantity == 1) Danger else Ink,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "${item.quantity}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Ink
                            )

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        cartManager.updateQuantity(item.menuItem.id, item.quantity + 1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = Ink,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f), thickness = 0.5.dp)
                }

                // Bill Summary Card
                item {
                    Spacer(modifier = Modifier.height(24.dp))
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
                                text = "Bill Details",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Item Total", fontSize = 14.sp, color = InkSoft)
                                Text(cartState.totalPaise.toRupees(), fontSize = 14.sp, color = Ink)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Taxes & Charges", fontSize = 14.sp, color = InkSoft)
                                Text("Inclusive of taxes", fontSize = 12.sp, color = InkSoft)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = OutlineWarm)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("To Pay", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
                                Text(cartState.totalPaise.toRupees(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                        }
                    }
                }
            }
        }
    }

    // Slot Picker Sheet
    if (showSlotPicker && cartState.outletId != null) {
        SlotPickerSheet(
            outletId = cartState.outletId!!,
            totalUnits = cartState.totalPrepUnits,
            longestPrepSeconds = cartState.longestPrepSeconds,
            tokenStore = tokenStore,
            sheetState = slotSheetState,
            onDismiss = { showSlotPicker = false },
            onSlotConfirmed = { slotId, slotStartsAt ->
                showSlotPicker = false
                onProceedToCheckout(
                    cartState.outletId!!,
                    cartState.outletName ?: "Campus Outlet",
                    slotId,
                    slotStartsAt
                )
            }
        )
    }
}
