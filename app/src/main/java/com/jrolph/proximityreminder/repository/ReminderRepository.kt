package com.jrolph.proximityreminder.repository

import com.google.api.services.drive.Drive
import com.jrolph.proximityreminder.network.DriveHelper
import com.jrolph.proximityreminder.database.Reminder
import com.jrolph.proximityreminder.ReminderSerializer
import com.jrolph.proximityreminder.database.ReminderDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class ReminderRepository(private val dao: ReminderDao,
                         private val externalScope: CoroutineScope
                         ) {

    private var driveApi: DriveHelper? = null

    //TODO code to synchronise local and cloud storage

    val allReminders: Flow<List<Reminder>> = flow {
        emitAll(dao.getAll())
    }

    fun getReminderById(reminderId: Int) : Flow<Reminder> {
        return dao.getReminderById(reminderId)
    }

    suspend fun deleteReminderById(reminderId: Int) {
        dao.deleteReminderById(reminderId)
        if (driveApi!=null) {
            driveApi!!.deleteFile("reminder$reminderId")
        }
    }

    suspend fun insertReminder(reminder: Reminder) {
        dao.insert(reminder)
        if (driveApi!=null) {
            driveApi!!.writeFile("reminder$reminder.id", ReminderSerializer.reminderToString(reminder))
        }
    }

    suspend fun updateReminder(reminder: Reminder) {
        dao.update(reminder)
        if (driveApi!=null) {
            driveApi!!.writeFile("reminder$reminder.id", ReminderSerializer.reminderToString(reminder))
        }
    }

    suspend fun deleteReminder(reminder: Reminder) {
        dao.delete(reminder)
        if (driveApi!=null) {
            driveApi!!.deleteFile("reminder$reminder.id")
        }
    }

    fun registerDrive(drive: Drive) { driveApi = DriveHelper(drive) }

}