package com.example.chatapp.onboarding.screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentSecondScreenBinding

class SecondScreen : Fragment() {
    private lateinit var _binding: FragmentSecondScreenBinding
    private val binding get() =  _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondScreenBinding.inflate(layoutInflater, container, false)

        binding.btnStart.setOnClickListener {
            findNavController().navigate(R.id.action_viewPager_Fragment_to_home_Fragment)
            onBoardingFinished()
        }

        return binding.root
    }


    private fun onBoardingFinished() {
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }
}