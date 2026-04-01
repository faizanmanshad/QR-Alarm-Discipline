package com.example.qralarm.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.qralarm.AlarmActivity
import com.example.qralarm.data.AlarmDatabase
import com.example.qralarm.util.AndroidAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    // Timers for Fade-in and Auto-stop
    private var volumeTimer: CountDownTimer? = null
    private var ringingTimer: CountDownTimer? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        val uriString = intent?.getStringExtra("RINGTONE_URI")
        val shouldVibrate = intent?.getBooleanExtra("VIBRATION_ENABLED", false) ?: false

        // 🚨 FIX: AUTO-RESCHEDULE LOGIC
        // We look up the alarm in the DB and schedule its NEXT occurrence immediately.
        if (alarmId != -1) {
            val context = applicationContext
            CoroutineScope(Dispatchers.IO).launch {
                val db = AlarmDatabase.getDatabase(context)
                val alarm = db.alarmDao().getAlarmById(alarmId)
                alarm?.let {
                    // This uses the new logic we put in the Scheduler to find the next day
                    AndroidAlarmScheduler(context).schedule(it)
                }
            }
        }

        // 🚨 LOAD GLOBAL SETTINGS
        val prefs = getSharedPreferences("QR_ALARM_PREFS", Context.MODE_PRIVATE)
        val fadeSeconds = prefs.getInt("FADE_DURATION", 30)
        val ringingMinutes = prefs.getInt("RINGING_DURATION", 5)

        // 1. Setup Ringtone
        val ringtoneUri = if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            Settings.System.DEFAULT_ALARM_ALERT_URI
        }

        // 2. Play Sound
        mediaPlayer = MediaPlayer().apply {
            setDataSource(applicationContext, ringtoneUri)
            setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            isLooping = true
            setVolume(0f, 0f)
            prepare()
            start()
        }

        // 🚨 GRADUAL VOLUME INCREASE
        val totalFadeSteps = fadeSeconds.toLong()
        if (totalFadeSteps > 0) {
            volumeTimer = object : CountDownTimer(totalFadeSteps * 1000, 1000) {
                var currentStep = 0
                override fun onTick(millisUntilFinished: Long) {
                    currentStep++
                    val volume = currentStep.toFloat() / totalFadeSteps
                    mediaPlayer?.setVolume(volume, volume)
                }
                override fun onFinish() {
                    mediaPlayer?.setVolume(1f, 1f)
                }
            }.start()
        } else {
            mediaPlayer?.setVolume(1f, 1f)
        }

        // 🚨 AUTOMATIC STOP
        ringingTimer = object : CountDownTimer(ringingMinutes * 60 * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                val finishIntent = Intent("FINISH_ALARM_ACTIVITY")
                sendBroadcast(finishIntent)
                stopSelf()
            }
        }.start()

        // 3. Handle Vibration
        if (shouldVibrate) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 500, 500)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }

        // 4. Notification
        startForeground(1, createNotification())

        // 5. Launch Activity
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(fullScreenIntent)

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarm Active", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Bismillah - Time to Wake Up!")
            .setContentText("Scan the QR code to stop the alarm.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
        volumeTimer?.cancel()
        ringingTimer?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}