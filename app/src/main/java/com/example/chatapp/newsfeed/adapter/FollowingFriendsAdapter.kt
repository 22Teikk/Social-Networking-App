package com.example.chatapp.newsfeed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FriendItemBinding
import com.example.chatapp.model.Users
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class FollowingFriendsAdapter(private val followingList: List<String>):
    RecyclerView.Adapter<FollowingFriendsAdapter.FollowingFriendsViewHolder>() {
    inner class FollowingFriendsViewHolder(val binding: FriendItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingFriendsViewHolder {
        return FollowingFriendsViewHolder(
            FriendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return followingList.size
    }

    override fun onBindViewHolder(holder: FollowingFriendsViewHolder, position: Int) {
        val uid = followingList[position]
        holder.apply {
            binding.apply {
                Firebase.database.reference.child(Constant.USER_TABLE_NAME).child(uid)
                    .addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user: Users = snapshot.getValue(Users::class.java)!!
                            Picasso.get().load(user.avatar).into(avatarProfile)
                            friendNameItem.setText(user.name)
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }
        }
    }
}