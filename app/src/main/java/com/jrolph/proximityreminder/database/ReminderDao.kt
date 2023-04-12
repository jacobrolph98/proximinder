package com.jrolph.proximityreminder.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("Select * FROM reminders")
    fun getAll(): Flow<List<Reminder>>
    
    @Query("Select * FROM reminders WHERE id = :reminderId")
    fun getReminderById(reminderId: Int): Flow<Reminder>

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    fun deleteReminderById(reminderId: Int)

    @Insert
    fun insert(reminder: Reminder)

    @Update
    fun update(reminder: Reminder)

    @Delete
    fun delete(reminder: Reminder)
}