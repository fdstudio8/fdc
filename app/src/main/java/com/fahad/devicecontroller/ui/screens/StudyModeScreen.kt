package com.fahad.devicecontroller.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.devicecontroller.core.model.OperatingMode
import com.fahad.devicecontroller.feature.notes.NotesManager
import com.fahad.devicecontroller.feature.study.StudyTimerManager
import com.fahad.devicecontroller.ui.components.AppleCard
import com.fahad.devicecontroller.ui.components.AppleSegmentedControl
import com.fahad.devicecontroller.ui.components.AppleSwitch
import com.fahad.devicecontroller.ui.theme.AppleTypography
import com.fahad.devicecontroller.ui.theme.LocalAppleColors

/**
 * Distraction-Free Study Focus & Lock Task (Kiosk) Mode.
 */
@Composable
fun StudyModeScreen(
    operatingMode: OperatingMode,
    timerManager: StudyTimerManager,
    notesManager: NotesManager,
    onStartLockTask: () -> Unit,
    onStopLockTask: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current
    val timerState by timerManager.state.collectAsState()
    val notes by notesManager.notes.collectAsState()

    var selectedIntervalIndex by remember { mutableStateOf(0) }
    val intervals = listOf("25 Min", "50 Min", "15 Min")

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Back Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.blue
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Study Focus",
                    style = AppleTypography.Title1.copy(color = colors.primaryLabel)
                )
            }
        }

        // Circular Timer Card
        item {
            AppleCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (timerState.isBreak) "BREAK SESSION" else "FOCUS SESSION",
                        style = AppleTypography.Caption.copy(
                            color = if (timerState.isBreak) colors.mint else colors.orange,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Ring with Countdown
                    val progress = if (timerState.totalSeconds > 0) {
                        timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()
                    } else 0f
                    val animatedProgress by animateFloatAsState(targetValue = progress, label = "TimerProgress")

                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (timerState.isBreak) colors.mint else colors.orange,
                            strokeWidth = 10.dp,
                            trackColor = colors.tertiaryBackground
                        )

                        val minutes = timerState.remainingSeconds / 60
                        val seconds = timerState.remainingSeconds % 60
                        Text(
                            text = "%02d:%02d".format(minutes, seconds),
                            style = AppleTypography.LargeTitle.copy(
                                fontSize = 42.sp,
                                color = colors.primaryLabel
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!timerState.isRunning && timerState.remainingSeconds == timerState.totalSeconds) {
                        AppleSegmentedControl(
                            items = intervals,
                            selectedIndex = selectedIntervalIndex,
                            onItemSelected = { index ->
                                selectedIntervalIndex = index
                                val mins = when (index) {
                                    0 -> 25
                                    1 -> 50
                                    else -> 15
                                }
                                timerManager.resetTimer(mins)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Timer Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (!timerState.isRunning) {
                            Button(
                                onClick = {
                                    if (timerState.remainingSeconds < timerState.totalSeconds) {
                                        timerManager.resumeTimer()
                                    } else {
                                        val mins = when (selectedIntervalIndex) {
                                            0 -> 25
                                            1 -> 50
                                            else -> 15
                                        }
                                        timerManager.startTimer(mins)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.mint),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = if (timerState.remainingSeconds < timerState.totalSeconds) "Resume" else "Start Focus",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Button(
                                onClick = { timerManager.pauseTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.orange),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text("Pause", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedButton(
                            onClick = { timerManager.resetTimer() },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("Reset", color = colors.primaryLabel)
                        }
                    }
                }
            }
        }

        // Lock Task Kiosk Mode Card
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Distraction-Free Kiosk Mode",
                            style = AppleTypography.Headline.copy(color = colors.primaryLabel)
                        )
                        Text(
                            text = if (operatingMode != OperatingMode.STANDARD)
                                "Pinches device to FDC screen during study"
                            else
                                "Requires Device Owner provisioning",
                            style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                        )
                    }

                    AppleSwitch(
                        checked = timerState.isLockTaskEnabled,
                        enabled = operatingMode != OperatingMode.STANDARD,
                        onCheckedChange = { enable ->
                            timerManager.setLockTaskOption(enable)
                            if (enable) onStartLockTask() else onStopLockTask()
                        }
                    )
                }
            }
        }

        // Study Notes Section
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "STUDY NOTES & SCHEDULES",
                        style = AppleTypography.Caption.copy(color = colors.secondaryLabel)
                    )
                    IconButton(onClick = { showAddNoteDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note", tint = colors.blue)
                    }
                }

                if (notes.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No study notes saved. Tap + to create your study goals.",
                        style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    notes.forEach { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title,
                                    style = AppleTypography.Headline.copy(fontSize = 15.sp, color = colors.primaryLabel)
                                )
                                Text(
                                    text = note.content,
                                    style = AppleTypography.Footnote.copy(color = colors.secondaryLabel)
                                )
                            }
                            IconButton(onClick = { notesManager.deleteNote(note.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = colors.red,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = colors.glassBorder)
                    }
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("New Study Goal", style = AppleTypography.Title2) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Topic or Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Key focus points") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (noteTitle.isNotBlank()) {
                            notesManager.addNote(noteTitle, noteContent)
                            noteTitle = ""
                            noteContent = ""
                            showAddNoteDialog = false
                        }
                    }
                ) {
                    Text("Save", color = colors.blue, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel", color = colors.secondaryLabel)
                }
            }
        )
    }
}

