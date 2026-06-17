package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WarehouseDocLine
import com.example.data.model.WarehouseDocument
import com.example.ui.theme.*
import com.example.ui.viewmodel.WmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetScreen(
    viewModel: WmsViewModel,
    initialTab: String = "RECEIPT"
) {
    val documents by viewModel.documents.collectAsState()
    val activeDoc by viewModel.activeDocument.collectAsState()
    val activeLines by viewModel.activeDocLines.collectAsState()
    val selectedTab by viewModel.selectedDocTypeTab.collectAsState()

    var showScanDialog by remember { mutableStateOf(false) }
    var selectedLineForScan by remember { mutableStateOf<WarehouseDocLine?>(null) }
    
    // Scan entry values
    var scanBarcode by remember { mutableStateOf("") }
    var scanQty by remember { mutableStateOf("1") }
    var scanBin by remember { mutableStateOf("") }
    var scanLU by remember { mutableStateOf("") }
    var scanError by remember { mutableStateOf<String?>(null) }

    // Receipt specific deliverables
    var showSignatureSheet by remember { mutableStateOf(false) }
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }

    // Sync tab when initialTab is updated
    LaunchedEffect(initialTab) {
        viewModel.setTab(initialTab)
    }

    if (activeDoc == null) {
        // --- Document Selection List View ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Warehouse Worksheets",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            // Horizontal Tab Row
            ScrollableTabRow(
                selectedTabIndex = when (selectedTab) {
                    "RECEIPT" -> 0
                    "PUT_AWAY" -> 1
                    "PICK" -> 2
                    "SHIPMENT" -> 3
                    "MOVEMENT" -> 4
                    "COUNT" -> 5
                    else -> 0
                },
                containerColor = CyberSlateSurface,
                contentColor = CyberTeal,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                listOf("RECEIPT", "PUT_AWAY", "PICK", "SHIPMENT", "MOVEMENT", "COUNT").forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.setTab(tab) },
                        text = {
                            Text(
                                text = tab.replace("_", " "),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Document Cards List
            val filteredDocs = documents.filter { it.type == selectedTab }
            if (filteredDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No pending worksheets for $selectedTab",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDocs) { doc ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSlateSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectDocument(doc) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = doc.docNo,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTeal
                                    )

                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (doc.status) {
                                                    "OPEN" -> SafetyGold.copy(alpha = 0.15f)
                                                    "IN_PROGRESS" -> CyberTeal.copy(alpha = 0.15f)
                                                    "COMPLETED" -> CopilotPurple.copy(alpha = 0.15f)
                                                    else -> CyberTeal.copy(alpha = 0.25f)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = doc.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when (doc.status) {
                                                "OPEN" -> SafetyGold
                                                "IN_PROGRESS" -> CyberTeal
                                                "COMPLETED" -> CopilotPurple
                                                else -> CyberTeal
                                            }
                                        )
                                    }
                                }

                                Text(
                                    text = "Source: ${doc.sourceType} (${doc.sourceNo})",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Assignee: ${doc.assignedUser}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Open Worksheet",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyberTeal,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = CyberTeal,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- Document Scanning Worksheet Details View ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / Back navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectDocument(null) }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeDoc!!.docNo,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[${activeDoc!!.type}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberTeal
                        )
                    }
                    Text(
                        text = "Source: ${activeDoc!!.sourceNo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Header Fast Post
                Button(
                    onClick = {
                        if (activeDoc!!.type == "RECEIPT") {
                            showSignatureSheet = true
                        } else {
                            viewModel.postDocument(activeDoc!!.docNo, null, null)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                    shape = RoundedCornerShape(8.dp),
                    enabled = activeDoc!!.status != "COMPLETED" && activeDoc!!.status != "SYNCED",
                    modifier = Modifier.testTag("post_bc_doc_button")
                ) {
                    Text("Post ERP", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // Attached deliverables display if available
            if (activeDoc!!.signatureData != null || activeDoc!!.photoPath != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSlateSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (activeDoc!!.signatureData != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gesture, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sig Captured", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }
                        }
                        if (activeDoc!!.photoPath != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = SafetyGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Photo Attached", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Line items worksheets list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeLines) { line ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (line.scannedQty == line.expectedQty) CyberSlateCard else CyberSlateSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (line.scannedQty == line.expectedQty) CyberTeal.copy(alpha = 0.5f) else Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = line.itemName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Item: ${line.itemNo} • Barcode: ${line.barcode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }

                                // Interactive Scan Trigger
                                if (activeDoc!!.status != "COMPLETED" && activeDoc!!.status != "SYNCED") {
                                    Button(
                                        onClick = {
                                            selectedLineForScan = line
                                            scanBarcode = ""
                                            scanQty = "1"
                                            scanBin = line.suggestedBin ?: ""
                                            scanLU = line.scannedLogisticsUnit ?: ""
                                            scanError = null
                                            showScanDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberSlateCard),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.testTag("scan_line_button_${line.itemNo}")
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Scan", color = TextPrimary, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            // Progressive Scanning bars
                            val progress = if (line.expectedQty > 0) line.scannedQty.toFloat() / line.expectedQty.toFloat() else 0f
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Scanned: ${line.scannedQty} / ${line.expectedQty} ${line.uom}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (line.scannedQty == line.expectedQty) CyberTeal else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = progress.coerceIn(0f, 1f),
                                    color = if (line.scannedQty == line.expectedQty) CyberTeal else SafetyGold,
                                    trackColor = CyberSlateCard,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }

                            // Scanned Location details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = SafetyGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Suggested Bin: ${line.suggestedBin ?: "None"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }

                                if (line.confirmedBin != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Bin: ${line.confirmedBin}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyberTeal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (line.scannedLogisticsUnit != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Grid3x3, contentDescription = null, tint = CopilotPurple, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Logistics Unit: ${line.scannedLogisticsUnit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CopilotPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Scan entry if any lines left
            if (activeDoc!!.status != "COMPLETED" && activeDoc!!.status != "SYNCED") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSlateSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Or attach dynamic supplier deliveries photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Button(
                            onClick = {
                                capturedPhotoPath = "SUPPLIER_PHOTO_PO_SIGNATURE_SHEET"
                                viewModel.postDocument(activeDoc!!.docNo, activeDoc!!.signatureData, "photo_po_delivery.jpg")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSlateCard),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = SafetyGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulate Photo Attachment Capture", color = TextPrimary)
                        }
                    }
                }
            }
        }
    }

    // --- Digit Signature Modal Capture Tool ---
    if (showSignatureSheet) {
        AlertDialog(
            onDismissRequest = { showSignatureSheet = false },
            title = { Text("Sign and Confirm Purchase Delivery", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Draw supplier's signature below to audit delivery of PO items and post to D365 Business Central ledger.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Touch Canvas Signature Pad
                    var signaturePath by remember { mutableStateOf(Path()) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF211F26))
                            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        signaturePath.moveTo(offset.x, offset.y)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        signaturePath.lineTo(change.position.x, change.position.y)
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawPath(
                                path = signaturePath,
                                color = CyberTeal,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        
                        Text(
                            text = "Touch Screen to Register Signature",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                            color = TextMuted,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        )
                    }

                    Button(
                        onClick = { signaturePath = Path() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSlateCard),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear Signature", color = TextPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignatureSheet = false
                        viewModel.postDocument(activeDoc!!.docNo, "SVG_CAPTURED_SIGNATURE", capturedPhotoPath)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal)
                ) {
                    Text("Confirm & Post", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignatureSheet = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberSlateSurface
        )
    }

    // --- Barcode Input and Scan Verification Dialog ---
    if (showScanDialog && selectedLineForScan != null) {
        val line = selectedLineForScan!!
        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            title = { Text("RF Scan Verification: ${line.itemName}", color = TextPrimary) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Text(
                            text = "Scan either the item SKU, its barcode [${line.barcode}], or map it into a logistics container (pallet).",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = scanBarcode,
                            onValueChange = { scanBarcode = it },
                            label = { Text("Scan / Type SKU Barcode", color = TextSecondary) },
                            placeholder = { Text(line.barcode, color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberTeal,
                                focusedContainerColor = CyberSlateCard,
                                unfocusedContainerColor = CyberSlateCard
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("scan_barcode_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = scanQty,
                            onValueChange = { scanQty = it },
                            label = { Text("Scanned Quantity", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberTeal,
                                focusedContainerColor = CyberSlateCard,
                                unfocusedContainerColor = CyberSlateCard
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("scan_qty_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = scanBin,
                            onValueChange = { scanBin = it },
                            label = { Text("Confirm Bin Code", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SafetyGold,
                                focusedContainerColor = CyberSlateCard,
                                unfocusedContainerColor = CyberSlateCard
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("scan_bin_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = scanLU,
                            onValueChange = { scanLU = it },
                            label = { Text("Confirm Logistics Unit (Pallet/Box)", color = TextSecondary) },
                            placeholder = { Text("PLT-1001", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CopilotPurple,
                                focusedContainerColor = CyberSlateCard,
                                unfocusedContainerColor = CyberSlateCard
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("scan_lu_input")
                        )
                    }

                    // Simulated quick scan buttons for fast user interaction inside Emulator
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { scanBarcode = line.barcode },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberSlateCard),
                                modifier = Modifier.weight(1f).testTag("quick_barcode_scan_btn")
                            ) {
                                Text("Auto Scan Barcode", style = MaterialTheme.typography.labelSmall, color = CyberTeal)
                            }
                            Button(
                                onClick = { scanQty = line.expectedQty.toString() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberSlateCard),
                                modifier = Modifier.weight(1f).testTag("quick_qty_scan_btn")
                            ) {
                                Text("Set Total Qty", style = MaterialTheme.typography.labelSmall, color = SafetyGold)
                            }
                        }
                    }

                    scanError?.let { err ->
                        item {
                            Text(text = err, color = LaserRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = scanQty.toIntOrNull() ?: 0
                        if (qty <= 0) {
                            scanError = "Please enter a valid scanned quantity."
                            return@Button
                        }
                        if (scanBarcode.isNotEmpty() && scanBarcode != line.barcode && scanBarcode != line.itemNo) {
                            scanError = "RF Scanner Mismatch! Expected item barcode '${line.barcode}' or SKU '${line.itemNo}'"
                            return@Button
                        }
                        if (scanBin.isBlank()) {
                            scanError = "A bin code is required to confirm inventory location."
                            return@Button
                        }

                        // Success scan submission
                        viewModel.submitScanAndVerify(
                            docNo = line.docNo,
                            lineId = line.id,
                            itemNo = line.itemNo,
                            scannedQty = qty,
                            binCode = scanBin.uppercase(),
                            unitNo = if (scanLU.isNotBlank()) scanLU.uppercase() else null
                        )
                        showScanDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                    modifier = Modifier.testTag("scan_verify_confirm_button")
                ) {
                    Text("Register & Verify", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberSlateSurface
        )
    }
}
