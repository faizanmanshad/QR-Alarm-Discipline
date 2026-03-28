package com.example.qralarm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qralarm.data.Alarm
import com.example.qralarm.ui.viewmodel.AlarmViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(viewModel: AlarmViewModel) {
    val alarms by viewModel.allAlarms.collectAsState(initial = emptyList())
    var currentLiveTime by remember { mutableStateOf("") }
    var currentDayDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val timeFormatter = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            val dayDateFormatter = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
            val now = Date()
            currentLiveTime = timeFormatter.format(now)
            currentDayDate = dayDateFormatter.format(now)
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    // 🚨 NEW: Added Emoji to the Title
                    title = { Text("My Alarms ⏰", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.startNewAlarm() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Alarm")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentDayDate,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentLiveTime,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (alarms.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No alarms set", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(alarms) { alarm ->
                            AlarmItem(
                                alarm = alarm,
                                onToggle = { viewModel.toggleAlarm(alarm, it) },
                                onDelete = { viewModel.deleteAlarm(alarm) },
                                onEdit = { viewModel.startEditingAlarm(alarm) }
                            )
                        }
                    }
                }
            }
        }

        if (viewModel.isEditing) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF1A162B)
            ) {
                AlarmEditorScreen(
                    viewModel = viewModel,
                    onDismiss = { viewModel.isEditing = false }
                )
            }
        }
    }
}

@Composable
fun AlarmItem(alarm: Alarm, onToggle: (Boolean) -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val hourLabel = if (alarm.hour > 12) alarm.hour - 12 else if (alarm.hour == 0) 12 else alarm.hour
                val amPm = if (alarm.hour >= 12) "PM" else "AM"

                Text(
                    text = String.format("%02d:%02d %s", hourLabel, alarm.minute, amPm),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                )

                // 🚨 NEW: Dynamic Repeat Text
                Text(
                    text = getRepeatText(alarm.repeatDays),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle(it) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// 🚨 NEW: Helper Function for Translate stored days to UI text
fun getRepeatText(repeatDays: String): String {
    if (repeatDays.isEmpty()) return "Never"

    return when (repeatDays) {
        "1,2,3,4,5,6,7" -> "Every day"
        "1,2,3,4,5" -> "Monday to Friday"
        "6,7" -> "Saturday Sunday"
        else -> {
            // Logic for Custom days (e.g., "1,3,5" -> "Mon, Wed, Fri")
            val daysMap = mapOf(
                "1" to "Mon", "2" to "Tue", "3" to "Wed",
                "4" to "Thu", "5" to "Fri", "6" to "Sat", "7" to "Sun"
            )
            repeatDays.split(",")
                .mapNotNull { daysMap[it] }
                .joinToString(", ")
        }
    }
}