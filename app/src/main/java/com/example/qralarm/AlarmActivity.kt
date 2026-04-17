package com.example.qralarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.qralarm.service.AlarmService
import com.example.qralarm.ui.screens.AlarmTriggerScreen
import com.example.qralarm.ui.theme.QRAlarmTheme

class AlarmActivity : ComponentActivity() {

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "FINISH_ALARM_ACTIVITY") {
                finishAndRemoveTask()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filter = IntentFilter("FINISH_ALARM_ACTIVITY").apply {
            priority = 999
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(stopReceiver, filter)
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        setContent {
            QRAlarmTheme {
                AlarmTriggerScreen(onSuccess = {
                    stopService(Intent(this, AlarmService::class.java))
                    finishAndRemoveTask()
                })
            }
        }
    }

    // 🚨 FIX: Replaced dispatchKeyEvent with standard onKeyDown to avoid the AndroidX library error
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // If the user presses Volume Up or Volume Down, consume the action (do nothing)
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // 🚨 FIX: Also block onKeyUp to ensure it doesn't trigger when the user lets go of the button
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(stopReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
}