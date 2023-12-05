package com.example.chatapp.newsfeed.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Constant
import com.example.chatapp.databinding.FragmentNotificationsBinding
import com.example.chatapp.model.Notifications
import com.example.chatapp.newsfeed.adapter.NotificationAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NotificationsFragment : Fragment() {
    private lateinit var _binding: FragmentNotificationsBinding
    private lateinit var notificationAdapter: NotificationAdapter
    private var listNotification: ArrayList<Notifications> = arrayListOf()
    private lateinit var database : DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        database = Firebase.database.reference
        auth = Firebase.auth
        binding.apply {
            notificationAdapter = NotificationAdapter(listNotification, findNavController())
            with(rcvNotifications) {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = notificationAdapter
            }
        }
        getNotifications()

        return binding.root
    }

    private fun getNotifications() {
        database.child(Constant.NOTIFICATION_TABLE_NAME).child(auth.uid.toString())
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listNotification.clear()
                    for (data in snapshot.children) {
                        val notificationData = data.getValue(Notifications::class.java)
                        listNotification.add(0, notificationData!!)
                    }
                    notificationAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}