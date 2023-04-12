package com.jrolph.proximityreminder.network

import com.fasterxml.jackson.databind.ObjectMapper
import com.jrolph.proximityreminder.database.Reminder

class UserService {

    private val retrofit = RetrofitHelper.getInstance()
    private val reminderApi = retrofit.create(ReminderApi::class.java)

    fun successfulUsersResponse() {
        val usersResponse = reminderApi.fetchReminders()
            .execute()
        val successful = usersResponse.isSuccessful
        val httpStatusCode = usersResponse.code()
        val httpStatusMessage = usersResponse.message()
        val body: List<Reminder>? = usersResponse.body()
    }

    fun errorUsersResponse() {
        val usersResponse = reminderApi.fetchReminders()
            .execute()

        val errorBody = usersResponse.errorBody()

        val mapper = ObjectMapper()
        val mappedBody: ErrorResponse? = errorBody?.let { notNullErrorBody ->
            mapper.readValue(notNullErrorBody.toString(), ErrorResponse::class.java)
        }
    }

    fun headersUsersResponse() {
        val usersResponse = reminderApi.fetchReminders()
            .execute()

        val headers = usersResponse.headers()
        val customHeaderValue = headers["custom-header"]
    }

}