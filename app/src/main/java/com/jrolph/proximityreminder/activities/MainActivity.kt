package com.jrolph.proximityreminder.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.api.services.drive.DriveScopes
import com.jrolph.proximityreminder.*
import com.jrolph.proximityreminder.databinding.ActivityMainBinding
import com.jrolph.proximityreminder.fragments.AccountFragment
import com.jrolph.proximityreminder.fragments.ReminderListFragment
import com.jrolph.proximityreminder.fragments.SettingsFragment
import com.jrolph.proximityreminder.viewmodels.ReminderViewModel
import com.jrolph.proximityreminder.viewmodels.ReminderViewModelFactory

class MainActivity : AppCompatActivity() {

    // Create viewmodel using factory to pass repository as parameter during initialization
    val viewModel: ReminderViewModel by viewModels {
        ReminderViewModelFactory((application as ProximityReminderApplication).repository)
    }

    private val fragManager = this@MainActivity.supportFragmentManager
    private lateinit var binding: ActivityMainBinding

    // Shared preferences for storing dark mode and other settings
    private lateinit var sharedPrefs: SharedPreferences

    // Activity result for requesting permissions
    lateinit var permissionResultLauncher: ActivityResultLauncher<Intent>

    // Sign in client for google
    private lateinit var googleSignInClient: GoogleSignInClient

    // Client for Geofence
    private lateinit var geofencingClient: GeofencingClient
    // This is relevant for determining which location permissions need to be requested for Geofence
    private val runningQorLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    // Constant values accessible inside & out of main activity to sync result codes and keys
    companion object {
        const val LOG_TAG = "LOG"
        const val REMINDER_ID = "id"
        const val NOTE = "note"
        const val RADIUS = "radius"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val NAME = "name"
        const val SIGNED_IN = "signed_in"

        const val ACTION_GEOFENCE_EVENT = "MainActivity.proximity.action.ACTION_GEOFENCE_EVENT"

        private const val LOCATION_REQUEST_CODE = 29
        private const val REQUEST_BOTH_LOCATION_PERMISSIONS_CODE = 33
        private const val REQUEST_FOREGROUND_LOCATION_PERMISSION_CODE = 34
    }


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // Respond to successful login result, returning to home page
    private val loginResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fragManager.commit{
                setReorderingAllowed(true)
                replace<ReminderListFragment>(R.id.fragment_container)
            }
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.data))
        }
    }

    // Check for previously signed in account
    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null)
            viewModel.sendGoogleAccount(account)
        updateAccountStatus(account)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Determine whether to use dark mode
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode = sharedPrefs.getBoolean("dark_mode", true)
        if (darkMode) { AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES) }
        else { AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO) }

        // Prepare sign in client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // Bind UI objects
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // configure activity result launcher for permission request
        permissionResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(LOG_TAG, "permission request granted")
            }
        }
        geofencingClient = LocationServices.getGeofencingClient(this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) :Boolean {
        return when (item.itemId) {
            // When settings menu pressed
            R.id.action_settings -> {
                fragManager.commit{
                    setReorderingAllowed(true)
                    addToBackStack("MainActivity")
                    replace<SettingsFragment>(R.id.fragment_container)
                }
                true
            }
            // When account menu pressed
            R.id.action_account -> {
                // Send user login status as argument to account fragment
                val bundle = bundleOf(SIGNED_IN to viewModel.isLoggedIn())
                fragManager.commit{
                    setReorderingAllowed(true)
                    addToBackStack("MainActivity")
                    replace<AccountFragment>(R.id.fragment_container, args = bundle)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> { checkDeviceLocationSettingsAndStartGeofence(false) }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If user did not grant permission, declare a warning, otherwise begin geofencing
        if (
            grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_BOTH_LOCATION_PERMISSIONS_CODE &&
                    grantResults[1] == PackageManager.PERMISSION_DENIED))
        {
            Log.d(LOG_TAG, "This app is worthless without location. Should tell the user here")
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        fragManager.popBackStack("MainActivity", 0)
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return ReminderViewModelFactory((application as ProximityReminderApplication).repository)
    }

    // BUTTON PRESS FUNCTIONS
    fun signInPressed() {
        val signInIntent = googleSignInClient.signInIntent
        loginResultLauncher.launch(signInIntent)
    }

    fun signOutPressed() {
        googleSignInClient.signOut()
            .addOnCompleteListener {
                handleSignOut()
            }
    }

    fun deleteAccountPressed() {
        googleSignInClient.revokeAccess()
            .addOnCompleteListener{
                handleSignOut()
            }
    }

    // PRIVATE FUNCTIONS

    private fun handleSignOut() {
        updateAccountStatus(null)
        viewModel.sendGoogleAccount(null)
        fragManager.commit{
            setReorderingAllowed(true)
            replace<ReminderListFragment>(R.id.fragment_container)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.result
            updateAccountStatus(account)
            viewModel.sendGoogleAccount(account)
        }
        catch (exception: ApiException) {
            Log.e("Error", "Sign in result failed. Code: ${exception.statusCode}")
            updateAccountStatus(null)
        }
    }

    private fun updateAccountStatus(account: GoogleSignInAccount?) {
        val message: String = "account " + if (account==null ) "is null" else "logged in"
        Log.d(LOG_TAG, message)
        // Update UI
    }

    private fun isLocationPermissionApproved(): Boolean {
        // Check if all required location permissions are allowed
        val foregroundApproved = (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundApproved = if (runningQorLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        else { true }
        return foregroundApproved && backgroundApproved
    }

    private fun requestLocationPermissions() {
        if (isLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQorLater -> {
                permissionsArray += android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_BOTH_LOCATION_PERMISSIONS_CODE
            }
            else -> REQUEST_FOREGROUND_LOCATION_PERMISSION_CODE
        }
        ActivityCompat.requestPermissions(this@MainActivity, permissionsArray, resultCode)
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        // FAIL
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(this@MainActivity, LOCATION_REQUEST_CODE) // code to request turn location on
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(LOG_TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.appBar,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }

        // SUCCESS
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofenceForClue()
            }
        }
    }

    private fun addGeofenceForClue() {
        //TODO geofence code on location response success
    }
}