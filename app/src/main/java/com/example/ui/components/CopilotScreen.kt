package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.WmsViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CopilotScreen(viewModel: WmsViewModel) {
    val messages by viewModel.copilotMessages.collectAsState()
    val isLoading by viewModel.copilotLoading.collectAsState()
    var userText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CopilotPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CopilotPurple)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "WMS Smart Copilot",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "SaaS Analytics & BC Assistant",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Clear chat action
            IconButton(onClick = { viewModel.clearCopilotHistory() }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = TextMuted)
            }
        }

        // --- Live Analytics Quick Action Chips ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Fast AI Queries",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            // FlowLayout/Row of interactive quick-intelligence items
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "AI Replenishment" to "Give me warehouse replenishment recommendation based on active inventory levels and safety stocks.",
                    "AI Slotting Map" to "Show slotting optimization recommendations to reduce traversals for high pick rates.",
                    "AL Extension Spec" to "What is the recommended custom AL Language Extension structure inside Dynamics 365 Business Central to sync these Logistics Units (Pallets/Boxes) and Entries?",
                    "WMS Heat-Map" to "Generate a simulated spatial heat-map of bin activity.",
                    "Inventory prediction" to "Run demand forecasting prediction and stock aging forecasts on DeWalt Power Drills."
                )

                suggestions.forEach { (label, promptText) ->
                    AssistChip(
                        onClick = { viewModel.askCopilot(promptText) },
                        label = { Text(label, fontSize = 11.sp, color = CopilotPurple) },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CopilotPurple, modifier = Modifier.size(12.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = CyberSlateSurface,
                            labelColor = CopilotPurple
                        ),
                        border = BorderStroke(1.dp, CopilotPurple.copy(alpha = 0.3f)),
                        modifier = Modifier.testTag("ai_chip_${label.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        // --- Chat Output Screen Area ---
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSlateSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages) { msg ->
                        val isAImessage = msg.sender == "AI"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isAImessage) Arrangement.Start else Arrangement.End
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAImessage) CyberSlateCard else CopilotPurple
                                ),
                                shape = RoundedCornerShape(
                                    topStart = if (isAImessage) 0.dp else 12.dp,
                                    topEnd = if (isAImessage) 12.dp else 0.dp,
                                    bottomStart = 12.dp,
                                    bottomEnd = 12.dp
                                ),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isAImessage) Icons.Default.AutoAwesome else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isAImessage) CyberTeal else Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAImessage) "Smart Copilot" else "You",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isAImessage) CyberTeal else Color.White
                                        )
                                    }

                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isAImessage) TextPrimary else Color.White,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    // Loading item
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CyberSlateCard),
                                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                                    modifier = Modifier.widthIn(max = 200.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = CopilotPurple,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Reasoning...",
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Input Controls Text Area ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userText,
                onValueChange = { userText = it },
                placeholder = { Text("Ask Copilot or type customized prompt...", color = TextMuted) },
                singleLine = false,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CopilotPurple,
                    unfocusedBorderColor = CyberSlateCard,
                    focusedContainerColor = CyberSlateSurface,
                    unfocusedContainerColor = CyberSlateSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("copilot_chat_input")
            )

            FloatingActionButton(
                onClick = {
                    if (userText.isNotBlank()) {
                        viewModel.askCopilot(userText)
                        userText = ""
                    }
                },
                containerColor = CopilotPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .testTag("submit_copilot_chat_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}
