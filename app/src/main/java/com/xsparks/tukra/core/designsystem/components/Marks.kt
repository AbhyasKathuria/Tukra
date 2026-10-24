package com.xsparks.tukra.core.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Success
import com.xsparks.tukra.core.designsystem.TukraTheme

@Composable
fun VegNonVegMark(
    isVeg: Boolean,
    modifier: Modifier = Modifier,
    size: Int = 16
) {
    val color = if (isVeg) Success else Danger
    Box(
        modifier = modifier
            .size(size.dp)
            .border(1.5.dp, color, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size((size * 0.45).dp)
                .clip(CircleShape),
            color = color
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun VegNonVegMarkPreview() {
    TukraTheme {
        Box {
            VegNonVegMark(isVeg = true)
        }
    }
}
