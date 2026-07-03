package com.edt.doughminder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edt.doughminder.data.Starter
import com.edt.doughminder.ui.theme.Amber
import com.edt.doughminder.ui.theme.CreamDim
import com.edt.doughminder.ui.theme.InkBorder
import com.edt.doughminder.ui.theme.InkRaised
import com.edt.doughminder.data.Storage
import com.edt.doughminder.data.intervalHours
import com.edt.doughminder.ui.theme.Sage
import com.edt.doughminder.ui.theme.StarterPalette

private fun moodOf(starter: Starter): JarMood {
    if (starter.storage == Storage.FREEZER) return JarMood.SLEEPING
    val h = starter.hoursSinceFed() ?: return JarMood.WORRIED
    val limit = starter.storage.intervalHours
    return when {
        h < limit * 0.85 -> JarMood.HAPPY
        h < limit * 1.3 -> JarMood.WORRIED
        else -> JarMood.ANGRY
    }
}

private fun ago(hours: Long): String = when {
    hours < 1 -> "just now"
    hours < 48 -> "${hours}h ago"
    else -> "${hours / 24}d ago"
}

private fun statusText(starter: Starter): Pair<String, androidx.compose.ui.graphics.Color> {
    val where = when (starter.storage) {
        Storage.ROOM -> ""
        Storage.FRIDGE -> "In the fridge · "
        Storage.FREEZER -> "In the freezer · "
    }
    val h = starter.hoursSinceFed()
        ?: return "${where}never fed" to Amber
    val limit = starter.storage.intervalHours
    return when {
        starter.storage == Storage.FREEZER -> "${where}napping" to Sage
        h < limit * 0.85 -> "${where}fed ${ago(h)}" to Sage
        h < limit * 1.3 -> "${where}hungry — fed ${ago(h)}" to Amber
        else -> "${where}STARVING — fed ${ago(h)}" to MaterialTheme_error
    }
}

private val MaterialTheme_error = androidx.compose.ui.graphics.Color(0xFFE07A6B)

@Composable
fun HomeScreen(
    starters: List<Starter>,
    onStarterClick: (Starter) -> Unit,
) {
    if (starters.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            JarArt(
                doughColor = StarterPalette[0],
                mood = JarMood.WORRIED,
                modifier = Modifier.size(140.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text("No starters yet", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Tap + to add your sourdough.\nGive it a name. It will use that name against you.",
                style = MaterialTheme.typography.bodyMedium,
                color = CreamDim,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(starters, key = { it.id }) { starter ->
            StarterTile(starter, onClick = { onStarterClick(starter) })
        }
    }
}

@Composable
private fun StarterTile(starter: Starter, onClick: () -> Unit) {
    val (status, statusColor) = statusText(starter)
    Column(
        modifier = Modifier
            .background(InkRaised, MaterialTheme.shapes.medium)
            .border(1.dp, InkBorder, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.1f)) {
            JarArt(
                doughColor = StarterPalette[starter.jarColor % StarterPalette.size],
                mood = moodOf(starter),
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(starter.name, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(status, style = MaterialTheme.typography.bodyMedium, color = statusColor, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(
            "Reminds at %d:%02d".format(starter.reminderHour, starter.reminderMinute),
            style = MaterialTheme.typography.bodyMedium,
            color = CreamDim,
        )
    }
}
