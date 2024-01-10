package com.example.chatapp.newsfeed.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.databinding.FriendItemBinding
import com.example.chatapp.model.Stories
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.screens.FeedFragmentDirections
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class StoryAdapter(
    private val listStoryUID: ArrayList<String>,
    private val navController: NavController
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {
    inner class StoryViewHolder(val binding: FriendItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        return StoryViewHolder(
            FriendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listStoryUID.size
    }

    private fun getInfo(friendItemBinding: FriendItemBinding, userID: String) {
        FirebaseDatabase.getInstance().reference.child(Constant.USER_TABLE_NAME)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.child(userID.toString()).getValue(Users::class.java)
                    user?.let {
                        friendItemBinding.friendNameItem.text = user.name
                        Picasso.get().load(user.avatar).into(friendItemBinding.avatarProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                }

            })
        friendItemBinding.avatarProfile.strokeColor = ColorStateList.valueOf(Color.GRAY)
        Firebase.database.reference.child(Constant.STORY_TABLE_NAME).child(userID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val story = data.getValue(Stories::class.java)
                        story?.let {
                            if (story.viewer == null || story.viewer?.contains(Firebase.auth.uid) == false) {
                                friendItemBinding.avatarProfile.strokeColor =
                                    ColorStateList.valueOf(Color.RED)
                            }
                            if (story.uid == Firebase.auth.uid) {
                                friendItemBinding.avatarProfile.strokeColor =
                                    ColorStateList.valueOf(Color.BLACK)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val userID = listStoryUID[position]
        holder.apply {
            binding.apply {
                getInfo(this, userID)

                friendLayout.setOnClickListener {
                    setViewStory(userID)
                }
            }
        }
    }

    private fun setViewStory(userID: String) {
        val action = FeedFragmentDirections.actionFeedFragmentToViewStoryFragment(userID)
        navController.navigate(action)
    }
}


