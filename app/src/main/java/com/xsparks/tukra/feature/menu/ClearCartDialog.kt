package com.xsparks.tukra.feature.menu

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink

@Composable
fun ClearCartDialog(
    existingOutletName: String,
    newOutletName: String,
    onConfirmClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Start a new order?",
                fontFamily = SoraFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Ink
            )
        },
        text = {
            Text(
                text = "Your cart currently contains dishes from \"$existingOutletName\". Would you like to clear your cart and start a fresh order from \"$newOutletName\"?",
                fontSize = 14.sp,
                color = InkSoft,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmClear) {
                Text(
                    text = "Clear & Add",
                    color = Danger,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
