package com.xsparks.tukra.feature.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.PinkDeep
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.Success
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.AdminBottomBar
import com.xsparks.tukra.core.designsystem.components.AdminNavTab
import com.xsparks.tukra.core.network.AdminSyncJobItem
import com.xsparks.tukra.core.network.TokenStore

@Composable
fun AdminSyncScreen(
    tokenStore: TokenStore,
    viewModel: AdminViewModel,
    onTabSelected: (AdminNavTab) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadSyncJobs()
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
            AdminBottomBar(
                currentTab = AdminNavTab.SYNC,
                onTabSelected = onTabSelected
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
                                text = "TinyFish AI Sync Monitor",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = PinkDeep, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "Autonomous campus dining web intelligence runs",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerTinyFishSync() },
                        enabled = !uiState.isActionLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = TukraPink, contentColor = Ink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run Sync", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            HorizontalDivider(color = OutlineWarm, thickness = 1.dp)

            if (uiState.syncJobs.isEmpty() && uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TukraPink)
                }
            } else if (uiState.syncJobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No TinyFish runs recorded yet. Tap 'Run Sync' above.", fontSize = 13.sp, color = InkSoft)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.syncJobs, key = { it.id }) { job ->
                        SyncJobCard(
                            job = job,
                            onWatchLive = { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SyncJobCard(
    job: AdminSyncJobItem,
    onWatchLive: (String) -> Unit
) {
    val isCompleted = job.status == "COMPLETED"
    val isRunning = job.status == "RUNNING"
    val isFailed = job.status == "FAILED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isRunning) TukraPink else if (isCompleted) Success.copy(alpha = 0.5f) else OutlineWarm,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.outletName ?: "Campus Stall",
                    fontFamily = SoraFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Ink
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isRunning) Butter else if (isCompleted) Success.copy(alpha = 0.15f) else Danger.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = job.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isRunning) Ink else if (isCompleted) Success else Danger
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = job.progress ?: "TinyFish web agent execution",
                fontSize = 12.sp,
                color = InkSoft
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = OutlineWarm, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${job.proposalCount} proposed changes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PinkDeep
                )
                Text(
                    text = "${job.numSteps ?: 0} steps",
                    fontSize = 11.sp,
                    color = InkSoft
                )
            }

            if (!job.streamingUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onWatchLive(job.streamingUrl) },
                    colors = ButtonDefaults.buttonColors(containerColor = Butter, contentColor = Ink),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Icon(Icons.Outlined.OpenInBrowser, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Watch Live Agent Stream", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
