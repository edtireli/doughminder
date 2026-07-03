package com.edt.doughminder.data

import kotlinx.serialization.Serializable

@Serializable
enum class Gender { SHE, HE, THEY }

/** Where the starter lives — drives feeding cadence, mood, and the arguments. */
@Serializable
enum class Storage { ROOM, FRIDGE, FREEZER }

/** Feeding cadence in hours for each storage state. */
val Storage.intervalHours: Long
    get() = when (this) {
        Storage.ROOM -> 24
        Storage.FRIDGE -> 7 * 24
        Storage.FREEZER -> 30 * 24
    }

@Serializable
data class Starter(
    val id: String,
    val name: String,
    val gender: Gender = Gender.SHE,
    val jarColor: Int = 0,          // index into StarterPalette
    val storage: Storage = Storage.ROOM,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val lastFedEpochMillis: Long? = null,
    val createdAt: Long,
) {
    fun hoursSinceFed(now: Long = System.currentTimeMillis()): Long? =
        lastFedEpochMillis?.let { (now - it) / 3_600_000 }

    /** Fed recently enough that we shouldn't nag on this cycle. */
    fun fedRecently(now: Long = System.currentTimeMillis()): Boolean {
        val h = hoursSinceFed(now) ?: return false
        return h < storage.intervalHours * 0.85
    }
}

@Serializable
data class AppSettings(
    val defaultHour: Int = 8,
    val defaultMinute: Int = 0,
    val argueBack: Boolean = true,
)

data class RecipeStep(
    val title: String,
    val detail: String,
    val timerMinutes: Int? = null,
)

data class Recipe(
    val id: String,
    val title: String,
    val summary: String,
    val steps: List<RecipeStep>,
)
