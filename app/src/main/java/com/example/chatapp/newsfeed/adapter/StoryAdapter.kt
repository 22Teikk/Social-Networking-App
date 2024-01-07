package com.example.chatapp.newsfeed.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.databinding.FriendItemBinding
import com.example.chatapp.model.Stories
import com.example.chatapp.model.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class StoryAdapter(private val listStory: ArrayList<Stories>): RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {
    inner class StoryViewHolder(val binding: FriendItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        return StoryViewHolder(
            FriendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listStory.size
    }

    private fun getInfo(friendItemBinding: FriendItemBinding, uid: String) {
        FirebaseDatabase.getInstance().reference.child(Constant.USER_TABLE_NAME).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.child(uid).getValue(Users::class.java)
                user?.let {
                    friendItemBinding.friendNameItem.text = user.name
                    Picasso.get().load(user.avatar).into(friendItemBinding.avatarProfile)
                    friendItemBinding.avatarProfile.strokeColor = ColorStateList.valueOf(Color.RED)
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = listStory[position]
        holder.apply {
            binding.apply {
                getInfo(this, story.uid.toString())
                friendLayout.setOnClickListener {

                }
            }
        }
    }

}