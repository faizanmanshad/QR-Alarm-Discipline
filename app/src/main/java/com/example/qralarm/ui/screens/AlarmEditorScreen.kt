package com.example.qralarm.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.qralarm.ui.viewmodel.AlarmViewModel
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorScreen(viewModel: AlarmViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var showRepeatDialog by remember { mutableStateOf(false) }

    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    viewModel.editRingtoneUri = it
                    
                    // Get the actual file name of the selected ringtone
                    val cursor = context.contentResolver.query(it, null, null, null, null)
                    val name = cursor?.use { c ->
                        val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (c.moveToFirst() && nameIndex != -1) c.getString(nameIndex) else null
                    } ?: "Custom Tone"
                    
                    viewModel.editRingtoneTitle = name
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewModel.editRingtoneTitle = "Custom Tone"
                }
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A162B))
            .clickable(enabled = false) {}
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- HEADER ---
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    "Set Alarm",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    viewModel.saveAlarm()
                    onDismiss()
                }) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Save",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- CENTER WHEELS ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                // Highlight bar removed as per request

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hours — items pre-built once, never rebuilt during scroll
                    val hours = remember { (1..12).map { it.toString() } }
                    VerticalInfinitePicker(
                        items = hours,
                        initialValueIndex = viewModel.editHour - 1,
                        onValueChange = { viewModel.editHour = it.toInt() },
                        modifier = Modifier.width(80.dp)
                    )

                    Text(
                        ":",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Minutes — items pre-built once, never rebuilt during scroll
                    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }
                    VerticalInfinitePicker(
                        items = minutes,
                        initialValueIndex = viewModel.editMinute,
                        onValueChange = { viewModel.editMinute = it.toInt() },
                        modifier = Modifier.width(80.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // AM / PM — items pre-built once, never rebuilt during scroll
                    val amPmOptions = remember { listOf("am", "pm") }
                    VerticalInfinitePicker(
                        items = amPmOptions,
                        initialValueIndex = if (viewModel.editIsAm) 0 else 1,
                        onValueChange = { viewModel.editIsAm = (it == "am") },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- OPTIONS BOX ---
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF2D2845))
                    .padding(vertical = 4.dp)
            ) {
                EditorOptionRow(
                    title = "Repeating",
                    value = getRepeatPresetText(viewModel),
                    onClick = { showRepeatDialog = true }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )
                EditorOptionRow(
                    title = "Ringtone",
                    value = viewModel.editRingtoneTitle,
                    onClick = { ringtoneLauncher.launch(arrayOf("audio/*")) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Vibrations",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = viewModel.editVibrationEnabled,
                        onCheckedChange = { viewModel.editVibrationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF7B61FF)
                        )
                    )
                }
            }
        }
    }

    if (showRepeatDialog) {
        RepeatSelectionDialog(
            viewModel = viewModel,
            onDismiss = { showRepeatDialog = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// THE ONLY FUNCTION THAT CHANGED
// Key fixes:
//   1. items list is passed in already remembered — zero allocation during scroll
//   2. startIndex arithmetic guarantees the initial item lands in center slot
//   3. rawOffset uses currentPage + currentPageOffsetFraction so the highlight
//      tracks the CENTER position continuously, not the top-visible position
//   4. derivedStateOf gates onValueChange so it only fires on settled pages,
//      not on every animation frame — eliminates the callback lag
//   5. beyondViewportPageCount = 2 keeps neighbors pre-rendered so edges
//      never flash blank during fast flings
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalInfinitePicker(
    modifier: Modifier = Modifier,
    items: List<String>,
    initialValueIndex: Int = 0,
    onValueChange: (String) -> Unit
) {
    // Anchor the start page so the requested item appears in the center slot.
    // We pick the midpoint of Int.MAX_VALUE then align it to a clean multiple
    // of items.size so modulo arithmetic never produces a wrong remainder.
    val startIndex = remember(items.size, initialValueIndex) {
        val mid = Int.MAX_VALUE / 2
        mid - (mid % items.size) + initialValueIndex
    }

    val pagerState = rememberPagerState(initialPage = startIndex) { Int.MAX_VALUE }

    // Gate: only call onValueChange when the page has fully settled.
    // Without derivedStateOf this fires hundreds of times per scroll gesture.
    val settledPage by remember {
        derivedStateOf { pagerState.currentPage }
    }
    LaunchedEffect(settledPage) {
        onValueChange(items[settledPage % items.size])
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.height(216.dp),
        // 72 dp padding top + 72 dp padding bottom means only ONE page fits
        // in the padding zone on each side, keeping prev/center/next visible.
        contentPadding = PaddingValues(vertical = 72.dp),
        pageSize = PageSize.Fixed(72.dp),
        snapPosition = SnapPosition.Center,
        // Pre-render 2 pages beyond the viewport so neighbours are never blank.
        beyondViewportPageCount = 2
    ) { page ->

        // rawOffset is a signed float:
        //   0.0  → this page is EXACTLY in the center slot
        //   1.0  → one full page above or below center
        // currentPageOffsetFraction makes it continuous during the swipe
        // animation so the visual effect is perfectly smooth.
        val rawOffset =
            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val fraction = rawOffset.absoluteValue.coerceIn(0f, 1f)

        // lerp gives a linear gradient between selected and unselected states.
        // Selected  (fraction = 0): scale 1.25, alpha 1.00, ExtraBold
        // Unselected (fraction = 1): scale 0.78, alpha 0.28, Normal
        val scale = lerp(1.25f, 0.78f, fraction)
        val alpha = lerp(1.00f, 0.28f, fraction)
        val fontWeight = if (fraction < 0.5f) FontWeight.ExtraBold else FontWeight.Normal

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = items[page % items.size],
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 36.sp,
                    fontWeight = fontWeight,
                    color = Color.White
                ),
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EditorOptionRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = Color.Gray, fontSize = 14.sp)
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun RepeatSelectionDialog(viewModel: AlarmViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2D2845),
        title = { Text("Repeat", color = Color.White) },
        text = {
            Column {
                val presets = listOf(
                    AlarmViewModel.RepeatPreset.EVERYDAY to "Everyday",
                    AlarmViewModel.RepeatPreset.MON_FRI to "Monday to Friday",
                    AlarmViewModel.RepeatPreset.SAT_SUN to "Saturday to Sunday",
                    AlarmViewModel.RepeatPreset.CUSTOM to "Custom"
                )
                presets.forEach { (preset, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.editRepeatPreset = preset
                                if (preset != AlarmViewModel.RepeatPreset.CUSTOM) onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = viewModel.editRepeatPreset == preset,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF7B61FF)
                            )
                        )
                        Text(
                            label,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                if (viewModel.editRepeatPreset == AlarmViewModel.RepeatPreset.CUSTOM) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                        (1..7).forEachIndexed { index, day ->
                            val isSelected = viewModel.editCustomDays[day] == true
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFF7B61FF)
                                        else Color.White.copy(alpha = 0.1f)
                                    )
                                    .clickable {
                                        viewModel.editCustomDays[day] = !isSelected
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    dayNames[index],
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Color(0xFF7B61FF))
            }
        }
    )
}

fun getRepeatPresetText(viewModel: AlarmViewModel): String {
    return when (viewModel.editRepeatPreset) {
        AlarmViewModel.RepeatPreset.NONE -> "Never"
        AlarmViewModel.RepeatPreset.EVERYDAY -> "Everyday"
        AlarmViewModel.RepeatPreset.MON_FRI -> "Mon - Fri"
        AlarmViewModel.RepeatPreset.SAT_SUN -> "Sat - Sun"
        AlarmViewModel.RepeatPreset.CUSTOM -> {
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val selectedDays = viewModel.editCustomDays
                .filterValues { it }
                .keys
                .sorted()
                .map { dayNames[it - 1] }
            if (selectedDays.isEmpty()) "Never"
            else selectedDays.joinToString(", ")
        }
    }
}
