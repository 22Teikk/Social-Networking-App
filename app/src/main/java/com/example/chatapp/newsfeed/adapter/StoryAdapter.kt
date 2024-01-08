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
    private val listStory: ArrayList<Stories>,
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
        return listStory.size
    }

    private fun getInfo(friendItemBinding: FriendItemBinding, story: Stories) {
        FirebaseDatabase.getInstance().reference.child(Constant.USER_TABLE_NAME)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.child(story.uid.toString()).getValue(Users::class.java)
                    user?.let {
                        friendItemBinding.friendNameItem.text = user.name
                        Picasso.get().load(user.avatar).into(friendItemBinding.avatarProfile)
                        if (story.viewer!=null && story.viewer!!.contains(Firebase.auth.uid) ) {
                            friendItemBinding.avatarProfile.strokeColor =
                                ColorStateList.valueOf(Color.GRAY)
                        }else friendItemBinding.avatarProfile.strokeColor =
                            ColorStateList.valueOf(Color.RED)
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
                getInfo(this, story)
                friendLayout.setOnClickListener {
                        viewStory(story)
                        val action = FeedFragmentDirections.actionFeedFragmentToViewStoryFragment(
                            story.storyID.toString(),
                            story.uid.toString()
                                    )
                        navController.navigate(action)
                    }
                }
            }
        }
    }

    private fun viewStory(story: Stories) {
        var newStory = story
        newStory.viewer = story.viewer ?: mutableListOf()
        val setViewer: MutableSet<String> = mutableSetOf()
        if (story.viewer!=null) setViewer.addAll(story.viewer!!)
        setViewer.add(Firebase.auth.uid.toString())
        newStory.viewer = setViewer.toList()
        Firebase.database.reference.child(Constant.STORY_TABLE_NAME).child(story.storyID.toString()).setValue(newStory)

    }
