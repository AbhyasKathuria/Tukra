package com.xsparks.tukra.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Blush
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.Rose
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.TukraTheme

@Composable
fun SlotChip(
    timeLabel: String,
    remainingSlots: Int,
    totalCapacity: Int,
    isSelected: Boolean,
    isEarliest: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFull = remainingSlots <= 0
    val fillRatio = if (totalCapacity > 0) {
        ((totalCapacity - remainingSlots).toFloat() / totalCapacity.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val borderColor = when {
        isSelected -> TukraPink
        isFull -> OutlineWarm.copy(alpha = 0.5f)
        else -> OutlineWarm
    }

    val backgroundColor = when {
        isSelected -> Blush.copy(alpha = 0.35f)
        isFull -> Color(0xFFF9F9F9)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .width(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isFull, onClick = onClick)
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            if (isEarliest) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Butter)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Earliest",
                        color = Ink,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = timeLabel,
                color = if (isFull) InkSoft.copy(alpha = 0.5f) else Ink,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Fill meter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OutlineWarm)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fillRatio)
                        .fillMaxHeight()
                        .background(if (isFull) OutlineWarm else Rose)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isFull) "Full" else "$remainingSlots left",
                color = if (isFull) InkSoft.copy(alpha = 0.5f) else InkSoft,
                fontSize = 11.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SlotChipPreview() {
    TukraTheme {
        SlotChip(
            timeLabel = "01:15 PM",
            remainingSlots = 5,
            totalCapacity = 10,
            isSelected = true,
            isEarliest = true,
            onClick = {}
        )
    }
}
