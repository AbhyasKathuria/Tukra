package com.xsparks.tukra.core.designsystem.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.Success

data class SourceProgressItem(
    val sourceName: String,
    val status: String, // "READING", "DONE", "FAILED", "IDLE"
    val detail: String = ""
)

@Composable
fun TinyFishLiveProgress(
    modifier: Modifier = Modifier,
    sources: List<SourceProgressItem>,
    streamingUrl: String? = null,
    isStreaming: Boolean = false
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E101D))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🐟", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE AGENT SOURCES",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFFFFD166)
                    )
                }

                if (!streamingUrl.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF06D6A0))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(streamingUrl))
                                context.startActivity(intent)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "WATCH LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-source status pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sources.forEach { item ->
                    val (badgeBg, badgeTextColor, iconVector) = when (item.status.uppercase()) {
                        "DONE" -> Triple(Success.copy(alpha = 0.2f), Color(0xFF06D6A0), Icons.Outlined.Check)
                        "FAILED" -> Triple(Danger.copy(alpha = 0.2f), Danger, Icons.Outlined.Close)
                        "READING" -> Triple(Butter.copy(alpha = 0.2f), Color(0xFFFFD166), Icons.Outlined.Refresh)
                        else -> Triple(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.6f), null)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.sourceName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (item.status.uppercase() == "READING") {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        color = Color(0xFFFFD166),
                                        strokeWidth = 1.5.dp
                                    )
                                } else if (iconVector != null) {
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = null,
                                        tint = badgeTextColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Text(
                                text = item.status.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = badgeTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}
