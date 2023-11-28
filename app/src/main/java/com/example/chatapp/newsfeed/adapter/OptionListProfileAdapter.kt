package com.example.chatapp.newsfeed.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatapp.newsfeed.screens.ListPostFragment
import com.example.chatapp.newsfeed.screens.ListSaveFragment

class OptionListProfileAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle,val profileID: String):
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> ListPostFragment()
            else -> ListSaveFragment()
        }

        // Truyền profileID
        fragment.arguments = Bundle().apply {
            putString("profileID", profileID)
        }

        return fragment
    }
}