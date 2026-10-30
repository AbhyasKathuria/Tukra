package com.xsparks.tukra.feature.menu

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.xsparks.tukra.core.cart.AddToCartResult
import com.xsparks.tukra.core.cart.CartManager
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.CartBar
import com.xsparks.tukra.core.designsystem.components.DishRow
import com.xsparks.tukra.core.designsystem.components.EmptyState
import com.xsparks.tukra.core.designsystem.components.SkeletonShimmer
import com.xsparks.tukra.core.network.MenuItemDto
import com.xsparks.tukra.core.util.toRupees

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletMenuScreen(
    outletId: String,
    outletName: String,
    viewModel: MenuViewModel,
    onBackClick: () -> Unit,
    onViewCartClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cartManager = remember { CartManager.getInstance(context) }
    val cartState by cartManager.cartState.collectAsState()

    var selectedItemForSheet by remember { mutableStateOf<MenuItemDto?>(null) }
    var showClearDialog by remember { mutableStateOf<AddToCartResult.DifferentOutlet?>(null) }
    var pendingItemToAdd by remember { mutableStateOf<Pair<MenuItemDto, Int>?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(outletId) {
        viewModel.loadMenu(outletId, outletName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = outletName,
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Ink
                        )
                        Text(
                            text = "Campus Canteen Menu",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (cartState.totalItemsCount > 0) {
                CartBar(
                    itemCount = cartState.totalItemsCount,
                    totalRupees = cartState.totalPaise.toRupees(),
                    onViewCartClick = onViewCartClick
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Live Options Count Badge powered by TinyFish (Point 7)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF06D6A0))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.liveOptionsCount} Live Options Synced with TinyFish",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink
                        )
                    }
                    Text(
                        text = "Real-time kitchen stock",
                        fontSize = 11.sp,
                        color = InkSoft
                    )
                }
            }

            // Dietary & Quick Bites Filter Chips (Point 5 & 6)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.dietFilter == "ALL",
                        onClick = { viewModel.setDietFilter("ALL") },
                        label = { Text("All", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TukraPink,
                            selectedLabelColor = Ink
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.dietFilter == "VEG",
                        onClick = { viewModel.setDietFilter(if (uiState.dietFilter == "VEG") "ALL" else "VEG") },
                        label = { Text("🟢 Pure Veg", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD4EDDA),
                            selectedLabelColor = Color(0xFF155724)
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.dietFilter == "NON_VEG",
                        onClick = { viewModel.setDietFilter(if (uiState.dietFilter == "NON_VEG") "ALL" else "NON_VEG") },
                        label = { Text("🔴 Non-Veg", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF8D7DA),
                            selectedLabelColor = Color(0xFF721C24)
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.quickBiteFilter,
                        onClick = { viewModel.toggleQuickBites() },
                        label = { Text("⚡ Quick Bites (<3m)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFEECC),
                            selectedLabelColor = Color(0xFFB45309)
                        )
                    )
                }
            }

            // Category Tabs
            if (uiState.categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categories) { category ->
                        val isSelected = uiState.selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category) },
                            label = {
                                Text(
                                    text = category,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TukraPink,
                                selectedLabelColor = Ink,
                                containerColor = Color.White,
                                labelColor = InkSoft
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = OutlineWarm,
                                selectedBorderColor = TukraPink,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
                HorizontalDivider(color = OutlineWarm, thickness = 1.dp)
            }

            // Menu Items List
            if (uiState.isLoading) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(5) {
                        SkeletonShimmer(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), height = 72.dp)
                    }
                }
            } else if (uiState.items.isEmpty()) {
                EmptyState(
                    title = "No dishes found",
                    message = uiState.errorMessage ?: "Try changing your dietary or quick-bites filters.",
                    actionButtonText = "Reset Filters",
                    onActionClick = {
                        viewModel.setDietFilter("ALL")
                        if (uiState.quickBiteFilter) viewModel.toggleQuickBites()
                    }
                )
            } else {
                val displayedItems = if (uiState.selectedCategory != null) {
                    uiState.items.filter { it.category == uiState.selectedCategory }
                } else {
                    uiState.items
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(displayedItems, key = { it.id }) { item ->
                        val cartItem = cartState.items.find { it.menuItem.id == item.id }
                        val currentQty = cartItem?.quantity ?: 0

                        DishRow(
                            name = item.name,
                            priceRupees = item.pricePaise.toRupees(),
                            isVeg = item.isVeg,
                            description = item.description,
                            isSoldOut = !item.isAvailable,
                            quantity = currentQty,
                            isQuickBite = item.isQuickBite,
                            liveCount = item.liveAvailableCount,
                            onItemClick = {
                                selectedItemForSheet = item
                            },
                            onAddClick = {
                                val result = cartManager.addItem(
                                    outletId = outletId,
                                    outletName = outletName,
                                    item = item,
                                    qty = 1
                                )
                                if (result is AddToCartResult.DifferentOutlet) {
                                    showClearDialog = result
                                    pendingItemToAdd = Pair(item, 1)
                                }
                            },
                            onIncrement = {
                                cartManager.updateQuantity(item.id, currentQty + 1)
                            },
                            onDecrement = {
                                cartManager.updateQuantity(item.id, currentQty - 1)
                            }
                        )
                        HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    // Dish Sheet
    selectedItemForSheet?.let { item ->
        val cartItem = cartState.items.find { it.menuItem.id == item.id }
        DishSheet(
            item = item,
            sheetState = sheetState,
            initialQuantity = cartItem?.quantity ?: 1,
            onDismiss = { selectedItemForSheet = null },
            onAddToCart = { qty, notes ->
                selectedItemForSheet = null
                val result = cartManager.addItem(
                    outletId = outletId,
                    outletName = outletName,
                    item = item,
                    qty = qty,
                    notes = notes
                )
                if (result is AddToCartResult.DifferentOutlet) {
                    showClearDialog = result
                    pendingItemToAdd = Pair(item, qty)
                }
            }
        )
    }

    // Clear Cart Dialog if trying to order from multiple outlets
    showClearDialog?.let { diff ->
        ClearCartDialog(
            existingOutletName = diff.existingOutletName,
            newOutletName = diff.newOutletName,
            onConfirmClear = {
                cartManager.clearCart()
                pendingItemToAdd?.let { (item, qty) ->
                    cartManager.addItem(outletId, outletName, item, qty)
                }
                showClearDialog = null
                pendingItemToAdd = null
            },
            onDismiss = {
                showClearDialog = null
                pendingItemToAdd = null
            }
        )
    }
}
