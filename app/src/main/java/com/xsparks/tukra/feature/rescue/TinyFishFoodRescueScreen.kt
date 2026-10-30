package com.xsparks.tukra.feature.rescue

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Blush
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.TinyFishBadge
import com.xsparks.tukra.core.designsystem.components.TinyFishInfoBottomSheet
import com.xsparks.tukra.core.designsystem.components.TinyFishLiveProgress
import com.xsparks.tukra.core.designsystem.components.SourceProgressItem
import com.xsparks.tukra.core.network.FoodComparisonItemDto
import com.xsparks.tukra.core.network.TokenStore
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TinyFishFoodRescueScreen(
    initialDish: String = "Paneer Butter Masala",
    initialReason: String? = null,
    tokenStore: TokenStore,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { TinyFishFoodRescueViewModel(tokenStore) }
    val uiState by viewModel.uiState.collectAsState()

    var showAgentTrace by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(initialDish, initialReason) {
        viewModel.init(initialDish, initialReason)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E101D))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🐟 TinyFish Food Rescue",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFD166))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI AGENT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                        Text(
                            text = "Swiggy • Zomato • EatSure Live Scraper",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    TinyFishBadge(
                        text = "Safety Info",
                        showInfoIcon = true,
                        onClick = { showInfoSheet = true }
                    )
                }
            }
        },
        containerColor = Color(0xFFF9F7FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Auto-Detected Campus Location Card (Point 10 & User Prompt)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2A1525), Color(0xFF3F1F38))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF06D6A0).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF06D6A0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "AUTO-DETECTED LOCATION",
                                            fontFamily = SoraFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF06D6A0)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF06D6A0))
                                        )
                                    }
                                    Text(
                                        text = uiState.location.campusName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.detectCampusLocation() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                if (uiState.isLocating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFF06D6A0),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = "Refresh GPS",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "📍 ${uiState.location.zone}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Drop-off: Presidency Security Gate 2 Hub (${uiState.location.latitude}° N, ${uiState.location.longitude}° E)",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }

            // Student Dislike / Craving Input Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Restaurant,
                                contentDescription = null,
                                tint = TukraPink,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Didn't like your food? Tell TinyFish!",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Ink
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "TinyFish autonomous agent will search Swiggy, Zomato & EatSure in real-time, apply coupons, and find the cheapest meal delivering to Gate 2.",
                            fontSize = 12.sp,
                            color = InkSoft,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dish Query Input
                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = { viewModel.onQueryChanged(it) },
                            label = { Text("What dish or craving do you want instead?") },
                            placeholder = { Text("e.g. Paneer Butter Masala, Veg Biryani, Burger") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Search, contentDescription = null, tint = InkSoft)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TukraPink,
                                unfocusedBorderColor = OutlineWarm,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Craving Pills
                        Text("Popular Alternatives:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = InkSoft)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val suggestions = listOf("Paneer Butter Masala", "Veg Biryani", "Crispy Burger", "Cheese Pizza", "Kathi Roll")
                            items(suggestions) { dish ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (uiState.query == dish) TukraPink else Butter)
                                        .clickable {
                                            viewModel.onQueryChanged(dish)
                                            viewModel.triggerFoodRescue()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = dish,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dislike Reasons Chips
                        Text("Why didn't you like the meal?", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = InkSoft)
                        Spacer(modifier = Modifier.height(6.dp))
                        val reasons = listOf(
                            "Mess food was cold/bland",
                            "Canteen was out of stock",
                            "Too oily / heavy",
                            "Just craving something better"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            reasons.forEach { reason ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (uiState.dislikeReason == reason) Blush else Color(0xFFF9F7FA))
                                        .clickable { viewModel.onReasonSelected(reason) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (uiState.dislikeReason == reason) TukraPink else OutlineWarm)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = reason,
                                        fontSize = 12.sp,
                                        fontWeight = if (uiState.dislikeReason == reason) FontWeight.Bold else FontWeight.Normal,
                                        color = Ink
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Launch Scraper Button
                        PrimaryButton(
                            text = if (uiState.isLoading) "Scraping Swiggy, Zomato & EatSure..." else "🐟 Find Cheapest Alternative",
                            onClick = { viewModel.triggerFoodRescue() },
                            enabled = !uiState.isLoading && uiState.query.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Scraping Agent Pipeline Visualizer & Live Agent Progress
            if (uiState.isLoading || uiState.agentSteps.isNotEmpty()) {
                item {
                    val sources = listOf(
                        SourceProgressItem("Swiggy Live Agent", if (uiState.isLoading) "READING" else "DONE", "Parallel scraping run"),
                        SourceProgressItem("Zomato Live Agent", if (uiState.isLoading) "READING" else "DONE", "Coupons & delivery fee checks"),
                        SourceProgressItem("EatSure Live Agent", if (uiState.isLoading) "READING" else "DONE", "Direct cloud kitchen pricing")
                    )
                    TinyFishLiveProgress(
                        sources = sources,
                        streamingUrl = null,
                        isStreaming = uiState.isLoading
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E101D))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.ElectricBolt,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD166),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "TINYFISH AGENT PIPELINE",
                                        fontFamily = SoraFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFD166)
                                    )
                                }
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color(0xFFFFD166),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("DONE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06D6A0))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            uiState.agentSteps.forEach { step ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                ) {
                                    Text("•", color = Color(0xFFFFD166), fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = step,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 15.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results Section
            uiState.response?.let { res ->
                val cheapest = res.cheapestOption

                // 🏆 CHEAPEST GUARANTEED CARD
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0F3E2E), Color(0xFF1B6A4F))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF06D6A0))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "CHEAPEST GUARANTEED ON ${cheapest.platform.uppercase()}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.Black
                                        )
                                    }
                                }

                                if (cheapest.savingsRupees > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFFD166))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "SAVE ₹${cheapest.savingsRupees.toInt()}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = cheapest.dishTitle,
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )

                            Text(
                                text = "from ${cheapest.restaurant}",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "₹${cheapest.priceRupees.toInt()}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    if (cheapest.originalPriceRupees > cheapest.priceRupees) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "₹${cheapest.originalPriceRupees.toInt()}",
                                            fontSize = 14.sp,
                                            textDecoration = TextDecoration.LineThrough,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD166),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${cheapest.deliveryMinutes} mins to Gate 2",
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            if (cheapest.couponApplied.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Coupon Applied: ${cheapest.couponApplied}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFFD166)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 1-Tap Order Deep Link Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .clickable {
                                        val url = cheapest.deepLink.ifBlank { "https://www.eatsure.com" }
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🚀 Order on ${cheapest.platform} for ₹${cheapest.priceRupees.toInt()}",
                                        fontFamily = SoraFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F3E2E)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Outlined.OpenInNew,
                                        contentDescription = null,
                                        tint = Color(0xFF0F3E2E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3-Way Platform Comparison Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Live Scraped Platform Comparison",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Ink
                        )
                        Text(
                            text = "3 Platforms",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }
                }

                // Cards for each platform (Swiggy, Zomato, EatSure)
                items(res.comparisons) { comp ->
                    PlatformComparisonCard(
                        item = comp,
                        onOpenLink = {
                            val url = comp.deepLink.ifBlank { "https://www.google.com/search?q=" + comp.restaurantName }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }

                // Expandable TinyFish Scraper Agent Trace Log
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, OutlineWarm, RoundedCornerShape(12.dp))
                            .clickable { showAgentTrace = !showAgentTrace }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🤖 TinyFish Scraper Audit Trace", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(${res.agentTrace.size} steps)", fontSize = 11.sp, color = InkSoft)
                                }
                                Icon(
                                    imageVector = if (showAgentTrace) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = InkSoft
                                )
                            }

                            AnimatedVisibility(
                                visible = showAgentTrace,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    res.agentTrace.forEach { trace ->
                                        Text(
                                            text = trace,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = InkSoft,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showInfoSheet) {
            TinyFishInfoBottomSheet(
                onDismissRequest = { showInfoSheet = false },
                screenName = "Dislike Rescue"
            )
        }
    }
}

