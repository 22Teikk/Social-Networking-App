package com.example.chatapp.newsfeed.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentFeedBinding
import com.example.chatapp.model.Users

class FeedFragment : Fragment() {
    private lateinit var _binding: FragmentFeedBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)

        binding.imageAddFriends.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_searchFriendFragment)
        }

        return binding.root
    }

}