package com.edt.doughminder.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.edt.doughminder.MainActivity
import com.edt.doughminder.R
import com.edt.doughminder.data.Starter

object Channels {
    const val REMINDERS = "reminders"
    const val ARGUMENTS = "arguments"
    const val TIMERS = "timers"

    fun createAll(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(REMINDERS, "Feeding reminders", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Daily 'did you feed it yet' reminders" }
        )
        nm.createNotificationChannel(
            NotificationChannel(ARGUMENTS, "Arguments", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Your starter arguing back" }
        )
        nm.createNotificationChannel(
            NotificationChannel(TIMERS, "Recipe timers", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Step timers for recipes" }
        )
    }
}

object Notify {
    const val ACTION_FED = "com.edt.doughminder.FED"
    const val ACTION_LATER = "com.edt.doughminder.LATER"
    const val ACTION_LEAVE = "com.edt.doughminder.LEAVE"
    const val EXTRA_STARTER_ID = "starter_id"
    const val EXTRA_DEPTH = "depth"

    fun canPost(context: Context): Boolean =
        if (android.os.Build.VERSION.SDK_INT >= 33)
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        else true

    private fun actionIntent(context: Context, action: String, starterId: String, depth: Int): PendingIntent {
        val intent = Intent(context, ActionReceiver::class.java)
            .setAction(action)
            .putExtra(EXTRA_STARTER_ID, starterId)
            .putExtra(EXTRA_DEPTH, depth)
        return PendingIntent.getBroadcast(
            context,
            (action + starterId + depth).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun contentIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    fun notificationId(starter: Starter) = starter.id.hashCode()

    /** The nag itself — morning reminder or a follow-up in the argument. */
    fun postNag(
        context: Context,
        starter: Starter,
        title: String,
        body: String,
        depth: Int,
        channel: String,
        withActions: Boolean = true,
    ) {
        if (!canPost(context)) return
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent(context))
            .setAutoCancel(true)
        if (withActions) {
            builder
                .addAction(0, "Yes, I fed ${obj(starter)}", actionIntent(context, ACTION_FED, starter.id, depth))
                .addAction(0, "Later", actionIntent(context, ACTION_LATER, starter.id, depth))
                .addAction(0, "Leave me alone", actionIntent(context, ACTION_LEAVE, starter.id, depth))
        }
        NotificationManagerCompat.from(context).notify(notificationId(starter), builder.build())
    }

    private fun obj(starter: Starter) = when (starter.gender) {
        com.edt.doughminder.data.Gender.SHE -> "her"
        com.edt.doughminder.data.Gender.HE -> "him"
        com.edt.doughminder.data.Gender.THEY -> "them"
    }

    fun postSimple(context: Context, id: Int, channel: String, title: String, body: String) {
        if (!canPost(context)) return
        val n = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, n)
    }
}
