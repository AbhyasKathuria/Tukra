package com.xsparks.tukra.feature.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.SecondaryButton
import com.xsparks.tukra.core.designsystem.components.StudentBottomBar
import com.xsparks.tukra.core.designsystem.components.StudentNavTab
import com.xsparks.tukra.core.network.NetworkClient
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.core.util.QrGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    tokenStore: TokenStore,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onTabSelected: (StudentNavTab) -> Unit,
    onViewAgentConsole: () -> Unit = {},
    onViewAboutTinyFish: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.isAccountDeleted) {
        if (uiState.isAccountDeleted) {
            onLogout()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        val msg = uiState.successMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Ink
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            StudentBottomBar(
                currentTab = StudentNavTab.PROFILE,
                onTabSelected = onTabSelected
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        val user = uiState.user

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            val context = LocalContext.current
            val baseUrl = NetworkClient.getEffectiveBaseUrl()

            // Student Digital ID Card (Point 11: Whole Student Info)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2A1525), // Rich dark plum
                                Color(0xFF1B0C17)
                            )
                        )
                    )
                    .border(1.5.dp, Color(0xFFD4A373).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    // University Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.School,
                                contentDescription = null,
                                tint = Color(0xFFFFD166),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PRESIDENCY UNIVERSITY",
                                    fontFamily = SoraFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "BENGALURU • SMART CAMPUS ID",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFFD166)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF06D6A0).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF06D6A0), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF06D6A0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Student Details Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val initials = user?.name?.split(" ")?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("") ?: "AS"
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE07A5F))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.name ?: "Aarav Sharma",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Roll No: ${user?.rollNumber ?: "20231CSE0491"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFD166)
                            )
                            Text(
                                text = user?.email ?: "aarav@presidency.ac.in",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Academic & Residence Tags Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Department", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(user?.department ?: "Computer Science & Engg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Degree & Year", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(user?.degreeYear ?: "B.Tech • 3rd Year (2023-27)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Hostel & Room", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("${user?.hostel ?: "Cauvery Boys Hostel"} • ${user?.roomNumber ?: "B-204"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("RFID Card ID", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(user?.rfidCardId ?: "PU-RFID-98421", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD166))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Dietary Preference", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(
                                when (user?.dietPreference) {
                                    "VEG" -> "Pure Veg (FSSAI 🟢)"
                                    "NON_VEG" -> "Non-Veg (FSSAI 🔴)"
                                    else -> "Veg / Any Option"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user?.dietPreference == "NON_VEG") Color(0xFFFF6B6B) else Color(0xFF06D6A0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Green & Eco Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF06D6A0).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF06D6A0).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌱 Credits", fontSize = 10.sp, color = Color(0xFF06D6A0))
                                Text("${user?.greenCredits ?: 420} pts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFF9F1C).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFFF9F1C).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔥 Streak", fontSize = 10.sp, color = Color(0xFFFF9F1C))
                                Text("${user?.cleanStreakDays ?: 14} days", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF4EA8DE).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF4EA8DE).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🍲 Saved", fontSize = 10.sp, color = Color(0xFF4EA8DE))
                                Text("${user?.foodSavedKg ?: 4.2} kg", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Barcode section on ID card
                    val qrContent = "PU:ID:${user?.rollNumber ?: "20231CSE0491"}:${user?.email ?: "aarav@presidency.ac.in"}"
                    val qrBmp = remember(qrContent) { QrGenerator.generateQrBitmap(qrContent, sizePx = 240) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DIGITAL CAMPUS BARCODE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("Tap & scan at counter or mess turnstile", fontSize = 9.sp, color = InkSoft)
                            Text(user?.rfidCardId ?: "PU-RFID-98421", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                        }
                        if (qrBmp != null) {
                            Image(
                                bitmap = qrBmp.asImageBitmap(),
                                contentDescription = "Student QR",
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Institutional Demo Data Export Card (Point 2 & 3: University & Canteen Demo Data Excel)
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
                            Icons.Outlined.TableChart,
                            contentDescription = null,
                            tint = Color(0xFF1B8754),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Institutional Demo Datasets (Excel)",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Ink
                            )
                            Text(
                                text = "Official Presidency University records & canteen catalog",
                                fontSize = 11.sp,
                                color = InkSoft
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Students Dataset Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Blush.copy(alpha = 0.5f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👨‍🎓 Presidency University Students Data",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Ink
                            )
                            Text(
                                text = "Includes 12 demo students, roll numbers, RFID & credits",
                                fontSize = 10.sp,
                                color = InkSoft
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1B8754))
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${baseUrl}campus/export/students-excel"))
                                        context.startActivity(intent)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(".XLSX", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Butter)
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${baseUrl}campus/export/students-csv"))
                                        context.startActivity(intent)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(".CSV", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Canteen Dataset Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Blush.copy(alpha = 0.5f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🍱 Presidency Canteen Menu & Pricing Data",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Ink
                            )
                            Text(
                                text = "All outlets, veg/non-veg flags, prep times & quick bites",
                                fontSize = 10.sp,
                                color = InkSoft
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1B8754))
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${baseUrl}outlets/export/canteen-excel"))
                                        context.startActivity(intent)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(".XLSX", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Butter)
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${baseUrl}outlets/export/canteen-csv"))
                                        context.startActivity(intent)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(".CSV", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Campus Information",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Ink
                        )
                        Text(
                            text = "Edit",
                            color = PinkDeep,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { showEditDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileRowItem("Campus", "Presidency University, Bengaluru")
                    ProfileRowItem("Hostel / Room", user?.hostel ?: "Cauvery Boys Hostel (B-204)")
                    ProfileRowItem("Roll Number", user?.rollNumber ?: "20231CSE0491")
                    ProfileRowItem("Department", user?.department ?: "CSE")
                    ProfileRowItem(
                        "Diet Preference",
                        when (user?.dietPreference) {
                            "VEG" -> "Pure Veg (🟢)"
                            "NON_VEG" -> "Non-Vegetarian (🔴)"
                            else -> "No restrictions"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferences & Privacy card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Preferences & Privacy",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Ink
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notifications Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Ready Order Push Alerts", fontSize = 14.sp, color = Ink)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Ink,
                                checkedTrackColor = TukraPink
                            )
                        )
                    }

                    HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

                    // TinyFish About & Usage Map
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onViewAboutTinyFish() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🐟", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("About TinyFish in Tukra", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("8 Outside Web Integrations & Guardrails (for Judges)", fontSize = 11.sp, color = InkSoft)
                        }
                    }

                    HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f))

                    // TinyFish Agent Console
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onViewAgentConsole() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚡", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TinyFish Agent Console", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("Live job monitor, status, duration & watch live stream", fontSize = 11.sp, color = InkSoft)
                        }
                    }

                    HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f))

                    // Privacy Policy link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyPolicyDialog = true }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Policy, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Privacy Policy & Data Transparency", fontSize = 14.sp, color = Ink)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Log Out Button
            SecondaryButton(
                text = "Log Out",
                onClick = {
                    scope.launch {
                        tokenStore.clearSession()
                        onLogout()
                    }
                },
                isOutlined = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Delete Account Button (Part K)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showDeleteConfirmDialog = true }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = Danger, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delete my account",
                        color = Danger,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        var editName by remember { mutableStateOf(uiState.user?.name ?: "") }
        var editHostel by remember { mutableStateOf(uiState.user?.hostel ?: "") }
        var editDiet by remember { mutableStateOf(uiState.user?.dietPreference ?: "ANY") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", fontFamily = SoraFamily, fontWeight = FontWeight.Bold, color = Ink) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editHostel,
                        onValueChange = { editHostel = it },
                        label = { Text("Hostel / Room") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TukraPink),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Diet Preference", fontSize = 12.sp, color = InkSoft)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ANY" to "Any", "VEG" to "Veg", "NON_VEG" to "Non-Veg").forEach { (code, lbl) ->
                            FilterChip(
                                selected = editDiet == code,
                                onClick = { editDiet = code },
                                label = { Text(lbl, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TukraPink)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    viewModel.updateProfile(editName, editHostel, editDiet)
                }) {
                    Text("Save", fontWeight = FontWeight.Bold, color = Ink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = InkSoft)
                }
            },
            containerColor = Color.White
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account?", fontFamily = SoraFamily, fontWeight = FontWeight.Bold, color = Danger) },
            text = {
                Text(
                    text = "This action will permanently anonymise your name, email, and campus hostel details in compliance with data privacy standards. Aggregate order counts and payment receipts are retained for dining audit records. You will be logged out immediately.",
                    fontSize = 13.sp,
                    color = InkSoft,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    viewModel.deleteAccount()
                }) {
                    Text("Yes, Delete Account", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Ink)
                }
            },
            containerColor = Color.White
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("Tukra Privacy Policy", fontFamily = SoraFamily, fontWeight = FontWeight.Bold, color = Ink) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Tukra (टुकड़ा) by Xsparks commits to strict student privacy:\n\n" +
                            "• Minimal Data Collection: Only campus email, name, and optional hostel/diet preferences are stored.\n" +
                            "• Vendor Confidentiality: Canteen stall owners only see your first name and single-use pickup order code (TK-####). They never see your phone number, email, or diet preference.\n" +
                            "• Single-use QR: Pickup QR tokens expire immediately upon counter verification, preventing screenshot replays.\n" +
                            "• In-App Account Deletion: You can delete your account at any time to wipe your PII completely.\n" +
                            "• Secure Payments: Card, netbanking and UPI transactions are processed through RBI-authorized test/live gateways.",
                        fontSize = 13.sp,
                        color = InkSoft,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = Ink)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun ProfileRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = InkSoft)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
    }
}
