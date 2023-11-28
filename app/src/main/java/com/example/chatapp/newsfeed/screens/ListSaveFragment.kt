package com.example.chatapp.newsfeed.screens

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R

class ListSaveFragment() : Fragment() {
    private lateinit var rcvListSave: RecyclerView
    lateinit var profileID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_list_save, container, false)
            rcvListSave = view.findViewById(R.id.rcvListSave)
        profileID = arguments?.getString("profileID") ?: ""
        return view
    }

}