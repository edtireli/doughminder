package com.edt.doughminder.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "doughminder")

/** Single source of truth for starters + settings, JSON in DataStore. */
class StarterRepository private constructor(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val startersKey = stringPreferencesKey("starters")
    private val settingsKey = stringPreferencesKey("settings")

    val starters: Flow<List<Starter>> = context.dataStore.data.map { prefs ->
        prefs[startersKey]?.let { runCatching { json.decodeFromString<List<Starter>>(it) }.getOrNull() }
            ?: emptyList()
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        prefs[settingsKey]?.let { runCatching { json.decodeFromString<AppSettings>(it) }.getOrNull() }
            ?: AppSettings()
    }

    suspend fun currentStarters(): List<Starter> = starters.first()
    suspend fun currentSettings(): AppSettings = settings.first()

    suspend fun getStarter(id: String): Starter? = currentStarters().find { it.id == id }

    suspend fun upsert(starter: Starter) {
        val list = currentStarters().toMutableList()
        val idx = list.indexOfFirst { it.id == starter.id }
        if (idx >= 0) list[idx] = starter else list.add(starter)
        save(list)
    }

    suspend fun delete(id: String) = save(currentStarters().filterNot { it.id == id })

    suspend fun markFed(id: String, at: Long = System.currentTimeMillis()) {
        getStarter(id)?.let { upsert(it.copy(lastFedEpochMillis = at)) }
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { it[settingsKey] = json.encodeToString(AppSettings.serializer(), settings) }
    }

    private suspend fun save(list: List<Starter>) {
        context.dataStore.edit { it[startersKey] = json.encodeToString(list) }
    }

    companion object {
        @Volatile private var instance: StarterRepository? = null
        fun get(context: Context): StarterRepository =
            instance ?: synchronized(this) {
                instance ?: StarterRepository(context.applicationContext).also { instance = it }
            }
    }
}
