package com.edt.doughminder.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.edt.doughminder.data.Sass
import com.edt.doughminder.data.StarterRepository
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

/** Fires at the scheduled reminder time (or a snoozed follow-up). */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = async {
        val repo = StarterRepository.get(context)
        val id = intent.getStringExtra(Notify.EXTRA_STARTER_ID) ?: return@async
        val depth = intent.getIntExtra(Notify.EXTRA_DEPTH, 0)
        val starter = repo.getStarter(id) ?: return@async

        if (depth == 0) {
            // The daily opener. Skip the nag if already fed today, but always
            // re-arm tomorrow's alarm.
            if (!starter.fedToday()) {
                Notify.postNag(
                    context, starter,
                    title = Sass.morningTitle(starter),
                    body = Sass.morningBody(starter),
                    depth = 0,
                    channel = Channels.REMINDERS,
                )
            }
            ReminderScheduler.scheduleDaily(context, starter)
        } else {
            // Snoozed follow-up in an ongoing argument.
            if (!starter.fedToday()) {
                Notify.postNag(
                    context, starter,
                    title = "Still waiting. So is ${starter.name}.",
                    body = Sass.laterReply(starter, depth),
                    depth = depth,
                    channel = Channels.ARGUMENTS,
                )
            }
        }
    }
}

/** Handles the buttons on the notification — the user's side of the argument. */
class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = async {
        val repo = StarterRepository.get(context)
        val id = intent.getStringExtra(Notify.EXTRA_STARTER_ID) ?: return@async
        val depth = intent.getIntExtra(Notify.EXTRA_DEPTH, 0)
        val starter = repo.getStarter(id) ?: return@async
        val settings = repo.currentSettings()
        val nm = NotificationManagerCompat.from(context)

        when (intent.action) {
            Notify.ACTION_FED -> {
                repo.markFed(id)
                nm.cancel(Notify.notificationId(starter))
                if (settings.argueBack) {
                    Notify.postSimple(
                        context, Notify.notificationId(starter), Channels.ARGUMENTS,
                        title = starter.name, body = Sass.fedReply(starter),
                    )
                }
            }

            Notify.ACTION_LATER -> {
                nm.cancel(Notify.notificationId(starter))
                if (settings.argueBack) {
                    // Immediate comeback, then a re-nag after the snooze delay.
                    Notify.postNag(
                        context, starter,
                        title = "“Later.” Sure.",
                        body = Sass.laterReply(starter, depth),
                        depth = depth + 1,
                        channel = Channels.ARGUMENTS,
                    )
                    ReminderScheduler.scheduleNag(context, id, depth + 1, settings.nagDelayMinutes)
                }
            }

            Notify.ACTION_LEAVE -> {
                nm.cancel(Notify.notificationId(starter))
                if (settings.argueBack) {
                    // One parting shot, no buttons, then silence until tomorrow.
                    Notify.postNag(
                        context, starter,
                        title = "Fine.",
                        body = Sass.leaveMeAloneReply(starter),
                        depth = depth,
                        channel = Channels.ARGUMENTS,
                        withActions = false,
                    )
                }
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

/** Re-arm all alarms after reboot or app update. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        async {
            StarterRepository.get(context).currentStarters().forEach {
                ReminderScheduler.scheduleDaily(context, it)
            }
        }
    }
}
