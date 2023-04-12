package com.jrolph.proximityreminder

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.jrolph.proximityreminder.database.ReminderRoomDatabase
import com.jrolph.proximityreminder.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.*

class ProximityReminderApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val database: ReminderRoomDatabase by lazy { ReminderRoomDatabase.getDatabase(this, applicationScope)}
    val repository: ReminderRepository by lazy { ReminderRepository(database.reminderDao,  applicationScope) }

    fun registerDrive(account: GoogleSignInAccount) {
        val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
            this, Collections.singleton(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account
        val drive =  Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName(getString(R.string.app_name))
            .build()
        repository.registerDrive(drive)
    }
}