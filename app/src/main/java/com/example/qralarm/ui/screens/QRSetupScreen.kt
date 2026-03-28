package com.example.qralarm.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.qralarm.R
import java.io.File
import java.io.FileOutputStream

@Composable
fun QRSetupScreen() {
    val context = LocalContext.current
    val qrText = "STOP_ALARM"

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Your Verification Code",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🚨 ONLY ONE QR CODE: Using your local image from res/drawable
        // Make sure your file is named my_custom_qr.png in the drawable folder
        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Image(
                painter = painterResource(id = R.drawable.my_custom_qr),
                contentDescription = "My QR Code",
                modifier = Modifier.padding(16.dp).size(250.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Text: $qrText", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { shareQrImage(context) },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Share/Print PNG Code")
        }
    }
}

// 🚨 Helper function to share the actual PNG file
private fun shareQrImage(context: Context) {
    try {
        // 1. Convert the drawable into a file in the cache
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.my_custom_qr)
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "qr_code_verification.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        // 2. Get the URI using FileProvider
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // 3. Create the Share Intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Allow WhatsApp to read the file
            setDataAndType(contentUri, context.contentResolver.getType(contentUri))
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, "Print this QR code and place it in your bathroom to stop the alarm!")
            type = "image/png"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}