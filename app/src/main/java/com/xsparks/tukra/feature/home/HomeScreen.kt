package com.xsparks.tukra.feature.home

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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.xsparks.tukra.core.designsystem.components.CartBar
import com.xsparks.tukra.core.designsystem.components.EmptyState
import com.xsparks.tukra.core.designsystem.components.OutletCard
import com.xsparks.tukra.core.designsystem.components.SkeletonShimmer
import com.xsparks.tukra.core.designsystem.components.StudentBottomBar
import com.xsparks.tukra.core.designsystem.components.StudentNavTab
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.toRupees

@Composable
fun HomeScreen(
    tokenStore: TokenStore,
    viewModel: HomeViewModel,
    onOutletClick: (outletId: String, outletName: String) -> Unit,
    onViewCartClick: () -> Unit,
    onTabSelected: (StudentNavTab) -> Unit,
    onWalletClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cartManager = CartManager.getInstance(context)
    val cartState by cartManager.cartState.collectAsState()

    Scaffold(
        bottomBar = {
            Column {
                if (cartState.totalItemsCount > 0) {
                    CartBar(
                        itemCount = cartState.totalItemsCount,
                        totalRupees = cartState.totalPaise.toRupees(),
                        onViewCartClick = onViewCartClick
                    )
                }
                StudentBottomBar(
                    currentTab = StudentNavTab.HOME,
                    onTabSelected = onTabSelected
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header Section
            item {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val firstName = uiState.user?.name?.split(" ")?.firstOrNull() ?: "Student"
                            Text(
                                text = "Hello, $firstName 👋",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Ink
                            )
                            Text(
                                text = uiState.campus?.name ?: "Presidency University",
                                fontSize = 13.sp,
                                color = InkSoft
                            )
                        }

                        // Wallet Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Butter)
                                .clickable(onClick = onWalletClick)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Ink,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.walletBalancePaise.toRupees(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Ink
                                )
                            }
                        }
                    }

                    // Offline Banner
                    if (uiState.isOffline) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF3CD))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Offline Mode — Browsing cached outlets", fontSize = 12.sp, color = Ink)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Break Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Butter)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Break in 18 min",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.breakNotice ?: "Order ahead now to pick up without waiting in line.",
                                fontSize = 13.sp,
                                color = InkSoft
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search canteens, dishes...", color = InkSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = InkSoft, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Clear", tint = InkSoft)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TukraPink,
                            unfocusedBorderColor = OutlineWarm,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.filterOpenNow,
                                onClick = { viewModel.toggleOpenNow() },
                                label = { Text("Open now", fontSize = 12.sp) },
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
                                    selected = uiState.filterOpenNow
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterVegOnly,
                                onClick = { viewModel.toggleVegOnly() },
                                label = { Text("Veg only", fontSize = 12.sp) },
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
                                    selected = uiState.filterVegOnly
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterReadyFast,
                                onClick = { viewModel.toggleReadyFast() },
                                label = { Text("Ready fast (≤15m)", fontSize = 12.sp) },
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
                                    selected = uiState.filterReadyFast
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Campus Outlets",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Outlets List or Skeletons
            if (uiState.isLoading && uiState.outlets.isEmpty()) {
                items(3) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        SkeletonShimmer(modifier = Modifier.fillMaxWidth(), height = 88.dp, shapeRadius = 16.dp)
                    }
                }
            } else if (uiState.filteredOutlets.isEmpty()) {
                item {
                    EmptyState(
                        title = "No outlets match",
                        message = "Try clearing your search query or filters to see all campus canteens.",
                        actionButtonText = "Reset filters",
                        onActionClick = {
                            viewModel.onSearchQueryChanged("")
                            if (uiState.filterVegOnly) viewModel.toggleVegOnly()
                            if (uiState.filterReadyFast) viewModel.toggleReadyFast()
                            if (uiState.filterOpenNow) viewModel.toggleOpenNow()
                        }
                    )
                }
            } else {
                items(uiState.filteredOutlets, key = { it.id }) { outlet ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        OutletCard(
                            name = outlet.name,
                            category = outlet.category,
                            isOpen = outlet.isOpen,
                            waitMinutes = outlet.waitMinutes,
                            onClick = { onOutletClick(outlet.id, outlet.name) }
                        )
                    }
                }
            }
        }
    }
}
