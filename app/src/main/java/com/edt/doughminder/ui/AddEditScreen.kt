package com.edt.doughminder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.edt.doughminder.data.Gender
import com.edt.doughminder.data.Starter
import com.edt.doughminder.ui.theme.Coral
import com.edt.doughminder.ui.theme.Cream
import com.edt.doughminder.ui.theme.CreamDim
import com.edt.doughminder.ui.theme.StarterPalette
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    existing: Starter?,
    defaultHour: Int,
    defaultMinute: Int,
    onSave: (Starter) -> Unit,
    onDelete: (Starter) -> Unit,
    onFeedNow: (Starter) -> Unit,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var gender by remember { mutableStateOf(existing?.gender ?: Gender.SHE) }
    var jarColor by remember { mutableStateOf(existing?.jarColor ?: 0) }
    var hour by remember { mutableStateOf(existing?.reminderHour ?: defaultHour) }
    var minute by remember { mutableStateOf(existing?.reminderMinute ?: defaultMinute) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            if (existing == null) "New starter" else existing.name,
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(Modifier.height(24.dp))

        JarArt(
            doughColor = StarterPalette[jarColor % StarterPalette.size],
            mood = JarMood.HAPPY,
            modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            placeholder = { Text("Bertha, Doughvid, Clint Yeastwood…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))

        Text("Pronouns", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Gender.entries.forEach { g ->
                FilterChip(
                    selected = gender == g,
                    onClick = { gender = g },
                    label = {
                        Text(when (g) { Gender.SHE -> "She"; Gender.HE -> "He"; Gender.THEY -> "They" })
                    },
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        Text("Flour type", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StarterPalette.forEachIndexed { i, color ->
                Spacer(
                    Modifier
                        .size(40.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (jarColor == i) 3.dp else 1.dp,
                            color = if (jarColor == i) Coral else CreamDim,
                            shape = CircleShape,
                        )
                        .clickable { jarColor = i },
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        Text("Daily reminder", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { showTimePicker = true }) {
            Text("%d:%02d".format(hour, minute), color = Cream)
        }
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val starter = Starter(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    name = name.trim().ifEmpty { "The Blob" },
                    gender = gender,
                    jarColor = jarColor,
                    reminderHour = hour,
                    reminderMinute = minute,
                    lastFedEpochMillis = existing?.lastFedEpochMillis,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                )
                onSave(starter)
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (existing == null) "Adopt this starter" else "Save") }

        if (existing != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { onFeedNow(existing) }, modifier = Modifier.fillMaxWidth()) {
                Text("Feed now 🍞", color = Cream)
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { onDelete(existing) }, modifier = Modifier.fillMaxWidth()) {
                Text("Say goodbye (delete)", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back", color = CreamDim)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hour = state.hour
                    minute = state.minute
                    showTimePicker = false
                }) { Text("OK", color = Coral) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = CreamDim) }
            },
            text = { TimePicker(state = state) },
        )
    }
}
