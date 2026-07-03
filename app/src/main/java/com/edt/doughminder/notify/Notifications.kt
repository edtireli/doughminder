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
import com.edt.doughminder.data.Sass
import com.edt.doughminder.data.Starter
import com.edt.doughminder.data.Storage

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
                .apply { description = "Your starter negotiating, guilt-tripping, and arguing back" }
        )
        nm.createNotificationChannel(
            NotificationChannel(TIMERS, "Recipe timers", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Step timers for recipes" }
        )
    }
}

object Notify {
    // Actions in the negotiation state machine
    const val ACTION_FED = "com.edt.doughminder.FED"
    const val ACTION_LATER = "com.edt.doughminder.LATER"          // nag → "when?"
    const val ACTION_PICK = "com.edt.doughminder.PICK"            // "when?" → "are you sure?"
    const val ACTION_CONFIRM = "com.edt.doughminder.CONFIRM"      // settle on a duration
    const val ACTION_NOW = "com.edt.doughminder.NOW"             // "actually, now"
    const val ACTION_LEAVE = "com.edt.doughminder.LEAVE"         // leave me alone
    const val ACTION_MOVE_FRIDGE = "com.edt.doughminder.FRIDGE"  // escape hatch: refrigerate

    const val EXTRA_STARTER_ID = "starter_id"
    const val EXTRA_HOURS = "hours"
    const val EXTRA_DEPTH = "depth"

    fun canPost(context: Context): Boolean =
        if (android.os.Build.VERSION.SDK_INT >= 33)
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        else true

    fun notificationId(starter: Starter) = starter.id.hashCode()

    private var reqSeq = 1
    private fun action(
        context: Context,
        actionName: String,
        starterId: String,
        hours: Int,
        depth: Int,
    ): PendingIntent {
        val intent = Intent(context, ActionReceiver::class.java)
            .setAction(actionName)
            .putExtra(EXTRA_STARTER_ID, starterId)
            .putExtra(EXTRA_HOURS, hours)
            .putExtra(EXTRA_DEPTH, depth)
        // Unique request code per (action, hours) so buttons don't collide.
        val code = (actionName + starterId + hours + depth).hashCode()
        return PendingIntent.getBroadcast(
            context, code, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openApp(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun base(context: Context, channel: String, title: String, body: String) =
        NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openApp(context))
            .setAutoCancel(true)

    private fun post(context: Context, starter: Starter, builder: NotificationCompat.Builder) {
        if (!canPost(context)) return
        NotificationManagerCompat.from(context).notify(notificationId(starter), builder.build())
    }

    // ── Stage 1: the nag ────────────────────────────────────────────────
    fun postNag(context: Context, starter: Starter, depth: Int, channel: String = Channels.REMINDERS) {
        val obj = Sass.objPronoun(starter)
        val b = base(context, channel, Sass.nagTitle(starter, depth), Sass.nagBody(starter, depth))
            .addAction(0, "Yes, I fed $obj", action(context, ACTION_FED, starter.id, 0, depth))
            .addAction(0, "Later", action(context, ACTION_LATER, starter.id, 0, depth))
            .addAction(0, "Leave me alone", action(context, ACTION_LEAVE, starter.id, 0, depth))
        post(context, starter, b)
    }

    // ── Stage 2: "when??" ───────────────────────────────────────────────
    fun postWhen(context: Context, starter: Starter, depth: Int) {
        val b = base(context, Channels.ARGUMENTS, Sass.whenTitle(starter), Sass.whenBody(starter))
            .addAction(0, "In 1h", action(context, ACTION_PICK, starter.id, 1, depth))
            .addAction(0, "In 3h", action(context, ACTION_PICK, starter.id, 3, depth))
            .addAction(0, "In 6h", action(context, ACTION_PICK, starter.id, 6, depth))
        post(context, starter, b)
    }

    // ── Stage 3: "are you sure??" — counter one step shorter ────────────
    fun postConfirm(context: Context, starter: Starter, hours: Int, depth: Int) {
        val counter = Sass.shorter(hours)
        val b = base(context, Channels.ARGUMENTS, Sass.confirmTitle(starter, hours), Sass.confirmBody(starter, hours))
        if (hours <= 1) {
            b.addAction(0, "1 hour it is", action(context, ACTION_CONFIRM, starter.id, 1, depth))
            b.addAction(0, "Actually, now", action(context, ACTION_NOW, starter.id, 0, depth))
        } else {
            b.addAction(0, "No — ${hours}h", action(context, ACTION_CONFIRM, starter.id, hours, depth))
            b.addAction(0, "Fine, ${counter}h", action(context, ACTION_CONFIRM, starter.id, counter, depth))
            b.addAction(0, "Now", action(context, ACTION_NOW, starter.id, 0, depth))
        }
        post(context, starter, b)
    }

    // ── "Leave me alone" refused for a room-temp starter ────────────────
    fun postCantLeave(context: Context, starter: Starter, depth: Int) {
        val b = base(context, Channels.ARGUMENTS, Sass.cantLeaveTitle(starter), Sass.cantLeaveBody(starter))
            .addAction(0, "Move to fridge", action(context, ACTION_MOVE_FRIDGE, starter.id, 0, depth))
            .addAction(0, "Ugh, fine — 1h", action(context, ACTION_CONFIRM, starter.id, 1, depth))
        post(context, starter, b)
    }

    // ── Simple, button-less lines (settled, fed, parting, verify) ───────
    fun postLine(context: Context, starter: Starter, title: String, body: String) {
        post(context, starter, base(context, Channels.ARGUMENTS, title, body))
    }

    fun cancel(context: Context, starter: Starter) {
        NotificationManagerCompat.from(context).cancel(notificationId(starter))
    }

    fun postSimple(context: Context, id: Int, channel: String, title: String, body: String) {
        if (!canPost(context)) return
        NotificationManagerCompat.from(context).notify(id, base(context, channel, title, body).build())
    }
}
