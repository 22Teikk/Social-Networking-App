package com.example.chatapp.newsfeed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FriendSearchItemBinding
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.screens.SearchFriendFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class SearchFriendBeforeAdapter (
    private val userList: ArrayList<Users>, val navController: NavController
) : RecyclerView.Adapter<SearchFriendBeforeAdapter.SearchViewBeforeHolder>() {
    private lateinit var auth: FirebaseAuth

    inner class SearchViewBeforeHolder(val binding: FriendSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewBeforeHolder {
        return SearchViewBeforeHolder(
            FriendSearchItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: SearchViewBeforeHolder, position: Int) {
        val user = userList[position]
        auth = Firebase.auth
        holder.apply {
            binding.apply {
                Picasso.get().load(user.avatar).into(avatarFriendSearchItem)
                friendNameSearchItem.setText(user.name)
                if (user.bio == null || user.bio == "") friendBioSearchItem.setText(user.gender)
                else friendBioSearchItem.setText(user.bio)
                isFollowing(user.uid!!, followFriends)
                followFriends.setOnClickListener {
                    if (followFriends.contentDescription == "Follow") {
                        Firebase.database.reference.child(Constant.FOLLOW_TABLE_NAME).child(auth.uid!!)
                            .child(Constant.FOLLOW_TABLE_FOLLOWING).child(
                                user.uid!!
                            ).setValue(true)
                        Firebase.database.reference.child(Constant.FOLLOW_TABLE_NAME).child(user.uid!!)
                            .child(Constant.FOLLOW_TABLE_FOLLOWER).child(
                                auth.uid!!
                            ).setValue(true)
                    }else {
                        Firebase.database.reference.child(Constant.FOLLOW_TABLE_NAME).child(auth.uid!!)
                            .child(Constant.FOLLOW_TABLE_FOLLOWING).child(
                                user.uid!!
                            ).removeValue()
                        Firebase.database.reference.child(Constant.FOLLOW_TABLE_NAME).child(user.uid!!)
                            .child(Constant.FOLLOW_TABLE_FOLLOWER).child(
                                auth.uid!!
                            ).removeValue()
                    }

                }
                if (user.uid.equals(auth.uid)) {
                    followFriends.visibility = View.GONE
                }
                viewFriend.setOnClickListener {
                    val action = SearchFriendFragmentDirections.actionSearchFriendFragmentToProfileFragment(
                        user.uid!!
                    )
                    navController.navigate(action)
                }

            }
        }
    }

    fun isFollowing(userID: String, imageFollow: ImageView) {
        val database = Firebase.database.reference
        database.child(Constant.FOLLOW_TABLE_NAME).child(auth.uid!!)
            .child(Constant.FOLLOW_TABLE_FOLLOWING)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(userID).exists()) {
                        imageFollow.setImageResource(R.drawable.baseline_done_24)
                        imageFollow.contentDescription = "Following"
                    }
                    else {
                        imageFollow.setImageResource(R.drawable.baseline_add_24)
                        imageFollow.contentDescription = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}