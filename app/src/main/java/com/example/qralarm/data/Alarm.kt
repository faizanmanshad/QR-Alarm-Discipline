package com.example.qralarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int, // 24-hour format (0-23)
    val minute: Int,
    var isEnabled: Boolean = true,

    // 🚨 FIELDS FOR ADVANCED ALARMS
    val ringtoneUri: String? = null, // Stores the technical link (Uri)
    val isVibrationEnabled: Boolean = true,

    // Stores days as packed string.
    // Format: "1,2,3" where 1=Mon, 2=Tue... 7=Sun. "" means no repeat.
    val repeatDays: String = "",

    // 🚨 NEW: Stores the actual name of the ringtone (e.g., "Fajr Adhan")
    val ringtoneName: String? = "Default System Alarm"
)