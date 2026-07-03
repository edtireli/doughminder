package com.edt.doughminder.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.edt.doughminder.data.Starter
import java.util.Calendar

object ReminderScheduler {

    private fun alarmManager(context: Context) =
        context.getSystemService(AlarmManager::class.java)

    private fun reminderIntent(context: Context, starterId: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(Notify.EXTRA_STARTER_ID, starterId)
        return PendingIntent.getBroadcast(
            context,
            starterId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** Next occurrence of the starter's reminder time, today or tomorrow. */
    fun nextTrigger(hour: Int, minute: Int, now: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    fun scheduleDaily(context: Context, starter: Starter) {
        val at = nextTrigger(starter.reminderHour, starter.reminderMinute)
        setAlarm(context, at, reminderIntent(context, starter.id))
    }

    fun cancel(context: Context, starterId: String) {
        alarmManager(context).cancel(reminderIntent(context, starterId))
    }

    /** Follow-up nag partway through an argument ("later" snooze). */
    fun scheduleNag(context: Context, starterId: String, depth: Int, delayMinutes: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(Notify.EXTRA_STARTER_ID, starterId)
            .putExtra(Notify.EXTRA_DEPTH, depth)
        val pi = PendingIntent.getBroadcast(
            context,
            ("nag" + starterId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        setAlarm(context, System.currentTimeMillis() + delayMinutes * 60_000L, pi)
    }

    fun scheduleTimer(context: Context, title: String, minutes: Int) {
        val intent = Intent(context, TimerReceiver::class.java)
            .putExtra("title", title)
        val pi = PendingIntent.getBroadcast(
            context,
            ("timer" + title + System.currentTimeMillis()).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        setAlarm(context, System.currentTimeMillis() + minutes * 60_000L, pi)
    }

    private fun setAlarm(context: Context, at: Long, pi: PendingIntent) {
        val am = alarmManager(context)
        val canExact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        if (canExact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }
}
