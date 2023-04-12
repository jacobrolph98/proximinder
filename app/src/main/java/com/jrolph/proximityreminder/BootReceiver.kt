package com.jrolph.proximityreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.activity.viewModels

class BootReceiver: BroadcastReceiver() {

    // intent filter is set in android manifest, when this class' onReceive is called when device is booted
    // Geofences must be recreated on device reboot
    override fun onReceive(context: Context, intent: Intent?) {
        // TODO Reregister Geofences from reminders
    }
}