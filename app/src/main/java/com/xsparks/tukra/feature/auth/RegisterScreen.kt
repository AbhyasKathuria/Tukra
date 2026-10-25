package com.xsparks.tukra.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.xsparks.tukra.R
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.PinkDeep
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.PrimaryButton

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: (role: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var hostel by remember { mutableStateOf("") }
    var dietPreference by remember { mutableStateOf("ANY") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successRole) {
        val role = uiState.successRole
        if (role != null) {
            viewModel.resetSuccessState()
            onRegisterSuccess(role)
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

        Text(
            text = "Create Account",
            fontFamily = SoraFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Ink
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Join your campus dining network in seconds.",
            fontSize = 15.sp,
            color = InkSoft
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Full Name
        Text("Full Name", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; viewModel.clearError() },
            placeholder = { Text("e.g. Aarav Sharma", color = InkSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = InkSoft, modifier = Modifier.size(20.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        Text("Campus Email", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearError() },
            placeholder = { Text("student@presidency.edu.in", color = InkSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = InkSoft, modifier = Modifier.size(20.dp))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
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

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        Text("Password", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearError() },
            placeholder = { Text("At least 6 characters", color = InkSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = InkSoft, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = InkSoft
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
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

        Spacer(modifier = Modifier.height(16.dp))

        // Hostel (Optional)
        Text("Hostel / Block (Optional)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = hostel,
            onValueChange = { hostel = it },
            placeholder = { Text("e.g. Ganga Hostel, Room 204", color = InkSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Outlined.Home, contentDescription = null, tint = InkSoft, modifier = Modifier.size(20.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        // Diet Preference
        Text("Diet Preference", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ANY" to "No restriction", "VEG" to "Veg only", "NON_VEG" to "Non-Veg").forEach { (code, label) ->
                val isSelected = dietPreference == code
                FilterChip(
                    selected = isSelected,
                    onClick = { dietPreference = code },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TukraPink,
                        selectedLabelColor = Ink,
                        containerColor = Color.White,
                        labelColor = InkSoft
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = OutlineWarm,
                        selectedBorderColor = TukraPink,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.error!!,
                color = Danger,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryButton(
            text = "Create Account",
            onClick = {
                focusManager.clearFocus()
                viewModel.register(
                    email = email,
                    pass = password,
                    name = name,
                    hostel = hostel,
                    dietPreference = dietPreference
                )
            },
            isLoading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", fontSize = 14.sp, color = InkSoft)
            Text(
                text = "Log in",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PinkDeep,
                modifier = Modifier.clickable(onClick = onNavigateToLogin)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
