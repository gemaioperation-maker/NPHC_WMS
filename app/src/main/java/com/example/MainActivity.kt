package com.example

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.components.DashboardScreen
import com.example.ui.components.WorksheetScreen
import com.example.ui.components.LogisticsUnitScreen
import com.example.ui.components.CopilotScreen
import com.example.ui.theme.*
import androidx.compose.ui.graphics.Color
import com.example.ui.viewmodel.WmsViewModel

class MainActivity : ComponentActivity() {
    
    private val wmsViewModel: WmsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            val intent = Intent(this, com.example.data.sync.BackgroundSyncService::class.java)
            startService(intent)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to start BackgroundSyncService: ${e.message}")
        }

        setContent {
            MyApplicationTheme {
                var selectedBottomTab by remember { mutableStateOf(0) }
                var opsDocumentInitialTab by remember { mutableStateOf("RECEIPT") }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("app_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = selectedBottomTab == 0,
                                onClick = { selectedBottomTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedBottomTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                        contentDescription = "Dashboard"
                                    )
                                },
                                label = { Text("KPI Hub") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF381E72),
                                    selectedTextColor = CyberTeal,
                                    indicatorColor = CyberTeal,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_dashboard")
                            )

                            NavigationBarItem(
                                selected = selectedBottomTab == 1,
                                onClick = { selectedBottomTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedBottomTab == 1) Icons.Filled.AssignmentTurnedIn else Icons.Outlined.AssignmentTurnedIn,
                                        contentDescription = "Worksheets"
                                    )
                                },
                                label = { Text("Worksheets") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF381E72),
                                    selectedTextColor = CyberTeal,
                                    indicatorColor = CyberTeal,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_worksheets")
                            )

                            NavigationBarItem(
                                selected = selectedBottomTab == 2,
                                onClick = { selectedBottomTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedBottomTab == 2) Icons.Filled.Inbox else Icons.Outlined.Inbox,
                                        contentDescription = "Logistics"
                                    )
                                },
                                label = { Text("Logistics") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF381E72),
                                    selectedTextColor = CyberTeal,
                                    indicatorColor = CyberTeal,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_logistics")
                            )

                            NavigationBarItem(
                                selected = selectedBottomTab == 3,
                                onClick = { selectedBottomTab = 3 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedBottomTab == 3) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                        contentDescription = "Copilot"
                                    )
                                },
                                label = { Text("Copilot AI") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF381E72),
                                    selectedTextColor = CyberTeal,
                                    indicatorColor = CyberTeal,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_copilot")
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (selectedBottomTab) {
                            0 -> DashboardScreen(
                                viewModel = wmsViewModel,
                                onNavigateToOps = { initialType ->
                                    opsDocumentInitialTab = initialType
                                    selectedBottomTab = 1
                                }
                            )
                            1 -> WorksheetScreen(
                                viewModel = wmsViewModel,
                                initialTab = opsDocumentInitialTab
                            )
                            2 -> LogisticsUnitScreen(
                                viewModel = wmsViewModel
                            )
                            3 -> CopilotScreen(
                                viewModel = wmsViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
