package com.example.chatapp.onboarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentFirstScreenBinding

class FirstScreen : Fragment() {
    private lateinit var _binding: FragmentFirstScreenBinding
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstScreenBinding.inflate(layoutInflater, container, false)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
        binding.txtNext.setOnClickListener {
            viewPager?.currentItem = 1
        }
        return binding.root
    }

}