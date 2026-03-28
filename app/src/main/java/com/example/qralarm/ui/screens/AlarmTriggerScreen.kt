package com.example.qralarm.ui.screens

import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.qralarm.util.QRScannerProcessor

@Composable
fun AlarmTriggerScreen(onSuccess: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scannerProcessor = remember { QRScannerProcessor() }

    // Entire screen is black
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // 1. The Camera "Box" in the center
        Card(
            modifier = Modifier
                .size(300.dp) // This is your "Green Box" size
                .align(Alignment.Center),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, Color.Green) // The green border you requested
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scannerProcessor.startCamera(ctx, lifecycleOwner, this) { result ->
                            if (result == "STOP_ALARM") onSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Instructions at the bottom
        Text(
            "🚨WAKE UP FOR PRAYER🚨\nFit the QR code inside the green box",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
