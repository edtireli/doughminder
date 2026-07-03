package com.edt.doughminder

import android.app.Application
import com.edt.doughminder.data.StarterRepository
import com.edt.doughminder.notify.Channels
import com.edt.doughminder.notify.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DoughApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Channels.createAll(this)
        // Idempotent re-arm on every process start — covers force-stop, crashes,
        // and anything BootReceiver missed.
        CoroutineScope(Dispatchers.Default).launch {
            StarterRepository.get(this@DoughApp).currentStarters().forEach {
                ReminderScheduler.scheduleDaily(this@DoughApp, it)
            }
        }
    }
}
