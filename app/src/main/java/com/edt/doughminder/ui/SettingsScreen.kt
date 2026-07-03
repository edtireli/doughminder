package com.edt.doughminder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.edt.doughminder.data.AppSettings
import com.edt.doughminder.ui.theme.Coral
import com.edt.doughminder.ui.theme.Cream
import com.edt.doughminder.ui.theme.CreamDim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdate: (AppSettings) -> Unit,
    onTestNotification: () -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(28.dp))

        Text("Default reminder time", style = MaterialTheme.typography.titleMedium)
        Text(
            "Used for new starters. Existing starters keep their own time.",
            style = MaterialTheme.typography.bodyMedium, color = CreamDim,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { showTimePicker = true }) {
            Text("%d:%02d".format(settings.defaultHour, settings.defaultMinute), color = Cream)
        }
        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.fillMaxWidth(0.75f)) {
                Text("Argue back", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Your starter responds when you dismiss it. Turning this off is technically possible, emotionally cowardly.",
                    style = MaterialTheme.typography.bodyMedium, color = CreamDim,
                )
            }
            Switch(
                checked = settings.argueBack,
                onCheckedChange = { onUpdate(settings.copy(argueBack = it)) },
            )
        }
        Spacer(Modifier.height(28.dp))

        Text("“Later” means…", style = MaterialTheme.typography.titleMedium)
        Text(
            "How long before the follow-up argument lands.",
            style = MaterialTheme.typography.bodyMedium, color = CreamDim,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 30, 60).forEach { m ->
                FilterChip(
                    selected = settings.nagDelayMinutes == m,
                    onClick = { onUpdate(settings.copy(nagDelayMinutes = m)) },
                    label = { Text("$m min") },
                )
            }
        }
        Spacer(Modifier.height(36.dp))

        TextButton(onClick = onTestNotification) {
            Text("Send a test nag", color = Coral)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = settings.defaultHour,
            initialMinute = settings.defaultMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onUpdate(settings.copy(defaultHour = state.hour, defaultMinute = state.minute))
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
