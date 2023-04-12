package com.jrolph.proximityreminder.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.adevinta.leku.LocationPickerActivity
import com.jrolph.proximityreminder.R
import com.jrolph.proximityreminder.activities.MainActivity
import com.jrolph.proximityreminder.databinding.FragmentReminderWriterBinding
import com.jrolph.proximityreminder.viewmodels.ReminderViewModel

class ReminderWriterFragment : Fragment() {

    private val viewModel by activityViewModels<ReminderViewModel>()

    // ID of the reminder passed to this fragment as an argument, if any
    // Determines whether we are editing or creating a new reminder
    private var reminderId: Int? = null

    // cached reminder data from reminder being edited, or from fields being filled
    private var note: String = ""
    private var radius: Float = -0.1f
    private var longitude: Float = 0.0f
    private var latitude: Float = 0.0f
    private var name: String? = null

    private var locationSet: Boolean = false

    private var _binding: FragmentReminderWriterBinding? = null
    private val binding get() = _binding!!

    // Receive activity result from Leku and ensure valid result
    private val lekuActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            onLekuResult(result.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If fragment is opened with arguments (Reminder values), load them into fragment data
        arguments?.let {
            reminderId = it.getInt(MainActivity.REMINDER_ID)
            note = it.getString(MainActivity.NOTE)!!
            radius = it.getFloat(MainActivity.RADIUS)
            longitude = it.getFloat(MainActivity.LONGITUDE)
            latitude = it.getFloat(MainActivity.LATITUDE)
            name = it.getString(MainActivity.NAME)
            locationSet = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentReminderWriterBinding.inflate(inflater, container, false)
        // Check whether need to fill in reminder data if editing, or to leave things blank for creating
        if (reminderId!=null) {
            setCoordinatesVisible(true)
            refreshFieldValues()
            binding.deleteReminderButton.setOnClickListener { deleteClicked() }
            binding.deleteReminderButton.visibility = View.VISIBLE
        }
        else {
            setCoordinatesVisible(false)
            binding.deleteReminderButton.visibility = View.INVISIBLE
        }
        // Add listeners for when user changes values in fields and presses buttons
        binding.editTextNote.addTextChangedListener { editNoteChanged() }
        binding.editTextRadius.addTextChangedListener { editRadiusChanged() }
        binding.editTextName.addTextChangedListener { editNameChanged() }
        binding.setLocationButton.setOnClickListener { setLocationClicked() }
        binding.saveReminderButton.setOnClickListener { saveClicked() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Set whether long/lat coordinates are visible
    private fun setCoordinatesVisible(visible: Boolean) {
        binding.latTextView.visibility = if(visible) View.VISIBLE else View.INVISIBLE
        binding.longTextView.visibility = if(visible) View.VISIBLE else View.INVISIBLE
    }

    // Set value stored in fragment
    private fun editNoteChanged() {
        note = binding.editTextNote.text.toString()
    }

    // Set value in fragment, checking for input that may result in numerical errors
    private fun editRadiusChanged() {
        val editText = binding.editTextRadius.text.toString()
        try { radius = editText.toFloat() }
        catch (exception: NumberFormatException) {
            Log.e("Error", exception.message.toString())
        }
    }

    // Set value stored in fragment
    private fun editNameChanged() {
        name = binding.editTextName.text.toString()
    }

    // Set UI field values equal to value stored in fragment
    private fun refreshFieldValues() {
        binding.editTextNote.setText(note)
        binding.editTextRadius.setText(radius.toString())
        if (name!= null) { binding.editTextName.setText(name) }
        binding.longTextView.text = getString(R.string.longitude_label, longitude)
        binding.latTextView.text = getString(R.string.latitude_label, latitude)
    }

    // Open Leku activity to pick location
    private fun setLocationClicked() {
        val placePickerIntent = LocationPickerActivity.Builder()
            .build(requireActivity())
        lekuActivityResultLauncher.launch(placePickerIntent)
    }

    // Make sure fields are filled, save to database and return to home
    private fun saveClicked() {
        // guard clauses to only continue if all required fields are filled in
        if (note.isEmpty())
            return
        else if (radius <= 0.0f)
            return
        else if (!locationSet)
            return

        if (reminderId != null)  // writing to existing reminder
        { viewModel.updateReminder(reminderId!!, note, radius, longitude, latitude, name) }
        else //writing to new reminder
        { viewModel.insertReminder(note, radius, longitude, latitude, name) }
        returnToList()
    }

    // Confirm whether user wants to delete reminder
    private fun deleteClicked() {
        if (reminderId==null) { return } // no bundle is selected, delete button shouldn't even show in this case
        val dialogBuilder = AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
        dialogBuilder.setTitle(R.string.delete_dialog_title)
        dialogBuilder.setMessage(R.string.confirm_delete)
        dialogBuilder.setPositiveButton(R.string.yes) { _, _ -> doDelete() }
        dialogBuilder.setNegativeButton(R.string.no) {_, _ -> }
        dialogBuilder.show()
    }

    // When user has confirmed they want to delete reminder
    private fun doDelete() {
        viewModel.deleteReminderById(reminderId!!)
        returnToList()
    }

    // Return to home page showing list of all reminders
    private fun returnToList() {
        parentFragmentManager.commit{
            setReorderingAllowed(true)
            replace<ReminderListFragment>(R.id.fragment_container)
        }
    }

    // When the user successfully selects a location
    private fun onLekuResult(data: Intent?) {
        latitude = data!!.getDoubleExtra(MainActivity.LATITUDE, 0.0).toFloat()
        longitude = data!!.getDoubleExtra(MainActivity.LONGITUDE, 0.0).toFloat()
        setCoordinatesVisible(true)
        refreshFieldValues()
        locationSet = true
    }


}