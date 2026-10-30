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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.VegNonVegMark
import com.xsparks.tukra.core.designsystem.components.VendorBottomBar
import com.xsparks.tukra.core.designsystem.components.VendorNavTab
import com.xsparks.tukra.core.network.MenuItemDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.toRupees

@Composable
fun VendorMenuScreen(
    tokenStore: TokenStore,
    viewModel: VendorMenuViewModel,
    onTabSelected: (VendorNavTab) -> Unit,
    onNavigateToSync: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var itemToEditPrice by remember { mutableStateOf<MenuItemDto?>(null) }
    var newPriceInput by remember { mutableStateOf("") }
    var showAddDishDialog by remember { mutableStateOf(false) }
    var showSwapExchangeDialog by remember { mutableStateOf(false) }

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
                currentTab = VendorNavTab.MENU,
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
                        Text(
                            text = "Menu Manager",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Ink
                        )
                        Text(
                            text = "मेन्यू और कीमतें (Availability & Prices)",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }

                    Button(
                        onClick = onNavigateToSync,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Butter,
                            contentColor = Ink
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Ink,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Sync AI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar + Add Dish button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search dishes...") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = InkSoft)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TukraPink,
                            focusedLabelColor = Ink
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showAddDishDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TukraPink,
                            contentColor = Ink
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add Dish", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Point 4: TinyFish Live Menu Swapping Exchange Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A1525))
                        .clickable { showSwapExchangeDialog = true }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "🔄 TinyFish Live Menu Swapping Exchange",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Autonomous AI rotation: slow items ➔ high margin specials",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFD166))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("REVIEW SWAP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink)
                        }
                    }
                }
            }

            Divider(color = OutlineWarm, thickness = 1.dp)

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TukraPink)
                }
            } else if (uiState.filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No dishes found.",
                        color = InkSoft,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredItems, key = { it.id }) { item ->
                        VendorMenuItemCard(
                            item = item,
                            onToggleAvailability = {
                                viewModel.toggleAvailability(item.id, item.isAvailable)
                            },
                            onEditPriceClick = {
                                itemToEditPrice = item
                                newPriceInput = (item.pricePaise / 100).toString()
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Edit Price Dialog
    if (itemToEditPrice != null) {
        AlertDialog(
            onDismissRequest = { itemToEditPrice = null },
            title = {
                Text(
                    text = "Edit Price: ${itemToEditPrice?.name}",
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Ink
                )
            },
            text = {
                Column {
                    Text(text = "Current price: ${itemToEditPrice?.pricePaise?.toRupees()}", fontSize = 13.sp, color = InkSoft)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPriceInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) newPriceInput = it },
                        label = { Text("New Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TukraPink,
                            focusedLabelColor = Ink
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = newPriceInput.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            val itm = itemToEditPrice
                            if (itm != null) viewModel.updatePrice(itm.id, parsed)
                        }
                        itemToEditPrice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TukraPink, contentColor = Ink)
                ) {
                    Text("Save Price", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToEditPrice = null }) {
                    Text("Cancel", color = Ink)
                }
            },
            containerColor = Color.White
        )
    }

    // Add Dish Dialog
    if (showAddDishDialog) {
        AddDishDialog(
            onDismiss = { showAddDishDialog = false },
            onAdd = { name, price, isVeg, prepMins, prepUnits, desc ->
                viewModel.addNewDish(name, price, isVeg, prepMins, prepUnits, desc)
                showAddDishDialog = false
            }
        )
    }

    if (showSwapExchangeDialog) {
        AlertDialog(
            onDismissRequest = { showSwapExchangeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = Ink)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TinyFish Live Menu Swaps", fontFamily = SoraFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text(
                        text = "TinyFish autonomous agent analyzed past order velocity & campus demand to recommend these real-time menu substitutions:",
                        fontSize = 12.sp,
                        color = InkSoft
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.swapSuggestions.isEmpty()) {
                        Text("No active swap suggestions at the moment.", fontSize = 13.sp, color = InkSoft)
                    } else {
                        uiState.swapSuggestions.forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Blush.copy(alpha = 0.5f))
                                    .border(1.dp, OutlineWarm, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(suggestion.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
                                        Text("${Math.round(suggestion.tinyfishConfidence * 100)}% Match", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B8754))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(suggestion.reason, fontSize = 11.sp, color = InkSoft)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Current: ${suggestion.currentDish?.name ?: "Slow Item"} (₹${suggestion.currentDish?.priceRupees ?: 0.0})",
                                            fontSize = 11.sp,
                                            color = Danger,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("➔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "New: ${suggestion.incomingDish.name} (₹${suggestion.incomingDish.priceRupees})",
                                            fontSize = 11.sp,
                                            color = Color(0xFF1B8754),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    PrimaryButton(
                                        text = "Execute Live Swap ⚡",
                                        onClick = {
                                            viewModel.executeSwapExchange(suggestion)
                                            showSwapExchangeDialog = false
                                        },
                                        isLoading = uiState.isSwapLoading,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showSwapExchangeDialog = false }) {
                    Text("Close", color = Ink)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun VendorMenuItemCard(
    item: MenuItemDto,
    onToggleAvailability: () -> Unit,
    onEditPriceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineWarm, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VegNonVegMark(isVeg = item.isVeg)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.name,
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if (item.isAvailable) Ink else InkSoft
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.pricePaise.toRupees(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onEditPriceClick() }
                                .background(Butter.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "Edit Price",
                                    tint = Ink,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Edit ₹", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                        }
                    }
                    val cat = item.categoryName ?: item.category
                    if (!cat.isNullOrBlank()) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            color = InkSoft
                        )
                    }
                }
            }

            // Availability Switch
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = item.isAvailable,
                    onCheckedChange = { onToggleAvailability() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Ink,
                        checkedTrackColor = TukraPink,
                        uncheckedThumbColor = InkSoft,
                        uncheckedTrackColor = Color(0xFFE0E0E0)
                    )
                )
                Text(
                    text = if (item.isAvailable) "Available" else "Sold Out",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isAvailable) Success else InkSoft
                )
            }
        }
    }
}

@Composable
fun AddDishDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, priceRupees: Int, isVeg: Boolean, prepMinutes: Int, prepUnits: Int, description: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var isVeg by remember { mutableStateOf(true) }
    var prepMinsText by remember { mutableStateOf("3") }
    var prepUnitsText by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Dish to Menu",
                fontFamily = SoraFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) priceText = it },
                        label = { Text("Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink)
                    )

                    OutlinedTextField(
                        value = prepMinsText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) prepMinsText = it },
                        label = { Text("Prep (mins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isVeg) "Type: Pure Veg" else "Type: Non-Veg", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Switch(
                        checked = isVeg,
                        onCheckedChange = { isVeg = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Success, uncheckedTrackColor = Color.LightGray)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toIntOrNull() ?: 0
                    val pm = prepMinsText.toIntOrNull() ?: 3
                    val pu = prepUnitsText.toIntOrNull() ?: 1
                    if (name.isNotBlank() && p > 0) {
                        onAdd(name, p, isVeg, pm, pu, description)
                    }
                },
                enabled = name.isNotBlank() && (priceText.toIntOrNull() ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = TukraPink, contentColor = Ink)
            ) {
                Text("Add Dish", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Ink)
            }
        },
        containerColor = Color.White
    )
}
