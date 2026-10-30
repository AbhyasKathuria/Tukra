package com.xsparks.tukra.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TinyFishInfoBottomSheet(
    onDismissRequest: () -> Unit,
    screenName: String = "This Screen",
    whatItReads: String = "Public canteen menus, live Swiggy/Zomato/EatSure listings, and nearby restaurants.",
    whyItNeedsAgent: String = "Most stalls and delivery apps have no open API; TinyFish autonomously reads the messy web and extracts structured prices."
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🐟", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "How TinyFish Helps Here",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Ink
                    )
                    Text(
                        text = "Role on $screenName",
                        fontSize = 12.sp,
                        color = InkSoft
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = InkSoft)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Purpose Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF0F8))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "PURPOSE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD442A0)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TinyFish is our eyes on the outside web. It reads unstructured web pages with no API and converts them into verified prices for students and vendors.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // What It Reads
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("What TinyFish Reads Here", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
                    Text(text = whatItReads, fontSize = 12.sp, color = InkSoft, lineHeight = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEFEBF0))
            Spacer(modifier = Modifier.height(12.dp))

            // What It NEVER Does (Strict Safety Boundary)
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = Danger,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Strict Safety Guardrails (Never Done by AI)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Danger)
                    Text(
                        text = "TinyFish NEVER touches payments, bill totals, wallet deductions, pickup slot allocations, login passwords, or allergen advice. Those remain in strict deterministic code.",
                        fontSize = 12.sp,
                        color = InkSoft,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Human In The Loop Notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFAFAFC))
                    .padding(10.dp)
            ) {
                Text(
                    text = "🔒 Human-in-the-loop: All parsed prices and dishes require vendor/admin confirmation before entering the live system.",
                    fontSize = 11.sp,
                    color = InkSoft
                )
            }
        }
    }
}
