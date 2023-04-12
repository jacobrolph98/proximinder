package com.jrolph.proximityreminder.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.dragselectrecyclerview.DragSelectReceiver
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener
import com.jrolph.proximityreminder.*
import com.jrolph.proximityreminder.activities.MainActivity
import com.jrolph.proximityreminder.database.Reminder
import com.jrolph.proximityreminder.databinding.FragmentReminderListBinding
import com.jrolph.proximityreminder.viewmodels.ReminderListUiState
import com.jrolph.proximityreminder.viewmodels.ReminderViewModel
import kotlinx.coroutines.launch

class ReminderListFragment : Fragment(), ReminderAdapter.OnItemClickListener, ReminderAdapter.OnItemLongClickListener {

    private val viewModel: ReminderViewModel by activityViewModels()

    private var _binding: FragmentReminderListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReminderAdapter
    private lateinit var touchListener: DragSelectTouchListener
    private lateinit var receiver: DragSelectReceiver

    private var reminders: List<Reminder> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentReminderListBinding.inflate(inflater, container, false)
        val context = requireContext()
        adapter = ReminderAdapter(context, this, this)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStateList.collect { uiState ->
                    when (uiState) {
                        is ReminderListUiState.Success -> {remindersReceived(uiState.reminders)}
                        is ReminderListUiState.Error -> { uiState.exception.message?.let { Log.e("error", it) } }
                    }
                }
            }
        }
        binding.reminderRecyclerView.setHasFixedSize(false)
        binding.reminderRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.reminderRecyclerView.itemAnimator=null
        binding.reminderRecyclerView.adapter = adapter
        receiver = adapter
        touchListener = DragSelectTouchListener.create(context, receiver)
        binding.reminderRecyclerView.addOnItemTouchListener(touchListener)
        binding.fab.setOnClickListener{ navigateToCreateReminder() }
        return binding.root
    }

    private fun navigateToCreateReminder() {
        parentFragmentManager.commit{
            setReorderingAllowed(true)
            addToBackStack("MainActivity")
            replace<ReminderWriterFragment>(R.id.fragment_container)
        }
    }

    private fun remindersReceived(rems: List<Reminder>) {
        reminders = rems;
        adapter.setData(reminders)
    }

    // When reminder is clicked
    override fun onItemClick(position: Int) {
        val bundle = Bundle()
        bundle.putInt(MainActivity.REMINDER_ID, reminders[position].id!!)
        bundle.putString(MainActivity.NOTE, reminders[position].note)
        bundle.putFloat(MainActivity.RADIUS, reminders[position].radius)
        bundle.putFloat(MainActivity.LONGITUDE, reminders[position].longitude)
        bundle.putFloat(MainActivity.LATITUDE, reminders[position].latitude)
        bundle.putString(MainActivity.NAME, reminders[position].name)
        parentFragmentManager.commit{
            setReorderingAllowed(true)
            addToBackStack("MainActivity")
            replace<ReminderWriterFragment>(R.id.fragment_container, args = bundle)
        }
    }

    override fun onItemLongClicked(initialPosition: Int) {
        Log.d("Debug", "Beginning drag selection")
        //TODO complete code for dragging & selecting multiple reminders
        //touchListener.setIsActive(true, initialPosition)
    }
}