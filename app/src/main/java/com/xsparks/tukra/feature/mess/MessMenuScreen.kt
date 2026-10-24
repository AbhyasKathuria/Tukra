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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SoupKitchen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.xsparks.tukra.core.network.MessMenuItemDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.toRupees
import kotlinx.coroutines.launch

@Composable
fun MessMenuScreen(
    tokenStore: TokenStore,
    viewModel: MessMenuViewModel,
    onTabSelected: (MessNavTab) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var itemToEditQty by remember { mutableStateOf<MessMenuItemDto?>(null) }
    var newQtyInput by remember { mutableStateOf("") }
    var showPublishDialog by remember { mutableStateOf(false) }

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
                currentTab = MessNavTab.MENU,
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
                            text = "Hostel Mess Menu",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Ink
                        )
                        Text(
                            text = "Service Date: ${uiState.serviceDate}",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showPublishDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TukraPink, contentColor = Ink),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Publish Dish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Log Out", tint = InkSoft)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meal Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MessMeal.values()) { meal ->
                        val isSelected = uiState.selectedMeal == meal
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) TukraPink else Blush.copy(alpha = 0.35f))
                                .clickable { viewModel.selectMeal(meal) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${meal.title} (${meal.hindiTitle})",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Ink
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = OutlineWarm, thickness = 1.dp)

            // Content
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TukraPink)
                }
            } else if (uiState.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(Butter),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.SoupKitchen, contentDescription = null, tint = Ink, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No dishes published for ${uiState.selectedMeal.title}",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Publish Dish' above to add today's menu offerings.",
                            fontSize = 13.sp,
                            color = InkSoft
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.items, key = { it.messMenuId }) { item ->
                        MessDishCard(
                            item = item,
                            onEditQtyClick = {
                                itemToEditQty = item
                                newQtyInput = (item.plannedQty ?: 40).toString()
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

    // Edit Planned Qty Dialog
    if (itemToEditQty != null) {
        AlertDialog(
            onDismissRequest = { itemToEditQty = null },
            title = {
                Text(
                    text = "Set Planned Qty: ${itemToEditQty?.dishName}",
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Ink
                )
            },
            text = {
                Column {
                    Text(
                        text = "Current reservations: ${itemToEditQty?.reservationCount} portions",
                        fontSize = 13.sp,
                        color = InkSoft
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newQtyInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) newQtyInput = it },
                        label = { Text("Planned Portions to Cook") },
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
                        val qty = newQtyInput.toIntOrNull()
                        if (qty != null && qty > 0) {
                            val itm = itemToEditQty
                            if (itm != null) viewModel.updatePlannedQty(itm, qty)
                        }
                        itemToEditQty = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TukraPink, contentColor = Ink)
                ) {
                    Text("Save Portions", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToEditQty = null }) {
                    Text("Cancel", color = Ink)
                }
            },
            containerColor = Color.White
        )
    }

    // Publish Dish to Meal Dialog
    if (showPublishDialog) {
        PublishDishDialog(
            availableItems = uiState.availableOutletItems,
            mealTitle = uiState.selectedMeal.title,
            onDismiss = { showPublishDialog = false },
            onPublish = { menuItemId, qty ->
                viewModel.publishDishToMeal(menuItemId, qty)
                showPublishDialog = false
            }
        )
    }
}

@Composable
fun MessDishCard(
    item: MessMenuItemDto,
    onEditQtyClick: () -> Unit
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
                        text = item.dishName,
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Ink
                    )
                    if (item.pricePaise > 0) {
                        Text(
                            text = item.pricePaise.toRupees(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PinkDeep
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.People, contentDescription = null, tint = InkSoft, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.reservationCount} reserved",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }
                }
            }

            // Planned Qty Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onEditQtyClick() }
                    .background(Butter)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${item.plannedQty ?: 40}",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Ink
                        )
                        Text(
                            text = "Planned Qty",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkSoft
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Ink, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun PublishDishDialog(
    availableItems: List<MenuItemDto>,
    mealTitle: String,
    onDismiss: () -> Unit,
    onPublish: (menuItemId: String, plannedQty: Int) -> Unit
) {
    var selectedItemId by remember { mutableStateOf(availableItems.firstOrNull()?.id ?: "") }
    var plannedQtyText by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Publish Dish to $mealTitle",
                fontFamily = SoraFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Choose dish to offer:", fontSize = 13.sp, color = InkSoft)

                if (availableItems.isEmpty()) {
                    Text(
                        text = "No catalog dishes configured. Add items to mess outlet first.",
                        color = PinkDeep,
                        fontSize = 12.sp
                    )
                } else {
                    availableItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedItemId == item.id) Blush else Color(0xFFF9F9F9))
                                .clickable { selectedItemId = item.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.name,
                                fontWeight = if (selectedItemId == item.id) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = Ink
                            )
                            if (item.pricePaise > 0) {
                                Text(text = item.pricePaise.toRupees(), fontSize = 12.sp, color = InkSoft)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = plannedQtyText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) plannedQtyText = it },
                    label = { Text("Initial Planned Portions") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = plannedQtyText.toIntOrNull() ?: 50
                    if (selectedItemId.isNotBlank() && qty > 0) {
                        onPublish(selectedItemId, qty)
                    }
                },
                enabled = selectedItemId.isNotBlank() && (plannedQtyText.toIntOrNull() ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = TukraPink, contentColor = Ink)
            ) {
                Text("Publish to Menu", fontWeight = FontWeight.Bold)
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
