package com.example.chatapp.newsfeed.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatapp.newsfeed.screens.ListPostFragment
import com.example.chatapp.newsfeed.screens.ListSaveFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class OptionListProfileAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle,val profileID: String):
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return if (profileID == Firebase.auth.uid.toString() )
            2
        else 1
    }

    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment

        if (profileID == Firebase.auth.uid.toString() ) {
            // Nếu profileID khác "text", tạo ListPostFragment hoặc ListSaveFragment tùy thuộc vào position
            fragment = when (position) {
                0 -> ListPostFragment()
                else -> ListSaveFragment()
            }
        } else {
            // Nếu profileID là null hoặc bằng "text", chỉ tạo ListPostFragment
            fragment = ListPostFragment()
        }
        fragment.arguments = Bundle().apply {
            putString("profileID", profileID)
        }
        return fragment
    }
}