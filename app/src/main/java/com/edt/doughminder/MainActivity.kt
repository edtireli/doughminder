package com.edt.doughminder

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edt.doughminder.data.AppSettings
import com.edt.doughminder.data.Sass
import com.edt.doughminder.data.Starter
import com.edt.doughminder.data.StarterRepository
import com.edt.doughminder.notify.Channels
import com.edt.doughminder.notify.Notify
import com.edt.doughminder.notify.ReminderScheduler
import com.edt.doughminder.ui.AddEditScreen
import com.edt.doughminder.ui.HomeScreen
import com.edt.doughminder.ui.RecipesScreen
import com.edt.doughminder.ui.SettingsScreen
import com.edt.doughminder.ui.theme.DoughminderTheme
import com.edt.doughminder.ui.theme.Ink
import kotlinx.coroutines.launch

private enum class Tab { STARTERS, RECIPES, SETTINGS }

class MainActivity : ComponentActivity() {

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            notifPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            DoughminderTheme { DoughminderApp() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoughminderApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { StarterRepository.get(context) }
    val scope = rememberCoroutineScope()

    val starters by repo.starters.collectAsState(initial = emptyList())
    val settings by repo.settings.collectAsState(initial = AppSettings())

    var tab by remember { mutableStateOf(Tab.STARTERS) }
    // null = browsing; Some(null) = adding new; Some(starter) = editing
    var editing by remember { mutableStateOf<Starter?>(null) }
    var adding by remember { mutableStateOf(false) }

    fun closeEditor() { editing = null; adding = false }

    Scaffold(
        containerColor = Ink,
        topBar = {
            if (!adding && editing == null) {
                TopAppBar(
                    title = { Text("Doughminder", style = MaterialTheme.typography.headlineMedium) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Ink),
                )
            }
        },
        bottomBar = {
            if (!adding && editing == null) {
                NavigationBar(containerColor = Ink) {
                    NavigationBarItem(
                        selected = tab == Tab.STARTERS,
                        onClick = { tab = Tab.STARTERS },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("Starters") },
                    )
                    NavigationBarItem(
                        selected = tab == Tab.RECIPES,
                        onClick = { tab = Tab.RECIPES },
                        icon = { Icon(Icons.Filled.Menu, contentDescription = null) },
                        label = { Text("Recipes") },
                    )
                    NavigationBarItem(
                        selected = tab == Tab.SETTINGS,
                        onClick = { tab = Tab.SETTINGS },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == Tab.STARTERS && !adding && editing == null) {
                ExtendedFloatingActionButton(
                    onClick = { adding = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Starter") },
                )
            }
        },
    ) { padding ->
        val content = Modifier.fillMaxSize().padding(padding)
        when {
            adding || editing != null -> androidx.compose.foundation.layout.Box(content) {
                AddEditScreen(
                    existing = editing,
                    defaultHour = settings.defaultHour,
                    defaultMinute = settings.defaultMinute,
                    onSave = { starter ->
                        scope.launch {
                            repo.upsert(starter)
                            ReminderScheduler.scheduleDaily(context, starter)
                        }
                        closeEditor()
                    },
                    onDelete = { starter ->
                        scope.launch {
                            ReminderScheduler.cancel(context, starter.id)
                            repo.delete(starter.id)
                        }
                        closeEditor()
                    },
                    onFeedNow = { starter ->
                        scope.launch { repo.markFed(starter.id) }
                        closeEditor()
                    },
                    onBack = { closeEditor() },
                )
            }
            tab == Tab.STARTERS -> androidx.compose.foundation.layout.Box(content) {
                HomeScreen(starters = starters, onStarterClick = { editing = it })
            }
            tab == Tab.RECIPES -> androidx.compose.foundation.layout.Box(content) {
                RecipesScreen(onStartTimer = { title, minutes ->
                    ReminderScheduler.scheduleTimer(context, title, minutes)
                    Notify.postSimple(
                        context, ("timerstart$title").hashCode(), Channels.TIMERS,
                        title = "Timer started",
                        body = "$title — I'll come get you in $minutes minutes.",
                    )
                })
            }
            else -> androidx.compose.foundation.layout.Box(content) {
                SettingsScreen(
                    settings = settings,
                    onUpdate = { scope.launch { repo.updateSettings(it) } },
                    onTestNotification = {
                        val target = starters.firstOrNull() ?: Starter(
                            id = "demo", name = "Demo Dough",
                            createdAt = System.currentTimeMillis(),
                        )
                        Notify.postNag(
                            context, target,
                            title = Sass.morningTitle(target),
                            body = Sass.morningBody(target),
                            depth = 0,
                            channel = Channels.REMINDERS,
                        )
                    },
                )
            }
        }
    }
}
