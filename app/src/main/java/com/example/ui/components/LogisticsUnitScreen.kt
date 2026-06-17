package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LogisticsUnit
import com.example.ui.theme.*
import com.example.ui.viewmodel.WmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticsUnitScreen(viewModel: WmsViewModel) {
    val logisticsUnits by viewModel.logisticsUnits.collectAsState()
    
    var unitTypeFilter by remember { mutableStateOf("ALL") } // ALL, PALLET, CONTAINER, BOX
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSplitDialog by remember { mutableStateOf(false) }
    var showMergeDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }

    // Forms Inputs
    var luId by remember { mutableStateOf("") }
    var luType by remember { mutableStateOf("PALLET") }
    var luBin by remember { mutableStateOf("") }
    var luDimensions by remember { mutableStateOf("120x80x120 cm") }
    var luWeight by remember { mutableStateOf("15.0") }

    // Split Inputs
    var splitSourceId by remember { mutableStateOf("") }
    var splitQty by remember { mutableStateOf("5") }
    var splitNewId by remember { mutableStateOf("") }

    // Merge Inputs
    var mergeSourceId by remember { mutableStateOf("") }
    var mergeTargetId by remember { mutableStateOf("") }

    // Move Inputs
    var moveSourceId by remember { mutableStateOf("") }
    var moveBin by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header and Actions Row ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Logistics Unit Management",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pallet, Container, Board & Nesting controls",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = {
                        luId = "PLT-${(1004..9999).random()}"
                        luType = "PALLET"
                        luBin = "BULK-01"
                        luDimensions = "120x80x140 cm"
                        showCreateDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_lu_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Text("Create LU", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Fast Action Grid for Supervisors ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Logistics Workflows",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showSplitDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSlateSurface),
                        modifier = Modifier.weight(1f).testTag("split_lu_btn")
                    ) {
                        Icon(Icons.Default.CallSplit, contentDescription = null, tint = SafetyGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Split Qty", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = { showMergeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSlateSurface),
                        modifier = Modifier.weight(1f).testTag("merge_lu_btn")
                    ) {
                        Icon(Icons.Default.CallMerge, contentDescription = null, tint = CyberTeal)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Merge Units", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = { showMoveDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSlateSurface),
                        modifier = Modifier.weight(1f).testTag("move_lu_btn")
                    ) {
                        Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = CopilotPurple)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Zone Move", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // --- Filter Tabs ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "PALLET", "CONTAINER", "BOX").forEach { filter ->
                    InputChip(
                        selected = unitTypeFilter == filter,
                        onClick = { unitTypeFilter = filter },
                        label = { Text(filter, color = if (unitTypeFilter == filter) Color.Black else TextPrimary) },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = CyberTeal,
                            containerColor = CyberSlateSurface
                        )
                    )
                }
            }
        }

        // --- Logistics units list ---
        val luFiltered = if (unitTypeFilter == "ALL") logisticsUnits else logisticsUnits.filter { it.type == unitTypeFilter }
        if (luFiltered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No logistics units matched active filter.", color = TextMuted)
                }
            }
        } else {
            items(luFiltered) { unit ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (unit.type) {
                                        "PALLET" -> Icons.Default.Grid3x3
                                        "CONTAINER" -> Icons.Default.AllInbox
                                        else -> Icons.Default.Inbox
                                    },
                                    contentDescription = null,
                                    tint = CyberTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = unit.unitNo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "[Barcode: ${unit.barcode}]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }

                            // Weight Badge
                            Text(
                                text = "${unit.weight} kg",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        // Dimensions & details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Bin: ${unit.binCode} • Dim: ${unit.dimensions}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                if (unit.parentUnitNo != null) {
                                    Text(
                                        text = "Nested inside Parent Unit: ${unit.parentUnitNo}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CopilotPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Dynamic inventory count badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberTeal.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Qty: ${unit.quantity}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Create Logistics Unit Modal Dialog ---
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Generate New Logistics Unit", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = luId,
                        onValueChange = { luId = it },
                        label = { Text("Logistics Unit No", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("PALLET", "CONTAINER").forEach { type ->
                            Button(
                                onClick = { luType = type },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (luType == type) CyberTeal else CyberSlateCard
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(type, color = if (luType == type) Color.Black else TextPrimary)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = luBin,
                        onValueChange = { luBin = it },
                        label = { Text("Initial Bin Placement", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = luDimensions,
                        onValueChange = { luDimensions = it },
                        label = { Text("Dimensions", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = luWeight,
                        onValueChange = { luWeight = it },
                        label = { Text("Weight (kg)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wt = luWeight.toDoubleOrNull() ?: 15.0
                        viewModel.createLogisticsUnit(
                            unitNo = luId.uppercase(),
                            barcode = luId.uppercase(),
                            type = luType,
                            warehouseCode = "MAIN",
                            binCode = luBin.uppercase(),
                            dimensions = luDimensions,
                            weight = wt
                        )
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal)
                ) {
                    Text("Confirm Registry", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberSlateSurface
        )
    }

    // --- Split Logistics Unit Dialog ---
    if (showSplitDialog) {
        AlertDialog(
            onDismissRequest = { showSplitDialog = false },
            title = { Text("Split Logistics Unit Quantities", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Isolate a partial batch from a parent unit into a separate physical pallet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = splitSourceId,
                        onValueChange = { splitSourceId = it },
                        label = { Text("Source LU No (e.g., PLT-1001)", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("split_source_input")
                    )

                    OutlinedTextField(
                        value = splitQty,
                        onValueChange = { splitQty = it },
                        label = { Text("Quantity to Split off", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("split_qty_input")
                    )

                    OutlinedTextField(
                        value = splitNewId,
                        onValueChange = { splitNewId = it },
                        label = { Text("New LU No (e.g., PLT-1005)", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("split_target_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = splitQty.toIntOrNull() ?: 1
                        viewModel.splitLogisticsUnit(
                            unitNo = splitSourceId.uppercase(),
                            splitQty = qty,
                            newUnitNo = splitNewId.uppercase()
                        )
                        showSplitDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                    modifier = Modifier.testTag("split_confirm_btn")
                ) {
                    Text("Execute Split", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSplitDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberSlateSurface
        )
    }

    // --- Merge Logistics Units Dialog ---
    if (showMergeDialog) {
        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            title = { Text("Merge Logistics Units", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Consolidate the quantities representing two pallets into a single container entity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = mergeSourceId,
                        onValueChange = { mergeSourceId = it },
                        label = { Text("Source LU No to Consolidate", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("merge_source_input")
                    )

                    OutlinedTextField(
                        value = mergeTargetId,
                        onValueChange = { mergeTargetId = it },
                        label = { Text("Target consolidated LU No", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("merge_target_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.mergeLogisticsUnits(
                            sourceNo = mergeSourceId.uppercase(),
                            targetNo = mergeTargetId.uppercase()
                        )
                        showMergeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                    modifier = Modifier.testTag("merge_confirm_btn")
                ) {
                    Text("Execute Consolidation", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMergeDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberSlateSurface
        )
    }

    // --- Zone Move Dialog ---
    if (showMoveDialog) {
        AlertDialog(
            onDismissRequest = { showMoveDialog = false },
            title = { Text("Zone Movement Transfer", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Transfer an entire logistics unit (Pallet/Box) to another zone and shelf location.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = moveSourceId,
                        onValueChange = { moveSourceId = it },
                        label = { Text("LU No to Move (e.g., PLT-1002)", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("move_lu_input")
                    )

                    OutlinedTextField(
                        value = moveBin,
                        onValueChange = { moveBin = it },
                        label = { Text("Destination Bin Code", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberTeal, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("move_bin_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.moveLogisticsUnit(
                            unitNo = moveSourceId.uppercase(),
                            destinationBin = moveBin.uppercase()
                        )
                        showMoveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                    modifier = Modifier.testTag("move_confirm_btn")
                ) {
                    Text("Execute Movement", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoveDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberSlateSurface
        )
    }
}
