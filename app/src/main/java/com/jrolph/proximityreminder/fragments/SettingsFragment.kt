package com.jrolph.proximityreminder.fragments

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.preference.PreferenceFragmentCompat
import com.jrolph.proximityreminder.R

class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Listen for when preferences change
        preferenceScreen.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        // Refresh UI if dark mode is changed
        if (key!="dark_mode") return
        val darkMode = prefs!!.getBoolean(key, true)
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }
        else {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }
    }

}