package com.jrolph.proximityreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jrolph.proximityreminder.activities.MainActivity
import com.jrolph.proximityreminder.viewmodels.ReminderWriterUiState
import kotlinx.coroutines.launch

private const val NOTIFICATION_ID = 33
private const val CHANNEL_ID = "GeofenceChannel"

fun createChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_notif_channel_name),

            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setShowBadge(false)
            }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = context.getString(R.string.reminder_notif_channel_description)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

fun sendGeofenceEnteredNotification(context: Context, foundIndex: Int) {
    val repository = (context.applicationContext as ProximityReminderApplication).repository
    val scope = (context.applicationContext as ProximityReminderApplication).applicationScope

    val contentIntent = Intent(context, MainActivity::class.java)
    contentIntent.putExtra("geofenceIndex", foundIndex)
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val mapImage = BitmapFactory.decodeResource(
        context.resources,
        androidx.core.R.drawable.notification_bg
    )
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(mapImage)
        .bigLargeIcon(null)

    scope.launch {
        repository.getReminderById(foundIndex).collect { rem ->
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.geofence_entered, rem.name, rem.note))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(androidx.core.R.drawable.notification_bg)
                .setStyle(bigPicStyle)
                .setLargeIcon(mapImage)
            with (NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }
}