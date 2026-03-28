package com.example.qralarm.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.qralarm.ui.viewmodel.AlarmViewModel

@Composable
fun MainTabScreen(viewModel: AlarmViewModel, onPickRingtone: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Alarms", "QR Setup", "Settings")
    val icons = listOf(Icons.Default.Alarm, Icons.Default.QrCode, Icons.Default.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = { Icon(icons[index], contentDescription = title) }
                    )
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> AlarmListScreen(viewModel)
                1 -> QRSetupScreen()
                2 -> SettingsScreen(viewModel, onPickRingtone)
            }
        }
    }
}
