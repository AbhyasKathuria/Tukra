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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.xsparks.tukra.core.designsystem.components.VendorBottomBar
import com.xsparks.tukra.core.designsystem.components.VendorNavTab
import com.xsparks.tukra.core.network.TokenStore

@Composable
fun VendorCapacityScreen(
    tokenStore: TokenStore,
    viewModel: VendorCapacityViewModel,
    onTabSelected: (VendorNavTab) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                currentTab = VendorNavTab.CAPACITY,
                onTabSelected = onTabSelected
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TukraPink)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = "Capacity & Hours",
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Ink
                )
                Text(
                    text = "स्टॉल क्षमता और समय सीमा (Control crowd & pickup slots)",
                    fontSize = 13.sp,
                    color = InkSoft
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Card 1: Stall Open / Closed status
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (uiState.isOpen) Success.copy(alpha = 0.15f) else Danger.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storefront,
                                        contentDescription = null,
                                        tint = if (uiState.isOpen) Success else Danger,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (uiState.isOpen) "Stall is OPEN" else "Stall is CLOSED",
                                        fontFamily = SoraFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (uiState.isOpen) Success else Danger
                                    )
                                    Text(
                                        text = if (uiState.isOpen) "Accepting online orders" else "Students cannot place orders",
                                        fontSize = 12.sp,
                                        color = InkSoft
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isOpen,
                                onCheckedChange = { viewModel.setOpen(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Ink,
                                    checkedTrackColor = TukraPink,
                                    uncheckedThumbColor = InkSoft,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card 2: 5-minute Slot Capacity Control
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Tune, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "5-Minute Slot Capacity",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Ink
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Maximum preparation units allowed per 5-minute pickup interval.",
                            fontSize = 12.sp,
                            color = InkSoft
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large capacity display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${uiState.slotCapacity} units",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Ink
                            )
                            Text(
                                text = "per 5 mins",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = InkSoft
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = uiState.slotCapacity.toFloat(),
                            onValueChange = { viewModel.setSlotCapacity(it.toInt()) },
                            valueRange = 1f..50f,
                            steps = 49,
                            colors = SliderDefaults.colors(
                                thumbColor = TukraPink,
                                activeTrackColor = TukraPink,
                                inactiveTrackColor = Blush.copy(alpha = 0.5f)
                            )
                        )

                        // Quick preset chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 25, 40).forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (uiState.slotCapacity == preset) Butter else Blush.copy(alpha = 0.3f))
                                        .clickable { viewModel.setSlotCapacity(preset) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$preset",
                                        fontWeight = if (uiState.slotCapacity == preset) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = Ink
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card 3: Last Order Cutoff Time
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Last Order Cutoff Time",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Ink
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Orders will automatically stop after this time.",
                            fontSize = 12.sp,
                            color = InkSoft
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = uiState.acceptsOrdersUntil,
                            onValueChange = { viewModel.setAcceptsOrdersUntil(it) },
                            label = { Text("Last Order Time (HH:MM)") },
                            placeholder = { Text("e.g. 21:30") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TukraPink,
                                focusedLabelColor = Ink
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("20:30", "21:00", "21:30", "22:00", "23:00").forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (uiState.acceptsOrdersUntil == preset) Butter else Blush.copy(alpha = 0.3f))
                                        .clickable { viewModel.setAcceptsOrdersUntil(preset) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = preset,
                                        fontWeight = if (uiState.acceptsOrdersUntil == preset) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = Ink
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = if (uiState.isSaving) "Saving Changes..." else "Save Stall Settings (सेव करें)",
                    enabled = !uiState.isSaving,
                    onClick = { viewModel.saveChanges() }
                )
            }
        }
    }
}
