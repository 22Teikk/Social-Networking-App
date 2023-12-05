package com.example.chatapp.newsfeed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.databinding.NotificationItemBinding
import com.example.chatapp.model.Notifications
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.screens.NotificationsFragmentDirections
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class NotificationAdapter(private val listNotification: List<Notifications>, private val navController: NavController) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    inner class NotificationViewHolder(val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listNotification.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notifications = listNotification[position]
        holder.apply {
            binding.apply {
                friendAction.text = notifications.content
                getUserInfo(this, notifications.uid.toString())
                friendNotification.setOnClickListener {
                    if (notifications.pid != "") {
                        val action = NotificationsFragmentDirections.actionNotificationsFragmentToPostDetailFragment(notifications.pid.toString())
                        navController.navigate(action)
                    }else {
                        val action = NotificationsFragmentDirections.actionNotificationsFragmentToProfileFragment(notifications.uid.toString())
                        navController.navigate(action)
                    }
                }
            }
        }
    }

    private fun getUserInfo(binding: NotificationItemBinding, publisher: String) {
        Firebase.database.reference.child(Constant.USER_TABLE_NAME).child(publisher)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(Users::class.java)
                    binding.apply {
                        Picasso.get().load(user?.avatar).into(avatarFriend)
                        friendName.text = user!!.name
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


}