@Composable
fun PlatformComparisonCard(
    item: FoodComparisonItemDto,
    onOpenLink: () -> Unit
) {
    val platformColor = when (item.platform.lowercase()) {
        "eatsure" -> Color(0xFF6C2586) // Purple
        "swiggy" -> Color(0xFFFC8019) // Orange
        "zomato" -> Color(0xFFCB202D) // Red
        else -> TukraPink
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(
                width = if (item.isCheapest) 2.dp else 1.dp,
                color = if (item.isCheapest) Color(0xFF06D6A0) else OutlineWarm,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(platformColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.platform.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    if (item.isCheapest) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE8FDF5))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CHEAPEST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF06D6A0)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = item.rating.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.dishName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Ink
            )

            Text(
                text = item.restaurantName,
                fontSize = 12.sp,
                color = InkSoft
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${item.priceRupees.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Ink
                        )
                        if (item.originalPriceRupees > item.priceRupees) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "₹${item.originalPriceRupees.toInt()}",
                                fontSize = 12.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = InkSoft
                            )
                        }
                    }

                    if (item.couponApplied.isNotBlank()) {
                        Text(
                            text = item.couponApplied,
                            fontSize = 10.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ETA ${item.deliveryTimeMinutes}m",
                        fontSize = 11.sp,
                        color = InkSoft
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Butter)
                            .clickable(onClick = onOpenLink)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = Ink, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}
