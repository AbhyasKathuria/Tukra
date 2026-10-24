package com.xsparks.tukra.core.designsystem.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Blush
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.TukraPink

enum class VendorNavTab(val label: String, val hindiLabel: String, val icon: ImageVector) {
    QUEUE("Queue", "कतार", Icons.Outlined.ReceiptLong),
    MENU("Menu", "मेन्यू", Icons.Outlined.RestaurantMenu),
    CAPACITY("Capacity", "क्षमता", Icons.Outlined.Tune),
    PROPOSALS("Proposals", "प्रस्ताव", Icons.Outlined.AutoAwesome)
}

@Composable
fun VendorBottomBar(
    currentTab: VendorNavTab,
    onTabSelected: (VendorNavTab) -> Unit,
    pendingProposalsCount: Int = 0,
    activeOrdersCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp)
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VendorNavTab.values().forEach { tab ->
            val isSelected = currentTab == tab

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Blush.copy(alpha = 0.5f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    val badgeCount = when (tab) {
                        VendorNavTab.QUEUE -> activeOrdersCount
                        VendorNavTab.PROPOSALS -> pendingProposalsCount
                        else -> 0
                    }

                    if (badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = if (tab == VendorNavTab.PROPOSALS) TukraPink else Butter,
                                    contentColor = Ink
                                ) {
                                    Text(
                                        text = badgeCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) Ink else InkSoft,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) Ink else InkSoft,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${tab.label} (${tab.hindiLabel})",
                    fontSize = 11.sp,
                    color = if (isSelected) Ink else InkSoft,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
