package com.jrolph.proximityreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.jrolph.proximityreminder.activities.MainActivity

class GeofenceBroadcastReceiver: BroadcastReceiver() {

    // When a broadcast related to geofences is received, this is called
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != MainActivity.ACTION_GEOFENCE_EVENT) { return }

        val geofencingEvent = GeofencingEvent.fromIntent(intent)!!
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("Log", errorMessage)
            return
        }

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.v("Log", "Geofence entered")
            val triggeredFences = geofencingEvent.triggeringGeofences
            triggeredFences!!.forEach {
                createChannel(context)
                val reminderId = it.requestId.substring(8).toInt()
                sendGeofenceEnteredNotification(context, reminderId)
            }
        }
    }
}