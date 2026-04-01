package com.example.qralarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.qralarm.data.Alarm
import com.example.qralarm.receiver.AlarmReceiver
import java.util.*

class AndroidAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("RINGTONE_URI", alarm.ringtoneUri)
            putExtra("VIBRATION_ENABLED", alarm.isVibrationEnabled)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 🚨 FIX 1: The "AM/PM Flip" & "Tomorrow" Logic
        // If the calculated time is in the past, we start by moving to tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 🚨 FIX 2: Repeating Days Logic
        // If the user has specific days selected (e.g., Mon-Fri),
        // we must find the NEXT valid day from the current calculated date.
        if (alarm.repeatDays.isNotEmpty()) {
            val enabledDays = alarm.repeatDays.split(",").mapNotNull { it.toIntOrNull() }

            // We loop up to 7 days to find the next active day
            while (!enabledDays.contains(getCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)))) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // 🚨 FIX 3: High-Precision Scheduling
        // Using setAlarmClock ensures Android won't "sleep" through the alarm
        val alarmInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmInfo, pendingIntent)
    }

    /**
     * Converts Calendar.DAY_OF_WEEK (Sun=1, Mon=2...)
     * to your DB format (Mon=1, Tue=2... Sun=7)
     */
    private fun getCalendarDay(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}