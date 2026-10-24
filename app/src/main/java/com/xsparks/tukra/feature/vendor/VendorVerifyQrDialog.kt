package com.xsparks.tukra.feature.vendor

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.xsparks.tukra.core.designsystem.Butter
import com.xsparks.tukra.core.designsystem.Danger
import com.xsparks.tukra.core.designsystem.Ink
import com.xsparks.tukra.core.designsystem.InkSoft
import com.xsparks.tukra.core.designsystem.OutlineWarm
import com.xsparks.tukra.core.designsystem.SoraFamily
import com.xsparks.tukra.core.designsystem.Success
import com.xsparks.tukra.core.designsystem.TukraPink
import com.xsparks.tukra.core.designsystem.components.PrimaryButton
import com.xsparks.tukra.core.designsystem.components.SecondaryButton
import com.xsparks.tukra.core.network.VerifyQrResponse
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun VendorVerifyQrDialog(
    onDismiss: () -> Unit,
    onVerifyToken: suspend (String) -> Result<VerifyQrResponse>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var manualToken by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var verifyResult by remember { mutableStateOf<VerifyQrResponse?>(null) }
    var verifyError by remember { mutableStateOf<String?>(null) }
    var useCamera by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) useCamera = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Verify Pickup QR",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Ink
                        )
                        Text(
                            text = "क्यूआर कोड सत्यापित करें",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Ink)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // If result available: show Verification Banner
                if (verifyResult != null) {
                    val res = verifyResult!!
                    val order = res.order
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Success.copy(alpha = 0.12f))
                            .border(1.5.dp, Success, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Order Verified! (सत्यापित)",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Success
                            )
                            if (order != null) {
                                Text(
                                    text = "Code: ${order.code}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Ink
                                )
                                Text(
                                    text = "Student: ${order.studentFirstName ?: "Customer"}",
                                    fontSize = 14.sp,
                                    color = InkSoft
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Hand over items:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                order.items.forEach { item ->
                                    Text(
                                        text = "• ${item.qty}x ${item.name}",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = Ink
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Verify Another Order",
                        onClick = {
                            verifyResult = null
                            manualToken = ""
                            verifyError = null
                        }
                    )
                } else if (verifyError != null) {
                    // Error banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Danger.copy(alpha = 0.12f))
                            .border(1.5.dp, Danger, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = Danger,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Verification Failed",
                                fontFamily = SoraFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Danger
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = verifyError!!,
                                fontSize = 13.sp,
                                color = Ink,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "गलत आउटलेट या पहले ही ले लिया गया है",
                                fontSize = 12.sp,
                                color = InkSoft
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SecondaryButton(
                        text = "Try Again",
                        onClick = { verifyError = null }
                    )
                } else {
                    // Normal verification mode: Camera or manual input
                    if (useCamera && hasCameraPermission) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, OutlineWarm, RoundedCornerShape(16.dp))
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx)
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    val executor = Executors.newSingleThreadExecutor()

                                    cameraProviderFuture.addListener({
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.surfaceProvider = previewView.surfaceProvider
                                        }

                                        val scanner = BarcodeScanning.getClient()
                                        val imageAnalysis = ImageAnalysis.Builder()
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()

                                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null && !isVerifying) {
                                                val image = InputImage.fromMediaImage(
                                                    mediaImage,
                                                    imageProxy.imageInfo.rotationDegrees
                                                )
                                                scanner.process(image)
                                                    .addOnSuccessListener { barcodes ->
                                                        val rawVal = barcodes.firstOrNull()?.rawValue
                                                        if (!rawVal.isNullOrBlank() && !isVerifying) {
                                                            isVerifying = true
                                                            scope.launch {
                                                                val res = onVerifyToken(rawVal)
                                                                isVerifying = false
                                                                res.onSuccess {
                                                                    verifyResult = it
                                                                    useCamera = false
                                                                }.onFailure {
                                                                    verifyError = it.message ?: "Wrong outlet or already collected"
                                                                    useCamera = false
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .addOnCompleteListener {
                                                        imageProxy.close()
                                                    }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }

                                        try {
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                ctx as androidx.lifecycle.LifecycleOwner,
                                                CameraSelector.DEFAULT_BACK_CAMERA,
                                                preview,
                                                imageAnalysis
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))

                                    previewView
                                },
                                modifier = Modifier.fillMaxWidth().height(220.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        SecondaryButton(
                            text = "Switch to Manual Code Entry",
                            onClick = { useCamera = false }
                        )
                    } else {
                        // Manual Token Input
                        OutlinedTextField(
                            value = manualToken,
                            onValueChange = { manualToken = it },
                            label = { Text("Enter Pickup QR Token or Order Code") },
                            placeholder = { Text("e.g. q_tk4821_... or TK-4821") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TukraPink,
                                focusedLabelColor = Ink,
                                cursorColor = Ink
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            SecondaryButton(
                                text = "Open Camera Scanner",
                                onClick = {
                                    if (hasCameraPermission) {
                                        useCamera = true
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        PrimaryButton(
                            text = if (isVerifying) "Verifying..." else "Verify Order (सत्यापित करें)",
                            enabled = manualToken.isNotBlank() && !isVerifying,
                            onClick = {
                                isVerifying = true
                                scope.launch {
                                    val res = onVerifyToken(manualToken)
                                    isVerifying = false
                                    res.onSuccess {
                                        verifyResult = it
                                    }.onFailure {
                                        verifyError = it.message ?: "Wrong outlet or already collected"
                                    }
                                }
                            }
                        )
                    }

                    if (isVerifying) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = TukraPink, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
