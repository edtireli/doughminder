package com.edt.doughminder.data

import kotlinx.serialization.Serializable

@Serializable
enum class Gender { SHE, HE, THEY }

@Serializable
data class Starter(
    val id: String,
    val name: String,
    val gender: Gender = Gender.SHE,
    val jarColor: Int = 0,          // index into StarterPalette
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val lastFedEpochMillis: Long? = null,
    val createdAt: Long,
) {
    fun hoursSinceFed(now: Long = System.currentTimeMillis()): Long? =
        lastFedEpochMillis?.let { (now - it) / 3_600_000 }

    fun fedToday(now: Long = System.currentTimeMillis()): Boolean {
        val h = hoursSinceFed(now) ?: return false
        return h < 20
    }
}

@Serializable
data class AppSettings(
    val defaultHour: Int = 8,
    val defaultMinute: Int = 0,
    val argueBack: Boolean = true,
    val nagDelayMinutes: Int = 30,  // how long after "later" the follow-up lands
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
