package com.example.chatapp.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chatapp.databinding.FragmentViewPagerBinding
import com.example.chatapp.onboarding.screens.FirstScreen
import com.example.chatapp.onboarding.screens.SecondScreen

class ViewPager_Fragment : Fragment() {
    private lateinit var _binding: FragmentViewPagerBinding
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPagerBinding.inflate(layoutInflater, container, false)

        val fragmentList = arrayListOf<Fragment>(
            FirstScreen(),
            SecondScreen()
        )
        val adapter =
            ViewPager_Adapter(requireActivity().supportFragmentManager, lifecycle, fragmentList)
        binding.viewPager.adapter = adapter

        return binding.root
    }

}