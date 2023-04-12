package com.jrolph.proximityreminder

import com.jrolph.proximityreminder.database.Reminder

class ReminderSerializer {

    companion object {
        fun reminderToString(reminder: Reminder): String {
            return reminder.note + "|" + reminder.radius + "|" + reminder.longitude + "|" + reminder.latitude + "|" + (reminder.name
                ?: "")
        }

        fun stringToReminder(content: String): Reminder {
            val elements = content.split("|")
            val note = elements[0]
            val radius = elements[1].toFloat()
            val longitude = elements[2].toFloat()
            val latitude = elements[3].toFloat()
            val name = elements[4]
            return Reminder(null, note, radius, longitude, latitude, name)
        }
    }

}