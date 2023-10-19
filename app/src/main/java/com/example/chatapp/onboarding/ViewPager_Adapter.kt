package com.example.chatapp.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPager_Adapter(fm: FragmentManager, lifeCycle: Lifecycle, list: ArrayList<Fragment>) :
    FragmentStateAdapter(fm, lifeCycle) {
    private val fragmentList = list

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}