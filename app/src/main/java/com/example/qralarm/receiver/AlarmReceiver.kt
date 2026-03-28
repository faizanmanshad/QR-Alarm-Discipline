package com.example.qralarm.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.qralarm.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        // 1. Extract all data passed from the AlarmManager/ViewModel
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val ringtoneUri = intent.getStringExtra("RINGTONE_URI")
        val isVibrationEnabled = intent.getBooleanExtra("VIBRATION_ENABLED", false)

        // 2. Prepare the Intent for the AlarmService (the "Engine")
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("RINGTONE_URI", ringtoneUri)
            putExtra("VIBRATION_ENABLED", isVibrationEnabled)
        }

        // 3. Start the Service based on Android Version
        // Foreground services are required for Alarms on modern Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}