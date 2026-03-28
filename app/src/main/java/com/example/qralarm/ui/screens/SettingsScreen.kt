package com.example.qralarm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qralarm.ui.viewmodel.AlarmViewModel

@Composable
fun SettingsScreen(viewModel: AlarmViewModel, onPickRingtone: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("App Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        // --- 1. GRADUAL VOLUME INCREASE SETTING ---
        Text(
            text = "Gradual Volume Increase",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "${viewModel.fadeDurationSeconds} Seconds",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = viewModel.fadeDurationSeconds.toFloat(),
            onValueChange = { viewModel.updateFadeDuration(it.toInt()) },
            valueRange = 5f..120f, // From 5 seconds to 2 minutes
            steps = 22 // Increments of 5s
        )
        Text(
            text = "Time taken for the alarm to reach full volume.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. TOTAL RINGING DURATION SETTING ---
        Text(
            text = "Alarm Ringing Duration",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "${viewModel.ringingDurationMinutes} Minutes",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = viewModel.ringingDurationMinutes.toFloat(),
            onValueChange = { viewModel.updateRingingDuration(it.toInt()) },
            valueRange = 1f..30f, // From 1 minute to 30 minutes
            steps = 29 // Increments of 1 min
        )
        Text(
            text = "The alarm will stop automatically after this period if not scanned.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        // Note: Ringtone list item removed as it is now configured per alarm.
    }
}
