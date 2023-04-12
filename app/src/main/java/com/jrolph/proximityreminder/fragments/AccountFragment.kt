package com.jrolph.proximityreminder.fragments

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jrolph.proximityreminder.R
import com.jrolph.proximityreminder.activities.MainActivity
import com.jrolph.proximityreminder.databinding.FragmentAccountBinding
import kotlin.math.sign

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private var signedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { signedIn = it.getBoolean(MainActivity.SIGNED_IN) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        Log.d("log", "Is signed in: $signedIn")
        if (signedIn) {
            binding.signInoutButton.text = getString(R.string.sign_out)
            //TODO add more formal T&Cs
            binding.disclaimerText.text = getString(R.string.account_label)
            binding.signInoutButton.setOnClickListener{ (activity as MainActivity?)!!.signOutPressed() }
            binding.deleteAccountButton.visibility = View.VISIBLE
            binding.deleteAccountButton.setOnClickListener{
                (activity as MainActivity?)!!.deleteAccountPressed()
            }
        }
        else {
            binding.signInoutButton.text = getString(R.string.sign_in)
            binding.disclaimerText.text = getString(R.string.account_explanation)
            binding.signInoutButton.setOnClickListener{ (activity as MainActivity?)!!.signInPressed() }
            binding.deleteAccountButton.visibility = View.INVISIBLE
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}