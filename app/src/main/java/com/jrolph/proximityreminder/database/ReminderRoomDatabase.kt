package com.jrolph.proximityreminder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

@Database(
    entities=[Reminder::class],
    version=1
)
abstract class ReminderRoomDatabase : RoomDatabase() {
    abstract val reminderDao: ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ReminderRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance!=null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderRoomDatabase::class.java,
                    "reminder_room_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}