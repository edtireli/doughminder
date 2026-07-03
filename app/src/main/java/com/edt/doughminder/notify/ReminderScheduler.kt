package com.edt.doughminder.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.edt.doughminder.data.Starter
import com.edt.doughminder.data.Storage
import com.edt.doughminder.data.intervalHours
import java.util.Calendar

object ReminderScheduler {

    private fun am(context: Context) = context.getSystemService(AlarmManager::class.java)

    private fun reminderIntent(context: Context, starterId: String, depth: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(Notify.EXTRA_STARTER_ID, starterId)
            .putExtra(Notify.EXTRA_DEPTH, depth)
        return PendingIntent.getBroadcast(
            context, (starterId + "d" + depth).hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** Next base reminder occurrence, honoring storage cadence + reminder hour. */
    fun nextTrigger(starter: Starter, now: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, starter.reminderHour)
            set(Calendar.MINUTE, starter.reminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return when (starter.storage) {
            Storage.ROOM -> {
                if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.timeInMillis
            }
            Storage.FRIDGE, Storage.FREEZER -> {
                // Base off the last feeding; land on the reminder hour of that day.
                val base = starter.lastFedEpochMillis ?: now
                var target = base + starter.storage.intervalHours * 3_600_000L
                if (target <= now) target = now + 3_600_000L // overdue → nudge within the hour
                Calendar.getInstance().apply {
                    timeInMillis = target
                    set(Calendar.HOUR_OF_DAY, starter.reminderHour)
                    set(Calendar.MINUTE, starter.reminderMinute)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
                }.timeInMillis
            }
        }
    }

    /** (Re)arm the base reminder for a starter. */
    fun scheduleNext(context: Context, starter: Starter) {
        setAlarm(context, nextTrigger(starter), reminderIntent(context, starter.id, 0))
    }

    fun cancel(context: Context, starterId: String) {
        am(context).cancel(reminderIntent(context, starterId, 0))
    }

    /** A negotiated snooze — re-nag in [hours] hours at the given escalation depth. */
    fun scheduleSnoozeHours(context: Context, starterId: String, hours: Int, depth: Int) {
        scheduleSnoozeMinutes(context, starterId, hours * 60, depth)
    }

    /** Short follow-up (e.g. the "you said now" verify check). */
    fun scheduleSnoozeMinutes(context: Context, starterId: String, minutes: Int, depth: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(Notify.EXTRA_STARTER_ID, starterId)
            .putExtra(Notify.EXTRA_DEPTH, depth)
        val pi = PendingIntent.getBroadcast(
            context, ("snooze" + starterId).hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        setAlarm(context, System.currentTimeMillis() + minutes * 60_000L, pi)
    }

    fun scheduleTimer(context: Context, title: String, minutes: Int) {
        val intent = Intent(context, TimerReceiver::class.java).putExtra("title", title)
        val pi = PendingIntent.getBroadcast(
            context, ("timer" + title).hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        setAlarm(context, System.currentTimeMillis() + minutes * 60_000L, pi)
    }

    private fun setAlarm(context: Context, at: Long, pi: PendingIntent) {
        val m = am(context)
        val canExact = Build.VERSION.SDK_INT < 31 || m.canScheduleExactAlarms()
        if (canExact) m.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        else m.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }
}
