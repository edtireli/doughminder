package com.edt.doughminder.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.edt.doughminder.data.Sass
import com.edt.doughminder.data.StarterRepository
import com.edt.doughminder.data.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Run suspend work from a receiver without getting killed mid-flight. */
private fun BroadcastReceiver.async(block: suspend () -> Unit) {
    val pending = goAsync()
    CoroutineScope(Dispatchers.Default).launch {
        try {
            block()
        } finally {
            pending.finish()
        }
    }
}

/** Fires at a scheduled reminder time (base cadence) or a negotiated snooze. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = async {
        val repo = StarterRepository.get(context)
        val id = intent.getStringExtra(Notify.EXTRA_STARTER_ID) ?: return@async
        val depth = intent.getIntExtra(Notify.EXTRA_DEPTH, 0)
        val promised = intent.getIntExtra(Notify.EXTRA_HOURS, 0)
        val starter = repo.getStarter(id) ?: return@async

        if (depth == 0) {
            // Base daily reminder: re-arm tomorrow and ALWAYS fire on the dot at
            // the time the user set — that's the whole job. A "Yes, I fed her"
            // tap is one press away if they already did.
            ReminderScheduler.scheduleNext(context, starter)
            Notify.postNag(context, starter, 0, 0, channel = Channels.REMINDERS)
        } else if (!starter.fedRecently()) {
            // A promised snooze re-nag — skip it only if they've since fed.
            Notify.postNag(context, starter, depth, promised, channel = Channels.ARGUMENTS)
        }
    }
}

/** The user's side of the argument — every notification button lands here. */
class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = async {
        val repo = StarterRepository.get(context)
        val id = intent.getStringExtra(Notify.EXTRA_STARTER_ID) ?: return@async
        val hours = intent.getIntExtra(Notify.EXTRA_HOURS, 0)
        val depth = intent.getIntExtra(Notify.EXTRA_DEPTH, 0)
        val starter = repo.getStarter(id) ?: return@async
        val settings = repo.currentSettings()

        when (intent.action) {
            Notify.ACTION_FED -> {
                repo.markFed(id)
                Notify.cancel(context, starter)
                if (settings.argueBack) {
                    Notify.postLine(context, starter, starter.name, Sass.fedReply(starter))
                }
                // Feeding resets the cadence; re-arm from the new lastFed.
                repo.getStarter(id)?.let { ReminderScheduler.scheduleNext(context, it) }
            }

            Notify.ACTION_LATER -> {
                if (settings.argueBack) Notify.postWhen(context, starter, depth)
                else { Notify.cancel(context, starter); ReminderScheduler.scheduleSnoozeHours(context, id, 1, depth + 1, promised = 1) }
            }

            // Picked a duration → counter-offer / "are you sure?"
            Notify.ACTION_PICK -> Notify.postConfirm(context, starter, hours, depth)

            // Settled on a duration → set a real alarm, escalate next round.
            Notify.ACTION_CONFIRM -> {
                Notify.cancel(context, starter)
                ReminderScheduler.scheduleSnoozeHours(context, id, hours, depth + 1, promised = hours)
                Notify.postLine(context, starter, "Okay — ${hours}h.", Sass.settledBody(starter, hours))
            }

            // "Actually, now" → dismiss, then a short "you said now" check.
            Notify.ACTION_NOW -> {
                Notify.cancel(context, starter)
                ReminderScheduler.scheduleSnoozeMinutes(context, id, 15, depth + 1, promised = -1)
                Notify.postLine(context, starter, "Now. Good.", Sass.nowBody(starter))
            }

            Notify.ACTION_LEAVE -> {
                if (starter.storage == Storage.ROOM) {
                    // Refuse — a counter starter can't be left. Offer the real fix.
                    Notify.postCantLeave(context, starter, depth)
                } else {
                    // Fridge/freezer: honored. Next reminder is the long cadence.
                    Notify.cancel(context, starter)
                    ReminderScheduler.scheduleNext(context, starter)
                    Notify.postLine(context, starter, "Alright.", Sass.leaveHonoredBody(starter))
                }
            }

            Notify.ACTION_MOVE_FRIDGE -> {
                val fridged = starter.copy(storage = Storage.FRIDGE)
                repo.upsert(fridged)
                Notify.cancel(context, starter)
                ReminderScheduler.scheduleNext(context, fridged)
                Notify.postLine(context, fridged, "Into the fridge.", Sass.movedToFridgeBody(fridged))
            }
        }
    }
}

/** Recipe step timers. */
class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Timer"
        Notify.postSimple(
            context, ("timer$title").hashCode(), Channels.TIMERS,
            title = "⏱ $title", body = "Time's up — next step.",
        )
    }
}

/** Re-arm all base reminders after reboot or app update. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        async {
            StarterRepository.get(context).currentStarters().forEach {
                ReminderScheduler.scheduleNext(context, it)
            }
        }
    }
}
