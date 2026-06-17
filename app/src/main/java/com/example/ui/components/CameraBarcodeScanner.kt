@file:OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
package com.example.ui.components

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.CyberSlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraBarcodeScanner(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .testTag("camera_barcode_scanner_container")
    ) {
        if (cameraPermissionState.status.isGranted) {
            CameraPreviewAndAnalyzer(
                onBarcodeScanned = onBarcodeScanned,
                onDismiss = onDismiss
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(CyberSlateSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = CyberTeal,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "CAMERA ACCESS REQUIRED",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Grant permission to use your handheld terminal's hardware camera to scan and decode QR and EAN13 barcodes.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("grant_camera_permission_button")
                ) {
                    Text(
                        text = "GRANT CAMERA ACCESS",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F0E13)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_camera_permission_button")
                ) {
                    Text(text = "CANCEL & GO BACK", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun CameraPreviewAndAnalyzer(
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    
    // Scan line animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffsetPerc by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    // Trigger feedback flash when scanned
    var isScannedSuccessfully by remember { mutableStateOf(false) }

    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val barcodeAnalyzer = remember {
        BarcodeAnalyzer { parsedCode ->
            isScannedSuccessfully = true
            onBarcodeScanned(parsedCode)
        }
    }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderRef = cameraProvider
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, barcodeAnalyzer)
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                cameraControl = camera.cameraControl
            } catch (exc: Exception) {
                // Ignore config error
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Toggle Torch on state change
    LaunchedEffect(flashEnabled) {
        cameraControl?.enableTorch(flashEnabled)
    }

    DisposableEffect(cameraProviderRef) {
        onDispose {
            try {
                cameraProviderRef?.unbindAll()
            } catch (e: Exception) {
                // ignore
            }
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera live stream
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Bounded Scan scope & overlay mask
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Define targeted scan box region of interest
            val scanBoxSize = scanBoxDimension(canvasWidth, canvasHeight)
            val left = (canvasWidth - scanBoxSize) / 2f
            val top = (canvasHeight - scanBoxSize) / 2f
            
            // Transparent center frame cutout, opaque surrounding mask
            // Background darkened layer
            drawRect(
                color = Color.Black.copy(alpha = 0.45f),
                size = Size(canvasWidth, canvasHeight)
            )
            
            // Clear / Blend Mode or lighter shade on center scan ROI
            drawRect(
                color = Color.Black.copy(alpha = 0.1f),
                topLeft = Offset(left, top),
                size = Size(scanBoxSize, scanBoxSize)
            )

            // Dynamic futuristic grid corners/brackets outline
            val cornerLength = 32.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            
            // Top-Left corner
            drawLine(
                color = CyberTeal,
                start = Offset(left, top),
                end = Offset(left + cornerLength, top),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = CyberTeal,
                start = Offset(left, top),
                end = Offset(left, top + cornerLength),
                strokeWidth = strokeWidth
            )

            // Top-Right corner
            drawLine(
                color = CyberTeal,
                start = Offset(left + scanBoxSize, top),
                end = Offset(left + scanBoxSize - cornerLength, top),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = CyberTeal,
                start = Offset(left + scanBoxSize, top),
                end = Offset(left + scanBoxSize, top + cornerLength),
                strokeWidth = strokeWidth
            )

            // Bottom-Left corner
            drawLine(
                color = CyberTeal,
                start = Offset(left, top + scanBoxSize),
                end = Offset(left + cornerLength, top + scanBoxSize),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = CyberTeal,
                start = Offset(left, top + scanBoxSize),
                end = Offset(left, top + scanBoxSize - cornerLength),
                strokeWidth = strokeWidth
            )

            // Bottom-Right corner
            drawLine(
                color = CyberTeal,
                start = Offset(left + scanBoxSize, top + scanBoxSize),
                end = Offset(left + scanBoxSize - cornerLength, top + scanBoxSize),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = CyberTeal,
                start = Offset(left + scanBoxSize, top + scanBoxSize),
                end = Offset(left + scanBoxSize, top + scanBoxSize - cornerLength),
                strokeWidth = strokeWidth
            )

            // Moving horizontal neon-laser beam line
            val currentLaserY = top + (scanBoxSize * laserOffsetPerc)
            drawLine(
                color = Color(0xFFFF2443), // Laser red
                start = Offset(left + 8.dp.toPx(), currentLaserY),
                end = Offset(left + scanBoxSize - 8.dp.toPx(), currentLaserY),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Overlay text / layout info
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = CyberTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CAM SCANNER ACTIVE",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                        Text(
                            text = "Align QR/EAN13 Code",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                FilledIconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("dismiss_scan_camera_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close scanner")
                }
            }

            // Visual target hint text
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "BRING BARCODE CLOSER TO TARGET WINDOW",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = CyberTeal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            // Controls (Torch / Flash, Toggle, manual close help)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { flashEnabled = !flashEnabled },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (flashEnabled) CyberTeal else Color.Black.copy(alpha = 0.6f),
                        contentColor = if (flashEnabled) Color(0xFF0F0E13) else Color.White
                    ),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("scanner_toggle_torch_button")
                ) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight Toggle",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Green overlay splash when scanned successfully!
        AnimatedVisibility(
            visible = isScannedSuccessfully,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF34D399).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success beep",
                        tint = Color.White,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "DECODED SUCCESSFULLY",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Function to calculate exact scan scope bounds
private fun scanBoxDimension(width: Float, height: Float): Float {
    val sizeBase = minOf(width, height)
    return sizeBase * 0.65f
}

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)
    private var isScanningActive = true

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanningActive) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val firstBarcode = barcodes.first()
                        firstBarcode.rawValue?.let { rawValue ->
                            isScanningActive = false
                            onBarcodeScanned(rawValue)
                        }
                    }
                }
                .addOnFailureListener {
                    // Failures can happen during processing, just ignore them
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
