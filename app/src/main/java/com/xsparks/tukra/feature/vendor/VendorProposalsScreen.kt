package com.xsparks.tukra.feature.vendor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.core.designsystem.Blush
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.PinkDeep
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.Success
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.SecondaryButton
import com.xsparks.tukra.core.designsystem.components.VendorBottomBar
import com.xsparks.tukra.core.designsystem.components.VendorNavTab
import com.xsparks.tukra.core.network.ProposalDto
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.toRupees

@Composable
fun VendorProposalsScreen(
    tokenStore: TokenStore,
    viewModel: VendorProposalsViewModel,
    onTabSelected: (VendorNavTab) -> Unit
) {
    val context = LocalContext.current
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
                currentTab = VendorNavTab.PROPOSALS,
                onTabSelected = onTabSelected,
                pendingProposalsCount = uiState.proposals.size
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TinyFish AI Sync",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = PinkDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "स्वायत्त वेब एजेंट मेन्यू सिंक (Human-in-the-loop review)",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }

                    Button(
                        onClick = { viewModel.startSync() },
                        enabled = !uiState.isSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TukraPink,
                            contentColor = Ink
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(color = Ink, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Syncing...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Sync", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Divider(color = OutlineWarm, thickness = 1.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Sync Status Card (when active or recently run)
                if (uiState.activeSyncJob != null || uiState.isSyncing) {
                    item {
                        val job = uiState.activeSyncJob
                        val isRunning = job?.status == "RUNNING" || uiState.isSyncing
                        val isCompleted = job?.status == "COMPLETED"
                        val isFailed = job?.status == "FAILED"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isRunning) TukraPink else if (isCompleted) Success else Danger,
                                    RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isRunning) {
                                            CircularProgressIndicator(
                                                color = TukraPink,
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.5.dp
                                            )
                                        } else if (isCompleted) {
                                            Icon(
                                                Icons.Outlined.CheckCircle,
                                                contentDescription = null,
                                                tint = Success,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            Icon(
                                                Icons.Outlined.ErrorOutline,
                                                contentDescription = null,
                                                tint = Danger,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = if (isRunning) "Agent Navigating Public Menu" else if (isCompleted) "Sync Complete" else "Sync Failed",
                                            fontFamily = SoraFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Ink
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isRunning) Butter else if (isCompleted) Success.copy(alpha = 0.15f) else Danger.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = job?.status ?: "RUNNING",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Ink
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = job?.progress ?: "Starting TinyFish browser automation agent...",
                                    fontSize = 13.sp,
                                    color = InkSoft
                                )

                                // Watch Live Streaming Button
                                if (!job?.streamingUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job?.streamingUrl))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Butter,
                                            contentColor = Ink
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Outlined.OpenInBrowser,
                                            contentDescription = null,
                                            tint = Ink,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Watch Live Agent View (TinyFish Stream)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section Header: Pending Proposals
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pending Menu Proposals (${uiState.proposals.size})",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Ink
                        )

                        if (uiState.proposals.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.approveAllProposals() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Success,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Approve All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (uiState.proposals.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Blush.copy(alpha = 0.25f))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "All Menu Proposals Reviewed",
                                    fontFamily = SoraFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your outlet menu is 100% synchronized with the campus website.",
                                    fontSize = 12.sp,
                                    color = InkSoft,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.proposals, key = { it.id }) { proposal ->
                        ProposalReviewCard(
                            proposal = proposal,
                            onApprove = { viewModel.decideProposal(proposal.id, approve = true) },
                            onReject = { viewModel.decideProposal(proposal.id, approve = false) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ProposalReviewCard(
    proposal: ProposalDto,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (proposal.flagged) Butter else OutlineWarm,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Type Tag + Flagged Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (tagBg, tagFg, tagLabel) = when (proposal.changeType) {
                    "NEW" -> Triple(Blush, Ink, "NEW DISH (नया व्यंजन)")
                    "PRICE_CHANGE" -> Triple(Butter, Ink, "PRICE CHANGE (कीमत बदलाव)")
                    "REMOVED" -> Triple(Danger.copy(alpha = 0.15f), Danger, "REMOVED (हटाया गया)")
                    "AVAILABILITY" -> Triple(Butter.copy(alpha = 0.6f), Ink, "AVAILABILITY TOGGLE")
                    else -> Triple(Color(0xFFEEEEEE), InkSoft, proposal.changeType)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tagBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tagLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = tagFg
                    )
                }

                if (proposal.flagged) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Butter)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = Ink,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Check this price",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Ink
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dish Name
            Text(
                text = proposal.itemName,
                fontFamily = SoraFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Ink
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price Details
            when (proposal.changeType) {
                "PRICE_CHANGE" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = proposal.oldPricePaise?.toRupees() ?: "₹0",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkSoft
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = InkSoft,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = proposal.newPricePaise?.toRupees() ?: "₹0",
                            fontFamily = SoraFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinkDeep
                        )
                    }
                }
                "NEW" -> {
                    Text(
                        text = "Proposed Price: ${proposal.newPricePaise?.toRupees() ?: "₹0"}",
                        fontFamily = SoraFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                }
                "REMOVED" -> {
                    Text(
                        text = "Item will be marked sold out / removed from ordering.",
                        fontSize = 13.sp,
                        color = InkSoft
                    )
                }
            }

            // Flagged warning banner
            if (proposal.flagged) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Butter)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚠️ Extreme price change detected from previous rate. Please review carefully before approving.",
                        fontSize = 12.sp,
                        color = Ink,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Approve vs Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Danger)
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = null, tint = Danger, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject", color = Danger, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TukraPink,
                        contentColor = Ink
                    )
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Approve (स्वीकार करें)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
