package com.xsparks.tukra.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsparks.tukra.BuildConfig
import com.xsparks.tukra.R
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.PinkDeep
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.network.NetworkClient

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (role: String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showServerDialog by remember { mutableStateOf(false) }
    var currentBaseUrl by remember { mutableStateOf(NetworkClient.getEffectiveBaseUrl()) }
    var serverUrlInput by remember { mutableStateOf(currentBaseUrl) }

    LaunchedEffect(uiState.successRole) {
        val role = uiState.successRole
        if (role != null) {
            viewModel.resetSuccessState()
            onLoginSuccess(role)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Brand Logo
        Image(
            painter = painterResource(id = R.drawable.ic_tukra_logo),
            contentDescription = "Tukra Logo",
            modifier = Modifier
                .height(44.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Wordmark / Header
        Text(
            text = "Welcome to Tukra",
            fontFamily = SoraFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Ink
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Log in to pick dishes, skip queues, and pay your tukra.",
            fontSize = 15.sp,
            color = InkSoft,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        Text(
            text = "Campus Email",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearError()
            },
            placeholder = { Text("e.g. student@presidency.edu.in", color = InkSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = InkSoft,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
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

        Spacer(modifier = Modifier.height(20.dp))

        // Password Field
        Text(
            text = "Password",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            placeholder = { Text("••••••••", color = InkSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = InkSoft,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = InkSoft
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    viewModel.login(email, password)
                }
            ),
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

        // Error message under field in Danger
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = uiState.error!!,
                color = Danger,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Primary Log In Button
        PrimaryButton(
            text = "Log In",
            onClick = {
                focusManager.clearFocus()
                viewModel.login(email, password)
            },
            isLoading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Link to Register
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New here? ",
                fontSize = 14.sp,
                color = InkSoft
            )
            Text(
                text = "Create account",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PinkDeep,
                modifier = Modifier.clickable(onClick = onNavigateToRegister)
            )
        }

        // Demo Quick-Fill Demo Chips
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "DEMO ACCOUNTS (ONE-TAP FILL)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = InkSoft
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DemoFillChip("Student") {
                email = "aarav@presidency.ac.in"
                password = "demo1234"
                viewModel.clearError()
            }
            DemoFillChip("Vendor") {
                email = "ramesh@southtiffin.com"
                password = "demo1234"
                viewModel.clearError()
            }
            DemoFillChip("Mess Operator") {
                email = "mess@presidency.ac.in"
                password = "demo1234"
                viewModel.clearError()
            }
            DemoFillChip("Admin") {
                email = "admin@presidency.ac.in"
                password = "demo1234"
                viewModel.clearError()
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Server Connection Status & Config
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF7F5F8))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BACKEND SERVER URL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkSoft
                )
                Text(
                    text = currentBaseUrl,
                    fontSize = 12.sp,
                    color = Ink,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Change",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PinkDeep,
                modifier = Modifier.clickable {
                    serverUrlInput = currentBaseUrl
                    showServerDialog = true
                }
            )
        }

        if (showServerDialog) {
            AlertDialog(
                onDismissRequest = { showServerDialog = false },
                title = { Text("Configure Backend URL", fontWeight = FontWeight.Bold, color = Ink) },
                text = {
                    Column {
                        Text(
                            text = "Enter your live Vercel URL or local Wi-Fi IP (e.g. http://192.168.1.15:3000/):",
                            fontSize = 13.sp,
                            color = InkSoft
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val trimmed = serverUrlInput.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.updateServerUrl(trimmed)
                            currentBaseUrl = NetworkClient.getEffectiveBaseUrl()
                        }
                        showServerDialog = false
                    }) {
                        Text("Save & Connect", fontWeight = FontWeight.Bold, color = PinkDeep)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.updateServerUrl(BuildConfig.API_BASE_URL)
                        currentBaseUrl = NetworkClient.getEffectiveBaseUrl()
                        showServerDialog = false
                    }) {
                        Text("Reset Default", color = InkSoft)
                    }
                }
            )
        }
    }
}

@Composable
fun DemoFillChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Butter)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink
        )
    }
}
