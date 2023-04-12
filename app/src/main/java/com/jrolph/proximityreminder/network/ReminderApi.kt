package com.jrolph.proximityreminder.network

import com.jrolph.proximityreminder.database.Reminder
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface  ReminderApi {
    @GET("reminders")
    fun fetchReminders(): Call<List<Reminder>>

    @PUT("reminders")
    fun setReminder()

    @POST("reminder")
    fun addReminder()
}