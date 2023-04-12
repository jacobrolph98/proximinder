package com.jrolph.proximityreminder.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.jrolph.proximityreminder.database.Reminder
import com.jrolph.proximityreminder.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    // UI State of home page, list of reminders
    private var _uiStateList = MutableStateFlow(ReminderListUiState.Success(emptyList()))
    val uiStateList: StateFlow<ReminderListUiState> = _uiStateList.asStateFlow()

    // UI State of writer page, editing or creating reminder
    private var _uiStateWriter = MutableStateFlow(ReminderWriterUiState.Success(null))
    val uiStateWriter: StateFlow<ReminderWriterUiState> = _uiStateWriter.asStateFlow()

    // List of geofences
    private val geofenceList: ArrayList<Geofence> = arrayListOf()

    private var googleSignInAccount: GoogleSignInAccount? = null

    init {
        getAllReminders()
    }

    fun recreateAllGeofences() {
        geofenceList.clear()
        _uiStateList.value.reminders.forEach { it ->
            geofenceList.add(Geofence.Builder()
                .setRequestId("reminder"+it.id.toString())
                .setCircularRegion(
                    it.latitude.toDouble(),
                    it.longitude.toDouble(),
                    it.radius * 1000 // Geofence measures radius in metres, reminder value is stored in Km
                )
                .setExpirationDuration(604800000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .build())
        }

    }

    fun createGeofence(reminder: Reminder) {
        geofenceList.add(Geofence.Builder()
            .setRequestId("reminder"+reminder.id.toString())
            .setCircularRegion(
                reminder.latitude.toDouble(),
                reminder.longitude.toDouble(),
                reminder.radius
            )
            .setExpirationDuration(604800000)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(10000) // 10 seconds
            .build())
    }

    private fun clearGeofence(reminderid: Int) {

    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder(). apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
            addGeofences(geofenceList)
        }.build()
    }

    fun getAllReminders() {
        viewModelScope.launch {
            repository.allReminders.collect { reminders -> _uiStateList.value = ReminderListUiState.Success(reminders)}
        }
    }

    fun getReminderById(id: Int) {
        viewModelScope.launch {
            repository.getReminderById(id).collect { reminder -> _uiStateWriter.value = ReminderWriterUiState.Success(reminder)}
        }
    }

    fun deleteReminderById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReminderById(id)
        }
    }

    // Add new reminder from given data through repository, and create geofence
    fun insertReminder(note: String, radius: Float, longitude: Float, latitude: Float, name: String?) {
        val reminder = getReminderItem(null, note, radius, longitude, latitude, name)
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertReminder(reminder)
        }
        createGeofence(reminder)
    }

    // Update reminder with given data through repository
    fun updateReminder(id: Int, note: String, radius: Float, longitude: Float, latitude: Float, name: String?) {
        val reminder = getReminderItem(id, note, radius, longitude, latitude, name)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateReminder(reminder)
        }
    }

    fun isLoggedIn(): Boolean {
        if (googleSignInAccount!= null)
            return true
        return false
    }

    // Send google account from Activity's sign in result to ViewModel
    fun sendGoogleAccount(account: GoogleSignInAccount?) {
        googleSignInAccount = account
        Log.d("Log", "google account received")
        if (account==null) Log.d("Log", "Account is null")
    }

    // return a reminder object provided field data
    private fun getReminderItem(id: Int?, note: String, radius: Float, longitude: Float, latitude: Float, name: String?): Reminder {
        return Reminder(id, note, radius, longitude, latitude, name)
    }
}

sealed class ReminderListUiState {
    data class Success(val reminders: List<Reminder>): ReminderListUiState()
    data class Error(val exception: Throwable) :ReminderListUiState()
}

sealed class ReminderWriterUiState {
    data class Success(val reminder: Reminder?): ReminderWriterUiState()
    data class Error(val exception: Throwable) :ReminderWriterUiState()
}

// Factory for creating ViewModel with repository
class ReminderViewModelFactory(private val repository: ReminderRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}