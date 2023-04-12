package com.jrolph.proximityreminder

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ReminderRemoteDataSource(private val ioDispatcher: CoroutineDispatcher) {

    /*
    suspend fun fetchReminders(): List<Reminder> =
        withContext(ioDispatcher) {
            reminderApi.fetchReminders()
        }

     */

}