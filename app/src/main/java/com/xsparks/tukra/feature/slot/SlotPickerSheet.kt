package com.xsparks.tukra.feature.slot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.SlotChip
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.SlotDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.Resource
import com.xsparks.tukra.core.util.TimeFormat
import com.xsparks.tukra.core.util.safeApiCall

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SlotPickerSheet(
    outletId: String,
    totalUnits: Int,
    longestPrepSeconds: Int,
    tokenStore: TokenStore,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSlotConfirmed: (slotId: String, slotStartsAt: String) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var slots by remember { mutableStateOf<List<SlotDto>>(emptyList()) }
    var selectedSlot by remember { mutableStateOf<SlotDto?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val api = remember { NetworkClient.getApi(tokenStore) }

    LaunchedEffect(outletId) {
        isLoading = true
        val result = safeApiCall {
            api.getOutletSlots(
                outletId = outletId,
                units = totalUnits,
                longestPrepSeconds = longestPrepSeconds
            )
        }
        when (result) {
            is Resource.Success -> {
                slots = result.data
                // Pre-select earliest available slot if any
                val firstAvailable = result.data.firstOrNull { it.remaining > 0 }
                selectedSlot = firstAvailable
                isLoading = false
            }
            is Resource.Error -> {
                errorMessage = result.message
                isLoading = false
            }
            is Resource.Loading -> Unit
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "When do you want it?",
                fontFamily = SoraFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Ink
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Select a 5-minute counter pickup window",
                fontSize = 13.sp,
                color = InkSoft
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Ink)
                }
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Failed to load slots",
                    color = InkSoft,
                    fontSize = 14.sp
                )
            } else if (slots.isEmpty()) {
                Text(
                    text = "No pickup slots available for today. The outlet may be closed or at full capacity.",
                    color = InkSoft,
                    fontSize = 14.sp
                )
            } else {
                val earliestSlotId = slots.firstOrNull { it.remaining > 0 }?.id

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    slots.forEach { slot ->
                        val isSelected = selectedSlot?.id == slot.id
                        val isEarliest = slot.id == earliestSlotId

                        SlotChip(
                            timeLabel = TimeFormat.formatSlotTime(slot.startsAt),
                            remainingSlots = slot.remaining,
                            totalCapacity = 10,
                            isSelected = isSelected,
                            isEarliest = isEarliest,
                            onClick = {
                                selectedSlot = slot
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryButton(
                text = "Continue to Checkout",
                enabled = selectedSlot != null && selectedSlot!!.remaining > 0,
                onClick = {
                    selectedSlot?.let { slot ->
                        onSlotConfirmed(slot.id, slot.startsAt)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
