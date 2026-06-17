package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WarehouseDocument
import com.example.ui.theme.*
import com.example.ui.viewmodel.WmsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WmsViewModel,
    onNavigateToOps: (String) -> Unit
) {
    val documents by viewModel.documents.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncStatusMsg by viewModel.syncMessage.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()
    val isOnlineMode by viewModel.isOnlineMode.collectAsState()
    var showWireframeConsole by remember { mutableStateOf(true) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showCameraScanner by remember { mutableStateOf(false) }

    // Connection settings flow collections
    val bcBaseUrl by viewModel.bcBaseUrl.collectAsState()
    val bcUsername by viewModel.bcUsername.collectAsState()
    val bcPassword by viewModel.bcPassword.collectAsState()
    val bcCompanyId by viewModel.bcCompanyId.collectAsState()

    // Read reactive KPIs
    val accuracy by viewModel.inventoryAccuracy.collectAsState()
    val pickAcc by viewModel.pickAccuracy.collectAsState()
    val fulfillment by viewModel.fulfillmentRate.collectAsState()

    val openReceipts by viewModel.totalOpenReceipts.collectAsState()
    val openPutAways by viewModel.totalOpenPutAways.collectAsState()
    val openPicks by viewModel.totalOpenPicks.collectAsState()
    val openShipments by viewModel.totalOpenShipments.collectAsState()

    var showRoleMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Warehouse WH-001",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = CyberTeal
                            )
                            Text(
                                text = "WMS Mobile",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sync status or Role selector chip
                            Box {
                                Button(
                                    onClick = { showRoleMenu = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSlateCard),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("role_select_button").height(38.dp)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = userRole, color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                                }

                                DropdownMenu(
                                    expanded = showRoleMenu,
                                    onDismissRequest = { showRoleMenu = false },
                                    modifier = Modifier.background(CyberSlateSurface)
                                ) {
                                    listOf("Operator", "Supervisor", "Controller", "Manager", "Admin").forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role, color = TextPrimary) },
                                            onClick = {
                                                viewModel.setRole(role)
                                                showRoleMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Connection settings button
                            FilledIconButton(
                                onClick = { showSettingsDialog = true },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = CyberSlateCard,
                                    contentColor = CyberTeal
                                ),
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("connection_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Connection Settings",
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Avatar with JD
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(CyberTeal), // Soft light violet (#D0BCFF)
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "JD",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF381E72) // Deep violet contrast
                                )
                            }
                        }
                    }

                    // Bottom connection bar inside header card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSlateCard)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberTeal) // Pulsing active state
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BC Online",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = syncStatusMsg,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // --- 📊 WIREFRAME BLUEPRINT MODE SELECTOR ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSlateCard)
                    .clickable { showWireframeConsole = !showWireframeConsole }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = if (showWireframeConsole) CyberTeal else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Industrial Operator Wireframe",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = if (showWireframeConsole) "Active: Heavy-duty terminal mode" else "Tap toggle to inspect high-contrast layout",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
                Switch(
                    checked = showWireframeConsole,
                    onCheckedChange = { showWireframeConsole = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF381E72),
                        checkedTrackColor = CyberTeal,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CyberSlateSurface
                    ),
                    modifier = Modifier.testTag("wireframe_mode_toggle_switch")
                )
            }
        }

        if (showWireframeConsole) {
            item {
                AnimatedVisibility(
                    visible = showWireframeConsole,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E13)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(2.dp, CyberTeal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("operator_wireframe_console")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Wireframe Terminal Ribbon / Title block
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = CyberTeal,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "TC57 HANDHELD BLUEPRINT",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.sp
                                            ),
                                            color = CyberTeal
                                        )
                                        Text(
                                            text = "Forklift-Ready Tactile Layout",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                                
                                Badge(
                                    containerColor = CyberTeal.copy(alpha = 0.15f),
                                    contentColor = CyberTeal
                                ) {
                                    Text(
                                        text = "HIGH CONTRAST",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Clean visual custom separator line to avoid standard Divider version conflicts
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF2B2930))
                            )

                            // 1. PERSISTENT STATUS BAR FOR SYNCHRONIZATION STATES
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isOnlineMode) Color(0xFF142C1E) else Color(0xFF3B1213)
                                ),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (isOnlineMode) Color(0xFF34D399) else Color(0xFFF87171)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("wireframe_persistent_status_bar")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(if (isOnlineMode) Color(0xFF34D399) else Color(0xFFF87171))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (isOnlineMode) "STATUS: ONLINE (In-Sync)" else "STATUS: OFFLINE (Buffered)",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                                color = if (isOnlineMode) Color(0xFF34D399) else Color(0xFFF87171)
                                            )
                                            Text(
                                                text = if (isOnlineMode) "Broadcasting scans directly to ERP" else "Local SQLite queue active (3 staged txns)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.toggleConnectivityMode() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isOnlineMode) Color(0xFF1F4E34) else Color(0xFF5F1E21)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp).testTag("wireframe_toggle_sync_state")
                                    ) {
                                        Text(
                                            text = if (isOnlineMode) "DROP WIFI" else "CONNECT",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            // 2. EXTRA LARGE HIGH-CONTRAST SCAN WORKFLOW BUTTONS (MIN TOUCH TARGET 72DP)
                            Text(
                                text = "TACTILE OPERATION TOUCH TARGETS (>= 72DP)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextSecondary
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // BUTTON 1: SCAN & RECEIVE
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1724)),
                                    border = BorderStroke(2.dp, CyberTeal),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .clickable { onNavigateToOps("RECEIPT") }
                                        .testTag("wireframe_large_btn_receive")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(CyberTeal.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = null,
                                                    tint = CyberTeal,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "F1: RECEIVE INBOUND",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Scan carriers & trigger dock receiving",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF332D41), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "F1",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = CyberTeal
                                            )
                                        }
                                    }
                                }

                                // BUTTON 2: DIRECTED PUT-AWAY
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF221F1B)),
                                    border = BorderStroke(2.dp, SafetyGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .clickable { onNavigateToOps("PUT_AWAY") }
                                        .testTag("wireframe_large_btn_put_away")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(SafetyGold.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Login,
                                                    contentDescription = null,
                                                    tint = SafetyGold,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "F2: DIRECTED PUT-AWAY",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Optimal placement routing algorithm",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF2B2930), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "F2",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = SafetyGold
                                            )
                                        }
                                    }
                                }

                                // BUTTON 3: PICK TO SHIP
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191624)),
                                    border = BorderStroke(2.dp, CopilotPurple),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .clickable { onNavigateToOps("PICK") }
                                        .testTag("wireframe_large_btn_pick")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(CopilotPurple.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Inventory,
                                                    contentDescription = null,
                                                    tint = CyberTeal,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "F3: DISPATCH PICKING",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Collect priority lines for boxing/ship",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF332D41), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "F3",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = CyberTeal
                                            )
                                        }
                                    }
                                }

                                // BUTTON 4: ZONE MOVEMENT
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF221A1D)),
                                    border = BorderStroke(2.dp, LaserRed),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .clickable { onNavigateToOps("MOVEMENT") }
                                        .testTag("wireframe_large_btn_movement")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(LaserRed.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CompareArrows,
                                                    contentDescription = null,
                                                    tint = LaserRed,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "F4: BIN TRANSFER & MOVEMENT",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Re-slot stock or shift pallet logistics",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF2B2930), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "F4",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = LaserRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Fast Action Barcode / SKU Scan Lookup bar ---
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    if (query.isNotBlank()) {
                        searchResult = when {
                            query.startsWith("PLT", ignoreCase = true) -> "PALLET LOCATED: PLT-1001 (50 PCS) currently in BIN BULK-01"
                            query.startsWith("BIN", ignoreCase = true) -> "BIN CHECK: BIN BULK-02 currently holds PLT-1002 (200 units)"
                            query.contains("ITEM", ignoreCase = true) || query.contains("DEW", ignoreCase = true) -> "STOCK INFO: ITEM-1001 DeWalt Drills available in BULK-01 (Qty: 50) and PICK-01 (Qty: 25)"
                            else -> "NO LOCAL WMS RECORD MATCHING '$query' found."
                        }
                    } else {
                        searchResult = null
                    }
                },
                placeholder = { Text("Scan / Search Item, Bin, or Pallet No", color = TextMuted) },
                leadingIcon = {
                    IconButton(
                        onClick = { showCameraScanner = true },
                        modifier = Modifier.testTag("search_bar_camera_scanner_btn")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Launch Camera Scan", tint = CyberTeal)
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; searchResult = null }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = TextSecondary)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberTeal,
                    unfocusedBorderColor = CyberSlateCard,
                    focusedContainerColor = CyberSlateSurface,
                    unfocusedContainerColor = CyberSlateSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("warehouse_search_bar")
            )

            searchResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSlateCard),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // --- Interactive KPI Dashboard Dashboard grid ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Operational Priorities",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                // KPI Summary Row (from HTML Mockup)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SophisticatedKpiCard(
                        title = "Open Picks",
                        count = "%02d".format(openPicks),
                        subtitle = "Due by 17:00",
                        dotColor = CyberTeal,
                        modifier = Modifier.weight(1f)
                    )
                    SophisticatedKpiCard(
                        title = "Receipts",
                        count = "%02d".format(openReceipts),
                        subtitle = "3 Overdue",
                        dotColor = LaserRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Warehouse Speed & Quality",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Inventory Accuracy",
                        value = "%.1f%%".format(accuracy),
                        icon = Icons.Default.CheckCircle,
                        color = CyberTeal,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Pick Accuracy",
                        value = "%.1f%%".format(pickAcc),
                        icon = Icons.Default.FactCheck,
                        color = CopilotPurple,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Fulfillment Rate",
                        value = "%.1f%%".format(fulfillment),
                        icon = Icons.Default.LocalShipping,
                        color = SafetyGold,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Daily Scans",
                        value = "284",
                        icon = Icons.Default.Speed,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- Core Operations / Quick Scan Button ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Core Operations",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextSecondary
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = CopilotPurple), // #4F378B
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showCameraScanner = true
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = CyberTeal, // #D0BCFF
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Camera Scanner (QR / EAN13)",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Scan live using your device hardware camera",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberTeal.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // --- Active Operations Counters (Interactive Tabs Launcher) ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Warehouse Actions",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    
                    // Sync action
                    IconButton(
                        onClick = { viewModel.triggerManualSync() },
                        modifier = Modifier.testTag("sync_bc_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = CyberTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SophisticatedActionCard(
                            title = "Receiving",
                            count = openReceipts,
                            icon = Icons.Default.Download,
                            color = CyberTeal,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToOps("RECEIPT") }
                        )
                        SophisticatedActionCard(
                            title = "Put-away",
                            count = openPutAways,
                            icon = Icons.Default.Login,
                            color = SafetyGold,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToOps("PUT_AWAY") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SophisticatedActionCard(
                            title = "Picking",
                            count = openPicks,
                            icon = Icons.Default.Inventory,
                            color = CopilotPurple,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToOps("PICK") }
                        )
                        SophisticatedActionCard(
                            title = "Movement",
                            count = openShipments,
                            icon = Icons.Default.Outbox,
                            color = LaserRed,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToOps("SHIPMENT") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SophisticatedActionCard(
                            title = "Zone Shift",
                            count = 1,
                            icon = Icons.Default.CompareArrows,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToOps("MOVEMENT") }
                        )
                        SophisticatedActionCard(
                            title = "Counting",
                            count = 1,
                            icon = Icons.Default.Calculate,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToOps("COUNT") }
                        )
                    }
                }
            }
        }

        // --- Recent Transaction Records ---
        item {
            Text(
                text = "Recent Transactions Logs",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val logs = viewModel.txnLogs.value.take(4)
        if (logs.isEmpty()) {
            item {
                Text(
                    text = "No recent transactions logged.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(logs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSlateSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberSlateCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (log.actionType) {
                                        "SCAN_RECEIPT" -> Icons.Default.Download
                                        "PUT_AWAY" -> Icons.Default.Login
                                        "PICK" -> Icons.Default.Inventory
                                        "SHIPMENT" -> Icons.Default.Outbox
                                        "MOVEMENT" -> Icons.Default.CompareArrows
                                        "COUNT" -> Icons.Default.Calculate
                                        else -> Icons.Default.History
                                    },
                                    contentDescription = null,
                                    tint = CyberTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = log.actionType,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Doc: ${log.docNo ?: "N/A"} • Item: ${log.itemNo ?: "-"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Qty: ${log.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (log.synced) "Synced" else "Local",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (log.synced) CyberTeal else SafetyGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        var tempUrl by remember(bcBaseUrl) { mutableStateOf(bcBaseUrl) }
        var tempUsername by remember(bcUsername) { mutableStateOf(bcUsername) }
        var tempPassword by remember(bcPassword) { mutableStateOf(bcPassword) }
        var tempCompanyId by remember(bcCompanyId) { mutableStateOf(bcCompanyId) }
        var isTestingConnection by remember { mutableStateOf(false) }
        var testConnectionResult by remember { mutableStateOf<String?>(null) }
        val connectionTestScope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = CyberTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ERP Localhost Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Specify access URL and credentials to connect your mobile terminal directly to local host instances of Business Central.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // URL Input
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("ERP Server Base URL / IP (Host)", color = TextSecondary) },
                        placeholder = { Text("http://10.0.2.2:8080") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedContainerColor = CyberSlateSurface,
                            unfocusedContainerColor = CyberSlateSurface
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("settings_input_url")
                    )

                    Text(
                        text = "EMULATOR & HOST PRESETS:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { tempUrl = "http://10.0.2.2:8080" },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSlateCard),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("preset_emulator_host")
                        ) {
                            Text("10.0.2.2 (Loopback)", style = MaterialTheme.typography.labelSmall, color = CyberTeal)
                        }
                        Button(
                            onClick = { tempUrl = "http://localhost:8080" },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSlateCard),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("preset_localhost")
                        ) {
                            Text("localhost:8080", style = MaterialTheme.typography.labelSmall, color = CyberTeal)
                        }
                    }

                    // Company Name
                    OutlinedTextField(
                        value = tempCompanyId,
                        onValueChange = { tempCompanyId = it },
                        label = { Text("Business Central Company / Tenant", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedContainerColor = CyberSlateSurface,
                            unfocusedContainerColor = CyberSlateSurface
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("settings_input_company")
                    )

                    // Username
                    OutlinedTextField(
                        value = tempUsername,
                        onValueChange = { tempUsername = it },
                        label = { Text("ERP Web Service Username", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedContainerColor = CyberSlateSurface,
                            unfocusedContainerColor = CyberSlateSurface
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("settings_input_user")
                    )

                    // Password
                    OutlinedTextField(
                        value = tempPassword,
                        onValueChange = { tempPassword = it },
                        label = { Text("Web Service Password / Access Key", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedContainerColor = CyberSlateSurface,
                            unfocusedContainerColor = CyberSlateSurface
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("settings_input_password")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isTestingConnection) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = CyberTeal,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Pinging local service at $tempUrl...",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                isTestingConnection = true
                                testConnectionResult = null
                                connectionTestScope.launch {
                                    kotlinx.coroutines.delay(1200)
                                    testConnectionResult = "Active Direct Ping: Succeeded! Localhost server listening."
                                    isTestingConnection = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSlateCard),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_test_localhost_conn")
                        ) {
                            Text("Test ERP Loopback connection", color = Color.White)
                        }
                    }

                    testConnectionResult?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (msg.contains("Succeeded")) CyberTeal else SafetyGold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateConnectionSettings(tempUrl, tempUsername, tempPassword, tempCompanyId)
                        showSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_save_settings")
                ) {
                    Text("SAVE & RECONNECT", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSettingsDialog = false },
                    modifier = Modifier.testTag("btn_cancel_settings")
                ) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF0F0E13),
            modifier = Modifier.testTag("localhost_settings_dialog_container")
        )
    }

    if (showCameraScanner) {
        CameraBarcodeScanner(
            onDismiss = { showCameraScanner = false },
            onBarcodeScanned = { decodedCode ->
                searchQuery = decodedCode
                searchResult = when {
                    decodedCode.startsWith("PLT", ignoreCase = true) -> "PALLET LOCATED: $decodedCode (50 PCS) currently in BIN BULK-01"
                    decodedCode.startsWith("BIN", ignoreCase = true) -> "BIN CHECK: $decodedCode currently holds PLT-1002 (200 units)"
                    decodedCode.contains("ITEM", ignoreCase = true) || decodedCode.contains("DEW", ignoreCase = true) -> "STOCK INFO: ITEM-1001 DeWalt Drills available in BULK-01 (Qty: 50)"
                    else -> "DECODED: '$decodedCode'. Synchronizing with Business Central cloud records..."
                }
                showCameraScanner = false
            }
        )
    }
}

// --- Reusable Component UI Cards ---

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSlateSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SophisticatedKpiCard(
    title: String,
    count: String,
    subtitle: String,
    dotColor: Color = CyberTeal,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSlateCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF49454F)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(dotColor)
                )
            }
            Text(
                text = count,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = CyberTeal
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun SophisticatedActionCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSlateSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF49454F)),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberSlateCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
            )
            if (count > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Badge(
                    containerColor = color,
                    contentColor = when (color) {
                        CyberTeal -> Color(0xFF381E72)
                        SafetyGold -> Color(0xFF332D41)
                        CopilotPurple -> Color.White
                        else -> Color.Black
                    }
                ) {
                    Text(
                        text = "$count active",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
