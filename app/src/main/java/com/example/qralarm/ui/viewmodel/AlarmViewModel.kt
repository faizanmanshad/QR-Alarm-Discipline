package com.example.qralarm.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qralarm.data.Alarm
import com.example.qralarm.data.AlarmDatabase
import com.example.qralarm.util.AndroidAlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AlarmDatabase.getDatabase(application)
    private val dao = db.alarmDao()
    private val scheduler = AndroidAlarmScheduler(application)
    private val prefs = application.getSharedPreferences("QR_ALARM_PREFS", Context.MODE_PRIVATE)

    val allAlarms: Flow<List<Alarm>> = dao.getAllAlarms()

    // --- EDIT STATE ---
    var isEditing by mutableStateOf(false)
    var currentEditingAlarmId by mutableStateOf<Int?>(null)
    var forceCloseTrigger by mutableStateOf(false)

    fun triggerForceClose() {
        forceCloseTrigger = true
    }

    // 1. Time State
    var editHour by mutableIntStateOf(8)
    var editMinute by mutableIntStateOf(0)
    var editIsAm by mutableStateOf(true)

    // 2. Repeat State
    enum class RepeatPreset { NONE, EVERYDAY, MON_FRI, SAT_SUN, CUSTOM }
    var editRepeatPreset by mutableStateOf(RepeatPreset.NONE)
    val editCustomDays = mutableStateMapOf(
        1 to false, 2 to false, 3 to false, 4 to false, 5 to false, 6 to false, 7 to false
    )

    // 3. Ringtone & Vibration
    var editRingtoneUri by mutableStateOf<Uri?>(null)
    // 🚨 UPDATED: This now holds the real name from the DB
    var editRingtoneTitle by mutableStateOf("Default System Alarm")
    var editVibrationEnabled by mutableStateOf(true)

    // Global Settings
    var fadeDurationSeconds by mutableIntStateOf(prefs.getInt("FADE_DURATION", 30))
    var ringingDurationMinutes by mutableIntStateOf(prefs.getInt("RINGING_DURATION", 5))

    fun updateFadeDuration(seconds: Int) {
        fadeDurationSeconds = seconds
        prefs.edit().putInt("FADE_DURATION", seconds).apply()
    }

    fun updateRingingDuration(minutes: Int) {
        ringingDurationMinutes = minutes
        prefs.edit().putInt("RINGING_DURATION", minutes).apply()
    }

    // --- VIEWMODEL FUNCTIONS ---

    fun startNewAlarm() {
        editHour = 8
        editMinute = 0
        editIsAm = true
        currentEditingAlarmId = null
        editRepeatPreset = RepeatPreset.NONE
        editCustomDays.keys.forEach { editCustomDays[it] = false }
        editRingtoneUri = null
        editRingtoneTitle = "Default System Alarm" // Reset to default
        editVibrationEnabled = true
        isEditing = true
    }

    fun startEditingAlarm(alarm: Alarm) {
        currentEditingAlarmId = alarm.id

        val h24 = alarm.hour
        editIsAm = h24 < 12
        editHour = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        editMinute = alarm.minute

        parseSavedRepeatString(alarm.repeatDays)

        editRingtoneUri = alarm.ringtoneUri?.toUri()
        // 🚨 NEW: Load the actual name saved in the database
        editRingtoneTitle = alarm.ringtoneName ?: "Default System Alarm"

        editVibrationEnabled = alarm.isVibrationEnabled
        isEditing = true
    }

    fun saveAlarm() {
        viewModelScope.launch {
            var h24 = editHour
            if (!editIsAm && h24 != 12) h24 += 12
            if (editIsAm && h24 == 12) h24 = 0

            val daysString = formatRepeatStringForStorage()

            val alarmToSave = Alarm(
                id = currentEditingAlarmId ?: 0,
                hour = h24,
                minute = editMinute,
                isEnabled = true,
                repeatDays = daysString,
                ringtoneUri = editRingtoneUri?.toString(),
                isVibrationEnabled = editVibrationEnabled,
                // 🚨 NEW: Save the title currently shown in the UI
                ringtoneName = editRingtoneTitle
            )

            if (currentEditingAlarmId == null) {
                // 🚨 FIX: Capture the newly generated ID from Room
                val newId = dao.insertAlarm(alarmToSave).toInt()
                // Schedule with the CORRECT ID so multiple alarms don't overwrite each other in AlarmManager
                scheduler.schedule(alarmToSave.copy(id = newId))
            } else {
                dao.updateAlarm(alarmToSave)
                scheduler.schedule(alarmToSave)
            }

            isEditing = false
        }
    }

    // --- HELPERS (Unchanged) ---

    private fun parseSavedRepeatString(savedString: String) {
        editCustomDays.keys.forEach { editCustomDays[it] = false }
        if (savedString.isEmpty()) {
            editRepeatPreset = RepeatPreset.NONE
            return
        }
        editRepeatPreset = when(savedString) {
            "1,2,3,4,5,6,7" -> RepeatPreset.EVERYDAY
            "1,2,3,4,5" -> RepeatPreset.MON_FRI
            "6,7" -> RepeatPreset.SAT_SUN
            else -> RepeatPreset.CUSTOM
        }
        val savedDays = savedString.split(",").mapNotNull { it.toIntOrNull() }
        savedDays.forEach { dayNum -> editCustomDays[dayNum] = true }
    }

    private fun formatRepeatStringForStorage(): String {
        return when(editRepeatPreset) {
            RepeatPreset.NONE -> ""
            RepeatPreset.EVERYDAY -> "1,2,3,4,5,6,7"
            RepeatPreset.MON_FRI -> "1,2,3,4,5"
            RepeatPreset.SAT_SUN -> "6,7"
            RepeatPreset.CUSTOM -> {
                editCustomDays.filterValues { it }.keys.sorted().joinToString(",")
            }
        }
    }

    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = isEnabled)
            dao.updateAlarm(updatedAlarm)
            if (isEnabled) scheduler.schedule(updatedAlarm)
            else scheduler.cancel(updatedAlarm)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            dao.deleteAlarm(alarm)
            scheduler.cancel(alarm)
        }
    }
}
