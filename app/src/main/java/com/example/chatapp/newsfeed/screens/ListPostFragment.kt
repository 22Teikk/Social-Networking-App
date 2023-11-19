package com.example.chatapp.newsfeed.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
class ListPostFragment : Fragment() {
    private lateinit var rcvListPost: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_list_post, container, false)
            rcvListPost = view.findViewById(R.id.rcvListPost)
        return view
    }

}